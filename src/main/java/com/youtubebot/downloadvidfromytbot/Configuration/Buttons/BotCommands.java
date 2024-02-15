package com.youtubebot.downloadvidfromytbot.Configuration.Buttons;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public interface BotCommands {

    List<BotCommand> BOT_COMMANDS_LIST = List.of(
        new BotCommand("/start", "Start bot"),
        new BotCommand("/link", "Link to bot"),
        new BotCommand("/download", "Download YouTube video"),
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
                                    
            /link - ссылка на бота
            /clear - очистка всей переписки
            """;

}
