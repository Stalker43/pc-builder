package com.example.demo.controller;

import com.example.demo.entity.PcCase;
import com.example.demo.repository.PcCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pccases")
@RequiredArgsConstructor
public class PcCaseController {

    private final PcCaseRepository pcCaseRepository;

    @GetMapping
    public List<PcCase> getAllCases() {
        return pcCaseRepository.findAll();
    }

    @PostMapping
    public PcCase addCase(@RequestBody PcCase pcCase) {
        return pcCaseRepository.save(pcCase);
    }
}