package com.farm.management.controller;

import com.farm.management.entity.Crop;
import com.farm.management.entity.User;
import com.farm.management.repository.UserRepository;
import com.farm.management.service.CropImageUtil;
import com.farm.management.service.CropService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/crops")
public class CropController {

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
    public String listCrops(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
        model.addAttribute("crops", cropService.findByUser(user));
        model.addAttribute("activePage", "crops");
        model.addAttribute("cropImageMap", cropImageUtil.getCropImageMap());
        return "crops/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", getCurrentUser());
        model.addAttribute("crop", new Crop());
        model.addAttribute("activePage", "crops");
        model.addAttribute("cropImageMap", cropImageUtil.getCropImageMap());
        return "crops/add";
    }

    @PostMapping("/add")
    public String addCrop(
            @RequestParam String cropName,
            @RequestParam String cropType,
            @RequestParam String plantingDate,
            @RequestParam String expectedHarvestDate,
            @RequestParam String fieldLocation,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            Crop crop = new Crop();
            crop.setCropName(cropName);
            crop.setCropType(cropType);
            crop.setPlantingDate(java.time.LocalDate.parse(plantingDate));
            crop.setExpectedHarvestDate(
                    java.time.LocalDate.parse(expectedHarvestDate));
            crop.setFieldLocation(fieldLocation);
            crop.setStatus(status);
            crop.setNotes(notes);
            crop.setUser(getCurrentUser());
            cropService.save(crop);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Crop added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error adding crop: " + e.getMessage());
        }
        return "redirect:/crops";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Crop crop = cropService.findById(id)
                .orElseThrow(() -> new RuntimeException("Crop not found"));
        model.addAttribute("user", getCurrentUser());
        model.addAttribute("crop", crop);
        model.addAttribute("activePage", "crops");
        model.addAttribute("cropImageMap", cropImageUtil.getCropImageMap());
        return "crops/edit";
    }

    @PostMapping("/edit/{id}")
    public String editCrop(
            @PathVariable Long id,
            @RequestParam String cropName,
            @RequestParam String cropType,
            @RequestParam String plantingDate,
            @RequestParam String expectedHarvestDate,
            @RequestParam String fieldLocation,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            Crop crop = cropService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Crop not found"));
            crop.setCropName(cropName);
            crop.setCropType(cropType);
            crop.setPlantingDate(java.time.LocalDate.parse(plantingDate));
            crop.setExpectedHarvestDate(
                    java.time.LocalDate.parse(expectedHarvestDate));
            crop.setFieldLocation(fieldLocation);
            crop.setStatus(status);
            crop.setNotes(notes);
            cropService.save(crop);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Crop updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error updating crop: " + e.getMessage());
        }
        return "redirect:/crops";
    }

    @PostMapping("/delete/{id}")
    public String deleteCrop(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        cropService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Crop deleted successfully!");
        return "redirect:/crops";
    }
}