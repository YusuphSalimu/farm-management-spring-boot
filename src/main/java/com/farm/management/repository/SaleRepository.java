package com.farm.management.repository;

import com.farm.management.entity.Sale;
import com.farm.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByUserOrderByCreatedAtDesc(User user);
    List<Sale> findByUser(User user);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.user = :user")
    BigDecimal getTotalRevenueByUser(User user);

    Long countByUser(User user);
}
