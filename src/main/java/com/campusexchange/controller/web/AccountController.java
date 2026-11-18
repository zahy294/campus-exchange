package com.campusexchange.controller.web;

import com.campusexchange.dto.PasswordChangeForm;
import com.campusexchange.dto.ProfileForm;
import com.campusexchange.model.User;
import com.campusexchange.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Profile details and password change for the signed-in student. */
@Controller
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/account")
    public String account(Model model, Principal principal) {
        User user = userService.getUserByEmail(principal.getName());
        if (!model.containsAttribute("profileForm")) {
            model.addAttribute("profileForm", ProfileForm.from(user));
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new PasswordChangeForm());
        }
        model.addAttribute("user", user);
        return "account/profile";
    }

    @PostMapping("/account")
    public String updateProfile(
            @Valid @ModelAttribute("profileForm") ProfileForm profileForm,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userService.getUserByEmail(principal.getName()));
            model.addAttribute("passwordForm", new PasswordChangeForm());
            return "account/profile";
        }
        userService.updateProfile(principal.getName(), profileForm);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated.");
        return "redirect:/account";
    }

    @PostMapping("/account/password")
    public String changePassword(
            @Valid @ModelAttribute("passwordForm") PasswordChangeForm passwordForm,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!bindingResult.hasErrors() && !userService.changePassword(principal.getName(), passwordForm)) {
            bindingResult.rejectValue("currentPassword", "mismatch", "That isn't your current password.");
        }
        if (bindingResult.hasErrors()) {
            User user = userService.getUserByEmail(principal.getName());
            passwordForm.setCurrentPassword(null);
            passwordForm.setNewPassword(null);
            passwordForm.setConfirmPassword(null);
            model.addAttribute("user", user);
            model.addAttribute("profileForm", ProfileForm.from(user));
            return "account/profile";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Password changed. Use the new one next time you sign in.");
        return "redirect:/account";
    }
}
