import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedList;
import java.util.Random;

public class Bot extends TelegramLongPollingBot {

    private final static String lebedevPath = "src/main/resources/lebedev.png";
    private final static String lebedevHairPath = "src/main/resources/lebedev_hair.png";
    private final static String lebedevGrayPath = "src/main/resources/lebedev_gray.jpeg";
    private final static String fontPath = "src/main/resources/font.ttf";

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

            System.out.println("-- " + processedMessage);

            String chatId = update.getMessage().getChatId().toString();

            try {

                String fatherChatId = System.getenv("FATHER_CHAT_ID");

                if (!fatherChatId.equals(chatId)) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(fatherChatId);
                    sendMessage.setText("-- " + processedMessage);
                    execute(sendMessage);
                }

                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(addTextOnImage(processedMessage), "png", os);
                InputStream is = new ByteArrayInputStream(os.toByteArray());
                sendPhoto.setPhoto("lebedevMeme", is);
                execute(sendPhoto);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getBotUsername() {
        return System.getenv("BOT_NAME");
    }

    public String getBotToken() {
        return System.getenv("TOKEN");
    }

    private static BufferedImage addTextOnImage(String text) throws Exception {

        BufferedImage preparedImage = getProcessedImage();

        LinkedList<StringBuilder> memeLines = new LinkedList<>();

        memeLines.add(new StringBuilder().append("ну ").append(text));

        float fontSize = 110f;
        String blank = " ";
        if (text.length() > 7) {
            memeLines.add(new StringBuilder());
            blank = "";
        } else if (text.length() > 5)
            fontSize = 85f;

        memeLines.getLast().append(blank).append("и ").append(text);

        return drawText(preparedImage, fontSize, memeLines);
    }

    private static BufferedImage getProcessedImage() throws Exception {

        BufferedImage hairImg = ImageIO.read(new File(lebedevHairPath));
        BufferedImage grayImg = ImageIO.read(new File(lebedevGrayPath));

        BufferedImage image = new BufferedImage(grayImg.getWidth(), grayImg.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = image.createGraphics();

        g.drawImage(grayImg, 0, 0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(getRandomColor(0, 255));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        g.drawImage(hairImg, 0, 0, null);
        g.dispose();

        return image;
    }


    private static BufferedImage drawText(BufferedImage image, float fontSize, LinkedList memeLines) {

        Graphics g = image.getGraphics();
        Font font;

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath));
        } catch (IOException | FontFormatException e) {
            font = g.getFont();
        }

        g.setFont(font.deriveFont(fontSize));
        g.setColor(Color.white);

        FontMetrics metrics = g.getFontMetrics();

        for (int i = 0; i < memeLines.size(); i++) {
            int x = (image.getWidth() - metrics.stringWidth(memeLines.get(i).toString())) / 2;
            int y = (int) (image.getHeight()
                    - 0.08 * image.getHeight() - 0.5 * (memeLines.size() - 1) * metrics.getHeight()
                    + 0.9 * i * metrics.getHeight());

            g.drawString(memeLines.get(i).toString(), x, y);
        }

        g.dispose();

        return image;
    }

    private static Color getRandomColor(int min, int max) {
        Random random = new Random();
        return new Color(random.nextInt((max - min) + 1) + min,
                random.nextInt((max - min) + 1) + min,
                random.nextInt((max - min) + 1) + min,
                100);
    }
}