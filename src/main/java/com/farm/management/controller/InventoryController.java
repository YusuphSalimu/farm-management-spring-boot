package com.farm.management.controller;

import com.farm.management.entity.Inventory;
import com.farm.management.entity.User;
import com.farm.management.repository.UserRepository;
import com.farm.management.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // List all inventory
    @GetMapping
    public String listInventory(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("inventoryList",
                inventoryService.findByUser(user));
        model.addAttribute("activePage", "inventory");
        return "inventory/list";
    }

    // Show add form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", getCurrentUser());
        model.addAttribute("activePage", "inventory");
        return "inventory/add";
    }

    // Handle add form
    @PostMapping("/add")
    public String addInventory(
            @RequestParam String itemName,
            @RequestParam String category,
            @RequestParam String quantity,
            @RequestParam String unit,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String purchaseDate,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            Inventory inventory = new Inventory();
            inventory.setItemName(itemName);
            inventory.setCategory(category);
            inventory.setQuantity(new BigDecimal(quantity));
            inventory.setUnit(unit);
            inventory.setSupplier(supplier);
            if (purchaseDate != null && !purchaseDate.isEmpty()) {
                inventory.setPurchaseDate(
                        java.time.LocalDate.parse(purchaseDate));
            }
            inventory.setNotes(notes);
            inventory.setUser(getCurrentUser());
            inventoryService.save(inventory);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Inventory item added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error adding item: " + e.getMessage());
        }
        return "redirect:/inventory";
    }

    // Show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Inventory inventory = inventoryService.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Item not found"));
        model.addAttribute("user", getCurrentUser());
        model.addAttribute("inventory", inventory);
        model.addAttribute("activePage", "inventory");
        return "inventory/edit";
    }

    // Handle edit form
    @PostMapping("/edit/{id}")
    public String editInventory(
            @PathVariable Long id,
            @RequestParam String itemName,
            @RequestParam String category,
            @RequestParam String quantity,
            @RequestParam String unit,
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String purchaseDate,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            Inventory inventory = inventoryService.findById(id)
                    .orElseThrow(() ->
                            new RuntimeException("Item not found"));
            inventory.setItemName(itemName);
            inventory.setCategory(category);
            inventory.setQuantity(new BigDecimal(quantity));
            inventory.setUnit(unit);
            inventory.setSupplier(supplier);
            if (purchaseDate != null && !purchaseDate.isEmpty()) {
                inventory.setPurchaseDate(
                        java.time.LocalDate.parse(purchaseDate));
            }
            inventory.setNotes(notes);
            inventoryService.save(inventory);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Item updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating item: " + e.getMessage());
        }
        return "redirect:/inventory";
    }

    // Delete inventory
    @PostMapping("/delete/{id}")
    public String deleteInventory(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        inventoryService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Item deleted successfully!");
        return "redirect:/inventory";
    }
}