package com.accounting.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error details
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        
        // Set default message if none provided
        String errorMessage = "We are sorry, but something went wrong. Please try again later.";
        
        if (message != null && !message.toString().isEmpty()) {
            errorMessage = message.toString();
        } else if (exception != null) {
            errorMessage = exception.toString();
        }
        
        // Add error details to model
        model.addAttribute("error", true);
        model.addAttribute("message", errorMessage);
        model.addAttribute("status", status != null ? status.toString() : "");
        
        return "error";
    }
} 