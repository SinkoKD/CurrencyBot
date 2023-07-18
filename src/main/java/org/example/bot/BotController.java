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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
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
import static org.example.bot.RedissonDB.accountApprove;
import static org.example.bot.RedissonDB.accountDepositApprove;

public class BotController {

    static RedissonClient redisson;
    public static RMap<String, User> userDBMap;


    public static void main(String[] args) {
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

        Config config = new Config();
        redisson = getConnection(); // Получение подключения к Redis
        userDBMap = redisson.getMap("userDBMap");

        //       Hi, I'm Chat GPT bot for binary options trading. I was created to analyze brokers using artificial intelligence. Click the button below to get started!


        TelegramBot bot = new TelegramBot(TOKEN);


        bot.setUpdatesListener(updates -> {
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

//                bot.execute(new SendMessage(playerId, "Hi"));
//                bot.execute(new SendPhoto(playerId, "photoChatGPT.jpg"));

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
                } else if (userDBMap.containsKey(String.valueOf(playerId)) && userDBMap.get(String.valueOf(playerId)).isDeposited()) {
                    if (messageText.equals("Get Signal") || messageCallbackText.equals("getSignal")) {
                        Date date = new Date();
                        ArrayList<String> listOfPairs = new ArrayList<>();
                        if (date.getDay() == 0 || date.getDay() == 1) {
                            listOfPairs.add("AUD/CAD OTC");
                            listOfPairs.add("AUD/CHF OTC");
                            listOfPairs.add("AUD/NZD OTC");
                            listOfPairs.add("CAD/CHF OTC");
                            listOfPairs.add("EUR/CHF OTC");
                            listOfPairs.add("EUR/JPY OTC");
                            listOfPairs.add("EUR/USD OTC");
                            listOfPairs.add("GBP/JPY OTC");
                            listOfPairs.add("NZD/JPY OTC");
                            listOfPairs.add("NZD/USD OTC");
                            listOfPairs.add("USD/CAD OTC");
                            listOfPairs.add("USD/CNH OTC");
                            listOfPairs.add("CHF/NOK OTC");
                            listOfPairs.add("EUR/GBP OTC");
                            listOfPairs.add("EUR/TRY OTC");
                            listOfPairs.add("CHF/JPY OTC");
                            listOfPairs.add("EUR/NZD OTC");
                            listOfPairs.add("AUD/JPY OTC");
                            listOfPairs.add("AUD/USD OTC");
                            listOfPairs.add("EUR/HUF OTC");
                            listOfPairs.add("USD/CHF OTC");
                            listOfPairs.add("EUR/JPY");
                            listOfPairs.add("GBP/JPY");
                            listOfPairs.add("AUD/CAD");
                            listOfPairs.add("AUD/JPY");
                            listOfPairs.add("AUD/USD");
                            listOfPairs.add("CAD/CHF");
                            listOfPairs.add("CAD/JPY");
                            listOfPairs.add("CHF/JPY");
                            listOfPairs.add("EUR/AUD");
                            listOfPairs.add("EUR/CAD");
                            listOfPairs.add("EUR/CHF");
                            listOfPairs.add("EUR/GBP");
                            listOfPairs.add("EUR/USD");
                            listOfPairs.add("GBP/CAD");
                            listOfPairs.add("GBP/CHF");
                            listOfPairs.add("GBP/USD");
                            listOfPairs.add("USD/CAD");
                            listOfPairs.add("USD/CHF");
                            listOfPairs.add("GBP/USD");
                            listOfPairs.add("USD/CAD");
                            listOfPairs.add("USD/CHF");
                            listOfPairs.add("GBP/AUD");
                            listOfPairs.add("USD/JPY");
                            listOfPairs.add("USD/CNH");
                            listOfPairs.add("AUD/CHF");
                        } else {
                            listOfPairs.add("AUD/CAD OTC");
                            listOfPairs.add("AUD/CHF OTC");
                            listOfPairs.add("AUD/NZD OTC");
                            listOfPairs.add("CAD/CHF OTC");
                            listOfPairs.add("EUR/CHF OTC");
                            listOfPairs.add("EUR/JPY OTC");
                            listOfPairs.add("EUR/USD OTC");
                            listOfPairs.add("GBP/JPY OTC");
                            listOfPairs.add("NZD/JPY OTC");
                            listOfPairs.add("NZD/USD OTC");
                            listOfPairs.add("USD/CAD OTC");
                            listOfPairs.add("USD/CNH OTC");
                            listOfPairs.add("CHF/NOK OTC");
                            listOfPairs.add("EUR/GBP OTC");
                            listOfPairs.add("EUR/TRY OTC");
                            listOfPairs.add("CHF/JPY OTC");
                            listOfPairs.add("EUR/NZD OTC");
                            listOfPairs.add("AUD/JPY OTC");
                            listOfPairs.add("AUD/USD OTC");
                            listOfPairs.add("EUR/HUF OTC");
                            listOfPairs.add("USD/CHF OTC");
                            listOfPairs.add("EUR/JPY");
                            listOfPairs.add("GBP/JPY");
                            listOfPairs.add("AUD/CAD");
                            listOfPairs.add("AUD/JPY");
                            listOfPairs.add("AUD/USD");
                            listOfPairs.add("CAD/CHF");
                            listOfPairs.add("CAD/JPY");
                            listOfPairs.add("CHF/JPY");
                            listOfPairs.add("EUR/AUD");
                            listOfPairs.add("EUR/CAD");
                            listOfPairs.add("EUR/CHF");
                            listOfPairs.add("EUR/GBP");
                            listOfPairs.add("EUR/USD");
                            listOfPairs.add("GBP/CAD");
                            listOfPairs.add("GBP/CHF");
                            listOfPairs.add("GBP/USD");
                            listOfPairs.add("USD/CAD");
                            listOfPairs.add("USD/CHF");
                            listOfPairs.add("GBP/USD");
                            listOfPairs.add("USD/CAD");
                            listOfPairs.add("USD/CHF");
                            listOfPairs.add("GBP/AUD");
                            listOfPairs.add("USD/JPY");
                            listOfPairs.add("USD/CNH");
                            listOfPairs.add("AUD/CHF");
                        }
                        bot.execute(new SendMessage(playerId, "⌛\uFE0F Looking for the best pair...").parseMode(HTML));
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Random random = new Random();
                        int randomNumber = random.nextInt(listOfPairs.size());
                        int randomUp = random.nextInt(2);
                        String direction = "";
                        if (randomUp == 0) {
                            direction = "⬆\uFE0F Direction: <b>UP</b> ";
                        } else {
                            direction = "⬇\uFE0F Direction: <b>DOWN</b> ";
                        }
                        int randomAccuracy = random.nextInt(27) + 70;
                        int randomAddTime = random.nextInt(10000) + 8000;
                        // int randomTime = 1;
                        //   int randomTime = random.nextInt(userDBMap.get(String.valueOf(playerId)).getMaxTimeDeal() - userDBMap.get(String.valueOf(playerId)).getMinTimeDeal()) + userDBMap.get(String.valueOf(playerId)).getMinTimeDeal();
                        int randomTime = random.nextInt(3) + 1;
                        String pickedPair = listOfPairs.get(randomNumber);
                        EditMessageText editMessageText = new EditMessageText(playerId, messageId + 1, "Pair <b>" + pickedPair + "</b> has been picked. I am conducting an analysis on it.").parseMode(HTML);
                        bot.execute(editMessageText);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        InlineKeyboardButton button22 = new InlineKeyboardButton("Get new signal");
                        button22.callbackData("getSignal");
                        inlineKeyboardMarkup.addRow(button22);
                        EditMessageText editMessage = new EditMessageText(playerId, messageId + 1, "Your signal is: \n\uD83D\uDCB0 <b>" + pickedPair + "</b>\n" + direction + "\n⌛\uFE0F Time for deal:<b> " + randomTime + "M </b>\n\uD83C\uDFAF Accuracy calculated:<b> " + randomAccuracy + "%</b>\n⚡\uFE0F Wait for a message '<b>GO!</b>' and then make forecast.").parseMode(HTML);
                        bot.execute(editMessage);
                        try {
                            Thread.sleep(randomAddTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bot.execute(new SendMessage(playerId, "<b>GO!</b>").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                    }
                } else if (userDBMap.containsKey(String.valueOf(playerId)) && userDBMap.get(String.valueOf(playerId)).isRegistered()) {
                    if (messageCallbackText.equals("IDeposit")) {
                        bot.execute(new SendMessage(playerId, "⏳ Great your deposit will be checking soon."));
                        User user = userDBMap.get(String.valueOf(playerId));
                        String sendAdminUID = user.getUID();
                        bot.execute(new SendMessage(Long.valueOf(AdminID), "User with Telegram ID<code>" + playerId + "</code> and UID <code>" + sendAdminUID + "</code> deposited. Write 'Deposit11111111' (telegram id) to approve and 'NoDeposit1111111' to disapprove").parseMode(HTML));
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
                                "⚠\uFE0F Be sure to register using the button 'Register' below or link from the message. Otherwise, the bot will not be able to confirm that you have joined the team. \n" +
                                "\n" +
                                "‼\uFE0F It's important to note that if you already have an existing Pocket Option account, it's possible to delete and create a new one, and after you can go through the personality verification process again in your new account. This process of deleting and creating a new account is authorized and permitted by Pocket Option administrators.").replyMarkup(inlineKeyboardMarkup).parseMode(HTML).disableWebPagePreview(true));
                        bot.execute(new SendVideo(playerId, videoRegistrationFile));
                        bot.execute(new SendMessage(playerId, "☝\uFE0F Here is a video guide on how to register.").parseMode(HTML));
                    } else if (messageCallbackText.equals("ImRegistered")) {
                        bot.execute(new SendMessage(playerId, "✅ Good job! Now send me your Pocket Option ID in format 'ID12345678'.").parseMode(HTML));
                    } else if (messageText.startsWith("ID") && messageText.length() == 10 || messageText.length() == 11) {
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        InlineKeyboardButton button5 = new InlineKeyboardButton("Yes");
                        InlineKeyboardButton button6 = new InlineKeyboardButton("No");
                        button5.callbackData("YesIM");
                        button6.callbackData("ImRegistered");
                        inlineKeyboardMarkup.addRow(button5, button6);
                        String text = messageText.replaceAll("\\s", "");
                        uid = text.substring(2, 10);
                        User newUser = new User(playerName, uid, false, false);
                        bot.execute(new SendMessage(playerId, "\uD83D\uDCCC Your ID is " + uid + " is it correct?").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                        if (userDBMap.containsKey(String.valueOf(playerId))) {
                            userDBMap.replace(String.valueOf(playerId), newUser);
                        } else {
                            userDBMap.put(String.valueOf(playerId), newUser);
                        }
                    } else if (messageCallbackText.equals("YesIM")) {
                        bot.execute(new SendMessage(playerId, "⏳ Great, your UID will be verified soon"));
                        User user = userDBMap.get(String.valueOf(playerId));
                        String sendAdminUID = user.getUID();
                        bot.execute(new SendMessage(Long.valueOf(AdminID), "User with Telegram ID<code>" + playerId + "</code> and UID " + sendAdminUID + " want to register. Write 'Approve11111111' (telegram id) to approve and 'Disapprove1111111' to disapprove").parseMode(HTML));
                    } else if (messageText.startsWith("/") || messageText.equals("Get Signal")) {
                        bot.execute(new SendMessage(playerId, "Before trying any signals you need to register"));
                    }
                }

                System.out.println(update);

            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            redisson.shutdown();
        }));

    }

    private static RedissonClient getConnection() {
        try {
            TrustManager bogusTrustManager = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{bogusTrustManager}, new java.security.SecureRandom());

            HostnameVerifier bogusHostnameVerifier = (hostname, session) -> true;

            String redisURL = System.getenv("REDIS_URL"); // Получение URL Redis из переменной окружения Heroku

            Config config = new Config();
            config.useSingleServer()
                    .setAddress(redisURL);

            return Redisson.create(config);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Cannot obtain Redis connection!", e);
        }
    }
}

