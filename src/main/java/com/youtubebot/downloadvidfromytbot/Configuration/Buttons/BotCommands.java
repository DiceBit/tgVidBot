package com.youtubebot.downloadvidfromytbot.Configuration.Buttons;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public interface BotCommands {

    List<BotCommand> BOT_COMMANDS_LIST = List.of(
        new BotCommand("/start", "Start bot"),
        new BotCommand("/link", "Link to bot"),
        new BotCommand("/download", "Download YouTube video"),
        new BotCommand("/settings", "Setting"),
        new BotCommand("/clear", "Clear all message"),
        new BotCommand("/help", "Help")
    );

    String START_TEXT = """
            Добро пожаловать %s!
                                                
            Здесь вы можете скачивать видео с YouTube без платной подписки!
                                    
            Дополнительные комманды:
            /help
            """;

    String HELP_TEXT = """
            Справочная информация
                                                
            Комманды:
            /download [ссылка] - позволяет скачать видео с YouTube
            Формат ссылки - https://www.youtube.com/watch?v=abc123
                                
            /settings - настройка качества скачивания видео                     
            /link - ссылка на бота
            /clear - очистка всей переписки
            """;

    String ERROR_COMMAND = "Такой комманды нет, воспользуйтесь коммандой /help для получения справки";

    String SETTING_COMMAND = "Настройка качества скачивания видео";

    String ALL_QUALITY = "360p 720p";
}
