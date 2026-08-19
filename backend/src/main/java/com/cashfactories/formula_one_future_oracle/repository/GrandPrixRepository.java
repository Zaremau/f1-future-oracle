package com.cashfactories.formula_one_future_oracle.repository;

import com.cashfactories.formula_one_future_oracle.model.GrandPrix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrandPrixRepository extends JpaRepository<GrandPrix, Long> {
    Optional<GrandPrix> findFirstByStageNotOrderByRaceDateAsc(String stage);
}
