package com.accounting.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.accounting.entity.Company;
import com.accounting.entity.User;
import com.accounting.service.CompanyService;
import com.accounting.service.UserService;

@Controller
@RequestMapping("/companies")
public class CompanyController {
    private final CompanyService companyService;
    private final UserService userService;

    public CompanyController(CompanyService companyService, UserService userService) {
        this.companyService = companyService;
        this.userService = userService;
    }

    @GetMapping
    public String listCompanies(Model model) {
        User user = getCurrentUser();
        model.addAttribute("companies", companyService.getUserCompanies(user));
        return "companies/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("company", new Company());
        return "companies/form";
    }

    @PostMapping
    public String createCompany(@ModelAttribute Company company) {
        User user = getCurrentUser();
        companyService.createCompany(company, user);
        return "redirect:/companies";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        Company company = companyService.getCompany(id, user);
        model.addAttribute("company", company);
        return "companies/form";
    }

    @PostMapping("/{id}")
    public String updateCompany(@PathVariable Long id, @ModelAttribute Company company) {
        User user = getCurrentUser();
        company.setId(id);
        companyService.updateCompany(company, user);
        return "redirect:/companies";
    }

    @DeleteMapping("/{id}")
    public String deleteCompany(@PathVariable Long id) {
        User user = getCurrentUser();
        companyService.deleteCompany(id, user);
        return "redirect:/companies";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }
} 