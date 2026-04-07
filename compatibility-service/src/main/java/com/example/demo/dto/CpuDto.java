package com.example.demo.dto;

import lombok.Data;

@Data
public class CpuDto {
    private Long id;
    private String name;
    private String socket;
    private int cores;
    private int tdp;
    private double price;
}