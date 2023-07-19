package org.example.bot;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

import static org.example.bot.BotController.USER_DB_MAP_KEY;
import static org.example.bot.BotController.jedisPool;

public class JedisDB {

    static boolean userRegistered(long playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            return checkedUser.isRegistered();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean userDeposited(long playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            return checkedUser.isDeposited();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static void registrationApprove(long playerId){
        try (Jedis jedis = jedisPool.getResource()) {
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            checkedUser.setRegistered(true);
            String updatedUser = convertUserToJson(checkedUser);
            jedis.set(userKey, updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void depositApprove(long playerId){
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

    static String convertUserToJson(User user) {
        Gson gson = new Gson();
        return gson.toJson(user);
    }

    static User convertJsonToUser(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, User.class);
    }

}
