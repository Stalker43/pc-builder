package com.example.demo.controller;

import com.example.demo.entity.Motherboard;
import com.example.demo.repository.MotherboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motherboards")
@RequiredArgsConstructor
public class MotherboardController {

    private final MotherboardRepository motherboardRepository;

    @GetMapping
    public List<Motherboard> getAllMotherboards() {
        return motherboardRepository.findAll();
    }

    @GetMapping("/{id}")
    public Motherboard getMotherboardById(@PathVariable Long id) {
        return motherboardRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Motherboard addMotherboard(@RequestBody Motherboard motherboard) {
        return motherboardRepository.save(motherboard);
    }
}