package ltd.dreamcraft.xinxinwhitelist.listeners;

import fr.xephi.authme.api.v3.AuthMeApi;
import ltd.dreamcraft.xinxinwhitelist.XinxinWhiteList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class onJoin implements Listener {
    public static ConcurrentHashMap<String, String> names = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> nameCache = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> ipCache = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> times = new ConcurrentHashMap<>();

    private static String genCode(int length) {
        String words = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int random = (new Random()).nextInt(words.length());
            sb.append(words.toCharArray()[random]);
        }
        return sb.toString().toUpperCase();
    }

    public static String hide(String name) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (char c : name.toCharArray()) {
            i++;
            if (i > 2) {
                sb.append("*");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        try {
            FileConfiguration config = XinxinWhiteList.getInstance().getConfig();
            if (Bukkit.getPluginManager().getPlugin("AuthMe") != null &&
                    config.getBoolean("authme.ignore") && AuthMeApi.getInstance().isRegistered(e.getName()))
                return;
            if (onGroup.realIPS.containsKey(e.getName().toLowerCase()) && XinxinWhiteList.getInstance().getConfig().getBoolean("authme.force_reg")) {
                String ip = onGroup.realIPS.get(e.getName().toLowerCase());
                if (!ip.equals(e.getAddress().getHostAddress())) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, config.getString("messages.ipchanged").replace("&", "§"));
                    return;
                }
            }
            if (XinxinWhiteList.getPlayerData().getConfig().getString(e.getName().toLowerCase()) != null)
                return;
            int min = config.getInt("name.min_length");
            int max = config.getInt("name.max_length");
            if (e.getName().length() < min || e.getName().length() > max) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, config.getString("messages.length").replace("&", "§"));
                return;
            }
            String regex = "[" + config.getString("name.regex") + "]*";
            if (!Pattern.matches(regex, e.getName())) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, config.getString("messages.char").replace("&", "§"));
                return;
            }
            if (ipCache.containsKey(e.getName().toLowerCase())) {
                String ip = ipCache.get(e.getName().toLowerCase());
                if (!ip.equals(e.getAddress().getHostAddress())) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, config.getString("messages.ipchanged").replace("&", "§"));
                    return;
                }
            }
            String code = generateCode();
            names.put(code, e.getName());
            nameCache.put(e.getName().toLowerCase(), e.getName());
            ipCache.put(e.getName().toLowerCase(), e.getAddress().getHostAddress());
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = sdf.format(new Date(System.currentTimeMillis() + 600000L));
            if (times.containsKey(code)) {
                time = times.get(code);
            } else {
                times.put(code, time);
            }
            for (String s : XinxinWhiteList.getInstance().getConfig().getStringList("join_tip"))
                sb.append(s.replace("%name%", code).replace("%time%", time).replace("%player%", e.getName())).append("\n");
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, sb.toString().replace("&", "§"));
            String finalCode = code;
            Bukkit.getScheduler().runTaskLater(XinxinWhiteList.getInstance(), () -> {
                names.remove(finalCode);
                times.remove(finalCode);
                nameCache.remove(e.getName().toLowerCase());
                ipCache.remove(e.getName().toLowerCase());
            }, 12000L);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String generateCode() {
        String code;
        if (names.size() < 20) {
            code = genCode(4);
        } else if (names.size() < 200) {
            code = genCode(5);
        } else if (names.size() < 2000) {
            code = genCode(6);
        } else if (names.size() < 5000) {
            code = genCode(7);
        } else if (names.size() < 10000) {
            code = genCode(8);
        } else if (names.size() < 50000) {
            code = genCode(9);
        } else {
            code = genCode(10);
        }
        while (names.containsKey(code)) {
            code = genCode(code.length() + 1);
        }
        return code;
    }
}
