package com.example.demo.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class PcBuilderBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.name}")
    private String botName;

    // Спринг сам достанет токен из application.properties и передаст сюда
    public PcBuilderBot(@Value("${telegram.bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    // Этот метод срабатывает каждый раз, когда боту приходит сообщение
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getChat().getFirstName(); // Имя пользователя в Телеге

            // Если человек написал /start
            if (messageText.equals("/start")) {
                sendMessage(chatId, "Привет, " + firstName + "! 💻 Я бот для сборки идеального ПК. Скоро мы добавим базу пользователей, а пока я просто рад знакомству!");
            } else {
                // Если человек написал что-то другое
                sendMessage(chatId, "Я пока понимаю только команду /start ⚙️");
            }
        }
    }

    // Вспомогательный метод, чтобы было проще отправлять текст
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message); // Отправляем сообщение в Телеграм
        } catch (TelegramApiException e) {
            System.out.println("Ошибка отправки сообщения: " + e.getMessage());
        }
    }
}