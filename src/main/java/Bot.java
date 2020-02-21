import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;

public class Bot extends TelegramLongPollingBot {
    private static Properties prop;

    private static String fatherChatId;

    private static String lebedevPath;
    private static String lebedevHairPath;
    private static String lebedevGrayPath;

    static {
        FileInputStream fis;
        prop = new Properties();
        try {
            fis = new FileInputStream("src/main/resources/bot.properties");
            prop.load(fis);

            fatherChatId = prop.getProperty("fatherChatId");

            lebedevGrayPath = prop.getProperty("lebedevGrayPath");
            lebedevHairPath = prop.getProperty("lebedevHairPath");
            lebedevPath = prop.getProperty("lebedevPath");
        } catch (IOException e) {
            System.err.println("No property file");
        }
    }

    private static Bot bot;

    private Bot() {
    }

    static synchronized Bot getBot() {
        if (bot == null)
            bot = new Bot();

        return bot;
    }


    public void onUpdateReceived(Update update) {

        String emoRegex = "([\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee])";

        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();

            if (message.matches(emoRegex)) {
                message = "эмодзи";
            }

            String processedMessage = message.replaceAll("[^\\p{L}0-9]", " ").toLowerCase();

            System.out.println(processedMessage);
            String chatId = update.getMessage().getChatId().toString();

            try {
                addTextOnImage(processedMessage);
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new File("memeLebedev.png"));

                if (!fatherChatId.equals(chatId)) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(fatherChatId);
                    sendMessage.setText(processedMessage);
                    execute(sendMessage);
                }

                execute(sendPhoto);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getBotUsername() {
        return prop.getProperty("botName");
    }

    public String getBotToken() {
        return prop.getProperty("token");
    }

    private static void addTextOnImage(String text) throws Exception {

        BufferedImage preparedImage = imageProcessing();

        LinkedList<StringBuilder> memeLines = new LinkedList<>();

        memeLines.add(new StringBuilder().append("ну ").append(text));

        float fontSize = 150f;
        if (text.length() > 8)
            memeLines.add(new StringBuilder());
        else if (text.length() > 6)
            fontSize = 125f;

        memeLines.getLast().append(" и ").append(text);

        drawText(preparedImage, fontSize, memeLines);
    }

    private static BufferedImage imageProcessing() throws Exception {

        BufferedImage hairImg = ImageIO.read(new File(lebedevHairPath));
        BufferedImage grayImg = ImageIO.read(new File(lebedevGrayPath));

        BufferedImage image = new BufferedImage(grayImg.getWidth(), grayImg.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        g.drawImage(grayImg, 0, 0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(getRandomColor());
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        g.drawImage(hairImg, 0, 0, null);
        g.dispose();

        return image;
    }


    private static void drawText(BufferedImage image, float fontSize, LinkedList memeLines) throws Exception {

        Graphics g = image.getGraphics();
        Font font;

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/18645.ttf"));
        } catch (IOException|FontFormatException e) {
            font = g.getFont();
        }

        g.setFont(font.deriveFont(fontSize));
        g.setColor(Color.white);

        FontMetrics metrics = g.getFontMetrics();

        for (int i = 0; i < memeLines.size(); i++) {
            int x = (image.getWidth() - metrics.stringWidth(memeLines.get(i).toString())) / 2;
            double y = image.getHeight()
                    - 0.1 * image.getHeight() - 0.5 * (memeLines.size() - 1) * metrics.getHeight()
                    + 0.8 * i * metrics.getHeight();

            g.drawString(memeLines.get(i).toString(), x, (int) y);

        }

        g.dispose();

        ImageIO.write(image, "png", new File("memeLebedev.png"));
    }

    private static Color getRandomColor() {
        return new Color(new Random().nextInt(255),
                new Random().nextInt(255),
                new Random().nextInt(255),
                100);
    }
}