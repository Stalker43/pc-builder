package com.example.demo.dto;
import lombok.Data;

@Data
public class MotherboardDto {
    private Long id;
    private String name;
    private String socket;
    private String formFactor;
    private String ramType;
    private double price;
}