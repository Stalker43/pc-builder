package com.example.demo;


import com.example.demo.controller.CompatibilityController;
import com.example.demo.dto.CpuDto;
import com.example.demo.dto.MotherboardDto;
import com.example.demo.dto.RamDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class CompatibilityControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CompatibilityController compatibilityController;

    private final String COMPONENT_API = "http://component-service:8081/api/";

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(compatibilityController, "restTemplate", restTemplate);
    }

    @Test
    void testGetCompatibleMotherboards_Success() {
        // 1. Готовим данные
        CpuDto cpu = new CpuDto();
        cpu.setId(1L);
        cpu.setSocket("AM5");

        MotherboardDto mb = new MotherboardDto();
        mb.setId(10L);
        mb.setName("MSI B650");
        mb.setSocket("AM5");


        Mockito.when(restTemplate.getForObject(COMPONENT_API + "cpus", CpuDto[].class))
                .thenReturn(new CpuDto[]{cpu});
        Mockito.when(restTemplate.getForObject(COMPONENT_API + "motherboards", MotherboardDto[].class))
                .thenReturn(new MotherboardDto[]{mb});


        List<MotherboardDto> result = compatibilityController.getCompatibleMotherboards(1L);


        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals("AM5", result.get(0).getSocket());
        Assertions.assertEquals("MSI B650", result.get(0).getName());
    }

    @Test
    void testGetCompatibleRam_Success() {
        // 1. Готовим данные
        MotherboardDto mb = new MotherboardDto();
        mb.setId(10L);
        mb.setRamType("DDR5");

        RamDto ram = new RamDto();
        ram.setName("Kingston Fury");
        ram.setType("DDR5");

        Mockito.when(restTemplate.getForObject(COMPONENT_API + "motherboards", MotherboardDto[].class))
                .thenReturn(new MotherboardDto[]{mb});
        Mockito.when(restTemplate.getForObject(COMPONENT_API + "rams/type/DDR5", RamDto[].class))
                .thenReturn(new RamDto[]{ram});

        // 2. Вызов метода
        List<RamDto> result = compatibilityController.getCompatibleRam(10L);


        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals("DDR5", result.get(0).getType());
    }
}