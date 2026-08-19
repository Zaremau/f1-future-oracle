package com.cashfactories.formula_one_future_oracle.service;

import com.cashfactories.formula_one_future_oracle.model.GrandPrix;
import com.cashfactories.formula_one_future_oracle.repository.GrandPrixRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final GrandPrixRepository gpRepo;
    private final RssParserService rssParserService;
    private final PredictionService predictionService;

    /**
     * Раз в час обновляет новости и прогноз по каждому гран-при
     */
    @Scheduled(cron = "0 0 * * * *")
    public void hourlyNewsUpdate() {
        Optional<GrandPrix> upcomingGp = gpRepo.findFirstByStageNotOrderByRaceDateAsc("RACE_DONE");

        if (upcomingGp.isPresent()) {
            Long gpId = upcomingGp.get().getId();

            log.info("Планировщик: Обновление новостей для GP ID {}", gpId);

            rssParserService.fetchAndProcessNews(gpId);
            predictionService.generatePredictions(gpId);
        }
    }
}
