package com.campusexchange.exception;

import com.campusexchange.controller.web.HomeController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Renders friendly HTML error pages (templates/error/*) with the correct HTTP
 * status for the server-rendered web controllers ({@code controller.web} package).
 * Unexpected exceptions are left to Spring Boot's /error handling, which renders
 * error/500.html.
 */
@ControllerAdvice(basePackageClasses = HomeController.class)
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView notFound(ResourceNotFoundException ex, HttpServletRequest request,
                                 HttpServletResponse response) {
        return page("error/404", HttpStatus.NOT_FOUND, ex.getMessage(), request, response);
    }

    /** A non-numeric or out-of-range id in the URL is treated as "no such page". */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView badParameter(MethodArgumentTypeMismatchException ex, HttpServletRequest request,
                                     HttpServletResponse response) {
        return page("error/404", HttpStatus.NOT_FOUND, "That link doesn't point to a valid page.", request, response);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ModelAndView forbidden(ForbiddenOperationException ex, HttpServletRequest request,
                                  HttpServletResponse response) {
        return page("error/403", HttpStatus.FORBIDDEN, ex.getMessage(), request, response);
    }

    private ModelAndView page(String view, HttpStatus status, String message,
                              HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(status.value());
        ModelAndView mav = new ModelAndView(view, status);
        mav.addObject("message", message);
        mav.addObject("path", request.getRequestURI());
        return mav;
    }
}
