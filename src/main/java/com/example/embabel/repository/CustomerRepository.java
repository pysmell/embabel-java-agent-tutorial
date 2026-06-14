package com.example.embabel.repository;

import com.example.embabel.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(String userId);
    Optional<Customer> findByEmail(String email);
}