package com.farm.management.service;

import com.farm.management.entity.Crop;
import com.farm.management.entity.User;
import com.farm.management.repository.CropRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CropService {

    @Autowired
    private CropRepository cropRepository;

    public List<Crop> findByUser(User user) {
        return cropRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<Crop> findById(Long id) {
        return cropRepository.findById(id);
    }

    public Crop save(Crop crop) {
        return cropRepository.save(crop);
    }

    public void delete(Long id) {
        cropRepository.deleteById(id);
    }

    public Long countByUser(User user) {
        return cropRepository.countByUser(user);
    }
}