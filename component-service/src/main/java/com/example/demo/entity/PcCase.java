package com.example.demo.entity; // Твой пакет

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pc_cases")
public class PcCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           // Название (например, Zalman N5 MF)
    private String formFactor;     // Максимальный размер материнки (например, ATX)
    private String color;          // Цвет (например, Black)
    private double price;          // Цена
}