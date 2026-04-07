package com.example.demo.dto;
import lombok.Data;

@Data
public class RamDto {
    private Long id;
    private String name;
    private String type;     // DDR4, DDR5
    private int capacity;    // Объем в ГБ (например, 16)
    private double price;
}