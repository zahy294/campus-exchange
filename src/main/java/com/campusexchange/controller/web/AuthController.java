package com.campusexchange.controller.web;

import com.campusexchange.dto.RegisterForm;
import com.campusexchange.exception.DuplicateEmailException;
import com.campusexchange.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class AuthController {

    /** Session key holding the email from the last failed sign-in. */
    public static final String LAST_LOGIN_EMAIL = "LAST_LOGIN_EMAIL";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Principal principal,
            jakarta.servlet.http.HttpSession session,
            Model model) {

        if (principal != null) {
            return "redirect:/";
        }
        if (error != null) {
            model.addAttribute("errorMessage", "That email and password don't match an account. Check both and try again.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You've signed out. See you around campus.");
        }
        if (expired != null) {
            model.addAttribute("errorMessage", "Your session expired. Sign in again to continue.");
        }
        // Keep the typed email after a failed attempt (stored by the security failure handler).
        Object lastEmail = session.getAttribute(LAST_LOGIN_EMAIL);
        if (lastEmail != null) {
            session.removeAttribute(LAST_LOGIN_EMAIL);
            model.addAttribute("email", lastEmail);
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/";
        }
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(
            @Valid @ModelAttribute("registerForm") RegisterForm registerForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            clearPasswords(registerForm);
            return "register";
        }

        try {
            userService.registerUser(registerForm);
        } catch (DuplicateEmailException ex) {
            bindingResult.rejectValue("email", "duplicate", "An account with this email already exists.");
            clearPasswords(registerForm);
            return "register";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Account created. Sign in to start listing.");
        redirectAttributes.addFlashAttribute("email", registerForm.getEmail().trim().toLowerCase());
        return "redirect:/login";
    }

    /** Never echo passwords back into the HTML. */
    private void clearPasswords(RegisterForm form) {
        form.setPassword(null);
        form.setConfirmPassword(null);
    }
}
