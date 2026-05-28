package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bot")
public class BotInfoController {

    @GetMapping("/status")
    public String getStatus() {
        return "Bot service is running on port 8085!";
    }
}