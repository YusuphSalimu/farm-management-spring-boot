package com.farm.management.service;

import com.farm.management.entity.Farm;
import com.farm.management.entity.User;
import com.farm.management.repository.FarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FarmService {

    @Autowired
    private FarmRepository farmRepository;

    public List<Farm> findByUser(User user) {
        return farmRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<Farm> findById(Long id) {
        return farmRepository.findById(id);
    }

    public Farm save(Farm farm) {
        return farmRepository.save(farm);
    }

    public void delete(Long id) {
        farmRepository.deleteById(id);
    }

    public Long countByUser(User user) {
        return farmRepository.countByUser(user);
    }
}