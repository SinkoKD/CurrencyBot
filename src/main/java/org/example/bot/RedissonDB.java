package org.example.bot;

import com.pengrad.telegrambot.request.SendMessage;
import org.example.bot.User;

import static org.example.bot.BotController.userDBMap;

public class RedissonDB {

    public static void accountApprove(long playerId) {
        String playerName = userDBMap.get(String.valueOf(playerId)).getName();
        String playerUID = userDBMap.get(String.valueOf(playerId)).getUID();
        User newUser = new User(playerName, playerUID , true, false) ;
        userDBMap.replace(String.valueOf(playerId), newUser);
    }

    public static void accountDepositApprove(long playerId) {
        String playerName = userDBMap.get(String.valueOf(playerId)).getName();
        String playerUID = userDBMap.get(String.valueOf(playerId)).getUID();
        User newUser = new User(playerName, playerUID ,  true, true) ;
        userDBMap.replace(String.valueOf(playerId), newUser);
    }



}
