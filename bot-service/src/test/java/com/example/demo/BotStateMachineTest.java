package com.example.demo;

import com.example.demo.bot.PcBuilderBot;
import com.example.demo.repository.BotUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class BotStateMachineTest {

    @Mock
    private BotUserRepository botUserRepository;

    // Внедряем моки (заглушки) в нашего бота
    @InjectMocks
    private PcBuilderBot bot;

    @BeforeEach
    void setUp() {
        // Инициализируем моки перед каждым тестом
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testValidStateTransitions() {
        // Проверяем правильные переходы (Должно быть true)
        Assertions.assertTrue(bot.isCorrectStepForState("CHOOSING_CPU", "SELECT_CPU_1"));
        Assertions.assertTrue(bot.isCorrectStepForState("CHOOSING_MB", "SELECT_MB_2"));
        Assertions.assertTrue(bot.isCorrectStepForState("CHOOSING_GPU", "SELECT_GPU_3"));
    }

    @Test
    void testInvalidStateTransitions() {
        // Проверяем защиту от старых кнопок (Должно быть false)

        // Пытаемся выбрать процессор, когда бот ждет видеокарту
        Assertions.assertFalse(bot.isCorrectStepForState("CHOOSING_GPU", "SELECT_CPU_1"));

        // Пытаемся выбрать деталь, находясь в каталоге (IDLE)
        Assertions.assertFalse(bot.isCorrectStepForState("IDLE", "SELECT_RAM_1"));

        // Пытаемся выбрать материнку, когда ждем блок питания
        Assertions.assertFalse(bot.isCorrectStepForState("CHOOSING_PSU", "SELECT_MB_5"));
    }
}