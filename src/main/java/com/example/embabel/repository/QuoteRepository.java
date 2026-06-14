package com.example.embabel.repository;

import com.example.embabel.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByCustomerId(Long customerId);
    List<Quote> findByVehicleId(Long vehicleId);
    List<Quote> findByStatus(Quote.QuoteStatus status);
}