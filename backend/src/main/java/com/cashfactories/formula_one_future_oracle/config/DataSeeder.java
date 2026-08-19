package com.cashfactories.formula_one_future_oracle.config;

import com.cashfactories.formula_one_future_oracle.model.Driver;
import com.cashfactories.formula_one_future_oracle.model.GrandPrix;
import com.cashfactories.formula_one_future_oracle.repository.DriverRepository;
import com.cashfactories.formula_one_future_oracle.repository.GrandPrixRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final DriverRepository driverRepo;
    private final GrandPrixRepository gpRepo;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OPENF1_BASE = "https://api.openf1.org/v1";

    @Override
    public void run(String... args) {
        if (gpRepo.count() > 0) {
            log.info("База данных уже содержит календарь, пропускаем инициализацию.");
            return;
        }

        log.info("=== Начало инициализации данных (Системная дата: {} год) ===", LocalDateTime.now().getYear());
        try {
            // Пытаемся скачать реальный состав пилотов сезона
            seedRealDriversFromOpenF1();
            log.info("=== Реальные пилоты из OpenF1 загружены ===");
        } catch (Exception e) {
            log.warn("!!! Не удалось получить пилотов из OpenF1: {}. Используем Fallback.", e.getMessage());
            seedFallbackDrivers();
        }

        // Загружаем календарь текущего года (2026)
        seedCalendar2026();
        log.info("=== Инициализация завершена успешно ===");
    }

    // 1. Скачиваем только пилотов (это быстро)
    private void seedRealDriversFromOpenF1() throws Exception {
        log.info("Запрос пилотов из OpenF1...");
        String json = restTemplate.getForObject(OPENF1_BASE + "/drivers?session_key=latest", String.class);
        JsonNode root = objectMapper.readTree(json);
        log.info("Получено пилотов из API: {}", root.size());

        for (JsonNode node : root) {
            // Используем path() чтобы не падать, если поля нет в JSON
            Driver driver = Driver.builder()
                    .name(node.path("full_name").asText("Unknown Driver"))
                    .team(node.path("team_name").asText("Unknown Team"))
                    .driverNumber(node.path("driver_number").asInt(0))
                    .build();
            driverRepo.save(driver);
        }
    }

    // 2. Календарь 2026 года (относительно текущей даты системы)
    private void seedCalendar2026() {
        log.info("Загрузка календаря 2026 года...");
        LocalDateTime now = LocalDateTime.now();

        // Гонки, которые УЖЕ прошли (до августа 2026)
        gpRepo.save(GrandPrix.builder().name("Bahrain Grand Prix").country("Bahrain").raceDate(now.minusDays(140)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Saudi Arabian Grand Prix").country("Saudi Arabia").raceDate(now.minusDays(130)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Australian Grand Prix").country("Australia").raceDate(now.minusDays(120)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Japanese Grand Prix").country("Japan").raceDate(now.minusDays(110)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Chinese Grand Prix").country("China").raceDate(now.minusDays(100)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Miami Grand Prix").country("USA").raceDate(now.minusDays(90)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Emilia Romagna Grand Prix").country("Italy").raceDate(now.minusDays(80)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Monaco Grand Prix").country("Monaco").raceDate(now.minusDays(70)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Canadian Grand Prix").country("Canada").raceDate(now.minusDays(60)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Spanish Grand Prix").country("Spain").raceDate(now.minusDays(50)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Austrian Grand Prix").country("Austria").raceDate(now.minusDays(40)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("British Grand Prix").country("UK").raceDate(now.minusDays(30)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Hungarian Grand Prix").country("Hungary").raceDate(now.minusDays(20)).stage("RACE_DONE").build());
        gpRepo.save(GrandPrix.builder().name("Belgian Grand Prix").country("Belgium").raceDate(now.minusDays(10)).stage("RACE_DONE").build());

        // Гонки, которые ПЛАНИРУЕТСЯ ПРОВЕСТИ (после августа 2026)
        gpRepo.save(GrandPrix.builder().name("Dutch Grand Prix").country("Netherlands").raceDate(now.plusDays(5)).stage("UPCOMING").build());
        gpRepo.save(GrandPrix.builder().name("Italian Grand Prix").country("Italy").raceDate(now.plusDays(15)).stage("UPCOMING").build());
        gpRepo.save(GrandPrix.builder().name("Azerbaijan Grand Prix").country("Azerbaijan").raceDate(now.plusDays(25)).stage("UPCOMING").build());
        gpRepo.save(GrandPrix.builder().name("Singapore Grand Prix").country("Singapore").raceDate(now.plusDays(35)).stage("UPCOMING").build());
        gpRepo.save(GrandPrix.builder().name("United States Grand Prix").country("USA").raceDate(now.plusDays(45)).stage("UPCOMING").build());
        gpRepo.save(GrandPrix.builder().name("Mexico City Grand Prix").country("Mexico").raceDate(now.plusDays(55)).stage("UPCOMING").build());
        gpRepo.save(GrandPrix.builder().name("São Paulo Grand Prix").country("Brazil").raceDate(now.plusDays(65)).stage("UPCOMING").build());
        gpRepo.save(GrandPrix.builder().name("Las Vegas Grand Prix").country("USA").raceDate(now.plusDays(75)).stage("UPCOMING").build());
        gpRepo.save(GrandPrix.builder().name("Qatar Grand Prix").country("Qatar").raceDate(now.plusDays(85)).stage("UPCOMING").build());
        gpRepo.save(GrandPrix.builder().name("Abu Dhabi Grand Prix").country("UAE").raceDate(now.plusDays(95)).stage("UPCOMING").build());
    }

    // 3. Fallback для пилотов (если OpenF1 недоступен)
    private void seedFallbackDrivers() {
        log.info("Загрузка резервных пилотов...");
        driverRepo.save(Driver.builder().name("Max Verstappen").team("Red Bull Racing").driverNumber(1).build());
        driverRepo.save(Driver.builder().name("Sergio Perez").team("Red Bull Racing").driverNumber(11).build());
        driverRepo.save(Driver.builder().name("Charles Leclerc").team("Ferrari").driverNumber(16).build());
        driverRepo.save(Driver.builder().name("Lewis Hamilton").team("Ferrari").driverNumber(44).build());
        driverRepo.save(Driver.builder().name("Lando Norris").team("McLaren").driverNumber(4).build());
        driverRepo.save(Driver.builder().name("Oscar Piastri").team("McLaren").driverNumber(81).build());
        driverRepo.save(Driver.builder().name("George Russell").team("Mercedes").driverNumber(63).build());
        driverRepo.save(Driver.builder().name("Kimi Antonelli").team("Mercedes").driverNumber(12).build());
        driverRepo.save(Driver.builder().name("Fernando Alonso").team("Aston Martin").driverNumber(14).build());
        driverRepo.save(Driver.builder().name("Lance Stroll").team("Aston Martin").driverNumber(18).build());
        driverRepo.save(Driver.builder().name("Pierre Gasly").team("Alpine").driverNumber(10).build());
        driverRepo.save(Driver.builder().name("Esteban Ocon").team("Alpine").driverNumber(31).build());
        driverRepo.save(Driver.builder().name("Carlos Sainz").team("Williams").driverNumber(55).build());
        driverRepo.save(Driver.builder().name("Alex Albon").team("Williams").driverNumber(23).build());
        driverRepo.save(Driver.builder().name("Yuki Tsunoda").team("RB").driverNumber(22).build());
        driverRepo.save(Driver.builder().name("Liam Lawson").team("RB").driverNumber(30).build());
        driverRepo.save(Driver.builder().name("Nico Hulkenberg").team("Haas").driverNumber(27).build());
        driverRepo.save(Driver.builder().name("Valtteri Bottas").team("Kick Sauber").driverNumber(77).build());
        driverRepo.save(Driver.builder().name("Zhou Guanyu").team("Kick Sauber").driverNumber(24).build());
        driverRepo.save(Driver.builder().name("Franco Colapinto").team("Kick Sauber").driverNumber(43).build());
    }
}