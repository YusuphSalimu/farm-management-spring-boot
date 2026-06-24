package com.farm.management.service;

import com.farm.management.entity.Crop;
import com.farm.management.entity.Inventory;
import com.farm.management.entity.User;
import com.farm.management.repository.CropRepository;
import com.farm.management.repository.InventoryRepository;
import com.farm.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class AlertSchedulerService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CropRepository cropRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyHarvestAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);

        List<User> users = userRepository.findAll();
        for (User user : users) {
            List<Crop> crops = cropRepository.findByUser(user);
            for (Crop crop : crops) {
                if (crop.getExpectedHarvestDate() != null
                        && !crop.getExpectedHarvestDate().isBefore(today)
                        && !crop.getExpectedHarvestDate().isAfter(in7Days)
                        && "GROWING".equals(crop.getStatus())) {
                    emailService.sendHarvestAlert(
                            user.getEmail(),
                            user.getFullName(),
                            crop.getCropName(),
                            crop.getExpectedHarvestDate().toString(),
                            crop.getFieldLocation() != null ?
                                    crop.getFieldLocation() : "Not specified"
                    );
                }
            }
        }
    }

    // Runs every day at 9:00 AM — Low stock alerts
    @Scheduled(cron = "0 0 9 * * *")
    public void sendLowStockAlerts() {
        BigDecimal lowThreshold = new BigDecimal("10");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            List<Inventory> items = inventoryRepository.findByUser(user);
            for (Inventory item : items) {
                if (item.getQuantity() != null
                        && item.getQuantity().compareTo(lowThreshold) <= 0) {
                    emailService.sendLowStockAlert(
                            user.getEmail(),
                            user.getFullName(),
                            item.getItemName(),
                            item.getQuantity().toString(),
                            item.getUnit() != null ? item.getUnit() : ""
                    );
                }
            }
        }
    }
}
