package com.cashfactories.formula_one_future_oracle.repository;

import com.cashfactories.formula_one_future_oracle.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByDriverNumber(Integer driverNumber);
}
