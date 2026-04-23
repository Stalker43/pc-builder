package com.example.demo.controller;

import com.example.demo.entity.Cpu;
import com.example.demo.repository.CpuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cpus")
@RequiredArgsConstructor
public class CpuController {

    private final CpuRepository cpuRepository;

    // ТВОЙ СТАРЫЙ МЕТОД (Оставляем! Он нужен для каталога)
    @GetMapping
    public List<Cpu> getAllCpus() {
        return cpuRepository.findAll();
    }

    // НОВЫЙ МЕТОД (Добавляем! Он нужен для power-service)
    @GetMapping("/{id}")
    public Cpu getCpuById(@PathVariable Long id) {
        return cpuRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Cpu addCpu(@RequestBody Cpu cpu) {
        return cpuRepository.save(cpu);
    }
}