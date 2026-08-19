package com.cashfactories.formula_one_future_oracle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionDto {
    private String driverName;
    private String team;
    private Integer predictedPosition;
    private Double confidence;
    private String riskLevel;
    private String arguments;
    private String stage;
}
