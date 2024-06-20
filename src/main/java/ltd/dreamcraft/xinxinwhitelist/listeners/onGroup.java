package ltd.dreamcraft.xinxinwhitelist.listeners;


import com.xinxin.BotApi.BotBind;
import com.xinxin.BotEvent.GroupMessageEvent;
import fr.xephi.authme.api.v3.AuthMeApi;
import ltd.dreamcraft.xinxinwhitelist.BotActionLocal;
import ltd.dreamcraft.xinxinwhitelist.XinxinWhiteList;
import ltd.dreamcraft.xinxinwhitelist.beans.GroupMember;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class onGroup implements Listener {
  public static Set<String> playersMap = ConcurrentHashMap.newKeySet();
  public static ConcurrentHashMap<String, String> realNames = new ConcurrentHashMap<>();
  public static ConcurrentHashMap<String, String> realIPS = new ConcurrentHashMap<>();

  private static boolean isLong(String msg) {
    try {
      Long.parseLong(msg);
      return true;
    } catch (Exception var2) {
      return false;
    }
  }

  @EventHandler
  public void onGroupMsg(GroupMessageEvent e) {
    FileConfiguration config = XinxinWhiteList.getInstance().getConfig();
    List<Long> groups = config.getLongList("groups");
    long qq = e.getUser_id();
    if (groups.contains(e.getGroup_id())) {
      String code = e.getMessage().trim();
      if (code.length() == 4 && isLong(code)) {
        if (!onJoin.names.containsKey(code)) {
          e.replyMessage(config.getString("messages.invalid_code"));
        } else {
          int levelLimitMin = config.getInt("level_limit_min", 0);
          if (levelLimitMin != 0) {
            int level = getQQLevel(e);
            if (level == -1) {
              e.replyMessage("功能失效请联系插件作者QQ: 2821396723");
              return;
            }
            if (level < levelLimitMin) {
              e.replyMessage(config.getString("messages.level_limit", "你的等级不够无法申请白名单"));
              return;
            }
          }


          String name = onJoin.names.get(code);
          Map<Boolean, String> bindResult = XinxinWhiteList.getPlayerData().tryBind(qq, name);
          boolean result = bindResult.keySet().stream().findAny().get();
          String msg = bindResult.values().stream().findAny().get();
          onJoin.names.remove(code);
          Bukkit.getScheduler().runTaskLaterAsynchronously(XinxinWhiteList.getInstance(), () -> {
            e.replyMessage(msg);
          }, 30L);
          if (result) {
            playersMap.add(name);
            String realName = onJoin.nameCache.get(name);
            if (realName != null) realNames.put(name, realName);
            String ip = onJoin.ipCache.get(name);
            if (ip != null) realIPS.put(name, ip);
            Bukkit.getScheduler().runTaskLater(XinxinWhiteList.getInstance(), () -> {
              realIPS.remove(name);
              removePlayer(name);
            }, 12000L);
          }
        }
      }
    }
  }

  private int getQQLevel(GroupMessageEvent event) {
    long qq = event.getUser_id();
    GroupMember groupMemberInfo = BotActionLocal.getGroupMemberInfo(event.getGroup_id(), event.getUser_id(), true);
    int level = Integer.parseInt(groupMemberInfo.getLevel());
    if (level > 0) {
      return level;
    }

    String urlString = "https://api.52hyjs.com/api/level?qq=" + qq;
    int maxRetries = 3;

    for (int attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() != 200) {
          throw new RuntimeException("HTTP GET Request Failed with Error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
          sb.append(output);
        }
        conn.disconnect();

        JSONObject json = new JSONObject(sb.toString());

        if (json.has("data")) {
          JSONObject dataObject = json.getJSONObject("data");
          if (dataObject != null && dataObject.has("iQQLevel")) {
            return dataObject.getInt("iQQLevel");
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        if (attempt == maxRetries) {
          return -1;  // 返回-1表示查询失败
        }
      }
    }

    return -1;
  }

  public static void removePlayer(String name) {
    if (!playersMap.contains(name) || !XinxinWhiteList.getInstance().getConfig().getBoolean("authme.force_reg"))
      return;
    playersMap.remove(name);
    if (!AuthMeApi.getInstance().isRegistered(name.toLowerCase())) {
      if (realNames.containsKey(name)) name = realNames.get(name);
      Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
      if (plugin != null) {
        // Essentials插件相关代码
      }
//      FileConfiguration playerData = XinxinWhiteList.getPlayerName().getConfig();
//      playerData.set(name.toLowerCase(), null);
      XinxinWhiteList.getPlayerData().removePlayerByID(name.toLowerCase());
      BotBind.unBind(BotBind.getBindQQ(name));
    }
  }


}
