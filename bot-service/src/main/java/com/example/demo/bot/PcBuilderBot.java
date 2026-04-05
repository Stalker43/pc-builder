package com.example.demo.bot;

import com.example.demo.entity.BotUser;
import com.example.demo.repository.BotUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PcBuilderBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.name}")
    private String botName;

    private final BotUserRepository botUserRepository;

    public PcBuilderBot(@Value("${telegram.bot.token}") String botToken, BotUserRepository botUserRepository) {
        super(botToken);
        this.botUserRepository = botUserRepository;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long telegramId = update.getMessage().getFrom().getId();
            String firstName = update.getMessage().getChat().getFirstName();

            if (messageText.equals("/start")) {
                handleStartCommand(chatId, telegramId, firstName);
            } else if (messageText.equals("Мой профиль 👤")) {
                showUserProfile(chatId, telegramId);
            } else if (messageText.equals("Собрать ПК 🛠")) {
                sendMessage(chatId, "Скоро здесь будет мастер подбора комплектующих! ⚙️");
            } else if (messageText.equals("Каталог 📋")) {
                sendCatalog(chatId);
            } else {
                sendMessage(chatId, "Я пока учусь понимать кнопки. Нажми что-нибудь из меню ниже! ↓");
            }
        }
    }

    private void handleStartCommand(long chatId, long telegramId, String firstName) {
        Optional<BotUser> user = botUserRepository.findByTelegramId(telegramId);
        String welcomeText;

        if (user.isEmpty()) {
            BotUser newUser = new BotUser();
            newUser.setTelegramId(telegramId);
            newUser.setChatId(chatId);
            newUser.setFirstName(firstName);
            botUserRepository.save(newUser);
            welcomeText = "Привет, " + firstName + "! 💻 Я помогу тебе собрать идеальный ПК.";
        } else {
            welcomeText = "С возвращением, " + firstName + "! 🚀 Готов продолжить сборку?";
        }

        // Отправляем сообщение вместе с меню (кнопками)
        sendMenuMessage(chatId, welcomeText);
    }

    private void showUserProfile(long chatId, long telegramId) {
        botUserRepository.findByTelegramId(telegramId).ifPresentOrElse(
                u -> sendMessage(chatId, "Твой профиль:\n👤 Имя: " + u.getFirstName() + "\n🆔 ID: " + u.getTelegramId()),
                () -> sendMessage(chatId, "Профиль не найден 🤷‍♂️")
        );
    }

    private void sendMenuMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        // Создаем клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первый ряд кнопок
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Собрать ПК 🛠");

        // Второй ряд кнопок
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Каталог 📋");
        row2.add("Мой профиль 👤");

        keyboard.add(row1);
        keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true); // Делает кнопки компактными
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("Ошибка отправки сообщения: " + e.getMessage());
        }
    }

    private void sendCatalog(long chatId) {
        sendMessage(chatId, "⏳ Открываю каталог процессоров...");

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String url = "http://component-service:8081/api/cpus";


            com.example.demo.dto.CpuDto[] cpus = restTemplate.getForObject(url, com.example.demo.dto.CpuDto[].class);

            if (cpus == null || cpus.length == 0) {
                sendMessage(chatId, "📦 Склад работает, но он пока пуст. В базе данных еще нет процессоров!");
            } else {

                StringBuilder catalogMessage = new StringBuilder("📦 Доступные процессоры:\n\n");

                for (com.example.demo.dto.CpuDto cpu : cpus) {
                    catalogMessage.append("🔹 ").append(cpu.getName()).append("\n")
                            .append("   ⚙️ Сокет: ").append(cpu.getSocket()).append("\n")
                            .append("   🧠 Ядра: ").append(cpu.getCores()).append("\n")
                            .append("   🔥 TDP: ").append(cpu.getTdp()).append(" Вт\n")
                            .append("   💰 Цена: $").append(cpu.getPrice()).append("\n\n");
                }

                sendMessage(chatId, catalogMessage.toString());
            }

        } catch (Exception e) {
            sendMessage(chatId, "❌ Склад сейчас недоступен. Ошибка: " + e.getMessage());
        }
    }
}