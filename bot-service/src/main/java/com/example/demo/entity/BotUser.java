package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "bot_users")
public class BotUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramId;   // Уникальный ID пользователя в Telegram
    private Long chatId;       // ID чата для отправки сообщений
    private String firstName;  // Имя, чтобы бот знал, как обращаться

    // Запоминает, на каком шаге сборки находится пользователь
    // Например: "IDLE" (ничего не делает), "CHOOSING_CPU", "CHOOSING_MB" и т.д.
    private String state;
}