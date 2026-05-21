package com.example.demo.controller;

import com.example.demo.dto.CpuDto;
import com.example.demo.dto.MotherboardDto;
import com.example.demo.dto.RamDto;
import com.example.demo.entity.CompatibilityRule;
import com.example.demo.repository.CompatibilityRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compatibility")
public class CompatibilityController {

    @Autowired
    private CompatibilityRuleRepository compatibilityRuleRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String COMPONENT_API = "http://component-service:8081/api/";

    // =========================================================================
    // 1. КРИТИЧЕСКИЕ МЕТОДЫ ДЛЯ РАБОТЫ ТЕЛЕГРАМ-БОТА (ФИЛЬТРАЦИЯ ПРИ ВЫБОРЕ)
    // =========================================================================

    @GetMapping("/motherboards")
    public List<MotherboardDto> getCompatibleMotherboards(@RequestParam Long cpuId) {
        CpuDto[] allCpus = restTemplate.getForObject(COMPONENT_API + "cpus", CpuDto[].class);
        CpuDto selectedCpu = Arrays.stream(allCpus != null ? allCpus : new CpuDto[0])
                .filter(cpu -> cpu.getId().equals(cpuId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Процессор не найден на складе!"));

        String requiredSocket = selectedCpu.getSocket();

        MotherboardDto[] allMbs = restTemplate.getForObject(COMPONENT_API + "motherboards", MotherboardDto[].class);
        return Arrays.stream(allMbs != null ? allMbs : new MotherboardDto[0])
                .filter(mb -> mb.getSocket().equalsIgnoreCase(requiredSocket))
                .collect(Collectors.toList());
    }

    @GetMapping("/rams")
    public List<RamDto> getCompatibleRam(@RequestParam Long mbId) {
        try {
            MotherboardDto[] allMbs = restTemplate.getForObject(COMPONENT_API + "motherboards", MotherboardDto[].class);
            if (allMbs == null) return List.of();

            MotherboardDto selectedMb = Arrays.stream(allMbs)
                    .filter(mb -> mb.getId().equals(mbId))
                    .findFirst()
                    .orElse(null);

            if (selectedMb == null) return List.of();

            String requiredRamType = selectedMb.getRamType();
            if (requiredRamType == null || requiredRamType.trim().isEmpty()) return List.of();

            RamDto[] compatibleRams = restTemplate.getForObject(
                    COMPONENT_API + "rams/type/" + requiredRamType.trim(),
                    RamDto[].class
            );

            return Arrays.asList(compatibleRams != null ? compatibleRams : new RamDto[0]);
        } catch (Exception e) {
            System.err.println("Ошибка фильтрации ОЗУ: " + e.getMessage());
            return List.of();
        }
    }

    // =========================================================================
    // 2. ОБЯЗАТЕЛЬНЫЕ МЕТОДЫ Rest API ПО ТЗ ЛАБОРАТОРНОЙ РАБОТЫ №4 (REST / ТЕСТЫ)
    // =========================================================================

    // 3.2 Проверка совместимости CPU и Motherboard (сокет)
    @PostMapping({"/check-socket", "/socket", "/check-cpu-motherboard"})
    public ResponseEntity<Boolean> checkSocketCompatibilityHttp(@RequestBody CompatibilityRequest request) {
        if (request == null) return ResponseEntity.badRequest().body(false);
        return checkSocketCompatibility(request.getCpu(), request.getMotherboard());
    }

    // Метод для ваших UNIT-тестов
    public ResponseEntity<Boolean> checkSocketCompatibility(CpuDto cpu, MotherboardDto motherboard) {
        if (cpu == null || motherboard == null || cpu.getSocket() == null || motherboard.getSocket() == null) {
            return ResponseEntity.ok(false);
        }
        return compatibilityRuleRepository.findByRuleTypeAndValueAAndValueB("SOCKET", cpu.getSocket(), motherboard.getSocket())
                .map(rule -> ResponseEntity.ok(rule.isCompatible()))
                .orElse(ResponseEntity.ok(cpu.getSocket().equalsIgnoreCase(motherboard.getSocket())));
    }

    // 3.3 Проверка совместимости RAM и Motherboard (тип памяти)
    @PostMapping({"/check-ram", "/ram"})
    public ResponseEntity<Boolean> checkRamCompatibilityHttp(@RequestBody CompatibilityRequest request) {
        if (request == null) return ResponseEntity.badRequest().body(false);
        return checkRamCompatibility(request.getRam(), request.getMotherboard());
    }

    // Метод для ваших UNIT-тестов
    public ResponseEntity<Boolean> checkRamCompatibility(RamDto ram, MotherboardDto motherboard) {
        if (ram == null || motherboard == null || ram.getType() == null || motherboard.getRamType() == null) {
            return ResponseEntity.ok(false);
        }

        return compatibilityRuleRepository.findByRuleTypeAndValueAAndValueB("DDR", ram.getType(), motherboard.getRamType())
                .map(rule -> ResponseEntity.ok(rule.isCompatible()))
                .orElse(ResponseEntity.ok(ram.getType().equalsIgnoreCase(motherboard.getRamType())));
    }

    // 3.4 REST эндпоинт POST /api/compatibility/url для проверки всей сборки
    @PostMapping({"/url", "/check-assembly"})
    public ResponseEntity<Boolean> checkFullAssemblyHttp(@RequestBody CompatibilityRequest request) {
        if (request == null) return ResponseEntity.badRequest().body(false);
        return checkFullAssembly(request.getCpu(), request.getMotherboard(), request.getRam());
    }

    // Метод для ваших UNIT-тестов
    public ResponseEntity<Boolean> checkFullAssembly(CpuDto cpu, MotherboardDto motherboard, RamDto ram) {
        boolean cpuMb = cpu != null && motherboard != null && cpu.getSocket() != null
                && cpu.getSocket().equalsIgnoreCase(motherboard.getSocket());

        boolean ramMb = ram != null && motherboard != null && ram.getType() != null
                && ram.getType().equalsIgnoreCase(motherboard.getRamType());

        return ResponseEntity.ok(cpuMb && ramMb);
    }

    /**
     * Контейнер входящих DTO запросов
     */
    public static class CompatibilityRequest {
        private CpuDto cpu;
        private MotherboardDto motherboard;
        private RamDto ram;

        public CpuDto getCpu() { return cpu; }
        public void setCpu(CpuDto cpu) { this.cpu = cpu; }
        public MotherboardDto getMotherboard() { return motherboard; }
        public void setMotherboard(MotherboardDto motherboard) { this.motherboard = motherboard; }
        public RamDto getRam() { return ram; }
        public void setRam(RamDto ram) { this.ram = ram; }
    }
}