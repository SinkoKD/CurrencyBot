package org.example.bot;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;

import static org.example.bot.BotController.USER_DB_MAP_KEY;
import static org.example.bot.BotController.jedisPool;

public class JedisActions {
    public static boolean userRegistered(long playerId) {
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

    public static boolean userDeposited(long playerId) {
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

    public static boolean userCanWriteToSupport(long playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            if (!jedis.exists(userKey)) {
                return false;
            }
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            return checkedUser.isCanWriteToSupport();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void registrationApprove(long playerId) {
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

    public static void depositApprove(long playerId) {
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

    public static void depositDisapprove(long playerId) {
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

    public static void registrationDisapprove(long playerId) {
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

    public static void increaseTimesWasSent(String playerKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            User checkedUser = convertJsonToUser(jedis.get(playerKey));
            int num = checkedUser.getTimesTextWasSent();
            checkedUser.setTimesTextWasSent(num+1);
            String updatedUser = convertUserToJson(checkedUser);
            jedis.set(playerKey, updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setTo1TimesWasSent(String playerId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String userKey = USER_DB_MAP_KEY + ":" + playerId;
            User checkedUser = convertJsonToUser(jedis.get(userKey));
            checkedUser.setTimesTextWasSent(1);
            String updatedUser = convertUserToJson(checkedUser);
            jedis.set(userKey, updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String convertUserToJson(User user) {
        Gson gson = new Gson();
        return gson.toJson(user);
    }

    public static User convertJsonToUser(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, User.class);
    }
}
