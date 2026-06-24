package com.farm.management.service;

import com.farm.management.entity.Sale;
import com.farm.management.entity.User;
import com.farm.management.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    public List<Sale> findByUser(User user) {
        return saleRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<Sale> findById(Long id) {
        return saleRepository.findById(id);
    }

    public Sale save(Sale sale) {
        return saleRepository.save(sale);
    }

    public void delete(Long id) {
        saleRepository.deleteById(id);
    }

    public BigDecimal getTotalRevenue(User user) {
        return saleRepository.getTotalRevenueByUser(user);
    }

    public Long countByUser(User user) {
        return saleRepository.countByUser(user);
    }
}