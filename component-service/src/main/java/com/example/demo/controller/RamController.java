package com.example.demo.controller;

import com.example.demo.entity.Ram;
import com.example.demo.repository.RamRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rams") // Базовый путь
public class RamController {

    private final RamRepository ramRepository;

    public RamController(RamRepository ramRepository) {
        this.ramRepository = ramRepository;
    }

    // Получить всю память (путь: /api/rams)
    @GetMapping
    public List<Ram> getAllRams() {
        return ramRepository.findAll();
    }

    // ВОТ ОН! ТОТ САМЫЙ МЕТОД, КОТОРОГО НЕ ХВАТАЛО (путь: /api/rams/type/DDR5)
    @GetMapping("/type/{type}")
    public List<Ram> getRamsByType(@PathVariable String type) {
        return ramRepository.findByType(type);
    }

    @GetMapping("/{id}")
    public Ram getRamById(@PathVariable Long id) {
        return ramRepository.findById(id).orElse(null);
    }
}