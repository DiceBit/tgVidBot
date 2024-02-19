package com.youtubebot.downloadvidfromytbot.bot;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.youtubebot.downloadvidfromytbot.Configuration.Buttons.BotCommands;
import com.youtubebot.downloadvidfromytbot.Configuration.Buttons.Button;
import com.youtubebot.downloadvidfromytbot.Domain.Model.UserData;
import com.youtubebot.downloadvidfromytbot.Domain.Repository.UserRepository;
import org.apache.commons.io.input.CountingInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static com.youtubebot.downloadvidfromytbot.Configuration.Buttons.Button.QUALITY_MAP;

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
    @Value("${clear.dir}")
    private String clearDir;

    private final String START = "/start";
    private final String HELP = "/help";
    private final String DOWNLOAD = "/download";
    private final String LINK = "/link";
    private final String CLEAR = "/clear";
    private final String SETTINGS = "/settings";

    private final String QUALITY = "Качество: ";

    @Override
    public void onUpdateReceived(Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        var msg = update.getMessage().getText();
        var message = msg.startsWith("/") ? msg.split(" ")[0] : msg;
        var chatId = update.getMessage().getChatId();
        var user = update.getMessage().getFrom();

        if (message.contains(QUALITY)) message = QUALITY;

        if (ALL_QUALITY.contains(message)) {
            UserData userData = userRepository.findByUserId(user.getId());
            userData.setQuality(QUALITY_MAP.get(message));
            userRepository.save(userData);
            message = SETTINGS;
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
                sendMessage(chatId, "Ожидайте ⌛");
                /*String title = downloadVideo(videoId, userRepository.findByUserId(user.getId()), chatId);
                sendFile(chatId, title);*/
                sendFile(chatId, videoId, userRepository.findByUserId(user.getId()));
                try {
                    execute(new DeleteMessage(String.valueOf(chatId), update.getMessage().getMessageId() + 1));
                } catch (TelegramApiException e) {
                    System.out.println("Ошибка удаления сообщения " + e.getMessage());
                }
            }

            case QUALITY -> {
                sendMessage(chatId, "Выбирите качество", Button.changeQuality());
            }
            case SETTINGS -> {
                UserData userData = userRepository.findByUserId(user.getId());
                sendMessage(chatId, SETTING_COMMAND, Button.replyKeyboardMarkup(userData));
                try {
                    execute(new DeleteMessage(String.valueOf(chatId), update.getMessage().getMessageId() - 1));
                    execute(new DeleteMessage(String.valueOf(chatId), update.getMessage().getMessageId()));
                } catch (TelegramApiException e) {
                    System.out.println("Ошибка удаления сообщения " + e.getMessage());
                }
            }

            case CLEAR -> {
                clearMessageHistory(chatId, update.getMessage().getMessageId() + 1);
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

    private Message sendMessage(Long chatId, String msgText) {
        var chatIdStr = String.valueOf(chatId);
        var sendMsg = new SendMessage(chatIdStr, msgText);
        try {
            return execute(sendMsg);
        } catch (TelegramApiException e) {
            System.out.println("Ошибка отправки сообщения " + e.getMessage());
            return null;
        }
    }

    /*private void sendFile(Long chatId, String fileName) {
        System.out.println("отправка");
        var chadIdStr = String.valueOf(chatId);
        var sendVideo = new SendVideo();

        sendVideo.setChatId(chadIdStr);
        sendVideo.setVideo(new InputFile(new File(downloadDir + "/" + fileName), fileName));

        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            sendMessage(chatId, "Ошибка отправки видео, повторите позже");
            e.printStackTrace();
        }

        try {
            Files.delete(Paths.get(downloadDir + "/" + fileName));
        } catch (IOException e) {
            System.out.println("Ошибка удаления файла " + e.getMessage());
        }

    }

    private String downloadVideo(String videoId, UserData userData, Long chatId) {
        //4vZYnQcM070 - new vid //its work
        //dQw4w9WgXcQ - old vid //its work
        File outputDir = new File("C:/Users/danii/Downloads");
        YoutubeDownloader downloader = new YoutubeDownloader();

        RequestVideoInfo request = new RequestVideoInfo(videoId);
        Response<VideoInfo> response = downloader.getVideoInfo(request);

        VideoInfo video = response.data();
        VideoDetails details = video.details();

        Pattern reg = Pattern.compile("[^a-zA-Z0-9а-яА-Я]");
        Matcher matcher = reg.matcher(details.title());
        String title = matcher.replaceAll("_");

        System.out.println(details.title());

        //details.thumbnails().forEach(img -> System.out.println("Thumbnail: " + img));

        //Formats = https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2
        Format videoFormat = video.findFormatByItag(userData.getQuality());
        if (videoFormat != null) {
            System.out.println(videoFormat.url());
        }

        var msg = sendMessage(chatId, "Downloading 0%");
        RequestVideoFileDownload download = new RequestVideoFileDownload(videoFormat)
                .saveTo(outputDir)
                .renameTo(title)
                .overwriteIfExists(false)
                .callback(new YoutubeProgressCallback<File>() {

                    EditMessageText editMessageText = new EditMessageText();
                    @Override
                    public void onDownloading(int progress) {
                        String progressStr = String.format("Downloaded %d%%\n", progress);

                        editMessageText.setChatId(chatId);
                        editMessageText.setMessageId(msg.getMessageId());
                        editMessageText.setText(progressStr);

                        try {
                            execute(editMessageText);
                        } catch (TelegramApiException e) {
                            System.out.println("Ошибка отображения прогресса " + e.getMessage());
                        }
                    }
                    @Override
                    public void onFinished(File file) {

                        editMessageText.setChatId(chatId);
                        editMessageText.setMessageId(msg.getMessageId());
                        editMessageText.setText(details.title());
                        try {
                            execute(editMessageText);
                        } catch (TelegramApiException e) {
                            System.out.println("Ошибка отображения прогресса " + e.getMessage());
                        }
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        editMessageText.setChatId(chatId);
                        editMessageText.setMessageId(msg.getMessageId());
                        editMessageText.setText("Возникла ошибка при загрузке видео, повторите позже");
                        try {
                            execute(editMessageText);
                        } catch (TelegramApiException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    }
                });
        Response<File> fileResponse = downloader.downloadVideoFile(download);
        File data = fileResponse.data();


        return data.getName();
    }*/

    private void sendFile(Long chatId, String videoId, UserData userData) {

        var msg = sendMessage(chatId, "Загрузка 0%");
        var chadIdStr = String.valueOf(chatId);
        var sendVideo = new SendVideo();

        sendVideo.setChatId(chadIdStr);

        YoutubeDownloader downloader = new YoutubeDownloader();
        RequestVideoInfo request = new RequestVideoInfo(videoId);
        Response<VideoInfo> response = downloader.getVideoInfo(request);
        VideoInfo video = response.data();
        Format videoFormat = video.findFormatByItag(userData.getQuality());

        if (videoFormat != null) {
            System.out.println(videoFormat.url());

            try {
                URL url = new URL(videoFormat.url());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                long totalBytes = connection.getContentLengthLong();
                final int[] lastPercent = {0};

                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(msg.getMessageId());
                CountingInputStream progress = new CountingInputStream(inputStream) {
                    @Override
                    protected void afterRead(int bytesRead) {
                        super.afterRead(bytesRead);
                        float percent = 100f * getCount() / totalBytes;

                        if (bytesRead == -1) {
                            editMessageText.setText(video.details().title());
                        } else if ((int) percent != lastPercent[0]) {
                            editMessageText.setText("Загрузка " + (int) percent + "%");
                            lastPercent[0] = (int) percent;
                        }
                        try {
                            execute(editMessageText);
                        } catch (TelegramApiException e) {
                            System.out.println("Ошибка в progress bar: " + e.getMessage());
                        }
                    }
                };

                sendVideo.setVideo(new InputFile(progress, video.details().title() + ".mp4"));

                execute(sendVideo);
            } catch (IOException | TelegramApiException e) {
                sendMessage(chatId, "Ошибка отправки видео, повторите позже");
                e.printStackTrace();
            }
        }
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

    private void clearMessageHistory(Long chatId, int lastMessage) {
        sendMessage(chatId, "Очистка истории ⌛");

        int msgIdClr = 0;
        File msgClr = new File(clearDir);
        if (!msgClr.exists()) {
            try {
                msgClr.createNewFile();
            } catch (IOException e) {
                System.out.println("Не удалось создать файл " + e.getMessage());
            }
        }
        //чтение из файла
        try (Scanner reader = new Scanner(msgClr)){
            if (reader.hasNextInt()) msgIdClr = reader.nextInt();
        } catch (FileNotFoundException e) {
            System.out.println("Не удалось открыть файл " + e.getMessage());
        }

        for (int msgId = msgIdClr; msgId <= lastMessage; msgId++) {
            DeleteMessage deleteMessage = new DeleteMessage(String.valueOf(chatId), msgId);
            try {
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.out.println("Ошибка удаления сообщения " + e.getMessage());
            }
        }
        sendMessage(chatId, "История успешно очищена!✅");
        //перезапись в файл
        try (PrintWriter writer = new PrintWriter(msgClr)) {
            writer.println(lastMessage);
        } catch (IOException e) {
            System.out.println("Не удалось записать в файл " + e.getMessage());
        }

    }
}
