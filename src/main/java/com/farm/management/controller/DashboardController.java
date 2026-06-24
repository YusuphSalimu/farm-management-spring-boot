package com.farm.management.controller;

import com.farm.management.entity.Crop;
import com.farm.management.entity.User;
import com.farm.management.repository.UserRepository;
import com.farm.management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CropService cropService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private HarvestRecordService harvestRecordService;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "city", defaultValue = "Dodoma") String city,
            Model model) {

        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        //After getting the user
        userService.ensureDefaultFarm(user);

        // Stats
        model.addAttribute("user", user);
        model.addAttribute("totalCrops", cropService.countByUser(user));
        model.addAttribute("totalInventory", inventoryService.countByUser(user));
        model.addAttribute("totalHarvests", harvestRecordService.countByUser(user));

        // Recent crops
        List<Crop> allCrops = cropService.findByUser(user);
        model.addAttribute("recentCrops", allCrops.stream()
                .limit(5).collect(Collectors.toList()));

        // Harvest alerts — crops with expected harvest within 30 days
        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);
        List<Crop> upcomingHarvests = allCrops.stream()
                .filter(c -> c.getExpectedHarvestDate() != null
                        && !c.getExpectedHarvestDate().isBefore(today)
                        && !c.getExpectedHarvestDate().isAfter(in30Days)
                        && "GROWING".equals(c.getStatus()))
                .collect(Collectors.toList());
        model.addAttribute("upcomingHarvests", upcomingHarvests);
        model.addAttribute("hasAlerts", !upcomingHarvests.isEmpty());

        // Weather
        WeatherService.WeatherData weather = weatherService.getWeather(city);
        model.addAttribute("weather", weather);
        model.addAttribute("selectedCity", city);

        // Finance summary
        model.addAttribute("totalRevenue", saleService.getTotalRevenue(user));
        model.addAttribute("totalExpenses", expenseService.getTotalExpenses(user));
        model.addAttribute("netProfit",
                saleService.getTotalRevenue(user)
                        .subtract(expenseService.getTotalExpenses(user)));

        return "dashboard";
    }
}