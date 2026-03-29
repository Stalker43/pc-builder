package com.example.demo.bot;

import com.example.demo.entity.BotUser;
import com.example.demo.repository.BotUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Component
public class PcBuilderBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.name}")
    private String botName;

    // Добавляем репозиторий в бота
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
            long telegramId = update.getMessage().getFrom().getId(); // Получаем уникальный ID человека
            String firstName = update.getMessage().getChat().getFirstName();

            if (messageText.equals("/start")) {
                // Ищем пользователя в базе
                Optional<BotUser> existingUser = botUserRepository.findByTelegramId(telegramId);

                if (existingUser.isEmpty()) {
                    // Если человека нет в базе — создаем и сохраняем
                    BotUser newUser = new BotUser();
                    newUser.setTelegramId(telegramId);
                    newUser.setChatId(chatId);
                    newUser.setFirstName(firstName);
                    botUserRepository.save(newUser);

                    sendMessage(chatId, "Привет, " + firstName + "! 💻 Добро пожаловать! Я успешно сохранил тебя в базу данных.");
                } else {
                    // Если человек уже есть в базе
                    sendMessage(chatId, "С возвращением, " + firstName + "! 💻 Рад видеть тебя снова.");
                }
            } else {
                sendMessage(chatId, "Я пока понимаю только команду /start ⚙️");
            }
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
}