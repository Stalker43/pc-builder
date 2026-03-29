package com.example.demo.controller;

import com.example.demo.entity.Cpu;
import com.example.demo.repository.CpuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.Data;

@RestController
@RequestMapping("/api/cpus")
@RequiredArgsConstructor
public class CpuController {

    private final CpuRepository cpuRepository;

    @GetMapping
    public List<Cpu> getAllCpus() {
        return cpuRepository.findAll();
    }

    @PostMapping
    public Cpu addCpu(@RequestBody Cpu cpu) {
        return cpuRepository.save(cpu);
    }
}