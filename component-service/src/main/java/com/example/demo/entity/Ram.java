package com.example.demo.entity; // Проверь свой пакет!

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rams")
public class Ram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Название (например, Kingston FURY Beast)
    private String type;        // Тип памяти (DDR4, DDR5 - для проверки совместимости)
    private int capacity;       // Объем в ГБ (например, 16 или 32)
    private int speed;          // Частота в МГц (например, 3200)
    private double price;       // Цена
}