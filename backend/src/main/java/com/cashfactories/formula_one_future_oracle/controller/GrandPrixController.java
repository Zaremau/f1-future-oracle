package com.cashfactories.formula_one_future_oracle.controller;

import com.cashfactories.formula_one_future_oracle.model.GrandPrix;
import com.cashfactories.formula_one_future_oracle.service.GrandPrixFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для работы с данными Гран-при Формулы 1.
 * Предоставляет эндпоинты для получения списка гонок, прогнозов и результатов.
 */
@RestController
@RequestMapping("/api/grand-prix")
@RequiredArgsConstructor
public class GrandPrixController {

    private final GrandPrixFacadeService facadeService;

    /**
     * Возвращает список всех Гран-при сезона.
     *
     * @return список объектов GrandPrix и HTTP-статус 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<GrandPrix>> getAllGrandPrix() {
        return ResponseEntity.ok(facadeService.getAllGrandPrix());
    }

    /**
     * Возвращает данные по-конкретному Гран-при.
     * В зависимости от стадии гонки (UPCOMING, FP_DONE, QUALI_DONE, RACE_DONE)
     * возвращает либо прогноз на гонку, либо фактические результаты с подсчетом ошибки.
     *
     * @param gpId идентификатор Гран-при (передается в URL)
     * @return список DTO с прогнозами или результатами и HTTP-статус 200 (OK)
     */
    @GetMapping("/{gpId}/data")
    public ResponseEntity<List<?>> getGrandPrixData(@PathVariable Long gpId) {
        return ResponseEntity.ok(facadeService.getGrandPrixData(gpId));
    }
}