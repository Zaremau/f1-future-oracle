package com.cashfactories.formula_one_future_oracle.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gp_id", nullable = false)
    private GrandPrix grandPrix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "predicted_position")
    private Integer predictedPosition;

    @Column(name = "confidence")
    private Double confidence;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "arguments", columnDefinition = "jsonb")
    private String arguments;

    @Column(name = "stage", length = 20)
    private String stage;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Transient
    private Double score;
}