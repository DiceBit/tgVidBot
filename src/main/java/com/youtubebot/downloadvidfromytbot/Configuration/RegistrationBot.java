package com.youtubebot.downloadvidfromytbot.Configuration;

import com.youtubebot.downloadvidfromytbot.bot.MainBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class RegistrationBot {

    @Bean
    public TelegramBotsApi telegramBotsApi(MainBot mainBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(mainBot);
        return api;
    }


}
