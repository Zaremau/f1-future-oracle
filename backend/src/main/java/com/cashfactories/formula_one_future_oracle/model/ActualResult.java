package com.cashfactories.formula_one_future_oracle.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "actual_results",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_actual_gp_driver",
                columnNames = {"gp_id", "driver_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gp_id", nullable = false)
    private GrandPrix grandPrix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "final_position")
    private Integer finalPosition;

    @Column(name = "error_margin")
    private Integer errorMargin;

    @Column(name = "error_type", length = 30)
    private String errorType;

    @Column(name = "error_explanation", columnDefinition = "TEXT")
    private String errorExplanation;
}