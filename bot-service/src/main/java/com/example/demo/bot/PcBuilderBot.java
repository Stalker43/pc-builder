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
import java.util.Map;

@Component
public class PcBuilderBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.name}")
    private String botName;

    private final BotUserRepository botUserRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // Адреса микросервисов (порты 8081, 8082, 8083)
    private final String API_URL = "http://component-service:8081/api/";
    private final String COMPATIBILITY_API_URL = "http://compatibility-service:8082/api/compatibility/";
    private final String POWER_API_URL = "http://power-service:8083/api/power/";

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
            handleTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    // --- ОБРАБОТКА ТЕКСТА (НИЖНЕЕ МЕНЮ) ---

    private void handleTextMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        long telegramId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText();
        String name = update.getMessage().getChat().getFirstName();

        switch (text) {
            case "/start": initUser(chatId, telegramId, name); break;
            case "Мой профиль 👤": showProfile(chatId, telegramId); break;
            case "Собрать ПК 🛠": startAssembly(chatId, telegramId); break;
            case "Каталог 📋": enterCatalog(chatId, telegramId); break;
            default: sendMsg(chatId, "🤖 Пожалуйста, используй кнопки меню ниже!");
        }
    }

    // --- ОБРАБОТКА НАЖАТИЙ INLINE-КНОПОК ---

    private void handleCallbackQuery(Update update) {
        String callData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long telegramId = update.getCallbackQuery().getFrom().getId();

        BotUser user = botUserRepository.findByTelegramId(telegramId).orElse(null);
        if (user == null) return;

        if (callData.startsWith("CATALOG_")) {
            handleCatalogCategory(chatId, callData);
        }
        else if (callData.startsWith("BUY_")) {
            handleQuickBuy(chatId, user, callData);
        }
        else if (callData.startsWith("SELECT_")) {
            // ЗАЩИТА: Строгая проверка этапа сборки (Машина состояний)
            if (!isCorrectStepForState(user.getState(), callData)) {
                sendMsg(chatId, "⚠️ *Кнопка неактивна!*\n\nВы пытаетесь выбрать деталь не по порядку или нажали на старую кнопку.\nНажмите **'Собрать ПК 🛠'** внизу экрана, чтобы начать новую сборку!");
                return;
            }
            handleAssemblyStep(chatId, user, callData);
        }
    }

    // --- ВАЛИДАТОР СОСТОЯНИЙ (ЗАЩИТА ОТ СТАРЫХ КНОПОК) ---

    public boolean isCorrectStepForState(String currentState, String callData) {
        if (currentState == null || currentState.equals("IDLE")) return false;

        if (callData.startsWith("SELECT_CPU_") && currentState.equals("CHOOSING_CPU")) return true;
        if (callData.startsWith("SELECT_MB_") && currentState.equals("CHOOSING_MB")) return true;
        if (callData.startsWith("SELECT_RAM_") && currentState.equals("CHOOSING_RAM")) return true;
        if (callData.startsWith("SELECT_GPU_") && currentState.equals("CHOOSING_GPU")) return true;
        if (callData.startsWith("SELECT_PSU_") && currentState.equals("CHOOSING_PSU")) return true;
        if (callData.startsWith("SELECT_CASE_") && currentState.equals("CHOOSING_CASE")) return true;

        return false;
    }

    // --- 1. ЛОГИКА КАТАЛОГА (ПРОСТО ПОКУПКА) ---

    private void enterCatalog(long chatId, long telegramId) {
        botUserRepository.findByTelegramId(telegramId).ifPresent(user -> {
            user.setState("IDLE"); // Сброс сборки при входе в каталог
            botUserRepository.save(user);
            sendMenu(chatId, "📋 *КАТАЛОГ* 📋\n\nЗдесь можно купить отдельные детали без проверок совместимости. Выбери категорию:");
        });
    }

    private void handleCatalogCategory(long chatId, String data) {
        String[] parts = data.split("_");
        String type = parts[1].toLowerCase() + "s"; // cpus, gpus, cases...
        if (type.equals("mbs")) type = "motherboards";

        fetchAndSendList(chatId, API_URL + type, "BUY_" + parts[1] + "_", "📦 Доступные модели на складе:");
    }

    private void handleQuickBuy(long chatId, BotUser user, String data) {
        long id = Long.parseLong(data.substring(data.lastIndexOf("_") + 1));

        if (data.contains("CPU")) user.setSelectedCpuId(id);
        else if (data.contains("MB")) user.setSelectedMbId(id);
        else if (data.contains("RAM")) user.setSelectedRamId(id);
        else if (data.contains("GPU")) user.setSelectedGpuId(id);
        else if (data.contains("PSU")) user.setSelectedPsuId(id);
        else if (data.contains("CASE")) user.setSelectedCaseId(id);

        botUserRepository.save(user);
        sendMsg(chatId, "🛒 *Товар успешно добавлен в корзину!*\nМожешь выбрать что-то еще в каталоге.");
    }

    // --- 2. МАСТЕР СБОРКИ (ПО ШАГАМ С ПРОВЕРКАМИ) ---

    private void startAssembly(long chatId, long telegramId) {
        botUserRepository.findByTelegramId(telegramId).ifPresent(user -> {
            user.setState("CHOOSING_CPU");
            // Полная очистка корзины для новой сборки
            user.setSelectedCpuId(null); user.setSelectedMbId(null); user.setSelectedRamId(null);
            user.setSelectedGpuId(null); user.setSelectedPsuId(null); user.setSelectedCaseId(null);
            botUserRepository.save(user);

            sendMsg(chatId, "🛠 *Запуск Мастера Сборки!*\n\n💡 *СОВЕТ:* Начинаем с процессора. От него будет зависеть выбор материнской платы.");
            fetchAndSendList(chatId, API_URL + "cpus", "SELECT_CPU_", "🧠 Выбери процессор:");
        });
    }

    private void handleAssemblyStep(long chatId, BotUser user, String data) {
        long id = Long.parseLong(data.substring(data.lastIndexOf("_") + 1));

        if (data.startsWith("SELECT_CPU_")) {
            user.setSelectedCpuId(id); user.setState("CHOOSING_MB"); botUserRepository.save(user);
            sendMsg(chatId, "✅ *Процессор выбран!*\n\n💡 *РЕКОМЕНДАЦИЯ:* Я отфильтровал материнские платы. Выбирай ту, что подходит под твой сокет.");
            fetchAndSendList(chatId, COMPATIBILITY_API_URL + "motherboards?cpuId=" + id, "SELECT_MB_", "🎛 Совместимые материнские платы:");
        }
        else if (data.startsWith("SELECT_MB_")) {
            user.setSelectedMbId(id); user.setState("CHOOSING_RAM"); botUserRepository.save(user);
            sendMsg(chatId, "✅ *Плата добавлена!*\n\n💡 *РЕКОМЕНДАЦИЯ:* Выбираем ОЗУ. Поколение памяти (например, DDR4 или DDR5) должно строго совпадать.");
            fetchAndSendList(chatId, COMPATIBILITY_API_URL + "rams?mbId=" + id, "SELECT_RAM_", "⚡ Подходящая оперативная память:");
        }
        else if (data.startsWith("SELECT_RAM_")) {
            user.setSelectedRamId(id); user.setState("CHOOSING_GPU"); botUserRepository.save(user);
            sendMsg(chatId, "✅ *Память в корзине!*\n\n💡 *РЕКОМЕНДАЦИЯ:* Видеокарта — главная деталь для FPS в играх. Выбирай мощнее!");
            fetchAndSendList(chatId, API_URL + "gpus", "SELECT_GPU_", "🎮 Доступные видеокарты:");
        }
        else if (data.startsWith("SELECT_GPU_")) {
            user.setSelectedGpuId(id); user.setState("CHOOSING_PSU"); botUserRepository.save(user);
            sendMsg(chatId, "✅ *Видеокарта установлена!*\n\n⏳ *Связываюсь с power-service...*\nСчитаем энергопотребление CPU и GPU для подбора Блока Питания.");
            processPowerAndPsuStep(chatId, user.getSelectedCpuId(), id);
        }
        else if (data.startsWith("SELECT_PSU_")) {
            user.setSelectedPsuId(id); user.setState("CHOOSING_CASE"); botUserRepository.save(user);
            sendMsg(chatId, "✅ *Блок питания готов!*\n\n💡 *РЕКОМЕНДАЦИЯ:* Последний штрих — выбрать красивый и вместительный корпус.");
            fetchAndSendList(chatId, API_URL + "cases", "SELECT_CASE_", "📦 Выбери корпус:");
        }
        else if (data.startsWith("SELECT_CASE_")) {
            user.setSelectedCaseId(id); user.setState("IDLE"); botUserRepository.save(user);
            sendMsg(chatId, "🎉 *УРА! СБОРКА ПОЛНОСТЬЮ ЗАВЕРШЕНА!* 🎉\n\nВсе детали успешно подобраны и сохранены. Загляни в 'Мой профиль 👤', чтобы посмотреть результат.");
        }
        else if (data.startsWith("SELECT_CASE_")) {
            user.setSelectedCaseId(id);
            user.setState("IDLE");
            botUserRepository.save(user);

            // ФИНАЛ: Отправляем всю сборку в assembly-service (Порт 8084)
            try {
                String assemblyUrl = "http://assembly-service:8084/api/assembly/save";
                restTemplate.postForObject(assemblyUrl, user, String.class);

                sendMsg(chatId, "🎉 *УРА! СБОРКА ПОЛНОСТЬЮ ЗАВЕРШЕНА!* 🎉\n\nВсе детали успешно подобраны и сохранены на главном сервере. Загляни в 'Мой профиль 👤', чтобы посмотреть результат.");
            } catch (Exception e) {
                sendMsg(chatId, "🎉 Сборка завершена локально!\n⚠️ *Но микросервис assembly-service сейчас недоступен*, поэтому в историю она не попала.");
                System.err.println("Ошибка отправки в assembly-service: " + e.getMessage());
            }
        }
    }

    // --- 3. ИНТЕГРАЦИЯ С POWER-SERVICE ---

    private void processPowerAndPsuStep(long chatId, Long cpuId, long gpuId) {
        try {
            // 1. Узнаем ватты (Идем в power-service на порт 8083)
            String url = POWER_API_URL + "calculate?cpuId=" + cpuId + "&gpuId=" + gpuId;
            PowerResponseDto power = restTemplate.getForObject(url, PowerResponseDto.class);

            int minWatt = (power != null && power.getRequiredWattage() != null) ? power.getRequiredWattage() : 0;
            String advice = (power != null && power.getRecommendation() != null) ? power.getRecommendation() : "Расчет выполнен.";

            sendMsg(chatId, "📊 *Инженерный отчет:*\n• Минимальная мощность: `" + minWatt + "W`\n• Вердикт: _" + advice + "_");

            // 2. Получаем все БП со склада (Порт 8081)
            PsuDto[] allPsus = restTemplate.getForObject(API_URL + "psus", PsuDto[].class);
            if (allPsus == null || allPsus.length == 0) {
                sendMsg(chatId, "❌ Блоков питания нет на складе.");
                return;
            }

            // 3. Отбираем только те, что выдержат нагрузку
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (PsuDto psu : allPsus) {
                if (psu.getWattage() >= minWatt) {
                    rows.add(List.of(createBtn(psu.getName() + " (" + psu.getWattage() + "W) | $" + psu.getPrice(), "SELECT_PSU_" + psu.getId())));
                }
            }

            if (rows.isEmpty()) {
                sendMsg(chatId, "⚠️ На складе нет достаточно мощных БП для твоей системы!");
                return;
            }

            markup.setKeyboard(rows);
            SendMessage msg = new SendMessage(String.valueOf(chatId), "🔋 Рекомендуемые блоки питания:");
            msg.setReplyMarkup(markup);
            msg.setParseMode("Markdown");
            execute(msg);

        } catch (Exception e) {
            sendMsg(chatId, "❌ Ошибка при расчете мощности. Проверьте логи микросервисов.");
            e.printStackTrace();
        }
    }

    // --- УНИВЕРСАЛЬНЫЕ МЕТОДЫ-ПОМОЩНИКИ ---

    private void fetchAndSendList(long chatId, String url, String btnPrefix, String headerMsg) {
        try {
            Object[] items = restTemplate.getForObject(url, Object[].class);
            if (items == null || items.length == 0) {
                sendMsg(chatId, "📦 Склад пуст. Возвращайтесь позже.");
                return;
            }

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            for (Object item : items) {
                Map<String, Object> map = (Map<String, Object>) item;
                String btnText = map.get("name") + " | $" + map.get("price");
                rows.add(List.of(createBtn(btnText, btnPrefix + map.get("id"))));
            }

            markup.setKeyboard(rows);
            SendMessage msg = new SendMessage(String.valueOf(chatId), headerMsg);
            msg.setReplyMarkup(markup);
            msg.setParseMode("Markdown");
            execute(msg);

        } catch (Exception e) {
            sendMsg(chatId, "❌ Ошибка загрузки списка деталей.");
        }
    }

    private void initUser(long chatId, long telegramId, String name) {
        BotUser user = botUserRepository.findByTelegramId(telegramId).orElseGet(BotUser::new);
        user.setTelegramId(telegramId);
        user.setChatId(chatId);
        user.setFirstName(name);
        user.setState("IDLE");
        botUserRepository.save(user);
        sendMenu(chatId, "👋 Привет, " + name + "! Я твой личный помощник-инженер.\nВыбери нужный раздел в меню ниже:");
    }

    private void showProfile(long chatId, long telegramId) {
        botUserRepository.findByTelegramId(telegramId).ifPresent(u -> {
            if (u.getSelectedCpuId() == null) {
                sendMsg(chatId, "👤 *Профиль:* " + u.getFirstName() + "\n\nУ тебя пока нет сохраненных сборок. Нажми **'Собрать ПК 🛠'**, чтобы начать!");
                return;
            }

            try {
                // Запрашиваем детали со склада
                Map cpu = restTemplate.getForObject(API_URL + "cpus/" + u.getSelectedCpuId(), Map.class);
                Map mb = restTemplate.getForObject(API_URL + "motherboards/" + u.getSelectedMbId(), Map.class);
                Map ram = restTemplate.getForObject(API_URL + "rams/" + u.getSelectedRamId(), Map.class);
                Map gpu = restTemplate.getForObject(API_URL + "gpus/" + u.getSelectedGpuId(), Map.class);
                Map psu = restTemplate.getForObject(API_URL + "psus/" + u.getSelectedPsuId(), Map.class);
                Map pcCase = restTemplate.getForObject(API_URL + "cases/" + u.getSelectedCaseId(), Map.class);

                // Безопасное суммирование цен
                double total = 0;
                total += Double.parseDouble(cpu.get("price").toString());
                total += Double.parseDouble(mb.get("price").toString());
                total += Double.parseDouble(ram.get("price").toString());
                total += Double.parseDouble(gpu.get("price").toString());
                total += Double.parseDouble(psu.get("price").toString());
                total += Double.parseDouble(pcCase.get("price").toString());

                String report = "👤 *ТВОЯ ПОСЛЕДНЯЯ СБОРКА* 👤\n\n" +
                        "🧠 *Процессор:* " + cpu.get("name") + " ($" + cpu.get("price") + ")\n" +
                        "🎛 *Плата:* " + mb.get("name") + " ($" + mb.get("price") + ")\n" +
                        "⚡ *ОЗУ:* " + ram.get("name") + " ($" + ram.get("price") + ")\n" +
                        "🎮 *Видеокарта:* " + gpu.get("name") + " ($" + gpu.get("price") + ")\n" +
                        "🔋 *БП:* " + psu.get("name") + " ($" + psu.get("price") + ")\n" +
                        "📦 *Корпус:* " + pcCase.get("name") + " ($" + pcCase.get("price") + ")\n\n" +
                        "💰 *ИТОГО:* `$" + String.format("%.2f", total) + "`";

                sendMsg(chatId, report);

            } catch (Exception e) {
                sendMsg(chatId, "❌ Ошибка при загрузке деталей профиля. Убедись, что методы поиска по ID добавлены во все контроллеры.");
                e.printStackTrace(); // Выведет точную ошибку в консоль докера
            }
        });
    }

    private void sendMenu(long chatId, String text) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);

        ReplyKeyboardMarkup kb = new ReplyKeyboardMarkup();
        kb.setKeyboard(List.of(
                new KeyboardRow(List.of(new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton("Собрать ПК 🛠"))),
                new KeyboardRow(List.of(
                        new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton("Каталог 📋"),
                        new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton("Мой профиль 👤")
                ))
        ));
        kb.setResizeKeyboard(true);
        msg.setReplyMarkup(kb);
        msg.setParseMode("Markdown");

        // Если текст содержит слово КАТАЛОГ, прикрепляем inline-кнопки
        if (text.contains("КАТАЛОГ")) {
            InlineKeyboardMarkup inlineKb = new InlineKeyboardMarkup();
            inlineKb.setKeyboard(List.of(
                    List.of(createBtn("Процессоры 🧠", "CATALOG_CPU"), createBtn("Материнки 🎛", "CATALOG_MB")),
                    List.of(createBtn("Видеокарты 🎮", "CATALOG_GPU"), createBtn("Оперативка ⚡", "CATALOG_RAM")),
                    List.of(createBtn("Блоки питания 🔋", "CATALOG_PSU"), createBtn("Корпуса 📦", "CATALOG_CASE"))
            ));
            msg.setReplyMarkup(inlineKb);
        }
        try { execute(msg); } catch (TelegramApiException ignored) {}
    }

    private void sendMsg(long chatId, String text) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setParseMode("Markdown");
        try { execute(msg); } catch (TelegramApiException ignored) {}
    }

    private InlineKeyboardButton createBtn(String text, String callback) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(callback);
        return btn;
    }
}