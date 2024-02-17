package com.youtubebot.downloadvidfromytbot.Configuration.Buttons;

import com.youtubebot.downloadvidfromytbot.Domain.Model.UserData;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class Button {

    public static final InlineKeyboardButton START_BUTTON = new InlineKeyboardButton("Start");
    public static final InlineKeyboardButton HELP_BUTTON = new InlineKeyboardButton("Help");
    public static final InlineKeyboardButton DOWNLOAD_BUTTON = new InlineKeyboardButton("Download");
    public static final InlineKeyboardButton LINK_BUTTON = new InlineKeyboardButton("Link");
    public static final InlineKeyboardButton CLEAR_BUTTON = new InlineKeyboardButton("Clear");
    public static final InlineKeyboardButton SETTINGS_BUTTON = new InlineKeyboardButton("Settings");

    public static final String QUALITY = "Качество: ";

    public static InlineKeyboardMarkup inlineMarkup() {
        START_BUTTON.setCallbackData("/start");
        HELP_BUTTON.setCallbackData("/help");
        DOWNLOAD_BUTTON.setCallbackData("/download");
        LINK_BUTTON.setCallbackData("/link");
        CLEAR_BUTTON.setCallbackData("/clear");
        SETTINGS_BUTTON.setCallbackData("/settings");

        //объект для клавиатуры
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        //список кнопок для первого ряда
        List<InlineKeyboardButton> rowInline = List.of(START_BUTTON, HELP_BUTTON, DOWNLOAD_BUTTON, LINK_BUTTON, CLEAR_BUTTON);
        //список списков кнопок
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline);

        markup.setKeyboard(rowsInLine);

        return markup;
    }

    public static ReplyKeyboardMarkup replyKeyboardMarkup(UserData userData) {

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add(QUALITY + userData.getQuality());

        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }

    public static ReplyKeyboardMarkup changeQuality() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("240p"); // 133
        row.add("360p"); //18

        KeyboardRow row2 = new KeyboardRow();

        row2.add("720p"); //22
        row2.add("1080p"); //37

        keyboardRows.add(row);
        keyboardRows.add(row2);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);

        return replyKeyboardMarkup;
    }
}
