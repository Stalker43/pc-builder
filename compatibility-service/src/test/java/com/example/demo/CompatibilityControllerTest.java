package com.example.demo;

import com.example.demo.controller.CompatibilityController;
import com.example.demo.dto.CpuDto;
import com.example.demo.dto.MotherboardDto;
import com.example.demo.dto.RamDto;
import com.example.demo.repository.CompatibilityRuleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class CompatibilityControllerTest {

    @Autowired
    private CompatibilityController compatibilityController;

    @MockitoBean
    private CompatibilityRuleRepository compatibilityRuleRepository;

    @Test
    void testSocketCompatibility() {
        CpuDto cpu = new CpuDto();
        cpu.setSocket("LGA1700");

        MotherboardDto motherboard = new MotherboardDto();
        motherboard.setSocket("LGA1700");

        ResponseEntity<Boolean> response = compatibilityController.checkSocketCompatibility(cpu, motherboard);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody());
    }

    @Test
    void testRamCompatibility() {
        RamDto ram = new RamDto();
        ram.setType("DDR4"); // Используем getType() согласно вашему RamDto

        MotherboardDto motherboard = new MotherboardDto();
        motherboard.setRamType("DDR4"); // Используем getRamType() согласно вашему MotherboardDto

        ResponseEntity<Boolean> response = compatibilityController.checkRamCompatibility(ram, motherboard);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody());
    }

    @Test
    void testFullAssemblyCheck() {
        CpuDto cpu = new CpuDto();
        cpu.setSocket("LGA1700");

        MotherboardDto motherboard = new MotherboardDto();
        motherboard.setSocket("LGA1700");
        motherboard.setRamType("DDR4");

        RamDto ram = new RamDto();
        ram.setType("DDR4");

        ResponseEntity<Boolean> response = compatibilityController.checkFullAssembly(cpu, motherboard, ram);
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getBody());
    }
}