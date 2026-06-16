package com.brewledger.brewledger.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NotificationService {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.chat.id:}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sends a notification message. If Telegram is configured in .env, sends it to Telegram.
     * Always fallbacks to logging to console.
     */
    public void sendAlert(String message) {
        log.info("[NOTIFICATION ALERT] {}", message);

        if (botToken != null && !botToken.trim().isEmpty() && chatId != null && !chatId.trim().isEmpty()) {
            try {
                String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
                Map<String, String> request = new HashMap<>();
                request.put("chat_id", chatId);
                request.put("text", "🚨 *BrewLedger Alert* 🚨\n\n" + message);
                request.put("parse_mode", "Markdown");

                restTemplate.postForObject(url, request, String.class);
                log.info("Telegram notification sent successfully.");
            } catch (Exception e) {
                log.error("Failed to send Telegram notification: {}", e.getMessage());
            }
        } else {
            log.debug("Telegram credentials not set. Skipping Telegram notification.");
        }
    }
}
