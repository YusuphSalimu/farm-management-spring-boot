package com.farm.management.repository;

import com.farm.management.entity.HarvestRecord;
import com.farm.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HarvestRecordRepository extends JpaRepository<HarvestRecord, Long> {
    List<HarvestRecord> findByUser(User user);
    List<HarvestRecord> findByUserOrderByCreatedAtDesc(User user);
    Long countByUser(User user);
}