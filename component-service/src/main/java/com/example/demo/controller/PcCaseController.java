package com.example.demo.controller;

import com.example.demo.entity.PcCase;
import com.example.demo.repository.PcCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class PcCaseController {

    private final PcCaseRepository pcCaseRepository;

    @GetMapping
    public List<PcCase> getAllCases() {
        return pcCaseRepository.findAll();
    }

    @GetMapping("/{id}")
    public PcCase getCaseById(@PathVariable Long id) {
        return pcCaseRepository.findById(id).orElse(null); // замени caseRepository на название твоего репозитория
    }

    @PostMapping
    public PcCase addCase(@RequestBody PcCase pcCase) {
        return pcCaseRepository.save(pcCase);
    }
}