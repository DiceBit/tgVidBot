package com.youtubebot.downloadvidfromytbot.bot;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.youtubebot.downloadvidfromytbot.Configuration.Buttons.BotCommands;
import com.youtubebot.downloadvidfromytbot.Configuration.Buttons.Button;
import com.youtubebot.downloadvidfromytbot.Domain.Model.UserData;
import com.youtubebot.downloadvidfromytbot.Domain.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// TODO: 13-Feb-24 Сделать форматы на выбор 
@Component
public class MainBot extends TelegramLongPollingBot implements BotCommands {

    public MainBot(@Value("${bot.token}") String botToken) {
        super(botToken);
        try {
            this.execute(new SetMyCommands(BOT_COMMANDS_LIST, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            System.out.println("Error in constructor: " + e.getMessage());
        }
    }


    @Autowired
    private UserRepository userRepository;

    @Value("${download.dir}")
    private String downloadDir;

    private final String START = "/start";
    private final String HELP = "/help";
    private final String DOWNLOAD = "/download";
    private final String LINK = "/link";
    private final String CLEAR = "/clear";
    private final String SETTINGS = "/settings";

    private final String QUALITY = "Качество: ";
    private final String CHANGE_QUALITY = ALL_QUALITY;

    @Override
    public void onUpdateReceived(Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        var msg = update.getMessage().getText();
        var message = msg.startsWith("/") ? msg.split(" ")[0] : msg;
        var chatId = update.getMessage().getChatId();
        var user = update.getMessage().getFrom();

        if (message.contains(QUALITY)) {
            message = QUALITY;
        }

        switch (message) {
            case START -> {
                if (userRepository.findByUserId(user.getId()) == null) {
                    UserData userData = new UserData(
                            user.getId(),
                            user.getUserName(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getLanguageCode(),
                            String.valueOf(chatId),
                            1,
                            18
                    );
                    userRepository.save(userData);
                }

                var userName = update.getMessage().getChat().getFirstName();

                var formattedText = String.format(START_TEXT, userName);
                sendMessage(chatId, formattedText, Button.replyKeyboardMarkup(userRepository.findByUserId(user.getId())));
            }
            case HELP -> {
                sendMessage(chatId, HELP_TEXT);
            }

            case DOWNLOAD -> {
                System.out.println("Скачивание");
                String[] args = msg.split(" ");
                if (args.length != 2) {
                    System.out.println(message);
                    sendMessage(chatId, "Неверная формат комманды, воспользуйтесь коммандой /help для получения справки");
                    return;
                }
                String url = args[1];
                if (!url.startsWith("https://www.youtube.com/watch?v=")) {
                    System.out.println(message);
                    sendMessage(chatId, "Неверная ссылка, воспользуйтесь коммандой /help для получения справки");
                    return;
                }
                String videoId = args[1].split("v=")[1];
                String title = downloadVideo(videoId);
                sendFile(chatId, title);

            }

            case QUALITY -> {
                sendMessage(chatId, "Выбирите качество", Button.changeQuality());
            }
            case CHANGE_QUALITY -> {

            }
            case SETTINGS -> {
                UserData userData = userRepository.findByUserId(user.getId());
                sendMessage(chatId, SETTING_COMMAND, Button.replyKeyboardMarkup(userData));
            }

            case CLEAR -> {
                sendMessage(chatId, "Очистка истории ⌛");

                var firstMessage = 1;

                if (userRepository.findByChatId(String.valueOf(chatId)) != null) {
                    firstMessage = userRepository.findByChatId(String.valueOf(chatId)).getFirstMsg();
                }
                UserData userData = userRepository.findByChatId(String.valueOf(chatId));
                var lastMessage = update.getMessage().getMessageId() + 1;

                for (int msgId = firstMessage; msgId <= lastMessage; msgId++) {
                    DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), msgId);
                    try {
                        execute(deleteMessage);
                    } catch (TelegramApiException e) {
                        System.out.println("Ошибка удаления сообщения " + e.getMessage());
                    }
                }
                sendMessage(chatId, "История успешно очищена!✅");
                userData.setFirstMsg(lastMessage);
                userRepository.save(userData);

            }
            case LINK -> {
                String botLink = "https://t.me/" + getBotUsername();
                sendMessage(chatId, botLink);
            }
            default -> {
                System.out.println(message);
                sendMessage(chatId, ERROR_COMMAND);
            }
        }
    }

    private void sendMessage(Long chatId, String msgText) {
        var chatIdStr = String.valueOf(chatId);
        var sendMsg = new SendMessage(chatIdStr, msgText);
        try {
            execute(sendMsg);
        } catch (TelegramApiException e) {
            System.out.println("Ошибка отправки сообщения " + e.getMessage());
        }
    }

    private void sendFile(Long chatId, String fileName) {
        System.out.println("отправка");
        var chadIdStr = String.valueOf(chatId);
        var sendVideo = new SendVideo();

        sendVideo.setChatId(chadIdStr);
        sendVideo.setVideo(new InputFile(new File(downloadDir + "/" + fileName), fileName));

        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            System.out.println("Ошибка отправки файла " + e.getMessage());
        }

        try {
            Files.delete(Paths.get(downloadDir + "/" + fileName));
        } catch (IOException e) {
            System.out.println("Ошибка удаления файла " + e.getMessage());
        }

    }

    private String downloadVideo(String videoId) {
        //4vZYnQcM070 - new vid //its work
        //dQw4w9WgXcQ - old vid //its work
        File outputDir = new File("C:/Users/danii/Downloads");
        YoutubeDownloader downloader = new YoutubeDownloader();

        RequestVideoInfo request = new RequestVideoInfo(videoId);
        Response<VideoInfo> response = downloader.getVideoInfo(request);

        VideoInfo video = response.data();
        VideoDetails details = video.details();

        System.out.println(details.title());
        System.out.println(details.description());

        //details.thumbnails().forEach(img -> System.out.println("Thumbnail: " + img));

        //Formats = https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2
        Format videoFormat = video.findFormatByItag(18);
        if (videoFormat != null) {
            System.out.println(videoFormat.url());
        }

        RequestVideoFileDownload download = new RequestVideoFileDownload(videoFormat)
                .saveTo(outputDir)
                .renameTo(details.title())
                .overwriteIfExists(false);
        Response<File> fileResponse = downloader.downloadVideoFile(download);
        File data = fileResponse.data();

        return data.getName();
    }

    @Override
    public String getBotUsername() {
        return "VidDownloadBot";
    }
    private void sendMessage(Long chatId, String msgText, ReplyKeyboardMarkup replyKeyboardMarkup) {

        var sendMsg = new SendMessage();
        sendMsg.setChatId(chatId);
        sendMsg.setText(msgText);
        sendMsg.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(sendMsg);
        } catch (TelegramApiException e) {
            System.out.println("Ошибка отправки сообщения " + e.getMessage());
        }
    }
}
