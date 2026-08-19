package com.cashfactories.formula_one_future_oracle.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "practice_results",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_practice_gp_driver_session",
                columnNames = {"gp_id", "driver_id", "session_type"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PracticeResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gp_id", nullable = false)
    private GrandPrix grandPrix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "session_type", length = 10)
    private String sessionType;

    @Column(name = "position")
    private Integer position;

    @Column(name = "lap_time_ms")
    private Integer lapTimeMs;

    @Column(name = "gap_to_p1_ms")
    private Integer gapToP1Ms;
}