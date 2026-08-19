package com.cashfactories.formula_one_future_oracle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualResultDto {
    private String driverName;
    private String team;
    private Integer predictedPosition;
    private Integer actualPosition;
    private Integer errorMargin;
    private String explanation;
}
