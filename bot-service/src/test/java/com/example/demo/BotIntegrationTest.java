package com.example.demo;

import com.example.demo.bot.PcBuilderBot;
import com.example.demo.entity.BotUser;
import com.example.demo.repository.BotUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
class BotIntegrationTest {

    @MockitoBean
    private TelegramBotsApi telegramBotsApi;


    @MockitoBean
    private PcBuilderBot pcBuilderBot;

    @Autowired
    private BotUserRepository userRepository;

    @Test
    void testFullAssemblyCycleInDatabase() {
        // Имитация команды /start
        BotUser testUser = new BotUser();
        testUser.setTelegramId(123456789L);
        testUser.setFirstName("Test Student");
        testUser.setState("IDLE");
        userRepository.save(testUser);

        // Убеждаемся, что пользователь сохранился
        Optional<BotUser> savedUserOpt = userRepository.findByTelegramId(123456789L);
        Assertions.assertTrue(savedUserOpt.isPresent());
        BotUser savedUser = savedUserOpt.get();

        // Имитация нажатия "Собрать ПК"
        savedUser.setState("CHOOSING_CPU");
        userRepository.save(savedUser);
        Assertions.assertEquals("CHOOSING_CPU", userRepository.findByTelegramId(123456789L).get().getState());

        // Имитация выбора деталей по цепочке
        savedUser.setSelectedCpuId(1L);
        savedUser.setState("CHOOSING_MB");
        userRepository.save(savedUser);

        savedUser.setSelectedMbId(2L);
        savedUser.setState("CHOOSING_RAM");
        userRepository.save(savedUser);

        savedUser.setSelectedRamId(3L);
        savedUser.setState("CHOOSING_GPU");
        userRepository.save(savedUser);

        savedUser.setSelectedGpuId(4L);
        savedUser.setState("CHOOSING_PSU");
        userRepository.save(savedUser);

        savedUser.setSelectedPsuId(5L);
        savedUser.setState("CHOOSING_CASE");
        userRepository.save(savedUser);


        savedUser.setSelectedCaseId(6L);
        savedUser.setState("IDLE");
        userRepository.save(savedUser);


        BotUser finalUser = userRepository.findByTelegramId(123456789L).get();
        Assertions.assertEquals("IDLE", finalUser.getState());
        Assertions.assertEquals(1L, finalUser.getSelectedCpuId());
        Assertions.assertEquals(6L, finalUser.getSelectedCaseId());


        userRepository.delete(finalUser);
    }
}