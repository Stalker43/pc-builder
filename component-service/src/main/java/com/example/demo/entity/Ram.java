package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rams")
public class Ram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Название
    private String type;        // Тип памяти (для проверки совместимости)
    private int capacity;       // Объем в ГБ
    private int speed;          // Частота в МГц
    private double price;       // Цена
}