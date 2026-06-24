package com.farm.management.controller;

import com.farm.management.entity.Expense;
import com.farm.management.entity.Sale;
import com.farm.management.entity.User;
import com.farm.management.repository.UserRepository;
import com.farm.management.service.CropService;
import com.farm.management.service.ExpenseService;
import com.farm.management.service.SaleService;
import com.farm.management.service.CropImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private SaleService saleService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private CropService cropService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CropImageUtil cropImageUtil;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Finance Dashboard
    @GetMapping
    public String financeDashboard(Model model) {
        User user = getCurrentUser();

        BigDecimal totalRevenue = saleService.getTotalRevenue(user);
        BigDecimal totalExpenses = expenseService.getTotalExpenses(user);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        model.addAttribute("user", user);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("netProfit", netProfit);
        model.addAttribute("sales", saleService.findByUser(user));
        model.addAttribute("expenses", expenseService.findByUser(user));
        model.addAttribute("cropImageMap", cropImageUtil.getCropImageMap());
        model.addAttribute("activePage", "finance");
        return "finance/dashboard";
    }

    // Show add sale form
    @GetMapping("/sales/add")
    public String showAddSaleForm(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("crops", cropService.findByUser(user));
        model.addAttribute("cropImageMap", cropImageUtil.getCropImageMap());
        model.addAttribute("activePage", "finance");
        return "finance/add-sale";
    }

    // Handle add sale
    @PostMapping("/sales/add")
    public String addSale(
            @RequestParam Long cropId,
            @RequestParam String quantitySold,
            @RequestParam String unit,
            @RequestParam String pricePerUnit,
            @RequestParam(required = false) String buyerName,
            @RequestParam(required = false) String marketLocation,
            @RequestParam String saleDate,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            User user = getCurrentUser();
            Sale sale = new Sale();
            sale.setCrop(cropService.findById(cropId)
                    .orElseThrow(() -> new RuntimeException("Crop not found")));
            sale.setQuantitySold(new BigDecimal(quantitySold));
            sale.setUnit(unit);
            BigDecimal price = new BigDecimal(pricePerUnit);
            sale.setPricePerUnit(price);
            sale.setTotalAmount(price.multiply(new BigDecimal(quantitySold)));
            sale.setBuyerName(buyerName);
            sale.setMarketLocation(marketLocation);
            sale.setSaleDate(java.time.LocalDate.parse(saleDate));
            sale.setNotes(notes);
            sale.setUser(user);
            saleService.save(sale);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Sale recorded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: " + e.getMessage());
        }
        return "redirect:/finance";
    }

    // Delete sale
    @PostMapping("/sales/delete/{id}")
    public String deleteSale(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        saleService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Sales record has been deleted!");
        return "redirect:/finance";
    }

    // Show add expense form
    @GetMapping("/expenses/add")
    public String showAddExpenseForm(Model model) {
        model.addAttribute("user", getCurrentUser());
        model.addAttribute("activePage", "finance");
        return "finance/add-expense";
    }

    // Handle add expense
    @PostMapping("/expenses/add")
    public String addExpense(
            @RequestParam String expenseName,
            @RequestParam String category,
            @RequestParam String amount,
            @RequestParam String expenseDate,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {

        try {
            Expense expense = new Expense();
            expense.setExpenseName(expenseName);
            expense.setCategory(category);
            expense.setAmount(new BigDecimal(amount));
            expense.setExpenseDate(java.time.LocalDate.parse(expenseDate));
            expense.setDescription(description);
            expense.setUser(getCurrentUser());
            expenseService.save(expense);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Expense recorded successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: " + e.getMessage());
        }
        return "redirect:/finance";
    }

    // Delete expense
    @PostMapping("/expenses/delete/{id}")
    public String deleteExpense(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        expenseService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Expense Deleted!");
        return "redirect:/finance";
    }

    // Market Prices page
    @GetMapping("/market-prices")
    public String marketPrices(Model model) {
        model.addAttribute("user", getCurrentUser());
        model.addAttribute("cropImageMap", cropImageUtil.getCropImageMap());
        model.addAttribute("activePage", "finance");
        return "finance/market-prices";
    }
}
