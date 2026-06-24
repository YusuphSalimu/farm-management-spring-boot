package com.farm.management.service;

import com.farm.management.entity.Inventory;
import com.farm.management.entity.User;
import com.farm.management.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Inventory> findByUser(User user) {
        return inventoryRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<Inventory> findById(Long id) {
        return inventoryRepository.findById(id);
    }

    public Inventory save(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public void delete(Long id) {
        inventoryRepository.deleteById(id);
    }

    public Long countByUser(User user) {
        return inventoryRepository.countByUser(user);
    }
}