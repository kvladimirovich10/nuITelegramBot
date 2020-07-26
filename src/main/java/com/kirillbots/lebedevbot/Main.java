package com.kirillbots.lebedevbot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

public class Main {
    public static void main(String[] args) {
        try {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
            telegramBotsApi.registerBot(Bot.getBot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

    }
}
