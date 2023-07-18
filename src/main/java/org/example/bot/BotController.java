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
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.SetParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.pengrad.telegrambot.model.request.ParseMode.HTML;
import static org.example.bot.RedisDB.accountApprove;
import static org.example.bot.RedisDB.accountDepositApprove;


public class BotController {

    private static JedisPool jedisPool;
    private static final String USER_DB_MAP_KEY = "userDBMap";

    public static void main(String[] args) throws URISyntaxException {
        String TOKEN = "";
        String AdminID = "710511911";
        try {
            String configFilePath = "src/config.properties";
            FileInputStream propsInput = new FileInputStream(configFilePath);
            Properties prop = new Properties();
            prop.load(propsInput);
            TOKEN = prop.getProperty("TOKEN");

        } catch (IOException e) {
            e.printStackTrace();
        }

        String redisUriString = System.getenv("REDIS_URL");
        jedisPool = new JedisPool(new URI(redisUriString));

        TelegramBot bot = new TelegramBot(TOKEN);

        bot.setUpdatesListener(updates -> {
            try (Jedis jedis = jedisPool.getResource()) {
                updates.forEach(update -> {
                    String playerName = "Trader";
                    long playerId = 0L;
                    String messageText = "";
                    String messageCallbackText = "";
                    int messageId = 0;
                    String uid = "";
                    Path resourcePath = Paths.get("src/main/resources");
                    File imageFile = resourcePath.resolve("photoChatGPT.jpg").toFile();
                    File videoDepositFile = resourcePath.resolve("depositTutorial.mp4").toFile();
                    File videoRegistrationFile = resourcePath.resolve("videoRegistrationGuide.mp4").toFile();

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
                    }

                    if (String.valueOf(playerId).equals(AdminID)) {
                        if (messageText.startsWith("Approve")) {
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            InlineKeyboardButton button7 = new InlineKeyboardButton("Deposit done!");
                            button7.callbackData("IDeposit");
                            inlineKeyboardMarkup.addRow(button7);
                            System.out.println(messageText.length());
                            String tgID = messageText.substring(7, messageText.length());
                            System.out.println(tgID);
                            accountApprove(Long.parseLong(tgID));
                            bot.execute(new SendMessage(tgID, "✅ Great, your account is confirmed! The last step is to make any deposit by any convenient way. After that press the button 'Deposit done'.\n" +
                                    "\n" +
                                    "I would like to note that the recommended starting deposit of $50 - $150. But it is not necessary and is a tool for faster earnings. Also, if you deposit more than $50, use promo code 50START to get an extra 50% of your deposit. For example, with a deposit of 100$ you will get 50$ additional. It means that you will get 150$ in total.\n" +
                                    "\n" +
                                    "At the bottom there is a video instruction on how to top up the account.").replyMarkup(inlineKeyboardMarkup));
                            bot.execute(new SendVideo(tgID, videoDepositFile));
                            bot.execute(new SendMessage(tgID, "☝\uFE0F Here is a video guide on how to make a deposit.").parseMode(HTML));
                            bot.execute(new SendMessage(AdminID, "Registration for " + tgID + " was approved"));
                        } else if (messageText.startsWith("Disapprove")) {
                            String tgID = messageText.substring(10, messageText.length());
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
                        } else if (messageText.startsWith("Deposit")) {
                            String tgID = messageText.substring(7, messageText.length());
                            accountDepositApprove(Long.parseLong(tgID));
                            Keyboard replyKeyboardMarkup = (Keyboard) new ReplyKeyboardMarkup(
                                    new String[]{"Get Signal"});
                            bot.execute(new SendMessage(tgID, "✅ Great! Everything is ready! You can start getting signals. For this click on 'Get Signals' or write it manually. \n" +
                                    "\n" +
                                    "Below is a video guide on how to use signals from me. \n" +
                                    "\n" +
                                    "If you have any questions use the /support command.").replyMarkup((com.pengrad.telegrambot.model.request.Keyboard) replyKeyboardMarkup));
                            bot.execute(new SendMessage(AdminID, "Deposit for " + tgID + " was approved"));
                        } else if (messageText.startsWith("NoDeposit")) {
                            String tgID = messageText.substring(9, messageText.length());
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            InlineKeyboardButton button7 = new InlineKeyboardButton("Deposit done");
                            button7.callbackData("IDeposit");
                            inlineKeyboardMarkup.addRow(button7);
                            bot.execute(new SendMessage(tgID, "❌ Something went wrong. Make sure you deposit the new account you created through the link and then click 'Deposit done' ").replyMarkup(inlineKeyboardMarkup));
                            bot.execute(new SendMessage(AdminID, "Deposit for " + tgID + " was disapproved"));
                        }
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
                                "❗\uFE0F If you have any problems or suggestions, you can contact bot support via the /support command.").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                    } else if (messageText.equals("/help") || messageCallbackText.equals("Help")) {
                        bot.execute(new SendMessage(playerId, "There will be help"));
                    } else if (userRegistered(playerId)) {
                        // Логика для зарегистрированных пользователей
                        bot.execute(new SendMessage(playerId, "Welcome back, " + playerName + "!"));
                    } else {
                        // Логика для незарегистрированных пользователей
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        InlineKeyboardButton buttonRegister = new InlineKeyboardButton("Register here");
                        buttonRegister.url("https://bit.ly/ChatGPTtrading");
                        inlineKeyboardMarkup.addRow(buttonRegister);
                        bot.execute(new SendMessage(playerId, "🛑 You are not registered yet. To start receiving signals, please click the 'Register here' button to register.").replyMarkup(inlineKeyboardMarkup));
                    }

                    System.out.println(update);

                });

            } catch (Exception e) {
                e.printStackTrace();
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            jedisPool.close();
        }));
    }

    private static boolean userRegistered(long playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            return jedis.exists(userKey);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
