package com.example.demo.controller;

import com.example.demo.entity.Ram;
import com.example.demo.repository.RamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rams")
@RequiredArgsConstructor
public class RamController {

    private final RamRepository ramRepository;

    @GetMapping
    public List<Ram> getAllRams() {
        return ramRepository.findAll();
    }

    @PostMapping
    public Ram addRam(@RequestBody Ram ram) {
        return ramRepository.save(ram);
    }
}