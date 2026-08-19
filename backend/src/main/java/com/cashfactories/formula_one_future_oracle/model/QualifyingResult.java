package com.cashfactories.formula_one_future_oracle.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "qualifying_results",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_qualifying_gp_driver",
                columnNames = {"gp_id", "driver_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QualifyingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gp_id", nullable = false)
    private GrandPrix grandPrix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "position")
    private Integer position;

    @Column(name = "starting_grid")
    private Integer startingGrid;

    @Column(name = "q3_time_ms")
    private Integer q3TimeMs;
}