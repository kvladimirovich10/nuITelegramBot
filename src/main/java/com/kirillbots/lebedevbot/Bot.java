package com.kirillbots.lebedevbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.cached.InlineQueryResultCachedPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bot extends TelegramLongPollingBot {
    private final static String LEBEDEV_NO_HAIR_PATH = "src/main/resources/lebedev_hair.png";
    private final static String LEBEDEV_GRAY_PATH = "src/main/resources/lebedev_gray.jpeg";
    private final static String DUMMY_IMAGE_PATH = "src/main/resources/dummy_image.jpeg";
    private final static String FONT_PATH = "src/main/resources/font.ttf";
    private final static String CACHE_CHANNEL_ID = System.getenv("CACHE_CHANNEL_ID");

    private final static Logger LOG = LoggerFactory.getLogger(Bot.class);

    private static Bot bot;

    private String dummyImageId;
    private InlineKeyboardMarkup waitForAnImageKeyboardMarkup;
    private InlineKeyboardMarkup tryInlineModeKeyboardMarkup;


    private Bot() {
    }

    static synchronized Bot getBot() {
        if (bot == null) {
            bot = new Bot();
            bot.onStartup();
        }
        return bot;
    }

    private void onStartup() {
        try {
            // Buttons will always be the same, so no need to instantiate new button objects when making a meme
            makeButtons();

            // It is better to have this photo cached, because it will always be the same photo
            cacheDummyPhoto();
        } catch (Exception e) {
            LOG.error("Failed to start properly", e);
        }
    }

    private void cacheDummyPhoto() throws TelegramApiException {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(new File(DUMMY_IMAGE_PATH));
        sendPhoto.setCaption("<pre>бот включился</pre>");
        sendPhoto.setParseMode(ParseMode.HTML);
        sendPhoto.disableNotification();
        sendPhoto.setChatId(CACHE_CHANNEL_ID);
        Message sentMessage = bot.execute(sendPhoto);
        this.dummyImageId = sentMessage.getPhoto().get(0).getFileId();
    }

    private void makeButtons() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("здесь будет мем...");
        button.setCallbackData("does not matter what is here");
        waitForAnImageKeyboardMarkup = new InlineKeyboardMarkup(new ArrayList() {{
            add(new ArrayList() {{
                add(button);
            }});
        }});

        InlineKeyboardButton tryInlineModeButton = new InlineKeyboardButton("попробовать инлайн режим 😎");
        tryInlineModeButton.setSwitchInlineQuery("");
        tryInlineModeKeyboardMarkup = new InlineKeyboardMarkup(new ArrayList() {{
            add(new ArrayList() {{
                add(tryInlineModeButton);
            }});
        }});
    }

    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                processTextMessage(update);
            } else if (update.hasInlineQuery() && update.getInlineQuery().getQuery().length() > 0) {
                processInlineQuery(update);
            } else if (update.hasChosenInlineQuery()) {
                editInlinePhoto(update);
            }
        } catch (Exception e) {
            LOG.error("Failed to process update", e);
        }
    }

    private void processTextMessage(Update update) throws Exception {
        String chatId = update.getMessage().getChat().getId().toString();

        String message = update.getMessage().getText();
        String cleanedUpMessage = cleanUpText(message);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto("lebedevMeme", buildMemeFromText(cleanedUpMessage));
        sendPhoto.setReplyMarkup(tryInlineModeKeyboardMarkup);
        Message sentPhoto = execute(sendPhoto);

        List<PhotoSize> sentPhotos = sentPhoto.getPhoto();
        String sentPhotoFileId = sentPhotos.get(sentPhotos.size() - 1).getFileId();
        sendPhoto.setReplyMarkup(null);
        sendPhoto.setPhoto(sentPhotoFileId);
        sendPhoto.setChatId(CACHE_CHANNEL_ID);
        sendPhoto.setCaption("<pre>не инлайн режим</pre>");
        sendPhoto.setParseMode(ParseMode.HTML);
        execute(sendPhoto);
    }

    private void processInlineQuery(Update update) throws TelegramApiException {
        AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
        answerInlineQuery.setInlineQueryId(update.getInlineQuery().getId());

        InlineQueryResultCachedPhoto inlineQueryResultCachedPhoto = new InlineQueryResultCachedPhoto();
        inlineQueryResultCachedPhoto.setId("0");
        inlineQueryResultCachedPhoto.setPhotoFileId(dummyImageId);

        inlineQueryResultCachedPhoto.setReplyMarkup(waitForAnImageKeyboardMarkup);

        answerInlineQuery.setResults(inlineQueryResultCachedPhoto);
        execute(answerInlineQuery);
    }

    private void editInlinePhoto(Update update) throws IOException, TelegramApiException {
        String inlineMessageId = update.getChosenInlineQuery().getInlineMessageId();
        String lebedevText = update.getChosenInlineQuery().getQuery();
        String cleanedUpLebedevText = cleanUpText(lebedevText);

        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setInlineMessageId(inlineMessageId);

        InputMediaPhoto meme = new InputMediaPhoto();

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto("lebedevMeme", buildMemeFromText(cleanedUpLebedevText));
        sendPhoto.disableNotification();
        sendPhoto.setChatId(CACHE_CHANNEL_ID);
        sendPhoto.setCaption("<pre>инлайн режим</pre>");
        sendPhoto.setParseMode(ParseMode.HTML);
        Message sentPhoto = execute(sendPhoto);

        List<PhotoSize> sentPhotos = sentPhoto.getPhoto();
        String sentPhotoFileId = sentPhotos.get(sentPhotos.size() - 1).getFileId();
        meme.setMedia(sentPhotoFileId);
        editMessageMedia.setMedia(meme);
        execute(editMessageMedia);
    }

    private static String cleanUpText(String text) {
        String emojiRegexp = "([\\u20a0-\\u32ff\\ud83c\\udc00-\\ud83d\\udeff\\udbb9\\udce5-\\udbb9\\udcee])";
        if (text.matches(emojiRegexp)) {
            text = "эмодзи";
        }

        return text.replaceAll("[^\\p{L}0-9]", " ").toLowerCase();
    }

    private static ByteArrayInputStream buildMemeFromText(String text) throws IOException {
        BufferedImage preparedImage = buildImageWithColoredHair();
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

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(drawText(preparedImage, fontSize, memeLines), "png", os);

        return new ByteArrayInputStream(os.toByteArray());
    }

    private static BufferedImage buildImageWithColoredHair() throws IOException {
        BufferedImage hairImg = ImageIO.read(new File(LEBEDEV_NO_HAIR_PATH));
        BufferedImage grayImg = ImageIO.read(new File(LEBEDEV_GRAY_PATH));

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


    private static BufferedImage drawText(BufferedImage image, float fontSize, List memeLines) {
        Graphics g = image.getGraphics();
        Font font;

        try {
            font = Font.createFont(Font.TRUETYPE_FONT, new File(FONT_PATH));
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

    private static Color getRandomColor() {
        Random random = new Random();
        return new Color(random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                100);
    }

    public String getBotUsername() {
        return System.getenv("BOT_NAME");
    }

    public String getBotToken() {
        return System.getenv("TOKEN");
    }
}