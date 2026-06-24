package com.farm.management.repository;

import com.farm.management.entity.Crop;
import com.farm.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
    List<Crop> findByUser(User user);
    List<Crop> findByUserOrderByCreatedAtDesc(User user);
    Long countByUser(User user);
}