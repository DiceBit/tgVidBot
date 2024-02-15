package com.youtubebot.downloadvidfromytbot.bot;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoWithAudioFormat;
import com.youtubebot.downloadvidfromytbot.Configuration.Buttons.BotCommands;
import com.youtubebot.downloadvidfromytbot.Domain.Model.UserData;
import com.youtubebot.downloadvidfromytbot.Domain.Repository.UserRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

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


    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        var msg = update.getMessage().getText();
        var message = msg.startsWith("/") ? msg.split(" ")[0] : msg;
        var chatId = update.getMessage().getChatId();
        var user = update.getMessage().getFrom();

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
                            1
                    );
                    userRepository.save(userData);
                }

                var userName = update.getMessage().getChat().getFirstName();

                var formattedText = String.format(START_TEXT, userName);
                sendMessage(chatId, formattedText);
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
                downloadVideo(videoId);
                
                //String videoTitle = downloadYouTubeVideo(url);
                //sendFile(chatId, videoTitle);
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
                sendMessage(chatId, "Такой комманды нет, воспользуйтесь коммандой /help для получения справки");
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

        var chadIdStr = String.valueOf(chatId);
        var sendVideo = new SendDocument();

        sendVideo.setChatId(chadIdStr);
        sendVideo.setDocument(new InputFile(new File(downloadDir + "/" + fileName), fileName));


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

    @Override
    public String getBotUsername() {
        return "VidDownloadBot";
    }

    // TODO: 13-Feb-24 DELETE
    private String downloadYouTubeVideo(String vidUrl) {

        final String siteUrl = "https://ssyoutube.com/ru90xs/";

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(siteUrl);

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_url")));
        input.sendKeys(vidUrl);
        input.sendKeys(Keys.ENTER);

        WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("download-mp4-720-audio")));
        button.click();

        WebElement titleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"convert-result\"]/div[2]/div/div[1]/h6")));
        String videoTitle = titleElement.getText();
        String filename = videoTitle + ".mp4";

        waitForDownload(downloadDir, filename);

        driver.quit();

        return filename;
    }

    public void waitForDownload(String downloadDir, String filename) {

        Path downloadPath = Paths.get(downloadDir, filename);
        while (!Files.exists(downloadPath)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Error waiting for download: " + e.getMessage());
            }
        }
        long fileSize = -1;
        while (true) {
            long newFileSize = new File(downloadDir, filename).length();
            if (newFileSize == fileSize) {
                break;
            }
            fileSize = newFileSize;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Error waiting for download: " + e.getMessage());
            }
        }
    }

    private void downloadVideo(String videoId) {
        //4vZYnQcM070 - new vid
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

        List<VideoWithAudioFormat> videoWithAudioFormatList = video.videoWithAudioFormats();
        /*videoWithAudioFormatList.forEach(it -> {
            System.out.println(it.audioQuality() + ", " + it.videoQuality() + " : " + it.url());
        });*/

        //Formats = https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2
        Format videoFormat = video.findFormatByItag(18);
        if (videoFormat != null) {
            System.out.println(videoFormat.url());
        }

        RequestVideoFileDownload download = new RequestVideoFileDownload(videoFormat)
                .saveTo(outputDir)
                .renameTo(details.title() + ".mp4")
                .overwriteIfExists(true);
        Response<File> fileResponse = downloader.downloadVideoFile(download);
        File data = fileResponse.data();



    }
}
