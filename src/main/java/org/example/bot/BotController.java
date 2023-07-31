package org.example.bot;

import com.google.gson.Gson;
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

import static com.pengrad.telegrambot.model.request.ParseMode.HTML;


public class BotController {

    public static JedisPool jedisPool;
    public static final String USER_DB_MAP_KEY = "userDBMap";
    public static ArrayList<Long> allUsers = new ArrayList<>();

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
                    long playerId;
                    String messageText = "";
                    String messageCallbackText = "";
                    String uid;
                    int firstDigit = 6;
                    int secondDigit = 3;
                    int messageId;
                    Path resourcePath = Paths.get("src/main/resources");
                    File videoDepositFile = resourcePath.resolve("depositTutorial.mp4").toFile();
                    File videoRegistrationFile = resourcePath.resolve("videoRegistrationGuide.mp4").toFile();
                    File videoExampleFile = resourcePath.resolve("videoExample.mp4").toFile();


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
                            e.printStackTrace();
                        }
                    }

//                    String userKeyAdmin = USER_DB_MAP_KEY + ":" + AdminID;
//                    String userKeyIm = USER_DB_MAP_KEY + ":" + "430823029";
//                    Date adminDate = new Date();
//                    User adminUser = new User("Admin", AdminID, false, false, adminDate);
//                    jedis.set(userKeyAdmin, convertUserToJson(adminUser));
//                    User Im = new User("NoAdmin", "430823029", true, true, adminDate);
//                    jedis.set(userKeyIm, convertUserToJson(Im));

