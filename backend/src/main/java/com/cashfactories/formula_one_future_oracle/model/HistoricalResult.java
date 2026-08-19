package com.cashfactories.formula_one_future_oracle.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "historical_results",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_historical_driver_gp_season",
                columnNames = {"driver_id", "gp_name", "season"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "gp_name", length = 100, nullable = false)
    private String gpName;

    @Column(name = "season", nullable = false)
    private Integer season;

    @Column(name = "final_position")
    private Integer finalPosition;

    @Column(name = "team_name", length = 100)
    private String teamName;

    @Column(name = "session_key")
    private Integer sessionKey;
}