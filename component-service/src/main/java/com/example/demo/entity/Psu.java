package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "psus")
public class Psu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // Название
    private int wattage;           // Мощность в Ваттах
    private String certification;  // Сертификат
    private double price;          // Цена
}