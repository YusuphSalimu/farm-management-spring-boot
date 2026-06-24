package com.farm.management.controller;

import com.farm.management.entity.Crop;
import com.farm.management.entity.HarvestRecord;
import com.farm.management.entity.User;
import com.farm.management.repository.UserRepository;
import com.farm.management.service.CropImageUtil;
import com.farm.management.service.CropService;
import com.farm.management.service.HarvestRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/harvest")
public class HarvestController {

    @Autowired
    private HarvestRecordService harvestRecordService;

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

    @GetMapping
    public String listHarvests(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("harvests",
                harvestRecordService.findByUser(user));
        model.addAttribute("activePage", "harvest");
        model.addAttribute("cropImageMap", cropImageUtil.getCropImageMap());
        return "harvest/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("crops", cropService.findByUser(user));
        model.addAttribute("activePage", "harvest");
        model.addAttribute("cropImageMap", cropImageUtil.getCropImageMap());
        return "harvest/add";
    }

    @PostMapping("/add")
    public String addHarvest(
            @RequestParam Long cropId,
            @RequestParam String harvestDate,
            @RequestParam String quantityHarvested,
            @RequestParam String unit,
            @RequestParam String quality,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            User user = getCurrentUser();
            Crop crop = cropService.findById(cropId)
                    .orElseThrow(() ->
                            new RuntimeException("Crop not found"));

            HarvestRecord record = new HarvestRecord();
            record.setCrop(crop);
            record.setHarvestDate(
                    java.time.LocalDate.parse(harvestDate));
            record.setQuantityHarvested(
                    new BigDecimal(quantityHarvested));
            record.setUnit(unit);
            record.setQuality(quality);
            record.setNotes(notes);
            record.setUser(user);

            crop.setStatus("HARVESTED");
            cropService.save(crop);

            harvestRecordService.save(record);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Harvest record added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error adding harvest: " + e.getMessage());
        }
        return "redirect:/harvest";
    }

    @PostMapping("/delete/{id}")
    public String deleteHarvest(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        harvestRecordService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Harvest record deleted successfully!");
        return "redirect:/harvest";
    }
}