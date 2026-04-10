package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pc_cases")
public class PcCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // Название
    private String formFactor;     // Максимальный размер материнки
    private String color;          // Цвет
    private double price;          // Цена
}