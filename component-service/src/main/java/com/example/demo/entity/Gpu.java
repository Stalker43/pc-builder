package com.example.demo.entity; // Твой пакет

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "gpus")
public class Gpu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;          // Название (например, NVIDIA RTX 3060)
    private int memory;           // Объем видеопамяти в ГБ (например, 12)
    private int powerRequired;    // Требуемая мощность блока питания в Ваттах (например, 550)
    private double price;         // Цена
}