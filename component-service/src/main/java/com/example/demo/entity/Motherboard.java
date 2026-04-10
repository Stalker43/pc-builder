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

    private String name;        // Название
    private String socket;      // Сокет
    private String formFactor;  // Форм-фактор
    private String ramType;     // Тип поддерживаемой памяти
    private double price;       // Цена
}