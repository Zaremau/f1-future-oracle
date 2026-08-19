package com.cashfactories.formula_one_future_oracle.repository;

import com.cashfactories.formula_one_future_oracle.model.ActualResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActualResultRepository extends JpaRepository<ActualResult,Long> {
    List<ActualResult> findByGrandPrix_IdOrderByFinalPositionAsc(Long gpId);
}
