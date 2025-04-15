package com.accounting.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.accounting.entity.Company;
import com.accounting.entity.Revenue;
import com.accounting.entity.User;
import com.accounting.service.CompanyService;
import com.accounting.service.RevenueService;
import com.accounting.service.UserService;

@Controller
@RequestMapping("/revenues")
public class RevenueController {
    private final RevenueService revenueService;
    private final CompanyService companyService;
    private final UserService userService;

    public RevenueController(RevenueService revenueService, CompanyService companyService, UserService userService) {
        this.revenueService = revenueService;
        this.companyService = companyService;
        this.userService = userService;
    }

    @GetMapping
    public String listRevenues(Model model) {
        User user = getCurrentUser();
        List<Company> companies = companyService.getUserCompanies(user);
        model.addAttribute("companies", companies);
        model.addAttribute("revenues", revenueService.getAllRevenues(user));
        return "revenues/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        User user = getCurrentUser();
        model.addAttribute("revenue", new Revenue());
        model.addAttribute("companies", companyService.getUserCompanies(user));
        return "revenues/form";
    }

    @PostMapping
    public String createRevenue(@ModelAttribute Revenue revenue, @RequestParam Long companyId) {
        User user = getCurrentUser();
        revenueService.createRevenue(revenue, companyId, user);
        return "redirect:/revenues";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        Revenue revenue = revenueService.getRevenue(id, user);
        model.addAttribute("revenue", revenue);
        model.addAttribute("companies", companyService.getUserCompanies(user));
        return "revenues/form";
    }

    @PostMapping("/{id}")
    public String updateRevenue(@PathVariable Long id, @ModelAttribute Revenue revenue, @RequestParam Long companyId) {
        User user = getCurrentUser();
        revenue.setId(id);
        revenueService.updateRevenue(revenue, companyId, user);
        return "redirect:/revenues";
    }

    @DeleteMapping("/{id}")
    public String deleteRevenue(@PathVariable Long id) {
        User user = getCurrentUser();
        revenueService.deleteRevenue(id, user);
        return "redirect:/revenues";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.findByUsername(auth.getName());
    }
} 