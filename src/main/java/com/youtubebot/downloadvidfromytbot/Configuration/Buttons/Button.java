package com.youtubebot.downloadvidfromytbot.Configuration.Buttons;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class Button {

    public static final InlineKeyboardButton START_BUTTON = new InlineKeyboardButton("Start");
    public static final InlineKeyboardButton HELP_BUTTON = new InlineKeyboardButton("Help");
    public static final InlineKeyboardButton DOWNLOAD_BUTTON = new InlineKeyboardButton("Download");
    public static final InlineKeyboardButton LINK_BUTTON = new InlineKeyboardButton("Link");
    public static final InlineKeyboardButton CLEAR_BUTTON = new InlineKeyboardButton("Clear");

    public static InlineKeyboardMarkup inlineMarkup() {
        START_BUTTON.setCallbackData("/start");
        HELP_BUTTON.setCallbackData("/help");
        DOWNLOAD_BUTTON.setCallbackData("/download");
        LINK_BUTTON.setCallbackData("/link");
        CLEAR_BUTTON.setCallbackData("/clear");

        List<InlineKeyboardButton> rowInline = List.of(START_BUTTON, HELP_BUTTON, DOWNLOAD_BUTTON, LINK_BUTTON, CLEAR_BUTTON);
        List<List<InlineKeyboardButton>> rowsInLine = List.of(rowInline);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rowsInLine);

        return markup;
    }
}
