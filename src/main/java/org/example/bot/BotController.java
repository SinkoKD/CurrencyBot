package org.example.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendVideo;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pengrad.telegrambot.model.request.ParseMode.HTML;
import static org.example.bot.JedisActions.*;


public class BotController {

    public static JedisPool jedisPool;
    public static final String USER_DB_MAP_KEY = "userDBMap";
    public static TelegramBot bot;
    private static final Logger logger = Logger.getLogger(BotController.class.getName());

    public static void main(String[] args) throws URISyntaxException {
        String TOKEN = "";
        String AdminID = "5674163986";
        try {
            String configFilePath = "src/config.properties";
            FileInputStream propsInput = new FileInputStream(configFilePath);
            Properties prop = new Properties();
            prop.load(propsInput);
            TOKEN = prop.getProperty("TOKEN");

        } catch (IOException e) {
            logger.severe("An error occurred in your method or class: " + e.getMessage());
        }
        // String redisUriString = "redis://localhost:6379";
        String redisUriString = System.getenv("REDIS_URL");
        jedisPool = new JedisPool(new URI(redisUriString));

        bot = new TelegramBot(TOKEN);

        bot.setUpdatesListener(updates -> {
            try (Jedis jedis = jedisPool.getResource()) {
                updates.forEach(update -> {
                    String playerName = "Trader";
                    long playerId;
                    String messageText = "";
                    String messageCallbackText = "";
                    String uid;
                    int messageId;
                    Path resourcePath = Paths.get("src/main/resources");
                    File videoDepositFile = resourcePath.resolve("depositTutorial.mp4").toFile();
                    File videoRegistrationFile = resourcePath.resolve("videoRegistrationGuide.mp4").toFile();
                    File videoExampleFile = resourcePath.resolve("videoExample.mp4").toFile();


//                    Date ytDate = new Date();
//                    User admin = new User("Admin", "15", true, true, ytDate, ytDate, 1, true, true, true);
//                    jedis.set(AdminID, convertUserToJson(admin));

                    if (update.callbackQuery() == null && (update.message() == null || update.message().text() == null)) {
                        return;
                    }

                    if (update.callbackQuery() == null) {
                        playerName = update.message().from().firstName();
                        playerId = update.message().from().id();
                        messageText = update.message().text();
                        messageId = update.message().messageId();
                    } else if (update.message() == null) {
                        playerName = update.callbackQuery().from().firstName();
                        playerId = update.callbackQuery().from().id();
                        messageCallbackText = update.callbackQuery().data();
                        messageId = update.callbackQuery().message().messageId();
                    } else {
                        messageId = 0;
                        playerId = 0L;
                    }

                    if (playerId != Long.parseLong(AdminID)) {
                        try {
                            String userKey = USER_DB_MAP_KEY + ":" + playerId;
                            User checkedUser = convertJsonToUser(jedis.get(userKey));
                            Date date = new Date();
                            checkedUser.setLastTimeTexted(date);
                            String updatedUser = convertUserToJson(checkedUser);
                            jedis.set(userKey, updatedUser);
                        } catch (Exception e) {
                            logger.severe("An error occurred in your method or class: " + e.getMessage());
                        }
                    }

                    try {
                        User checkedAdmin = convertJsonToUser(jedis.get(AdminID));
                        Date currentDate = new Date();
                        Date checkAdminDate = DateUtil.addDays(checkedAdmin.getLastTimeTexted(), 2);
                        System.out.println("Im not there");
                        if (checkAdminDate.getTime() < currentDate.getTime()) {
                            System.out.println("Im there");
                            checkedAdmin.setLastTimeTexted(currentDate);
                            jedis.set(AdminID, convertUserToJson(checkedAdmin));
                            System.out.println("Admin done");
                            Set<String> userKeys = jedis.keys("userDBMap:*");
                            System.out.println("Keys done");
                            System.out.println(userKeys.size());
                            for (String keyForUser : userKeys) {
                                User currentUser = convertJsonToUser(jedis.get(keyForUser));
                                if (currentUser.getLastTimeTexted() != null && currentUser.getTimesTextWasSent() != 0) {
                                    Date checkUserDate = DateUtil.addDays(currentUser.getLastTimeTexted(), 1);
                                    if (checkUserDate.getTime() < currentDate.getTime()) {
                                        String userTgID = keyForUser.substring(10);
                                        if (currentUser.isDeposited() && currentUser.getTimesTextWasSent() == 1) {
                                            bot.execute(new SendMessage(userTgID, "\uD83C\uDFC6 For new users, there's an opportunity to upgrade signal accuracy to 94%. Use the command /upgrade. \uD83C\uDFAF").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (currentUser.isDeposited() && currentUser.getTimesTextWasSent() == 2) {
                                            bot.execute(new SendMessage(userTgID, "\uD83C\uDF81 90% of users already earn more in the first day than they paid for upgrading my version. To do this now, use the command /upgrade. ✅").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (currentUser.isDeposited() && currentUser.getTimesTextWasSent() == 3) {
                                            bot.execute(new SendMessage(userTgID, "\uD83C\uDFC6 For new users, there's an opportunity to upgrade signal accuracy to 94%. Use the command /upgrade. \uD83C\uDFAF").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (currentUser.isRegistered() && currentUser.getTimesTextWasSent() == 1) {
                                            bot.execute(new SendMessage(userTgID, "\uD83D\uDD14 The final step to receiving signals is left! Everything can be done quickly and conveniently for you! If you encounter any issues while depositing, please review the video above. Also use promo code 50START to receive bonus to your deposit.").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (currentUser.isRegistered() && currentUser.getTimesTextWasSent() == 2) {
                                            bot.execute(new SendMessage(userTgID, "\uD83D\uDD14 It seems you still don't want to start earning. After depositing, you will gain access to my accurate signals. I'm not human, but my analysis indicates that you're making a mistake by not working with me.").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (!currentUser.isRegistered() && currentUser.getTimesTextWasSent() == 1) {
                                            bot.execute(new SendMessage(userTgID, "\uD83D\uDD14 I want to remind you that for registration, you need to create a new account using this link: https://bit.ly/ChatGPTtrading. It won't take more than 2 minutes. You can also review the video above, it should help you. I'm ready to give you my signals.").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        } else if (!currentUser.isRegistered() && currentUser.getTimesTextWasSent() == 2) {
                                            bot.execute(new SendMessage(userTgID, "\uD83D\uDD14 I want to remind you that signing up doesn't require much time! Just make a new account using this link: https://bit.ly/ChatGPTtrading. (This is the final reminder, if you don't manage to create an account within the next 3 days, you won't get access to my signals)").parseMode(HTML));
                                            increaseTimesWasSent(keyForUser);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.severe("An error occurred in your method or class: " + e.getMessage());
                    }

                    if (String.valueOf(playerId).equals(AdminID)) {
                        if (messageText.startsWith("A") || messageText.startsWith("a") || messageText.startsWith("Ф") || messageText.startsWith("ф")) {
                            try {
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                InlineKeyboardButton button7 = new InlineKeyboardButton("Deposit done!");
                                button7.callbackData("IDeposit");
                                inlineKeyboardMarkup.addRow(button7);
                                System.out.println(messageText.length());
                                String tgID = messageText.substring(1);
                                System.out.println(tgID);
                                String TGId = USER_DB_MAP_KEY + ":" + tgID;
                                User userBanned = convertJsonToUser(jedis.get(TGId));
                                Date currentDate = new Date();
                                userBanned.setLastTimePressedDeposit(DateUtil.addMinutes(currentDate, 3));
                                String updatedBannedUser = convertUserToJson(userBanned);
                                jedis.set(TGId, updatedBannedUser);
                                registrationApprove(Long.parseLong(tgID));
                                registrationApprove(Long.parseLong(tgID));
                                bot.execute(new SendMessage(tgID, "✅ Great, your account is confirmed! The last step is to " +
                                        "make any deposit at least 50$ by any convenient way. After that press the button 'Deposit done'.\n" +
                                        "\n" +
                                        "I would like to note that the recommended starting deposit of $50 - $350. " +
                                        "Also use promo code 50START to get an extra 50% of your deposit. " +
                                        "For example, with a deposit of 100$ you will get 50$ additional. It means that you will get 150$ in total.\n" +
                                        "\n" +
                                        "At the bottom there is a video instruction on how to top up the account.").replyMarkup(inlineKeyboardMarkup));
                                bot.execute(new SendVideo(tgID, videoDepositFile));
                                bot.execute(new SendMessage(tgID, "☝️ Here is a video guide on how to make a deposit.").parseMode(HTML));
                                bot.execute(new SendMessage(AdminID, "Registration for " + tgID + " was approved"));
                                setTo1TimesWasSent(tgID);
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please ЙЙЙЙЙЙ "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("reply:") || messageText.startsWith("куздн:")) {
                            int indexOfAnd = messageText.indexOf("&");
                            String tgID = messageText.substring(6, indexOfAnd);
                            String reply = messageText.substring(indexOfAnd + 1);
                            System.out.println(indexOfAnd + "\n" + tgID + "\n" + reply);
                            bot.execute(new SendMessage(tgID, reply));
                            bot.execute(new SendMessage(AdminID, "Reply was sent"));
                        } else if (messageText.startsWith("deleteUser:")) {
                            try {
                                String TGId = USER_DB_MAP_KEY + ":" + (messageText.substring(11));
                                jedis.del(TGId);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " was fully deleted"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("banSupport:")) {
                            try {
                                String TGId = USER_DB_MAP_KEY + ":" + (messageText.substring(11));
                                User userBanned = convertJsonToUser(jedis.get(TGId));
                                userBanned.setCanWriteToSupport(true);
                                String updatedBannedUser = convertUserToJson(userBanned);
                                jedis.set(TGId, updatedBannedUser);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " was banned to write to support"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("banDeposit30:")) {
                            try {
                                String TGId = USER_DB_MAP_KEY + ":" + (messageText.substring(13));
                                User userBanned = convertJsonToUser(jedis.get(TGId));
                                Date currentDate = new Date();
                                userBanned.setLastTimePressedDeposit(DateUtil.addMinutes(currentDate, 30));
                                String updatedBannedUser = convertUserToJson(userBanned);
                                jedis.set(TGId, updatedBannedUser);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " was banned to press button 'Deposit done' for 30 minutes. "));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("giveV4:")) {
                            try {
                                String TGId = USER_DB_MAP_KEY + ":" + (messageText.substring(7));
                                User userUpdated = convertJsonToUser(jedis.get(TGId));
                                userUpdated.setMinimumPercent(70);
                                String updatedUser = convertUserToJson(userUpdated);
                                jedis.set(TGId, updatedUser);
                                bot.execute(new SendMessage(AdminID, "User with ID " + messageText.substring(7) + " now has V 4!"));
                                bot.execute(new SendMessage(messageText.substring(7), "✅ Congratulations! Now I've upgraded and am using version 4!"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("giveV4.5:")) {
                            try {
                                String TGId = USER_DB_MAP_KEY + ":" + (messageText.substring(9));
                                User userUpdated = convertJsonToUser(jedis.get(TGId));
                                userUpdated.setMinimumPercent(90);
                                String updatedUser = convertUserToJson(userUpdated);
                                jedis.set(TGId, updatedUser);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " now has V 4.5!"));
                                bot.execute(new SendMessage(messageText.substring(9), "✅ Congratulations! Now I've upgraded and am using version 4.5!"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("deleteDeposit:")) {
                            try {
                                String TGId = (messageText.substring(14));
                                depositDisapprove(Long.parseLong(TGId));
                                System.out.println(TGId);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " got deleted"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("deleteRegistration:")) {
                            try {
                                String TGId = (messageText.substring(19));
                                registrationDisapprove(Long.parseLong(TGId));
                                System.out.println(TGId);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " got register disapprove"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("getUserName:")) {
                            try {
                                String TGId = USER_DB_MAP_KEY + ":" + (messageText.substring(12));
                                User newUser = convertJsonToUser(jedis.get(TGId));
                                bot.execute(new SendMessage(AdminID, "Name of user is: " + newUser.getName() + " his TG id: " + TGId));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("setCheckForUID:")) {
                            try {
                                long newCheck = Integer.parseInt(messageText.substring(15));
                                User adminUser = convertJsonToUser(jedis.get(AdminID));
                                adminUser.setUID(String.valueOf(newCheck));
                                String updatedAdminUser = convertUserToJson(adminUser);
                                jedis.set(AdminID, updatedAdminUser);
                                bot.execute(new SendMessage(AdminID, "First numbers is: " + newCheck + "."));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("createNewPost:")) {
                            try {
                                String postText = messageText.substring(14);
                                Set<String> userKeys = jedis.keys("userDBMap:*");
                                System.out.println("Amount of users: " + userKeys.size());
                                for (String keyForUser : userKeys) {
                                    String userTgID = keyForUser.substring(10);
                                    bot.execute(new SendMessage(userTgID, postText));
                                }
                                bot.execute(new SendMessage(AdminID, "The message " + postText + " has been sent."));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("D") || messageText.startsWith("d") || messageText.startsWith("В") || messageText.startsWith("в")) {
                            String tgID = messageText.substring(1);
                            InlineKeyboardButton button12 = new InlineKeyboardButton("Register here");
                            InlineKeyboardButton button13 = new InlineKeyboardButton("I'm ready!");
                            button12.url("https://bit.ly/ChatGPTtrading");
                            button13.callbackData("ImRegistered");
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            inlineKeyboardMarkup.addRow(button12, button13);
                            bot.execute(new SendMessage(tgID, "❌ Something went wrong. Make sure you registered with the 'Register here' button and sent a new UID. There is an example of how to do it step by step in the video below. After that press the 'I'm ready!'\n" +
                                    "\n" +
                                    "If you still have problems, then write to support with the command /support. ").replyMarkup(inlineKeyboardMarkup));
                            bot.execute(new SendMessage(AdminID, "Registration for " + tgID + " was disapproved"));
                        } else if (messageText.startsWith("Y") || messageText.startsWith("y") || messageText.startsWith("Н") || messageText.startsWith("н")) {
                            try {
                                String tgID = messageText.substring(1);
                                depositApprove(Long.parseLong(tgID));
                                depositApprove(Long.parseLong(tgID));
                                Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                        "Get Signal");
                                bot.execute(new SendMessage(AdminID, "Deposit for " + tgID + " was approved"));
                                bot.execute(new SendMessage(tgID, "✅ Great! Everything is ready! You can start getting signals. For this click on 'Get Signal' or write it manually. \n" +
                                        "\n" +
                                        "<b>❗️IMPORTANT ❗️</b> \n\n" +
                                        "<i>1⃣ I am analyzing only the real market, so I won't work on a demo properly. To achieve better accuracy, trade on a real account.\n\n" +
                                        "2⃣ I analyze all successful and failed signals. The more signals you get, the better they become.\n\n" +
                                        "3⃣ The recommended amount to use for trading is 15-20% per trade.</i>\n\n" +
                                        "Below is a video guide on how to use signals from me. \n" + "\n" +
                                        "If you have any questions use the /support command.").parseMode(HTML).replyMarkup(replyKeyboardMarkup));
                                setTo1TimesWasSent(tgID);
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("N") || messageText.startsWith("n") || messageText.startsWith("Т") || messageText.startsWith("т")) {
                            String tgID = messageText.substring(1);
                            String TGId = USER_DB_MAP_KEY + ":" + tgID;
                            User userBanned = convertJsonToUser(jedis.get(TGId));
                            Date currentDate = new Date();
                            userBanned.setLastTimePressedDeposit(DateUtil.addMinutes(currentDate, 3));
                            String updatedBannedUser = convertUserToJson(userBanned);
                            jedis.set(TGId, updatedBannedUser);
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            InlineKeyboardButton button7 = new InlineKeyboardButton("Deposit done");
                            button7.callbackData("IDeposit");
                            inlineKeyboardMarkup.addRow(button7);
                            bot.execute(new SendMessage(tgID, "❌ Something went wrong. Make sure you deposited at least 50$ to the new account you created through the link and then click 'Deposit done' ").replyMarkup(inlineKeyboardMarkup));
                            bot.execute(new SendMessage(AdminID, "Deposit for " + tgID + " was disapproved"));
                        }
                    } else if (messageText.startsWith("needReply:")) {
                        String userQuestion = messageText.substring(10);
                        if (!userCanWriteToSupport(playerId)) {
                            bot.execute(new SendMessage(playerId, "✅ I received your message and will respond to you as soon as possible. Your message: " + userQuestion).parseMode(HTML));
                            bot.execute(new SendMessage(AdminID, "✅ ID:<code>" + playerId + "</code> has a question" + userQuestion + " To answer it write a message: <code>reply:111111111&</code> *your text*").parseMode(HTML));
                            System.out.println("Really works");
                        } else {
                            bot.execute(new SendMessage(playerId, "❌ Something went wrong. The support is not available. Try once again later. ").parseMode(HTML));
                        }
                    } else if (messageText.equals("/support") || messageCallbackText.equals("Help")) {
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        InlineKeyboardButton button52 = new InlineKeyboardButton("FAQ");
                        InlineKeyboardButton button53 = new InlineKeyboardButton("Need reply!");
                        button52.callbackData("FAQ");
                        button53.callbackData("Answer");
                        inlineKeyboardMarkup.addRow(button52, button53);
                        bot.execute(new SendMessage(playerId, "⏳ You reached out to support. Please make sure to watch the video guides I sent you earlier before reaching out directly. You can review the most frequently asked questions by clicking on the 'FAQ' button, or you can message me directly by clicking on the 'Need reply!' button.").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                    } else if (messageCallbackText.equals("FAQ")) {
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        InlineKeyboardButton button32 = new InlineKeyboardButton("Return back");
                        button32.callbackData("Help");
                        inlineKeyboardMarkup.addRow(button32);
                        bot.execute(new SendMessage(playerId, "1. <b>How to Trade and Earn with the Bot</b>\n" +
                                "\n" +
                                "Thank you for your interest in the ChatGPT bot! To understand how the bot works, please watch our instructional video.\n" +
                                "\n" +
                                "2. <b>What's the Best Starting Amount?</b>\n" +
                                "\n" +
                                "Traders who begin with $50 to $350 often achieve the most success.\n" +
                                "\n" +
                                "3. <b>Can I Start with $50?</b>\n" +
                                "\n" +
                                "Yes, you can, but it's better to start with a larger amount.\n" +
                                "\n" +
                                "4. <b>How Reliable is the Bot?</b>\n" +
                                "\n" +
                                "The bot learns from mistakes and victories, so its reliability increases over time.\n" +
                                "\n" +
                                "5. <b>How Much Can I Earn with $100?</b>\n" +
                                "\n" +
                                "Earnings depend on your stake amount and how often you use the bot. I recommend using 25% of your deposit to achieve good profits. On average, users who follow my advice earn $800 to $3000 per week.\n" +
                                "\n" +
                                "6. <b>What are the Bot's Advantages?</b>\n" +
                                "\n" +
                                "The bot employs the latest AI version, providing the most accurate signals compared to other bots. You can use it whenever you want by entering the relevant command.\n" +
                                "\n" +
                                "7. <b>How to Find My ID</b>\n" +
                                "\n" +
                                "In the trading profile section, you'll find your UID.\n" +
                                "\n" +
                                "8. <b>Is Sharing My UID Safe?</b>\n" +
                                "\n" +
                                "Absolutely. Your User Identification or Account ID (UID) is a unique digital identifier on the platform. It doesn't contain personal information and can't be used to access your account. You can share your account ID with other traders so they can find you in chats, ratings, and social trading.\n" +
                                "\n" +
                                "9. <b>How Can I Get a Deposit Bonus? (Promo Code)</b>\n" +
                                "\n" +
                                "Promo codes: 50START (works from $50) and WELCOME50 (works from $100).\n" +
                                "\n" +
                                "10. <b>What's the Signal Accuracy?</b>\n" +
                                "\n" +
                                "I search for signals with an accuracy of over 70%.").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                    } else if (messageCallbackText.equals("Answer")) {
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        InlineKeyboardButton button32 = new InlineKeyboardButton("Return back");
                        button32.callbackData("Help");
                        inlineKeyboardMarkup.addRow(button32);
                        bot.execute(new SendMessage(playerId, "If you don't find an answer to your question or if you have a different request, please send a message in the format:\n<code>needReply:</code> *your text*. \n" +
                                "Please do this in one message, and I'll get back to you as soon as possible.").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                    }  else if (messageCallbackText.equals("card")) {
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        InlineKeyboardButton button8 = new InlineKeyboardButton("Version 4 - 60$");
                        InlineKeyboardButton button9 = new InlineKeyboardButton("Version 4.5 - 150$");
                        button8.callbackData("card60");
                        button9.callbackData("card150");
                        inlineKeyboardMarkup.addRow(button9);
                        inlineKeyboardMarkup.addRow(button8);
                        bot.execute(new SendMessage(playerId, "✨ Choose the version of the bot you want to get!").parseMode(HTML).replyMarkup(inlineKeyboardMarkup));

                    } else if (messageCallbackText.equals("card60")) {
                        sendFirstCardInstruction(60,playerId);
                    } else if (messageCallbackText.equals("card150")) {
                        sendFirstCardInstruction(150,playerId);
                    } else if (messageCallbackText.equals("pay60")) {
                        sendSecondCardInstruction(60,playerId);
                    } else if (messageCallbackText.equals("pay150")) {
                        sendSecondCardInstruction(150,playerId);
                    } else if (messageText.equals("/start")) {
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        InlineKeyboardButton button32 = new InlineKeyboardButton("Let's start");
                        button32.callbackData("RegisterMe");
                        inlineKeyboardMarkup.addRow(button32);
                        bot.execute(new SendMessage(playerId, "\uD83D\uDC4B Hi, " + playerName + "\n" +
                                "\n" +
                                "\uD83E\uDD16 I'm Chat GPT bot for binary options trading and I am based on the latest technology. I'm analyzing brokers using artificial intelligence. That's why my signals are highly accurate and I can analyze the market in real time at your request. All you have to do is copy it! \uD83D\uDCC8 \n" +
                                "\n" +
                                "\uD83D\uDCCA In order to start receiving signals you need to follow a couple of steps. In the beginning, click on 'Let's start'.\n" +
                                "\n" +
                                "❗️ If you have any problems or suggestions, you can contact bot support via the /support command.").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                        bot.execute(new SendVideo(playerId, videoExampleFile));
                        bot.execute(new SendMessage(playerId, "☝️ Here is a video example of how I work.").parseMode(HTML));

                    } else if (userDeposited(playerId) || userDeposited(playerId)) {
                        if (messageText.equals("Get Signal") || messageCallbackText.equals("getSignal") || messageText.equals("/getsignal") || messageText.equals("Get signal") || messageText.equals("get signal")) {
                            List<String> listOfPairs = Arrays.asList(
                                    "AUD/CAD OTC", "AUD/CHF OTC", "AUD/NZD OTC", "CAD/CHF OTC", "EUR/CHF OTC",
                                    "EUR/JPY OTC", "EUR/USD OTC", "GBP/JPY OTC", "NZD/JPY OTC", "NZD/USD OTC",
                                    "USD/CAD OTC", "USD/CNH OTC", "CHF/NOK OTC", "EUR/GBP OTC", "EUR/TRY OTC",
                                    "CHF/JPY OTC", "EUR/NZD OTC", "AUD/JPY OTC", "AUD/USD OTC", "EUR/HUF OTC",
                                    "USD/CHF OTC"
                            );
                            Runnable signalGeneratorTask = () -> {
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                User currentUser = convertJsonToUser(jedis.get(userKey));
                                int minPercent = 0;
                                try {
                                    minPercent = currentUser.getMinimumPercent();
                                } catch (Exception e) {
                                    currentUser.setMinimumPercent(0);
                                    jedis.set(userKey, convertUserToJson(currentUser));
                                    bot.execute(new SendMessage(AdminID, "❌ There was an issue. Please try again. "));
                                    logger.severe("An error occurred in your method or class: " + e.getMessage());
                                }
                                bot.execute(new SendMessage(playerId, "⌛️ Looking for the best pair...").parseMode(HTML));
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                                    logger.severe("An error occurred in your method or class: " + e.getMessage());
                                }
                                Random random = new Random();
                                int randomNumber = random.nextInt(listOfPairs.size());
                                int randomUp = random.nextInt(2);
                                String direction;
                                if (randomUp == 0) {
                                    direction = "⬆️ Direction: <b>UP</b> ";
                                } else {
                                    direction = "⬇️ Direction: <b>DOWN</b> ";
                                }
                                int randomAccuracy = random.nextInt(28) + 70;
                                if (minPercent == 70) {
                                    randomAccuracy = random.nextInt(20) + 80;
                                } else if (minPercent == 90) {
                                    randomAccuracy = random.nextInt(6) + 94;
                                }
                                int randomAddTime = random.nextInt(10000) + 8000;
                                int randomTime = random.nextInt(3) + 1;
                                String pickedPair = listOfPairs.get(randomNumber);
                                EditMessageText editMessageText = new EditMessageText(playerId, messageId + 1, "Pair <b>" + pickedPair + "</b> has been picked. I am conducting an analysis on it.").parseMode(HTML);
                                bot.execute(editMessageText);
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                                    logger.severe("An error occurred in your method or class: " + e.getMessage());
                                }
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                InlineKeyboardButton button22 = new InlineKeyboardButton("Get new signal");
                                button22.callbackData("getSignal");
                                inlineKeyboardMarkup.addRow(button22);
                                EditMessageText editMessage = new EditMessageText(playerId, messageId + 1, "Your signal is: \n\uD83D\uDCB0 <b>" + pickedPair + "</b>\n" + direction + "\n⌛️ Time for deal:<b> " + randomTime + "M </b>\n\uD83C\uDFAF Accuracy calculated:<b> " + randomAccuracy + "%</b>\n⚡️ Wait for a message '<b>GO!</b>' and then make forecast.").parseMode(HTML);
                                bot.execute(editMessage);
                                try {
                                    Thread.sleep(randomAddTime);
                                } catch (InterruptedException e) {
                                    logger.severe("An error occurred in your method or class: " + e.getMessage());
                                }
                                Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(
                                        new String[]{"Get Signal"});
                                bot.execute(new SendMessage(playerId, "<b>GO!</b>").parseMode(HTML).replyMarkup(replyKeyboardMarkup));
                            };
                            new Thread(signalGeneratorTask).start();
                        } else if (messageText.equals("/upgrade")) {
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

                            InlineKeyboardButton button8 = new InlineKeyboardButton("Pay with crypto");
                            InlineKeyboardButton button9 = new InlineKeyboardButton("Pay with bank card");
                            button8.callbackData("crypto");
                            button9.callbackData("card");
                            inlineKeyboardMarkup.addRow(button9);
                            inlineKeyboardMarkup.addRow(button8);
                            bot.execute(new SendMessage(playerId, "\uD83C\uDF89 Exciting news! We've got two new versions " +
                                    "ready to make the signal even better, up to 99% accuracy! \uD83C\uDFAF\n" +
                                    "\n" +
                                    "\uD83C\uDFC6 Enjoy special holiday discounts:\n" +
                                    "\n" +
                                    "\uD83D\uDFE2 ChatGPT Version 4.5 - Was $200, now just $150! Achieve 94%+ accuracy. \uD83E\uDD2F <b>Most popular</b>.\n" +
                                    "\n" +
                                    "\uD83D\uDFE1 ChatGPT Version 4 - Was $100, now only $60! Get 80%+ accuracy. \n" +
                                    "\n" +
                                    "\uD83D\uDEA8 Don't miss out! The chance to upgrade your bot is limited. ⏳").parseMode(HTML).replyMarkup(inlineKeyboardMarkup));
                        } else if (messageCallbackText.equals("crypto")) {
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            InlineKeyboardButton button8 = new InlineKeyboardButton("Version 4 - 60$");
                            InlineKeyboardButton button9 = new InlineKeyboardButton("Version 4.5 - 150$");
                            button8.callbackData("gl");
                            button9.callbackData("pl");
                            inlineKeyboardMarkup.addRow(button9);
                            inlineKeyboardMarkup.addRow(button8);
                            bot.execute(new SendMessage(playerId, "✨ Choose the version of the bot you want to get!").parseMode(HTML).replyMarkup(inlineKeyboardMarkup));
                        } else if (messageCallbackText.equals("gl")) {
                            try {
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                User currentUser = convertJsonToUser(jedis.get(userKey));
                                System.out.println(currentUser.getMinimumPercent());
                                if (currentUser.getMinimumPercent() == 90) {
                                    bot.execute(new SendMessage(playerId, "<b>\uD83D\uDFE2 You shouldn't pick lower plan.</b>").parseMode(HTML));
                                } else {
                                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                    InlineKeyboardButton button22 = new InlineKeyboardButton("Next!");
                                    button22.callbackData("Next");
                                    inlineKeyboardMarkup.addRow(button22);
                                    bot.execute(new SendMessage(playerId, "Now, please use the details below to make a $60 payment for the bot upgrade using your preferred method. \uD83D\uDCB3\uD83D\uDCB5\n\n" +
                                            "Payment Details:\n\nUSDT TRC20 <code>TVqr4evixik9qDcq4x4by5DtkhDaGQszo3</code>\n\n" +
                                            "BTC <code>3Ngts7pzw6JF9VBo7zpgKbx1XT7SLBUvRC</code>\n\n" +
                                            "ETH ERC20 <code>0x475b2e0849a1b88fc10d963621404c055ff6002c</code>\n\n" +
                                            "<i>Important! Please consider any transaction fees, if the amount " +
                                            "received is less than the required sum, the version won't be updated! " +
                                            "</i>\n\n \uD83D\uDE0A\uD83D\uDCB3\uD83D\uDE80 After making the payment, " +
                                            "click the 'Next!' button.").parseMode(HTML).replyMarkup(inlineKeyboardMarkup));
                                }
                            } catch (Exception e) {
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                User currentUser = convertJsonToUser(jedis.get(userKey));
                                currentUser.setMinimumPercent(0);
                                jedis.set(userKey, convertUserToJson(currentUser));
                                bot.execute(new SendMessage(playerId, "❌ There was an issue. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageCallbackText.equals("pl")) {
                            try {
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                InlineKeyboardButton button22 = new InlineKeyboardButton("Next!");
                                button22.callbackData("Next");
                                inlineKeyboardMarkup.addRow(button22);
                                bot.execute(new SendMessage(playerId, "Now, please use the details below to make a $150 payment for the bot upgrade using your preferred method. \uD83D\uDCB3\uD83D\uDCB5\n\n" +
                                        "Payment Details:\n\nUSDT TRC20 <code>TVqr4evixik9qDcq4x4by5DtkhDaGQszo3</code>\n\n" +
                                        "BTC <code>3Ngts7pzw6JF9VBo7zpgKbx1XT7SLBUvRC</code>\n\n" +
                                        "ETH ERC20 <code>0x475b2e0849a1b88fc10d963621404c055ff6002c</code>\n\n" +
                                        "<i>Important! Please consider any transaction fees, if the amount " +
                                        "received is less than the required sum, the version won't be updated! " +
                                        "</i>\n\n \uD83D\uDE0A\uD83D\uDCB3\uD83D\uDE80 After making the payment, " +
                                        "click the 'Next!' button.").parseMode(HTML).replyMarkup(inlineKeyboardMarkup));
                            } catch (Exception e) {
                                System.out.println("error2");
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                User currentUser = convertJsonToUser(jedis.get(userKey));
                                currentUser.setMinimumPercent(0);
                                jedis.set(userKey, convertUserToJson(currentUser));
                                bot.execute(new SendMessage(playerId, "❌ There was an issue. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageCallbackText.equals("Next")) {
                            try {
                                bot.execute(new SendMessage(playerId, "🚀 Great! Now, let's proceed with the next steps:\n\n" +
                                        "1️⃣ Capture a screenshot confirming your payment, showcasing the amount and transaction number.\n" +
                                        "\n" +
                                        "2️⃣ Send this screenshot, along with your ID <code>" + playerId + "</code>, for verification to our admins at @AmeliaEvansss. " +
                                        "<i>Send ONLY the screenshot and ID. They'll respond to these specific details only.</i> 📬\n" +
                                        "\n" +
                                        "Wait for confirmation that the version is activated, and get ready to start earning! 💼💰").parseMode(HTML));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ There was an issue. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        }

                    } else if (userRegistered(playerId)) {
                        if (messageCallbackText.equals("IDeposit")) {
                            try {
                                Date currentDate = new Date();
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                User checkedUser = convertJsonToUser(jedis.get(userKey));
                                Date userDate = checkedUser.getLastTimePressedDeposit();
                                if (userDate == null) {
                                    checkedUser.setLastTimePressedDeposit(currentDate);
                                    String updatedUser = convertUserToJson(checkedUser);
                                    jedis.set(userKey, updatedUser);
                                    String sendAdminUID = checkedUser.getUID();
                                    bot.execute(new SendMessage(Long.valueOf(AdminID), "User with Telegram ID<code>" + playerId + "</code> and UID <code>" + sendAdminUID + "</code> \uD83D\uDFE1 deposited. Write 'Y11111111' (telegram id) to approve and 'N1111111' to disapprove").parseMode(HTML));
                                    bot.execute(new SendMessage(playerId, "⏳ Great your deposit will be checking soon."));
                                } else {
                                    if (userDate.getTime() <= currentDate.getTime()) {
                                        String sendAdminUID = checkedUser.getUID();
                                        bot.execute(new SendMessage(Long.valueOf(AdminID), "User with Telegram ID<code>" + playerId + "</code> and UID <code>" + sendAdminUID + "</code> \uD83D\uDFE1 deposited. Write 'Y11111111' (telegram id) to approve and 'N1111111' to disapprove").parseMode(HTML));
                                        bot.execute(new SendMessage(playerId, "⏳ Great your deposit will be checking soon."));
                                    } else {
                                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                        InlineKeyboardButton button7 = new InlineKeyboardButton("Deposit done");
                                        button7.callbackData("IDeposit");
                                        inlineKeyboardMarkup.addRow(button7);
                                        bot.execute(new SendMessage(playerId, "❌ Something went wrong. Make sure you deposited at least 50$ to the new account you created through the link and then click 'Deposit done' ").replyMarkup(inlineKeyboardMarkup));
                                    }
                                }

                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (userDeposited(playerId)) {
                            bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                        } else if (messageText.startsWith("/") || messageText.equals("Get Signal")) {
                            bot.execute(new SendMessage(playerId, "Before trying any signals you need to deposit"));
                        }
                    } else {
                        if (messageText.equals("/register") || messageCallbackText.equals("RegisterMe")) {
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            InlineKeyboardButton button2 = new InlineKeyboardButton("Register here");
                            InlineKeyboardButton button3 = new InlineKeyboardButton("I'm ready!");
                            button2.url("https://bit.ly/ChatGPTtrading");
                            button3.callbackData("ImRegistered");
                            inlineKeyboardMarkup.addRow(button2, button3);
                            bot.execute(new SendMessage(playerId, "✅ Great job! To get started, you need to create a new account on the Pocket Option platform through the button below. \n" +
                                    "\n" +
                                    " \uD83D\uDD17 bit.ly/ChatGPTtrading \n" +
                                    "\n" +
                                    "\uD83D\uDCD7 After registering, click on the 'I'm ready!\n" +
                                    "\n" +
                                    "⚠️ Be sure to register using the button 'Register' below or link from the message. Otherwise, the bot will not be able to confirm that you have joined the team. \n" +
                                    "\n" +
                                    "‼️ It's important to note that if you already have an existing Pocket Option account, it's possible to delete and create a new one, and after you can go through the personality verification process again in your new account. This process of deleting and creating a new account is authorized and permitted by Pocket Option administrators.").replyMarkup(inlineKeyboardMarkup).parseMode(HTML).disableWebPagePreview(true));
                            bot.execute(new SendVideo(playerId, videoRegistrationFile));
                            bot.execute(new SendMessage(playerId, "☝️ Here is a video guide on how to register.").parseMode(HTML));
                        } else if (messageCallbackText.equals("ImRegistered")) {
                            bot.execute(new SendMessage(playerId, "✅ Good job! Now send me your Pocket Option ID in format 'ID12345678'.").parseMode(HTML));
                        } else if (messageCallbackText.equals("YesIM")) {
                            String userKey = USER_DB_MAP_KEY + ":" + playerId;
                            try {
                                User user = convertJsonToUser(jedis.get(userKey));
                                String sendAdminUID = user.getUID();
                                User adminUser = convertJsonToUser(jedis.get(AdminID));
                                if (Integer.parseInt(sendAdminUID.substring(0, 2)) >= Integer.parseInt(adminUser.getUID())) {
                                    bot.execute(new SendMessage(Long.valueOf(AdminID), "User with Telegram ID<code>" + playerId + "</code> and UID <code>" + sendAdminUID + "</code> \uD83D\uDFE2 want to register. Write 'A11111111' (telegram id) to approve and 'D1111111' to disapprove").parseMode(HTML));
                                    bot.execute(new SendMessage(playerId, "⏳ Great, your UID will be verified soon"));
                                } else {
                                    InlineKeyboardButton button12 = new InlineKeyboardButton("Register here");
                                    InlineKeyboardButton button13 = new InlineKeyboardButton("I'm ready!");
                                    button12.url("https://bit.ly/ChatGPTtrading");
                                    button13.callbackData("ImRegistered");
                                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                    inlineKeyboardMarkup.addRow(button12, button13);
                                    bot.execute(new SendMessage(playerId, "❌ Something went wrong. You sent me old UID. Make sure you registered with the 'Register here' button and sent a new UID. There is an example of how to do it step by step in the video below. After that press the 'I'm ready!'\n" +
                                            "\n" +
                                            "If you still have problems, then write to support with the command /support. ").replyMarkup(inlineKeyboardMarkup));
                                }
                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ An error occurred. Please send your UID again. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        } else if (messageText.startsWith("/") || messageText.equals("Get Signal")) {
                            bot.execute(new SendMessage(playerId, "Before trying any signals you need to register"));
                        } else {
                            try {
                                Pattern pattern = Pattern.compile("\\d{8}");
                                Matcher matcher = pattern.matcher(messageText);
                                if (matcher.find()) {
                                    uid = matcher.group();
                                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                    InlineKeyboardButton button5 = new InlineKeyboardButton("Yes");
                                    InlineKeyboardButton button6 = new InlineKeyboardButton("No");
                                    button5.callbackData("YesIM");
                                    button6.callbackData("ImRegistered");
                                    inlineKeyboardMarkup.addRow(button5, button6);
                                    Date date = new Date();
                                    Date depositDate = DateUtil.addDays(date, -1);
                                    User newUser = new User(playerName, uid, false, false, date, depositDate, 1, "en", false, false, false, 0);
                                    bot.execute(new SendMessage(playerId, "\uD83D\uDCCC Is your ID " + uid + " correct? ✅\uD83C\uDD94").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                                    String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                    jedis.set(userKey, convertUserToJson(newUser));
                                } else {
                                    bot.execute(new SendMessage(playerId, "❌ There was an issue. Please try again.  "));
                                }
                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ There was an issue. Please send your ID again. Follow the instructions to receive signals. "));
                                logger.severe("An error occurred in your method or class: " + e.getMessage());
                            }
                        }
                    }
                });

            } catch (Exception e) {
                logger.severe("An error occurred in your method or class: " + e.getMessage());
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> jedisPool.close()));
    }

    public static void sendFirstCardInstruction(int amount, long playerId){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button32 = new InlineKeyboardButton("Next Step");
        String callBack = "pay" + amount;
        button32.callbackData(callBack);
        inlineKeyboardMarkup.addRow(button32);
        bot.execute(new SendMessage(playerId, "How to pay using a card? \uD83D\uDECD\uFE0F It's pretty simple, just follow the " +
                "instructions below. If you have any questions at any stage, use the /support command.\n" +
                "\n" +
                "1) We'll use the @wallet bot for payment. It's Telegram's official payment bot. Go to it.\n" +
                "2) Tap on the \"START\" button.\n" +
                "Tap on \"Open Wallet\". \n" +
                "3) Now let's buy cryptocurrency to make the payment. To do this, tap on \"Add crypto\", then tap on \"Bank card\".\n" +
                "4)Choose dollars (USDT). \n" +
                "5) Enter the amount of " + amount+ "$\uD83D\uDC48\n" +
                "6) Tap \"Buy " + amount+" USDT\"\n" +
                "7) And here's the final window tap on \"Pay with card\". Enter your card details for the purchase and you're done! \n" +
                "\n" +
                "\uD83D\uDE0A\uD83D\uDC4D After that, re-enter the bot and you'll have a balance greater than $0. If everything " +
                "went well, tap the \"Next Step\" button. If you encounter any difficulties, write to /support.").parseMode(HTML).replyMarkup(inlineKeyboardMarkup));
    }

    public static void sendSecondCardInstruction(int amount, long playerId){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button32 = new InlineKeyboardButton("Done!");
        button32.callbackData("Next");
        inlineKeyboardMarkup.addRow(button32);
        bot.execute(new SendMessage(playerId, "\uD83D\uDE80 Next step is to send the money. \n" +
                "\n" +
                "1) In the main menu of the @wallet bot, tap on \"Send\". \n" +
                "2) Choose \"External Wallet\". \n" +
                "3) Select dollars (USDT). \n" +
                "4) Paste my address into the top line. Tap on it to copy: <code>TVqr4evixik9qDcq4x4by5DtkhDaGQszo3</code>. \n" +
                "5) Enter the "+ amount +  "$. Note that if you send less than required, the service won't activate. \n" +
                "6) Everything's set, proceed with the payment. \n" +
                "\n" +
                "After the operation is completed, tap on the button below \"Done!\"." +
                " If you encounter any difficulties, write to /support.").parseMode(HTML).replyMarkup(inlineKeyboardMarkup));
    }

}
