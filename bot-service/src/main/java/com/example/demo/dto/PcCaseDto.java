package com.example.demo.dto;
import lombok.Data;

@Data
public class PcCaseDto {
    private Long id;
    private String name;
    private String formFactor;
    private double price;
}