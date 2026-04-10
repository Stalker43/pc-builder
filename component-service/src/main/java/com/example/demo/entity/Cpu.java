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

    private String name;        // Название
    private String socket;      // Сокет
    private int cores;          // Количество ядер
    private int tdp;            // Тепловыделение
    private double price;       // Цена
}