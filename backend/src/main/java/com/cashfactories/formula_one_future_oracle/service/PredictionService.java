package com.cashfactories.formula_one_future_oracle.service;

import com.cashfactories.formula_one_future_oracle.model.*;
import com.cashfactories.formula_one_future_oracle.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Сервис прогнозирования результатов Гран-при.
 * Рассчитывает итоговый скор (Score) для каждого пилота на основе взвешенной модели,
 * учитывающей историю выступлений, новости, результаты практик и квалификации.
 */
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final DriverRepository driverRepo;
    private final GrandPrixRepository gpRepo;
    private final NewsRepository newsRepo;
    private final HistoricalResultRepository histRepo;
    private final PracticeRepository practiceRepo;
    private final QualifyingRepository qualiRepo;
    private final PredictionRepository predictionRepo;

    /**
     * Генерирует прогнозы для всех пилотов на указанный Гран-при.
     * Сортирует результаты по убыванию Score и назначает предсказанные позиции.
     *
     * @param gpId идентификатор Гран-при
     * @return список сохраненных прогнозов
     */
    @Transactional
    public List<Prediction> generatePredictions(Long gpId) {
        GrandPrix gp = gpRepo.findById(gpId).orElseThrow();
        List<Driver> drivers = driverRepo.findAll();

        // Новости одинаковы для всех пилотов, загружаем один раз
        List<News> gpNews = newsRepo.findByGrandPrix_Id(gp.getId());

        int currentSeason = gp.getRaceDate().getYear();
        List<Prediction> predictions = new ArrayList<>();

        for (Driver driver : drivers) {
            double seasonHistory = calculateSeasonHistoryScore(driver.getId(), currentSeason);
            double trackHistory = calculateTrackHistoryScore(driver.getId(), gp.getName(), currentSeason);

            List<News> driverNews = gpNews.stream()
                    .filter(news -> news.getMentionedDrivers() != null
                            && Arrays.asList(news.getMentionedDrivers()).contains(driver.getName()))
                    .toList();

            double newsScore = calculateNewsScore(driverNews);
            boolean hasPenalty = driverNews.stream()
                    .anyMatch(news -> news.getRiskKeywords() != null && news.getRiskKeywords().length > 0);

            PracticeMetrics practice = calculatePracticeMetrics(gp, driver);
            QualifyingMetrics qualifying = calculateQualifyingMetrics(gp, driver);

            double score = calculateScore(gp.getStage(), seasonHistory, trackHistory, newsScore, practice.score(), qualifying.score(), hasPenalty);
            double confidence = calculateConfidence(gp.getStage(), score, seasonHistory, trackHistory, practice, qualifying, hasPenalty);
            String risk = calculateRisk(gp, qualifying, hasPenalty, practice);
            String arguments = formArguments(seasonHistory, trackHistory, practice, qualifying, newsScore, hasPenalty);

            predictions.add(Prediction.builder()
                    .grandPrix(gp)
                    .driver(driver)
                    .stage(gp.getStage())
                    .score(score)
                    .confidence(confidence)
                    .riskLevel(risk)
                    .arguments(arguments)
                    .build());
        }

        predictions.sort(Comparator.comparing(Prediction::getScore).reversed());
        for (int i = 0; i < predictions.size(); i++) {
            predictions.get(i).setPredictedPosition(i + 1);
        }

        return predictionRepo.saveAll(predictions);
    }

    private double calculateSeasonHistoryScore(Long driverId, int season) {
        Double averagePosition = histRepo.findAveragePositionByDriverAndSeason(driverId, season);
        if (averagePosition == null) return 50.0;
        return positionToScore(averagePosition);
    }

    private double calculateTrackHistoryScore(Long driverId, String gpName, int currentSeason) {
        Double averagePosition = histRepo.findAverageTrackPosition(driverId, gpName, currentSeason);
        if (averagePosition == null) return 50.0;
        return positionToScore(averagePosition);
    }

    private double positionToScore(double averagePosition) {
        return Math.max(0, Math.min(100, 100 - ((averagePosition - 1) * 5)));
    }

    private double calculateNewsScore(List<News> driverNews) {
        if (driverNews.isEmpty()) return 50.0;
        double averageSentiment = driverNews.stream()
                .mapToDouble(News::getSentimentScore)
                .average()
                .orElse(0.0);
        return Math.max(0, Math.min(100, 50 + averageSentiment * 50));
    }

    private PracticeMetrics calculatePracticeMetrics(GrandPrix gp, Driver driver) {
        PracticeResult result = practiceRepo.findTopByGrandPrix_IdAndDriver_IdOrderByLapTimeMsAsc(gp.getId(), driver.getId());
        if (result == null) return PracticeMetrics.empty();

        double positionScore = positionToScore(result.getPosition());
        double gapScore = Math.max(0, 100 - (result.getGapToP1Ms() / 1000.0 * 10));

        // Место — 40%, Отставание от P1 — 60%
        double score = positionScore * 0.4 + gapScore * 0.6;

        return new PracticeMetrics(score, result.getPosition(), result.getGapToP1Ms());
    }

    private QualifyingMetrics calculateQualifyingMetrics(GrandPrix gp, Driver driver) {
        QualifyingResult result = qualiRepo.findByGrandPrix_IdAndDriver_Id(gp.getId(), driver.getId());
        if (result == null) return QualifyingMetrics.empty();

        double qualifyingScore = positionToScore(result.getPosition());
        double gridScore = 0.0;

        if (result.getStartingGrid() != null) {
            gridScore = positionToScore(result.getStartingGrid());
        }

        // Квалификация 40%, Фактическая стартовая позиция 60%
        double score = qualifyingScore * 0.4 + gridScore * 0.6;

        return new QualifyingMetrics(score, result.getPosition(), result.getStartingGrid());
    }

    private double calculateScore(String stage, double seasonHistory, double trackHistory, double newsScore, double practiceScore, double qualifyingScore, boolean hasPenalty) {
        double score;

        switch (stage) {
            case "UPCOMING" -> score = seasonHistory * 0.50 + trackHistory * 0.20 + newsScore * 0.30;
            case "FP_DONE" -> score = seasonHistory * 0.30 + trackHistory * 0.10 + newsScore * 0.20 + practiceScore * 0.40;
            case "QUALI_DONE" -> score = seasonHistory * 0.20 + trackHistory * 0.10 + newsScore * 0.10 + practiceScore * 0.20 + qualifyingScore * 0.40;
            default -> score = 50.0;
        }

        if (hasPenalty) {
            score -= 15;
        }

        return Math.max(0, Math.min(100, score));
    }

    private double calculateConfidence(String stage, double score, double seasonHistory, double trackHistory, PracticeMetrics practice, QualifyingMetrics qualifying, boolean hasPenalty) {
        double base;
        switch (stage) {
            case "UPCOMING" -> base = 0.40;
            case "FP_DONE" -> base = 0.55;
            case "QUALI_DONE" -> base = 0.70;
            default -> base = 0.30;
        }

        // Чем выше качество самого score, тем больше уверенность
        double scoreComponent = Math.abs(score - 50) / 50.0 * 0.15;

        // Наличие реальных данных
        double dataBonus = 0.0;
        if (seasonHistory != 50.0) dataBonus += 0.05;
        if (trackHistory != 50.0) dataBonus += 0.03;
        if (practice.available()) dataBonus += 0.05;
        if (qualifying.available()) dataBonus += 0.07;

        // Если данные противоречат друг другу, уверенность снижаем
        double disagreement = calculateDisagreement(seasonHistory, trackHistory, practice, qualifying);

        double confidence = base + scoreComponent + dataBonus - disagreement;

        if (hasPenalty) {
            confidence -= 0.05;
        }

        return Math.max(0.10, Math.min(0.99, confidence));
    }

    private double calculateDisagreement(double seasonHistory, double trackHistory, PracticeMetrics practice, QualifyingMetrics qualifying) {
        List<Double> scores = new ArrayList<>();
        scores.add(seasonHistory);
        scores.add(trackHistory);

        if (practice.available()) scores.add(practice.score());
        if (qualifying.available()) scores.add(qualifying.score());

        if (scores.size() < 2) return 0.0;

        double average = scores.stream().mapToDouble(Double::doubleValue).average().orElse(50);
        double variance = scores.stream()
                .mapToDouble(value -> Math.pow(value - average, 2))
                .average()
                .orElse(0);

        double standardDeviation = Math.sqrt(variance);
        return Math.min(0.15, standardDeviation / 100.0);
    }

    private String calculateRisk(GrandPrix gp, QualifyingMetrics qualifying, boolean hasPenalty, PracticeMetrics practice) {
        if (hasPenalty) return "HIGH";

        if (qualifying.available() && qualifying.startingGrid() != null && qualifying.startingGrid() > 15) {
            return "HIGH";
        }

        if (practice.available() && practice.position() > 15) {
            return "HIGH";
        }

        if ("UPCOMING".equals(gp.getStage())) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private String formArguments(double seasonHistory, double trackHistory, PracticeMetrics practice, QualifyingMetrics qualifying, double newsScore, boolean hasPenalty) {
        ObjectNode json = new ObjectMapper().createObjectNode();

        json.put("seasonHistory", round(seasonHistory));
        json.put("trackHistory", round(trackHistory));
        json.put("news", round(newsScore));
        json.put("penalty", hasPenalty);

        if (practice.available()) {
            json.put("practiceScore", round(practice.score()));
            json.put("practicePosition", practice.position());
            json.put("practiceGapToP1Ms", practice.gapToP1Ms());
        }

        if (qualifying.available()) {
            json.put("qualifyingScore", round(qualifying.score()));
            json.put("qualifyingPosition", qualifying.qualifyingPosition());
            json.put("startingGrid", qualifying.startingGrid());
        }

        return json.toString();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record PracticeMetrics(double score, Integer position, Integer gapToP1Ms) {
        static PracticeMetrics empty() {
            return new PracticeMetrics(50.0, null, null);
        }
        boolean available() {
            return position != null;
        }
    }

    private record QualifyingMetrics(double score, Integer qualifyingPosition, Integer startingGrid) {
        static QualifyingMetrics empty() {
            return new QualifyingMetrics(50.0, null, null);
        }
        boolean available() {
            return qualifyingPosition != null;
        }
    }
}