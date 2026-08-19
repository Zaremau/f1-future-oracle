package com.cashfactories.formula_one_future_oracle.repository;

import com.cashfactories.formula_one_future_oracle.model.HistoricalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoricalResultRepository
        extends JpaRepository<HistoricalResult, Long> {

    List<HistoricalResult> findByDriver_Id(Long driverId);

    List<HistoricalResult> findByDriver_IdAndGpName(
            Long driverId,
            String gpName
    );

    List<HistoricalResult> findByDriver_IdAndSeason(
            Long driverId,
            Integer season
    );

    List<HistoricalResult> findByDriver_IdAndGpNameAndSeason(
            Long driverId,
            String gpName,
            Integer season
    );

    boolean existsByDriver_IdAndGpNameAndSeason(
            Long driverId,
            String gpName,
            Integer season
    );

    boolean existsByGpNameAndSeason(
            String gpName,
            Integer season
    );

    @Query("""
        select avg(h.finalPosition)
        from HistoricalResult h
        where h.driver.id = :driverId
          and h.season = :season
          and h.finalPosition is not null
    """)
    Double findAveragePositionByDriverAndSeason(
            @Param("driverId") Long driverId,
            @Param("season") Integer season
    );

    @Query("""
        select avg(h.finalPosition)
        from HistoricalResult h
        where h.driver.id = :driverId
          and h.gpName = :gpName
          and h.season < :currentSeason
          and h.finalPosition is not null
    """)
    Double findAverageTrackPosition(
            @Param("driverId") Long driverId,
            @Param("gpName") String gpName,
            @Param("currentSeason") Integer currentSeason
    );
}