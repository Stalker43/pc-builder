package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cpus")
public class Cpu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Название (например, Intel Core i5-12400F)
    private String socket;      // Сокет (например, LGA1700)
    private int cores;          // Количество ядер
    private int tdp;            // Тепловыделение (Ватт)
    private double price;       // Цена
}