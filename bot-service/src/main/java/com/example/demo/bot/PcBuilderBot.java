package com.example.demo.bot;

import com.example.demo.dto.*;
import com.example.demo.entity.BotUser;
import com.example.demo.repository.BotUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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
    private final RestTemplate restTemplate = new RestTemplate();

    // Ссылки на наши микросервисы
    private final String API_URL = "http://component-service:8081/api/";
    private final String COMPATIBILITY_API_URL = "http://compatibility-service:8082/api/compatibility/";

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
        // 1. Обработка текстовых сообщений (нижние кнопки)
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
                startPcAssembly(chatId, telegramId);
            } else if (messageText.equals("Каталог 📋")) {
                showCatalogMenu(chatId);
            } else {
                sendMessage(chatId, "Я пока учусь понимать слова. Нажми что-нибудь из меню ниже! ↓");
            }
        }
        // 2. Обработка Inline-кнопок (под сообщениями)
        else if (update.hasCallbackQuery()) {
            String callData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long telegramId = update.getCallbackQuery().getFrom().getId();

            // Точное совпадение (меню каталога - только просмотр)
            switch (callData) {
                case "CATALOG_CPU": sendCpuCatalog(chatId); break;
                case "CATALOG_GPU": sendGpuCatalog(chatId); break;
                case "CATALOG_MB":  sendMbCatalog(chatId); break;
                case "CATALOG_RAM": sendRamCatalog(chatId); break;
                case "CATALOG_PSU": sendPsuCatalog(chatId); break;
                case "CATALOG_CASE": sendCaseCatalog(chatId); break;
            }

            // Динамические кнопки (выбор деталей в режиме сборки)
            if (callData.startsWith("SELECT_CPU_")) {
                long cpuId = Long.parseLong(callData.replace("SELECT_CPU_", ""));
                handleCpuSelection(chatId, telegramId, cpuId);
            } else if (callData.startsWith("SELECT_MB_")) {
                long mbId = Long.parseLong(callData.replace("SELECT_MB_", ""));
                handleMbSelection(chatId, telegramId, mbId);
            }
        }
    }

    // --- БАЗОВЫЕ МЕТОДЫ (МЕНЮ И ПРОФИЛЬ) ---

    private void handleStartCommand(long chatId, long telegramId, String firstName) {
        Optional<BotUser> user = botUserRepository.findByTelegramId(telegramId);
        String welcomeText;
        if (user.isEmpty()) {
            BotUser newUser = new BotUser();
            newUser.setTelegramId(telegramId);
            newUser.setChatId(chatId);
            newUser.setFirstName(firstName);
            newUser.setState("IDLE");
            botUserRepository.save(newUser);
            welcomeText = "Привет, " + firstName + "! 💻 Я помогу тебе собрать идеальный ПК.";
        } else {
            welcomeText = "С возвращением, " + firstName + "! 🚀 Готов продолжить сборку?";
            BotUser existingUser = user.get();
            existingUser.setState("IDLE");
            botUserRepository.save(existingUser);
        }
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

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow(); row1.add("Собрать ПК 🛠");
        KeyboardRow row2 = new KeyboardRow(); row2.add("Каталог 📋"); row2.add("Мой профиль 👤");
        keyboard.add(row1); keyboard.add(row2);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);
        executeMessage(message);
    }

    private void showCatalogMenu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выбери категорию комплектующих: 🔎");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createBtn("Процессоры 🧠", "CATALOG_CPU"));
        row1.add(createBtn("Материнки 🎛", "CATALOG_MB"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createBtn("Видеокарты 🎮", "CATALOG_GPU"));
        row2.add(createBtn("Оперативка ⚡", "CATALOG_RAM"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createBtn("Блоки питания 🔋", "CATALOG_PSU"));
        row3.add(createBtn("Корпуса 📦", "CATALOG_CASE"));

        rowsInline.add(row1); rowsInline.add(row2); rowsInline.add(row3);
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        executeMessage(message);
    }

    private InlineKeyboardButton createBtn(String text, String callback) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(callback);
        return btn;
    }

    // --- МЕТОДЫ СБОРКИ ПК (МАСТЕР ПОДБОРА) ---

    private void startPcAssembly(long chatId, long telegramId) {
        botUserRepository.findByTelegramId(telegramId).ifPresent(user -> {
            user.setState("CHOOSING_CPU");
            botUserRepository.save(user);

            String adviceText = "🛠 Отлично, начинаем сборку!\n\n" +
                    "💡 *СОВЕТ ОТ БОТА:*\n" +
                    "Сердце компьютера — процессор. От него зависит, насколько быстро будут компилироваться программы и работать тяжелые приложения.\n" +
                    "• Для базовых задач и легких игр хватит 4-6 ядер.\n" +
                    "• Для программирования, работы с виртуалками и современных игр бери 6-8 ядер.\n" +
                    "• Для экстремальных нагрузок смотри на Core i9.\n\n" +
                    "Подожди секунду, загружаю список с нашего склада...";

            sendMessage(chatId, adviceText);
            sendCpuSelectionStep(chatId);
        });
    }

    private void sendCpuSelectionStep(long chatId) {
        try {
            CpuDto[] items = restTemplate.getForObject(API_URL + "cpus", CpuDto[].class);
            if (items == null || items.length == 0) {
                sendMessage(chatId, "📦 Процессоров пока нет на складе. Сборка невозможна.");
                return;
            }

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("📦 Выбери процессор для своей сборки:");

            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            for (CpuDto item : items) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(item.getName() + " | $" + item.getPrice());
                btn.setCallbackData("SELECT_CPU_" + item.getId());
                row.add(btn);
                rowsInline.add(row);
            }

            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);
            executeMessage(message);

        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка связи со складом: " + e.getMessage());
        }
    }

    private void handleCpuSelection(long chatId, long telegramId, long cpuId) {
        botUserRepository.findByTelegramId(telegramId).ifPresent(user -> {
            user.setSelectedCpuId(cpuId);
            user.setState("CHOOSING_MB");
            botUserRepository.save(user);

            sendMessage(chatId, "✅ Отличный выбор! Процессор сохранен.\n\n" +
                    "💡 *СЛЕДУЮЩИЙ ШАГ: Материнская плата*\n" +
                    "Служба совместимости уже отфильтровала каталог. Вот платы с подходящим сокетом:");

            sendMbSelectionStep(chatId, cpuId);
        });
    }

    private void sendMbSelectionStep(long chatId, long cpuId) {
        try {
            String url = COMPATIBILITY_API_URL + "motherboards?cpuId=" + cpuId;
            MotherboardDto[] items = restTemplate.getForObject(url, MotherboardDto[].class);

            if (items == null || items.length == 0) {
                sendMessage(chatId, "❌ К сожалению, на складе сейчас нет подходящих материнских плат для этого процессора.");
                return;
            }

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("🎛 Совместимые материнские платы:");

            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            for (MotherboardDto item : items) {
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(item.getName() + " | " + item.getSocket() + " | $" + item.getPrice());
                btn.setCallbackData("SELECT_MB_" + item.getId());
                row.add(btn);
                rowsInline.add(row);
            }

            markupInline.setKeyboard(rowsInline);
            message.setReplyMarkup(markupInline);
            executeMessage(message);

        } catch (Exception e) {
            sendMessage(chatId, "❌ Ошибка связи со Службой Совместимости: " + e.getMessage());
        }
    }

    private void handleMbSelection(long chatId, long telegramId, long mbId) {
        botUserRepository.findByTelegramId(telegramId).ifPresent(user -> {
            user.setSelectedMbId(mbId);
            user.setState("CHOOSING_RAM");
            botUserRepository.save(user);

            sendMessage(chatId, "✅ Материнская плата успешно добавлена в сборку!\n\n" +
                    "💡 *СЛЕДУЮЩИЙ ШАГ: Оперативная память*\n" +
                    "Скоро здесь будет подбор памяти по стандарту (DDR4/DDR5) 🚧");
        });
    }


    // --- МЕТОДЫ КАТАЛОГА (ТОЛЬКО ТЕКСТ, БЕЗ КНОПОК ВЫБОРА) ---

    private void sendCpuCatalog(long chatId) {
        try {
            CpuDto[] items = restTemplate.getForObject(API_URL + "cpus", CpuDto[].class);
            if (items == null || items.length == 0) { sendMessage(chatId, "📦 Процессоров пока нет на складе."); return; }

            StringBuilder sb = new StringBuilder("📦 Доступные процессоры:\n\n");
            for (CpuDto item : items) {
                sb.append("🔹 ").append(item.getName()).append("\n")
                        .append("   ⚙️ Сокет: ").append(item.getSocket()).append(" | 🧠 Ядра: ").append(item.getCores()).append("\n")
                        .append("   💰 Цена: $").append(item.getPrice()).append("\n\n");
            }
            sendMessage(chatId, sb.toString());
        } catch (Exception e) { sendMessage(chatId, "❌ Ошибка связи со складом: " + e.getMessage()); }
    }

    private void sendGpuCatalog(long chatId) {
        try {
            GpuDto[] items = restTemplate.getForObject(API_URL + "gpus", GpuDto[].class);
            if (items == null || items.length == 0) { sendMessage(chatId, "📦 Видеокарт пока нет на складе."); return; }

            StringBuilder sb = new StringBuilder("📦 Доступные видеокарты:\n\n");
            for (GpuDto item : items) {
                sb.append("🔹 ").append(item.getName()).append("\n")
                        .append("   🎮 Память: ").append(item.getMemory()).append(" GB | ⚡ Требует БП: ").append(item.getPowerRequired()).append(" Вт\n")
                        .append("   💰 Цена: $").append(item.getPrice()).append("\n\n");
            }
            sendMessage(chatId, sb.toString());
        } catch (Exception e) { sendMessage(chatId, "❌ Ошибка связи со складом: " + e.getMessage()); }
    }

    private void sendMbCatalog(long chatId) {
        try {
            MotherboardDto[] items = restTemplate.getForObject(API_URL + "motherboards", MotherboardDto[].class);
            if (items == null || items.length == 0) { sendMessage(chatId, "📦 Материнских плат пока нет на складе."); return; }

            StringBuilder sb = new StringBuilder("📦 Доступные материнские платы:\n\n");
            for (MotherboardDto item : items) {
                sb.append("🔹 ").append(item.getName()).append("\n")
                        .append("   ⚙️ Сокет: ").append(item.getSocket()).append(" | 📏 Форм-фактор: ").append(item.getFormFactor()).append("\n")
                        .append("   💰 Цена: $").append(item.getPrice()).append("\n\n");
            }
            sendMessage(chatId, sb.toString());
        } catch (Exception e) { sendMessage(chatId, "❌ Ошибка связи со складом: " + e.getMessage()); }
    }

    private void sendRamCatalog(long chatId) {
        try {
            RamDto[] items = restTemplate.getForObject(API_URL + "rams", RamDto[].class);
            if (items == null || items.length == 0) { sendMessage(chatId, "📦 Оперативной памяти пока нет на складе."); return; }

            StringBuilder sb = new StringBuilder("📦 Доступная оперативная память:\n\n");
            for (RamDto item : items) {
                sb.append("🔹 ").append(item.getName()).append("\n")
                        .append("   ⚡ Тип: ").append(item.getType()).append(" | 💾 Объем: ").append(item.getCapacity()).append(" GB\n")
                        .append("   💰 Цена: $").append(item.getPrice()).append("\n\n");
            }
            sendMessage(chatId, sb.toString());
        } catch (Exception e) { sendMessage(chatId, "❌ Ошибка связи со складом: " + e.getMessage()); }
    }

    private void sendPsuCatalog(long chatId) {
        try {
            PsuDto[] items = restTemplate.getForObject(API_URL + "psus", PsuDto[].class);
            if (items == null || items.length == 0) { sendMessage(chatId, "📦 Блоков питания пока нет на складе."); return; }

            StringBuilder sb = new StringBuilder("📦 Доступные блоки питания:\n\n");
            for (PsuDto item : items) {
                sb.append("🔹 ").append(item.getName()).append("\n")
                        .append("   🔋 Мощность: ").append(item.getWattage()).append(" Вт\n")
                        .append("   💰 Цена: $").append(item.getPrice()).append("\n\n");
            }
            sendMessage(chatId, sb.toString());
        } catch (Exception e) { sendMessage(chatId, "❌ Ошибка связи со складом: " + e.getMessage()); }
    }

    private void sendCaseCatalog(long chatId) {
        try {
            PcCaseDto[] items = restTemplate.getForObject(API_URL + "pccases", PcCaseDto[].class);
            if (items == null || items.length == 0) { sendMessage(chatId, "📦 Корпусов пока нет на складе."); return; }

            StringBuilder sb = new StringBuilder("📦 Доступные корпуса:\n\n");
            for (PcCaseDto item : items) {
                sb.append("🔹 ").append(item.getName()).append("\n")
                        .append("   📏 Форм-фактор: ").append(item.getFormFactor()).append("\n")
                        .append("   💰 Цена: $").append(item.getPrice()).append("\n\n");
            }
            sendMessage(chatId, sb.toString());
        } catch (Exception e) { sendMessage(chatId, "❌ Ошибка связи со складом: " + e.getMessage()); }
    }

    // --- СЛУЖЕБНЫЕ МЕТОДЫ ОТПРАВКИ ---

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("Ошибка отправки сообщения: " + e.getMessage());
        }
    }
}