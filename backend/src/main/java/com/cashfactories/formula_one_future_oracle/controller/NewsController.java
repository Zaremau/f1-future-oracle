package com.cashfactories.formula_one_future_oracle.controller;

import com.cashfactories.formula_one_future_oracle.service.RssParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для управления новостями.
 * Предоставляет эндпоинт для принудительного обновления RSS-лент.
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final RssParserService rssParserService;

    /**
     * Принудительно запускает процесс скачивания, парсинга и анализа новостей
     * (тональность, ключевые слова риска, упоминания пилотов) для указанного Гран-при.
     *
     * @param gpId идентификатор Гран-при, для которого обновляются новости
     * @return строку с подтверждением успешного запуска обновления и HTTP-статус 200 (OK)
     */
    @GetMapping("/refresh/{gpId}")
    public ResponseEntity<String> refreshNews(@PathVariable Long gpId) {
        rssParserService.fetchAndProcessNews(gpId);
        return ResponseEntity.ok("Новости успешно обновлены для GP ID: " + gpId);
    }
}