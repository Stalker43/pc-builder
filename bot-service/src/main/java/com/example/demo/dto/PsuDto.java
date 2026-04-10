package com.example.demo.dto;
import lombok.Data;

@Data
public class PsuDto {
    private Long id;
    private String name;
    private int wattage;
    private double price;
}