package com.example.demo.controller;

import com.example.demo.dto.AssemblyRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assembly")
public class AssemblyController {

    @PostMapping("/save")
    public ResponseEntity<String> saveAssembly(@RequestBody AssemblyRequest request) {
        System.out.println("✅ Получена новая сборка от пользователя: " + request.getTelegramId());
        System.out.println("CPU: " + request.getCpuId() + ", GPU: " + request.getGpuId());

        // Здесь в будущем будет логика сохранения в PostgreSQL (assemblyRepository.save(...))

        return ResponseEntity.ok("Сборка успешно сохранена в Assembly Service!");
    }
}