package com.example.demo.dto;
import lombok.Data;

@Data
public class MotherboardDto {
    private Long id;
    private String name;
    private String socket;     // AM4, LGA1700
    private String formFactor; // ATX, Micro-ATX
    private double price;
}