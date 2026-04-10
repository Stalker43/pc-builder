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

    private Long telegramId;
    private Long chatId;       // ID чата для отправки сообщений
    private String firstName;  // Имя, чтобы бот знал, как обращаться

    // Запоминает, на каком шаге сборки находится пользователь

    private String state;
    private Long selectedCpuId;
    private Long selectedMbId;
    private Long selectedGpuId;
    private Long selectedRamId;
    private Long selectedPsuId;
    private Long selectedCaseId;
}