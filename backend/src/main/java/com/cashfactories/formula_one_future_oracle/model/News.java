package com.cashfactories.formula_one_future_oracle.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gp_id")
    private GrandPrix grandPrix;

    @Column(length = 50)
    private String source;

    @Column(columnDefinition = "text")
    private String title;

    @Column(columnDefinition = "text")
    private String url;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "sentiment_score")
    private Double sentimentScore = 0.0;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "risk_keywords", columnDefinition = "text[]")
    private String[] riskKeywords;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "mentioned_drivers", columnDefinition = "text[]")
    private String[] mentionedDrivers;

    @Column(name = "is_processed")
    private Boolean isProcessed = false;

    @Column(name = "raw_xml", columnDefinition = "text")
    private String rawXml;
}