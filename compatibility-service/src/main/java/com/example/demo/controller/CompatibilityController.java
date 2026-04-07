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
    // Ссылка на наш склад
    private final String COMPONENT_API = "http://component-service:8081/api/";

    @GetMapping("/motherboards")
    public List<MotherboardDto> getCompatibleMotherboards(@RequestParam Long cpuId) {

        // 1. Идем на склад, берем все процессоры и находим тот, который выбрал юзер
        CpuDto[] allCpus = restTemplate.getForObject(COMPONENT_API + "cpus", CpuDto[].class);
        CpuDto selectedCpu = Arrays.stream(allCpus)
                .filter(cpu -> cpu.getId().equals(cpuId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Процессор не найден!"));

        // 2. Снова идем на склад и берем вообще ВСЕ материнские платы
        MotherboardDto[] allMbs = restTemplate.getForObject(COMPONENT_API + "motherboards", MotherboardDto[].class);

        // 3. МАГИЯ ФИЛЬТРАЦИИ: оставляем только те платы, у которых сокет совпадает с сокетом процессора!
        return Arrays.stream(allMbs)
                .filter(mb -> mb.getSocket().equals(selectedCpu.getSocket()))
                .collect(Collectors.toList());
    }

    @GetMapping("/rams")
    public List<RamDto> getCompatibleRam(@RequestParam Long mbId) {

        // 1. Находим материнскую плату на складе
        MotherboardDto[] allMbs = restTemplate.getForObject(COMPONENT_API + "motherboards", MotherboardDto[].class);
        MotherboardDto selectedMb = Arrays.stream(allMbs)
                .filter(mb -> mb.getId().equals(mbId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Материнская плата не найдена!"));

        // 2. БЕРЕМ ТИП ПАМЯТИ ПРЯМО ИЗ БАЗЫ! Никаких хаков!
        String requiredRamType = selectedMb.getRamType();

        // 3. Фильтруем оперативку
        RamDto[] allRams = restTemplate.getForObject(COMPONENT_API + "rams", RamDto[].class);
        return Arrays.stream(allRams)
                .filter(ram -> ram.getType().equals(requiredRamType))
                .collect(Collectors.toList());
    }
}