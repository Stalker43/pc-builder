package com.example.demo.dto;
import lombok.Data;

@Data
public class GpuDto {
    private Long id;
    private String name;
    private int memory;
    private int powerRequired;
    private double price;
}