package com.example.demo.repository;

import com.example.demo.entity.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotUserRepository extends JpaRepository<BotUser, Long> {

    // Этот метод позволит нам искать пользователя по его ID в Телеграме
    Optional<BotUser> findByTelegramId(Long telegramId);

}