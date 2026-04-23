package com.example.demo.controller;

import com.example.demo.dto.PowerResponse;
import com.example.demo.service.PowerCalculatorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/power")
public class PowerController {

    private final PowerCalculatorService powerService;

    public PowerController(PowerCalculatorService powerService) {
        this.powerService = powerService;
    }

    @GetMapping("/calculate")
    public PowerResponse getCalculation(@RequestParam Long cpuId, @RequestParam Long gpuId) {
        int wattage = powerService.calculate(cpuId, gpuId);

        // Формируем умную рекомендацию
        String advice;
        if (wattage >= 800) {
            advice = "⚠️ Нужен очень мощный БП! Рекомендуем сертификат 80+ Gold.";
        } else if (wattage >= 600) {
            advice = "⚡ Отличная игровая сборка. Подойдет надежный БП от 600W.";
        } else {
            advice = "✅ Стандартная сборка. Подойдет обычный БП.";
        }

        return new PowerResponse(wattage, advice);
    }
}