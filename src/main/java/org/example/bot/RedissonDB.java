package org.example.bot;

import com.pengrad.telegrambot.request.SendMessage;
import org.example.bot.User;

import static org.example.bot.BotController.userDBMap;

public class RedissonDB {

    public static void accountApprove(long playerId) {
        String playerName = userDBMap.get(String.valueOf(playerId)).getName();
        String playerUID = userDBMap.get(String.valueOf(playerId)).getUID();
        User newUser = new User(playerName, playerUID , 1, 5, true, false) ;
        userDBMap.replace(String.valueOf(playerId), newUser);
    }

    public static void accountDepositApprove(long playerId) {
        String playerName = userDBMap.get(String.valueOf(playerId)).getName();
        String playerUID = userDBMap.get(String.valueOf(playerId)).getUID();
        User newUser = new User(playerName, playerUID , 1, 5, true, true) ;
        userDBMap.replace(String.valueOf(playerId), newUser);
    }


    public static SendMessage changeMinTimeForDeal(long playerId, int minTime) {
        User currentUser = userDBMap.get(String.valueOf(playerId));
        currentUser.setMinTimeDeal(minTime);
        userDBMap.replace(String.valueOf(playerId), currentUser);
        return new SendMessage(playerId, currentUser.getName() + " you are successfully updated your min value");
    }

    public static SendMessage changeMaxTimeForDeal(long playerId, int maxTime) {
        User currentUser = userDBMap.get(String.valueOf(playerId));
        currentUser.setMaxTimeDeal(maxTime);
        userDBMap.replace(String.valueOf(playerId), currentUser);
        return new SendMessage(playerId, currentUser.getName() + " you are successfully updated your max value");
    }

}
