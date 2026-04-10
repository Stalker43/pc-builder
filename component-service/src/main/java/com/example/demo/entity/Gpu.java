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

    private String name;          // Название
    private int memory;           // Объем видеопамяти в ГБ
    private int powerRequired;    // Требуемая мощность блока питания в Ваттах
    private double price;         // Цена
}