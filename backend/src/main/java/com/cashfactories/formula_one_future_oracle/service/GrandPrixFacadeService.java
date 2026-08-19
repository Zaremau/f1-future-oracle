package com.cashfactories.formula_one_future_oracle.service;

import com.cashfactories.formula_one_future_oracle.dto.ActualResultDto;
import com.cashfactories.formula_one_future_oracle.dto.PredictionDto;
import com.cashfactories.formula_one_future_oracle.model.*;
import com.cashfactories.formula_one_future_oracle.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Фасадный сервис для предоставления данных фронтенду.
 * Определяет, какую информацию вернуть пользователю в зависимости от стадии Гран-при:
 * реальные результаты (если гонка завершена) или прогнозы (если гонка предстоящая).
 */
@Service
@RequiredArgsConstructor
public class GrandPrixFacadeService {

    private final GrandPrixRepository gpRepo;
    private final PredictionRepository predictionRepo;
    private final ActualResultRepository actualResultRepo;
    private final OpenF1Service openF1Service;
    private final PredictionService predictionService;

    /**
     * Возвращает список всех Гран-при сезона.
     *
     * @return список сущностей GrandPrix
     */
    public List<GrandPrix> getAllGrandPrix() {
        return gpRepo.findAll();
    }

    /**
     * Главный метод получения данных по Гран-при для фронтенда.
     * Если гонка завершена, возвращает фактические результаты (ActualResultDto).
     * Если гонка предстоящая, синхронизирует данные с OpenF1, при необходимости
     * генерирует новый прогноз и возвращает его (PredictionDto).
     *
     * @param gpId идентификатор Гран-при
     * @return список DTO (прогнозы или результаты)
     */
    public List<?> getGrandPrixData(Long gpId) {
        GrandPrix gp = gpRepo.findById(gpId).orElseThrow();

        if ("RACE_DONE".equals(gp.getStage())) {
            return getRaceResults(gpId);
        }

        openF1Service.syncGrandPrixData(gpId);

        GrandPrix updatedGp = gpRepo.findById(gpId).orElseThrow();

        if ("RACE_DONE".equals(updatedGp.getStage())) {
            return getRaceResults(gpId);
        }

        List<Prediction> predictions = predictionRepo.findByGrandPrix_Id(gpId);

        boolean needNewPrediction = predictions.isEmpty()
                || !predictions.get(0).getStage().equals(updatedGp.getStage());

        if (needNewPrediction) {
            if (!predictions.isEmpty()) {
                predictionRepo.deleteAll(predictions);
            }

            openF1Service.syncHistoricalData(updatedGp);

            predictions = predictionService.generatePredictions(gpId);
        }

        return predictions.stream()
                .map(this::convertToPredictionDto)
                .toList();
    }

    /**
     * Формирует список результатов завершенной гонки.
     * Если результатов в базе нет, пытается загрузить их из OpenF1.
     * Также сопоставляет результаты с прогнозами для вычисления ошибки (Explainable AI).
     *
     * @param gpId идентификатор Гран-при
     * @return список DTO с результатами и оценкой ошибки прогноза
     */
    private List<ActualResultDto> getRaceResults(Long gpId) {
        List<ActualResult> results = actualResultRepo.findByGrandPrix_IdOrderByFinalPositionAsc(gpId);

        if (results.isEmpty()) {
            openF1Service.fetchAndSaveRaceResults(gpId);
            results = actualResultRepo.findByGrandPrix_IdOrderByFinalPositionAsc(gpId);
        }

        return results.stream()
                .map(res -> {
                    Prediction prediction = predictionRepo.findByGrandPrix_IdAndDriver_Id(gpId, res.getDriver().getId());

                    Integer predictedPosition = (prediction == null) ? null : prediction.getPredictedPosition();

                    String explanation;
                    if (prediction == null) {
                        explanation = "Прогноз на эту гонку не строился.";
                    } else {
                        // Защита от null, если explanation по какой-то причине не сформировался
                        explanation = res.getErrorExplanation() != null ? res.getErrorExplanation() : "Система не смогла объяснить ошибку.";
                    }

                    return ActualResultDto.builder()
                            .driverName(res.getDriver().getName())
                            .team(res.getDriver().getTeam())
                            .predictedPosition(predictedPosition)
                            .actualPosition(res.getFinalPosition())
                            .errorMargin(res.getErrorMargin())
                            .explanation(explanation)
                            .build();
                })
                .toList();
    }

    /**
     * Конвертирует сущность Prediction в PredictionDto для передачи на фронтенд.
     *
     * @param pred сущность прогноза
     * @return DTO прогноза
     */
    private PredictionDto convertToPredictionDto(Prediction pred) {
        return PredictionDto.builder()
                .driverName(pred.getDriver().getName())
                .team(pred.getDriver().getTeam())
                .predictedPosition(pred.getPredictedPosition())
                .confidence(pred.getConfidence())
                .riskLevel(pred.getRiskLevel())
                .arguments(pred.getArguments())
                .stage(pred.getStage())
                .build();
    }
}