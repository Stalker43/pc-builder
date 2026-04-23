package com.example.demo.dto;

import lombok.Data;

@Data
public class PowerResponseDto {
    private Integer requiredWattage;
    private String recommendation;
}