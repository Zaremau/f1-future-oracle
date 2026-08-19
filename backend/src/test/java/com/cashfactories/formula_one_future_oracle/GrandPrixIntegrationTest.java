package com.cashfactories.formula_one_future_oracle;

import com.cashfactories.formula_one_future_oracle.model.*;
import com.cashfactories.formula_one_future_oracle.repository.*;
import com.cashfactories.formula_one_future_oracle.service.OpenF1Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GrandPrixIntegrationTest {

    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired private MockMvc mockMvc;
    @Autowired private DriverRepository driverRepository;
    @Autowired private GrandPrixRepository grandPrixRepository;
    @Autowired private PredictionRepository predictionRepository;
    @Autowired private ActualResultRepository actualResultRepository;
    @Autowired private HistoricalResultRepository historicalResultRepository;

    @MockitoBean
    private OpenF1Service openF1Service;

    @BeforeEach
    void setUp() {
        actualResultRepository.deleteAll();
        predictionRepository.deleteAll();
        historicalResultRepository.deleteAll();
        grandPrixRepository.deleteAll();
        driverRepository.deleteAll();

        Driver driver = driverRepository.save(Driver.builder().name("Lando Norris").team("McLaren").driverNumber(4).build());

        GrandPrix gp = GrandPrix.builder()
                .name("Dutch Grand Prix")
                .country("Netherlands")
                .raceDate(LocalDateTime.now().plusDays(5))
                .stage("UPCOMING")
                .build();
        grandPrixRepository.save(gp);
    }

    @Test
    void getAllGrandPrix_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/grand-prix"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dutch Grand Prix"))
                .andExpect(jsonPath("$[0].stage").value("UPCOMING"));
    }

    @Test
    void getGrandPrixData_WhenUpcoming_ShouldReturnPredictions() throws Exception {
        Long gpId = grandPrixRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/grand-prix/{id}/data", gpId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].driverName").value("Lando Norris"))
                .andExpect(jsonPath("$[0].predictedPosition").value(1))
                .andExpect(jsonPath("$[0].confidence").isNumber());
    }
}