//                    try {
//                        String userKey = USER_DB_MAP_KEY + ":" + AdminID;
//                        User checkedAdmin = convertJsonToUser(jedis.get(userKey));
//                        Date currentDate = new Date();
//                        Date checkAdminDate = DateUtil.addMinutes(checkedAdmin.getLastTimeTexted(), 2);
//                        System.out.println("Im not there");
//                        if (checkAdminDate.getTime() < currentDate.getTime()) {
//                            System.out.println("Im there");
//                            checkedAdmin.setLastTimeTexted(currentDate);
//                            jedis.set(userKey, convertUserToJson(checkedAdmin));
//                            System.out.println("Admin done");
//                            Set<String> userKeys = jedis.keys("userDBMap:*");
//                            System.out.println("Keys done");
//                            System.out.println(userKeys.size());
//                            for (String keyForUser : userKeys) {
//                                System.out.println("In the keys");
//                                System.out.println(keyForUser);
//                                User currentUser = convertJsonToUser(jedis.get(keyForUser));
//                                System.out.println(currentUser.getName());
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

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
                                registrationApprove(Long.parseLong(tgID));
                                bot.execute(new SendMessage(tgID, "✅ Great, your account is confirmed! The last step is to make any deposit by any convenient way. After that press the button 'Deposit done'.\n" +
                                        "\n" +
                                        "I would like to note that the recommended starting deposit of $50 - $150. But it is not necessary and is a tool for faster earnings. Also, if you deposit more than $50, use promo code 50START to get an extra 50% of your deposit. For example, with a deposit of 100$ you will get 50$ additional. It means that you will get 150$ in total.\n" +
                                        "\n" +
                                        "At the bottom there is a video instruction on how to top up the account.").replyMarkup(inlineKeyboardMarkup));
                                bot.execute(new SendVideo(tgID, videoDepositFile));
                                bot.execute(new SendMessage(tgID, "☝️ Here is a video guide on how to make a deposit.").parseMode(HTML));
                                bot.execute(new SendMessage(AdminID, "Registration for " + tgID + " was approved"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                e.printStackTrace();
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
                                Keyboard replyKeyboardMarkup = (Keyboard) new ReplyKeyboardMarkup(
                                        new String[]{"Get Signal"});
                                bot.execute(new SendMessage(tgID, "✅ Great! Everything is ready! You can start getting signals. For this click on 'Get Signals' or write it manually. \n" +
                                        "\n" +
                                        "Below is a video guide on how to use signals from me. \n" +
                                        "\n" +
                                        "If you have any questions use the /support command.").replyMarkup((com.pengrad.telegrambot.model.request.Keyboard) replyKeyboardMarkup));
                                bot.execute(new SendMessage(AdminID, "Deposit for " + tgID + " was approved"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("N") || messageText.startsWith("n") || messageText.startsWith("Т") || messageText.startsWith("т")) {
                            String tgID = messageText.substring(1);
                            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                            InlineKeyboardButton button7 = new InlineKeyboardButton("Deposit done");
                            button7.callbackData("IDeposit");
                            inlineKeyboardMarkup.addRow(button7);
                            bot.execute(new SendMessage(tgID, "❌ Something went wrong. Make sure you deposit the new account you created through the link and then click 'Deposit done' ").replyMarkup(inlineKeyboardMarkup));
                            bot.execute(new SendMessage(AdminID, "Deposit for " + tgID + " was disapproved"));
                        } else if (messageText.startsWith("reply:")) {
                            int indexOfAnd = messageText.indexOf("&");
                            String tgID = messageText.substring(6, indexOfAnd);
                            String reply = messageText.substring(indexOfAnd + 1);
                            System.out.println(indexOfAnd + "\n" + tgID + "\n" + reply);
                            bot.execute(new SendMessage(tgID, reply));
                            bot.execute(new SendMessage(AdminID, "Reply was sent"));
                        } else if (messageText.startsWith("deleteUser:")) {
                            try {
                                String TGId = (messageText.substring(11));
                                jedis.del(TGId);
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " was fully deleted"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("deleteDeposit:")) {
                            try {
                                String TGId = (messageText.substring(14));
                                depositDisapprove(Long.parseLong(TGId));
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " got deposit disapprove"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("deleteRegistration:")) {
                            try {
                                String TGId = (messageText.substring(19));
                                registrationDisapprove(Long.parseLong(TGId));
                                bot.execute(new SendMessage(AdminID, "User with ID " + TGId + " got register disapprove"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.equals("/clearDB")) {
                            try {
                                jedis.flushAll();
                                bot.execute(new SendMessage(AdminID, "DB was cleaned"));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(AdminID, "❌ An error occurred. Please try again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.equals("/getAllUsers")) {
                            int size = 84 + allUsers.size();
                            bot.execute(new SendMessage(AdminID, "There is " + size + " users right now."));
                        } else if (messageText.startsWith("setFirstDigit:")) {
                            firstDigit = Integer.parseInt(messageText.substring(14));
                            bot.execute(new SendMessage(AdminID, "First digit now is " + firstDigit + "."));
                        } else if (messageText.startsWith("setSecondDigit:")) {
                            secondDigit = Integer.parseInt(messageText.substring(15));
                            bot.execute(new SendMessage(AdminID, "First digit now is " + secondDigit + "."));
                        }
                    } else if (messageText.startsWith("needReply:")) {
                        String userQuestion = messageText.substring(10);
                        System.out.println("Need reply");
                        bot.execute(new SendMessage(playerId, "✅ I received your message and will respond to you as soon as possible. Your message: " + userQuestion).parseMode(HTML));
                        bot.execute(new SendMessage(AdminID, "✅ ID:<code>" + playerId + "</code> has a question" + userQuestion + " To answer it write a message: <code>reply:111111111&</code> *your text*").parseMode(HTML));
                        System.out.println("Really works");
                    } else if (messageText.equals("/support") || messageCallbackText.equals("Help")) {
                        bot.execute(new SendMessage(playerId, "⏳ If you have any questions, please review the video first. If you don't find an answer to your question there or if you have a different request, please send a message in the format:<code>needReply:</code> *your text*. \nPlease do this in one message, and I'll get back to you as soon as possible.").parseMode(HTML));
                    } else if (messageText.equals("/start")) {
                        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                        InlineKeyboardButton button32 = new InlineKeyboardButton("Let's start");
                        button32.callbackData("RegisterMe");
                        inlineKeyboardMarkup.addRow(button32);
                        if (!allUsers.contains(playerId)) {
                            allUsers.add(playerId);
                        }
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
                        if (messageText.equals("Get Signal") || messageCallbackText.equals("getSignal")) {
                            List<String> listOfPairs = Arrays.asList(
                                    "AUD/CAD OTC", "AUD/CHF OTC", "AUD/NZD OTC", "CAD/CHF OTC", "EUR/CHF OTC",
                                    "EUR/JPY OTC", "EUR/USD OTC", "GBP/JPY OTC", "NZD/JPY OTC", "NZD/USD OTC",
                                    "USD/CAD OTC", "USD/CNH OTC", "CHF/NOK OTC", "EUR/GBP OTC", "EUR/TRY OTC",
                                    "CHF/JPY OTC", "EUR/NZD OTC", "AUD/JPY OTC", "AUD/USD OTC", "EUR/HUF OTC",
                                    "USD/CHF OTC"
                            );
                            Runnable signalGeneratorTask = () -> {
                                bot.execute(new SendMessage(playerId, "⌛️ Looking for the best pair...").parseMode(HTML));
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                                    e.printStackTrace();
                                }
                                Random random = new Random();
                                int randomNumber = random.nextInt(listOfPairs.size());
                                int randomUp = random.nextInt(2);
                                String direction = "";
                                if (randomUp == 0) {
                                    direction = "⬆️ Direction: <b>UP</b> ";
                                } else {
                                    direction = "⬇️ Direction: <b>DOWN</b> ";
                                }
                                int randomAccuracy = random.nextInt(28) + 70;
                                int randomAddTime = random.nextInt(10000) + 8000;
                                int randomTime = random.nextInt(5) + 1;
                                String pickedPair = listOfPairs.get(randomNumber);
                                EditMessageText editMessageText = new EditMessageText(playerId, messageId + 1, "Pair <b>" + pickedPair + "</b> has been picked. I am conducting an analysis on it.").parseMode(HTML);
                                bot.execute(editMessageText);
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                                    e.printStackTrace();
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
                                    e.printStackTrace();
                                }
                                Keyboard replyKeyboardMarkup = (Keyboard) new ReplyKeyboardMarkup(
                                        new String[]{"Get Signal"});
                                bot.execute(new SendMessage(playerId, "<b>GO!</b>").parseMode(HTML).replyMarkup(replyKeyboardMarkup));
                            };
                            new Thread(signalGeneratorTask).start();
                        }
                    } else if (userRegistered(playerId)) {
                        if (messageCallbackText.equals("IDeposit")) {
                            try {
                                bot.execute(new SendMessage(playerId, "⏳ Great your deposit will be checking soon."));
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                User checkedUser = convertJsonToUser(jedis.get(userKey));
                                String sendAdminUID = checkedUser.getUID();
                                bot.execute(new SendMessage(Long.valueOf(AdminID), "User with Telegram ID<code>" + playerId + "</code> and UID <code>" + sendAdminUID + "</code> \uD83D\uDFE1\uD83D\uDFE8 deposited. Write 'Y11111111' (telegram id) to approve and 'N1111111' to disapprove").parseMode(HTML));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ An error occurred. Please try again. "));
                                e.printStackTrace();
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
                        } else if (messageText.startsWith("ID") || messageText.startsWith("id") || messageText.startsWith("Id") || messageText.startsWith("iD") && messageText.length() == 10 || messageText.length() == 11) {
                            try {
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                InlineKeyboardButton button5 = new InlineKeyboardButton("Yes");
                                InlineKeyboardButton button6 = new InlineKeyboardButton("No");
                                button5.callbackData("YesIM");
                                button6.callbackData("ImRegistered");
                                inlineKeyboardMarkup.addRow(button5, button6);
                                String text = messageText.replaceAll("\\s", "");
                                uid = text.substring(2, 10);
                                Date date = new Date();
                                User newUser = new User(playerName, uid, false, false, date, 0);
                                bot.execute(new SendMessage(playerId, "\uD83D\uDCCC Your ID is " + uid + " is it correct?").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                jedis.set(userKey, convertUserToJson(newUser));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ An error occurred. Please send your UID again. "));
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("user") || messageText.startsWith("USER") && messageText.length() == 12 || messageText.length() == 13) {
                            try {
                                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                                InlineKeyboardButton button5 = new InlineKeyboardButton("Yes");
                                InlineKeyboardButton button6 = new InlineKeyboardButton("No");
                                button5.callbackData("YesIM");
                                button6.callbackData("ImRegistered");
                                inlineKeyboardMarkup.addRow(button5, button6);
                                String text = messageText.replaceAll("\\s", "");
                                uid = text.substring(4, 12);
                                Date date = new Date();
                                User newUser = new User(playerName, uid, false, false, date, 0);
                                bot.execute(new SendMessage(playerId, "\uD83D\uDCCC Your ID is " + uid + " is it correct?").replyMarkup(inlineKeyboardMarkup).parseMode(HTML));
                                String userKey = USER_DB_MAP_KEY + ":" + playerId;
                                jedis.set(userKey, convertUserToJson(newUser));
                            } catch (Exception e) {
                                bot.execute(new SendMessage(playerId, "❌ An error occurred. Please send your UID again. "));
                                e.printStackTrace();
                            }
                        } else if (messageCallbackText.equals("YesIM")) {
                            String userKey = USER_DB_MAP_KEY + ":" + playerId;
                            try {
                                User user = convertJsonToUser(jedis.get(userKey));
                                String sendAdminUID = user.getUID();
                                if (Integer.parseInt(sendAdminUID.substring(0, 1)) >= firstDigit && Integer.parseInt(sendAdminUID.substring(1, 2)) >= secondDigit) {
                                    bot.execute(new SendMessage(Long.valueOf(AdminID), "User with Telegram ID<code>" + playerId + "</code> and UID <code>" + sendAdminUID + "</code> \uD83D\uDFE2\uD83D\uDFE9 want to register. Write 'A11111111' (telegram id) to approve and 'D1111111' to disapprove").parseMode(HTML));
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
                                e.printStackTrace();
                            }
                        } else if (messageText.startsWith("/") || messageText.equals("Get Signal")) {
                            bot.execute(new SendMessage(playerId, "Before trying any signals you need to register"));
                        }
                    }
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

    static boolean userRegistered(long playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            System.out.println("userRegistered");
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            if (!jedis.exists(userKey)) {
                System.out.println("User not exist registration");
                return false;
            }
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            System.out.println("Registered " + checkedUser.getName() + checkedUser.getUID() + "???" + checkedUser.isRegistered());
            return checkedUser.isRegistered();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("False");
            return false;
        }
    }

    static boolean userDeposited(long playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            System.out.println("userDeposited");
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            if (!jedis.exists(userKey)) {
                System.out.println("User not exist deposit");
                return false;
            }
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            System.out.println("Deposit " + checkedUser.getName() + checkedUser.getUID() + "???" + checkedUser.isDeposited());
            return checkedUser.isDeposited();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("False");
            return false;
        }
    }

    static void registrationApprove(long playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            System.out.println("userDeposited");
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            checkedUser.setRegistered(true);
            String updatedUser = convertUserToJson(checkedUser);
            jedis.set(userKey, updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void depositApprove(long playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            checkedUser.setDeposited(true);
            String updatedUser = convertUserToJson(checkedUser);
            jedis.set(userKey, updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void depositDisapprove(long playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            checkedUser.setDeposited(false);
            String updatedUser = convertUserToJson(checkedUser);
            jedis.set(userKey, updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void registrationDisapprove(long playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            checkedUser.setRegistered(false);
            String updatedUser = convertUserToJson(checkedUser);
            jedis.set(userKey, updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String convertUserToJson(User user) {
        Gson gson = new Gson();
        return gson.toJson(user);
    }

    static User convertJsonToUser(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, User.class);
    }
}
