package com.cashfactories.formula_one_future_oracle.repository;

import com.cashfactories.formula_one_future_oracle.model.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    // Найти все прогнозы для конкретного Гран-при
    List<Prediction> findByGrandPrix_Id(Long gpId);

    // Найти прогноз для конкретного пилота в конкретном Гран-при (для подсчета error_margin)
    Prediction findByGrandPrix_IdAndDriver_Id(Long gpId, Long driverId);
}
