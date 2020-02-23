import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/bot.properties")) {
            properties.load(fis);
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

            telegramBotsApi.registerBot(Bot.getBot(properties));

        } catch (IOException e) {
            System.err.println("No property file");
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

    }
}
