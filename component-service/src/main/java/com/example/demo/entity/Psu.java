package com.example.demo.entity; // Проверь свой пакет!

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "psus")
public class Psu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // Название (например, Deepcool PK650D)
    private int wattage;           // Мощность в Ваттах (например, 650)
    private String certification;  // Сертификат (например, 80+ Bronze)
    private double price;          // Цена
}