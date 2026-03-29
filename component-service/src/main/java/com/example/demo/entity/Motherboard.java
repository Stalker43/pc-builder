package com.example.demo.entity; // Убедись, что пакет твой!

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "motherboards")
public class Motherboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Название (например, MSI MAG B660M)
    private String socket;      // Сокет (например, LGA1700 - для совместимости с CPU)
    private String formFactor;  // Форм-фактор (ATX, Micro-ATX)
    private String ramType;     // Тип поддерживаемой памяти (DDR4, DDR5)
    private double price;       // Цена
}