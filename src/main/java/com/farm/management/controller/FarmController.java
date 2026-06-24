package com.farm.management.controller;

import com.farm.management.entity.Farm;
import com.farm.management.entity.User;
import com.farm.management.repository.UserRepository;
import com.farm.management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/farms")
public class FarmController {

    @Autowired
    private FarmService farmService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CropService cropService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private HarvestRecordService harvestRecordService;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // List all farms
    @GetMapping
    public String listFarms(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("farms", farmService.findByUser(user));
        model.addAttribute("totalFarms", farmService.countByUser(user));
        model.addAttribute("activePage", "farms");
        return "farms/list";
    }

    // View single farm detail
    @GetMapping("/{id}")
    public String viewFarm(@PathVariable Long id, Model model) {
        Farm farm = farmService.findById(id)
                .orElseThrow(() -> new RuntimeException("Farm not found"));
        User user = getCurrentUser();

        List<com.farm.management.entity.Crop> allCrops =
                cropService.findByUser(user);
        List<com.farm.management.entity.Crop> farmCrops = allCrops.stream()
                .filter(c -> c.getFarm() != null &&
                        c.getFarm().getId().equals(id))
                .collect(Collectors.toList());

        List<com.farm.management.entity.Inventory> allInventory =
                inventoryService.findByUser(user);
        List<com.farm.management.entity.Inventory> farmInventory =
                allInventory.stream()
                        .filter(i -> i.getFarm() != null &&
                                i.getFarm().getId().equals(id))
                        .collect(Collectors.toList());

        long farmHarvestsCount = harvestRecordService.findByUser(user)
                .stream()
                .filter(h -> h.getCrop() != null &&
                        h.getCrop().getFarm() != null &&
                        h.getCrop().getFarm().getId().equals(id))
                .count();

        BigDecimal farmSalesTotal = saleService.findByUser(user)
                .stream()
                .filter(s -> s.getCrop() != null &&
                        s.getCrop().getFarm() != null &&
                        s.getCrop().getFarm().getId().equals(id))
                .map(s -> s.getTotalAmount() != null ?
                        s.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("user", user);
        model.addAttribute("farm", farm);
        model.addAttribute("farmCrops", farmCrops);
        model.addAttribute("farmCropsCount", farmCrops.size());
        model.addAttribute("farmInventory", farmInventory);
        model.addAttribute("farmInventoryCount", farmInventory.size());
        model.addAttribute("farmHarvestsCount", farmHarvestsCount);
        model.addAttribute("farmSalesTotal", farmSalesTotal);
        model.addAttribute("activePage", "farms");
        return "farms/detail";
    }

    // Show add farm form
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", getCurrentUser());
        model.addAttribute("activePage", "farms");
        return "farms/add";
    }

    // Handle add farm
    @PostMapping("/add")
    public String addFarm(
            @RequestParam String farmName,
            @RequestParam String location,
            @RequestParam(required = false) String sizeAcres,
            @RequestParam(required = false) String description,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            Farm farm = new Farm();
            farm.setFarmName(farmName);
            farm.setLocation(location);
            if (sizeAcres != null && !sizeAcres.isEmpty()) {
                farm.setSizeAcres(new BigDecimal(sizeAcres));
            }
            farm.setDescription(description);

            String lat = request.getParameter("latitude");
            String lng = request.getParameter("longitude");
            if (lat != null && !lat.isEmpty()) {
                farm.setLatitude(new BigDecimal(lat));
                farm.setLongitude(new BigDecimal(lng));
            }

            farm.setUser(getCurrentUser());
            farmService.save(farm);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Farm added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error: " + e.getMessage());
        }
        return "redirect:/farms";
    }

    // Delete farm
    @PostMapping("/delete/{id}")
    public String deleteFarm(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        farmService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Farm deleted successfully!");
        return "redirect:/farms";
    }
}