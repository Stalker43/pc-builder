package com.example.demo.controller;

import com.example.demo.dto.CpuDto;
import com.example.demo.dto.MotherboardDto;
import com.example.demo.dto.RamDto;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compatibility")
public class CompatibilityController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String COMPONENT_API = "http://component-service:8081/api/";

    // Возвращаем правильный @GetMapping
    @GetMapping("/motherboards")
    public List<MotherboardDto> getCompatibleMotherboards(@RequestParam Long cpuId) {

        // 1. Находим процессор на складе по ID
        CpuDto[] allCpus = restTemplate.getForObject(COMPONENT_API + "cpus", CpuDto[].class);
        CpuDto selectedCpu = Arrays.stream(allCpus)
                .filter(cpu -> cpu.getId().equals(cpuId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Процессор не найден на складе!"));

        String requiredSocket = selectedCpu.getSocket();

        // 2. Находим материнские платы и фильтруем по сокету
        MotherboardDto[] allMbs = restTemplate.getForObject(COMPONENT_API + "motherboards", MotherboardDto[].class);
        return Arrays.stream(allMbs)
                .filter(mb -> mb.getSocket().equalsIgnoreCase(requiredSocket))
                .collect(Collectors.toList());
    }

    // Возвращаем правильный @GetMapping
    @GetMapping("/rams")
    public List<RamDto> getCompatibleRam(@RequestParam Long mbId) {
        try {
            // 1. Находим материнскую плату на складе по ID
            MotherboardDto[] allMbs = restTemplate.getForObject(COMPONENT_API + "motherboards", MotherboardDto[].class);

            if (allMbs == null) {
                System.err.println("Склад не вернул материнские платы.");
                return List.of();
            }

            // Ищем нужную плату безопасно (без throw)
            MotherboardDto selectedMb = Arrays.stream(allMbs)
                    .filter(mb -> mb.getId().equals(mbId))
                    .findFirst()
                    .orElse(null);

            // Защита от старых кнопок в Телеграме
            if (selectedMb == null) {
                System.err.println("ОШИБКА: Материнская плата с ID " + mbId + " не найдена на складе! Скорее всего, нажата старая кнопка в боте.");
                return List.of();
            }

            // 2. Достаем тип памяти и проверяем на null
            String requiredRamType = selectedMb.getRamType();
            if (requiredRamType == null || requiredRamType.trim().isEmpty()) {
                System.err.println("ОШИБКА: У материнской платы " + selectedMb.getName() + " поле ramType пустое (null)!");
                return List.of();
            }

            // 3. Запрашиваем со склада только подходящую память
            RamDto[] compatibleRams = restTemplate.getForObject(
                    COMPONENT_API + "rams/type/" + requiredRamType.trim(),
                    RamDto[].class
            );

            return Arrays.asList(compatibleRams != null ? compatibleRams : new RamDto[0]);

        } catch (Exception e) {
            // Если упала сеть или склад недоступен
            System.err.println("Критическая ошибка при поиске совместимой ОЗУ: " + e.getMessage());
            return List.of();
        }
    }
}