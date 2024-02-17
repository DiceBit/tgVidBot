package com.youtubebot.downloadvidfromytbot.Domain.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "usr")
public class UserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String languageCode;
    private int quality;

    private String chatId;
    private int firstMsg;

    public UserData(Long userId,
                    String userName, String firstName, String lastName,
                    String languageCode, String chatId, int firstMsg, int quality) {

        this.userId = userId;
        this.userName = "@"+userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.languageCode = languageCode;
        this.chatId = chatId;
        this.firstMsg = firstMsg;
        this.quality = quality;
    }
}
