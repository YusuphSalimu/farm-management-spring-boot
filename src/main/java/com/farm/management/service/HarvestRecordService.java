package com.farm.management.service;

import com.farm.management.entity.HarvestRecord;
import com.farm.management.entity.User;
import com.farm.management.repository.HarvestRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HarvestRecordService {

    @Autowired
    private HarvestRecordRepository harvestRecordRepository;

    public List<HarvestRecord> findByUser(User user) {
        return harvestRecordRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<HarvestRecord> findById(Long id) {
        return harvestRecordRepository.findById(id);
    }

    public HarvestRecord save(HarvestRecord harvestRecord) {
        return harvestRecordRepository.save(harvestRecord);
    }

    public void delete(Long id) {
        harvestRecordRepository.deleteById(id);
    }

    public Long countByUser(User user) {
        return harvestRecordRepository.countByUser(user);
    }
}