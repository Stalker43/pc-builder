package com.example.demo;

import com.example.demo.service.PowerCalculatorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class PowerServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PowerCalculatorService powerService;

    private final String COMPONENT_SERVICE_URL = "http://component-service:8081/api/";

    @BeforeEach
    void setUp() {
        // Имитируем данные от component-service
        Map<String, Object> mockCpu = new HashMap<>();
        mockCpu.put("tdp", 105);

        Map<String, Object> mockGpu = new HashMap<>();
        mockGpu.put("powerRequired", 200);

        Mockito.lenient().when(restTemplate.getForObject(COMPONENT_SERVICE_URL + "cpus/1", Map.class))
                .thenReturn(mockCpu);
        Mockito.lenient().when(restTemplate.getForObject(COMPONENT_SERVICE_URL + "gpus/2", Map.class))
                .thenReturn(mockGpu);
    }

    @Test
    void testCalculatePower() {

        int resultWattage = powerService.calculate(1L, 2L);

        // Проверяем, что расчетная мощность верна
        Assertions.assertTrue(resultWattage >= 305, "Мощность должна быть не меньше суммы TDP компонентов");

        System.out.println("✅ Тест пройден. Рассчитанная мощность: " + resultWattage + "W");
    }
}