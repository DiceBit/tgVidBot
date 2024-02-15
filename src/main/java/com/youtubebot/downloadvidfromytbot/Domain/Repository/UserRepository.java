package com.youtubebot.downloadvidfromytbot.Domain.Repository;

import com.youtubebot.downloadvidfromytbot.Domain.Model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserData, Long> {
    UserData findByChatId(String chatId);
    UserData findByUserId(Long userId);
}
