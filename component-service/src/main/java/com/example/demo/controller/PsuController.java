package com.example.demo.controller;

import com.example.demo.entity.Psu;
import com.example.demo.repository.PsuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/psus")
@RequiredArgsConstructor
public class PsuController {

    private final PsuRepository psuRepository;

    @GetMapping
    public List<Psu> getAllPsus() {
        return psuRepository.findAll();
    }

    @PostMapping
    public Psu addPsu(@RequestBody Psu psu) {
        return psuRepository.save(psu);
    }
}