package org.example.bot;

import java.util.HashMap;
import java.util.Map;

public class RedisDB {

    private static Map<String, User> userDBMap = new HashMap<>();

    public static void accountApprove(long playerId) {
        User user = userDBMap.get(String.valueOf(playerId));
        if (user != null) {
            user.setRegistered(true);
            user.setDeposited(false);
            userDBMap.put(String.valueOf(playerId), user);
        }
    }

    public static void accountDepositApprove(long playerId) {
        User user = userDBMap.get(String.valueOf(playerId));
        if (user != null) {
            user.setRegistered(true);
            user.setDeposited(true);
            userDBMap.put(String.valueOf(playerId), user);
        }
    }
}
