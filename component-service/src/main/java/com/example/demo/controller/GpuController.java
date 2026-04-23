package com.example.demo.controller;

import com.example.demo.entity.Gpu;
import com.example.demo.repository.GpuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gpus")
@RequiredArgsConstructor
public class GpuController {

    private final GpuRepository gpuRepository;

    // Для кнопок в боте
    @GetMapping
    public List<Gpu> getAllGpus() {
        return gpuRepository.findAll();
    }

    // Для калькулятора мощности
    @GetMapping("/{id}")
    public Gpu getGpuById(@PathVariable Long id) {
        return gpuRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Gpu addGpu(@RequestBody Gpu gpu) {
        return gpuRepository.save(gpu);
    }
}