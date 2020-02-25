import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
            ApiContextInitializer.init();
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi();

            telegramBotsApi.registerBot(Bot.getBot());

        } catch (IOException e) {
            System.err.println("No property file");
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

    }
}
