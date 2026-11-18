package com.campusexchange.web;

import com.campusexchange.controller.web.HomeController;
import com.campusexchange.model.User;
import com.campusexchange.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Model values every server-rendered page needs: active nav state and the signed-in student. */
@ControllerAdvice(basePackageClasses = HomeController.class)
public class GlobalModelAttributes {

    private final UserService userService;
    private final boolean demoHints;

    public GlobalModelAttributes(UserService userService,
                                 @Value("${app.demo-hints:true}") boolean demoHints) {
        this.userService = userService;
        this.demoHints = demoHints;
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        return principal == null ? null : userService.findByEmail(principal.getName()).orElse(null);
    }

    /** Shows the seeded demo credentials on the sign-in page (handy for lab demos). */
    @ModelAttribute("demoHints")
    public boolean demoHints() {
        return demoHints;
    }
}
