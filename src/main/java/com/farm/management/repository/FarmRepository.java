package com.farm.management.repository;

import com.farm.management.entity.Farm;
import com.farm.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByUserOrderByCreatedAtDesc(User user);
    Long countByUser(User user);
}