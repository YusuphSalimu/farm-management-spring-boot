package com.farm.management.repository;

import com.farm.management.entity.Inventory;
import com.farm.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByUser(User user);
    List<Inventory> findByUserOrderByCreatedAtDesc(User user);
    Long countByUser(User user);
}