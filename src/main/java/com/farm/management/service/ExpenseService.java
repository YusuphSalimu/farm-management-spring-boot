package com.farm.management.service;

import com.farm.management.entity.Expense;
import com.farm.management.entity.User;
import com.farm.management.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    public List<Expense> findByUser(User user) {
        return expenseRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id);
    }

    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    public void delete(Long id) {
        expenseRepository.deleteById(id);
    }

    public BigDecimal getTotalExpenses(User user) {
        return expenseRepository.getTotalExpensesByUser(user);
    }

    public Long countByUser(User user) {
        return expenseRepository.countByUser(user);
    }
}
