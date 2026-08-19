package com.cashfactories.formula_one_future_oracle.repository;

import com.cashfactories.formula_one_future_oracle.model.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findByIsProcessedFalse();

    List<News> findByGrandPrix_Id(Long id);

    boolean existsByUrl(String link);

    boolean existsByGrandPrix_IdAndUrl(Long grandPrixId, String link);
}
