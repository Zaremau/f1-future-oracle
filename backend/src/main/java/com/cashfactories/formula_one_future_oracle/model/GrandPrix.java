package com.cashfactories.formula_one_future_oracle.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "grands_prix")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class GrandPrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "race_date")
    private LocalDateTime raceDate;

    @Column(name = "stage", length = 20)
    private String stage = "UPCOMING"; // UPCOMING, FP_DONE, QUALI_DONE, RACE_DONE
}
