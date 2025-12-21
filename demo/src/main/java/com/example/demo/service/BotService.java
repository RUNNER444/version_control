package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BotService extends TelegramLongPollingBot{
    private final NotificationService notificationService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(BotService.class);
    private final String botName;
    private final Long adminChatID;

    public BotService (
    @Value("${telegram.bot.name}")
    String botName,
    @Value("${telegram.bot.token}")
    String botToken,
    @Value("${telegram.bot.chat-id}")
    Long adminChatID,
    @Lazy NotificationService notificationService,
    @Lazy UserService userService) {
        super(botToken);
        this.botName = botName;
        this.adminChatID = adminChatID;
        this.notificationService = notificationService;
        this.userService = userService;
    }
    

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public void onUpdateReceived (Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
        else if (update.hasMessage() && update.getMessage().hasText()) {
            logger.info("Received message: {}", update.getMessage().getText());
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/start")) {
                handleStart(chatId, messageText);
            }
            else {
                sendMessage(chatId, "Unknown message. Send '/start <user_id>' to link your account.");
            }
        }
    }

    public void sendMessage (Long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        
        try {
            execute(message);
            logger.info("Sent message to chat {}", chatId);
        }
        catch (TelegramApiException e) {
            logger.error("Failed to send message", e);
        }
    }

    public void sendToAdmin (String text) {
        sendMessage(adminChatID, text);
        logger.info("Sent message to admin");
    }

    private void handleCallback (CallbackQuery query) {
        String[] data = query.getData().split(":");
        Long notificationId = Long.parseLong(data[1]);
        int messageId = query.getMessage().getMessageId();
        Long chatId = query.getMessage().getChatId();

        try {
            if (data[0].equals("READ")) {
                notificationService.markAsRead(notificationId);
                editMessage(chatId, messageId, 
                    "Marked as read: " + notificationId);
            }
            else if (data[0].equals("DISMISS")) {
                notificationService.markAsDismissed(notificationId);
                editMessage(chatId, messageId, 
                    "Marked as dismissed: " + notificationId);
            }

            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(query.getId());
            execute(answer);
            logger.info("Callback {} handled", query.getId());
        }
        catch (EntityNotFoundException e) {
            logger.error("Notification not found: {}", notificationId);
            sendMessage(chatId, "Error: notification not found");
        }
        catch (Exception e) {
            logger.error("Error handling callback", e);
        }
    }

    private void editMessage (Long chatId, int messageId, String newText) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(chatId); newMessage.setMessageId(messageId); newMessage.setText(newText);

        try {
            execute(newMessage);
        }
        catch (TelegramApiException e) {
            logger.error("Failed to edit message", e);
        }
    }

    public void sendNotificationWithButtons (Long chatId, String text, Long notificationId) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);

        List <List <InlineKeyboardButton>> rows = new ArrayList<>();
        List <InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton readButton = new InlineKeyboardButton("Read");
        readButton.setCallbackData("READ:" + notificationId);
        InlineKeyboardButton dismissButton = new InlineKeyboardButton("Dismiss");
        dismissButton.setCallbackData("DISMISS:" + notificationId);
        row.add(readButton); row.add(dismissButton);
        rows.add(row);
        message.setReplyMarkup(new InlineKeyboardMarkup(rows));

        try {
            execute(message);
            logger.info("Notification sent to chat: {}", chatId);
        }
        catch (TelegramApiException e) {
            logger.error("Failed to send notification", e);
        }
    }

    public void handleStart (Long chatId, String text) {
        String[] data = text.split(" ");

        if (data.length != 2) {
            sendMessage(chatId, "Invalid format. Use: /start <your_user_id>");
            return;
        }

        try {
            Long userId = Long.parseLong(data[1]);
            userService.updateTelegramId(userId, chatId);
            sendMessage(chatId, "Successfully linked your account to user " + userId);
        }
        catch (Exception e) {
            sendMessage(chatId, "An error occurred while linking");
            logger.error("Linking error", e);
        }
    }
}
