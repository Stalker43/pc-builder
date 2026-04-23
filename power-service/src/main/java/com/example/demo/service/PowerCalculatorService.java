package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class PowerCalculatorService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String COMPONENT_API = "http://component-service:8081/api/";

    public int calculate(Long cpuId, Long gpuId) {
        try {
            // Запрашиваем данные о компонентах со склада
            Map cpu = restTemplate.getForObject(COMPONENT_API + "cpus/" + cpuId, Map.class);
            Map gpu = restTemplate.getForObject(COMPONENT_API + "gpus/" + gpuId, Map.class);

            if (cpu == null || gpu == null) return 500;

            // БЕЗОПАСНОЕ ИЗВЛЕЧЕНИЕ ЧИСЕЛ (защита от ClassCastException)
            int cpuTdp = ((Number) cpu.get("tdp")).intValue();
            int gpuPower = ((Number) gpu.get("powerRequired")).intValue();

            // Считаем потребление: (Процессор + Видеокарта) + 20% запаса надежности
            double totalPower = (cpuTdp + gpuPower) * 1.2;
            return (int) Math.ceil(totalPower);

        } catch (Exception e) {
            // ТЕПЕРЬ ОШИБКА БУДЕТ ВИДНА В КОНСОЛИ
            System.err.println("КРИТИЧЕСКАЯ ОШИБКА РАСЧЕТА МОЩНОСТИ:");
            e.printStackTrace();
            return 500;
        }
    }
}