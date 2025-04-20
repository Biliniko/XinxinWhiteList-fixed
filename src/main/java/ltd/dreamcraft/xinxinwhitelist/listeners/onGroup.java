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
import java.io.OutputStream;
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
              e.replyMessage("接口失效,请联系群主");
//              return;
            } else if (level == -2) {
              // 已经在getQQLevel中发送了提示消息，这里直接返回
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
            e.replyMessage(msg);
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
    long groupId = event.getGroup_id();
    
    int groupMethodLevel = -1;
    int apiMethodLevel = -1;
    
    // 方法一：通过群成员信息获取等级
    try {
      GroupMember groupMemberInfo = BotActionLocal.getGroupMemberInfo(groupId, qq, true);
      if (groupMemberInfo != null) {
        String levelStr = groupMemberInfo.getLevel();
        if (levelStr != null && !levelStr.isEmpty()) {
          int level = Integer.parseInt(levelStr);
          if (level > 0) {
            groupMethodLevel = level;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    // 方法二：调用外部API获取等级
    apiMethodLevel = getQQLevelFromExternalAPI(qq, event);
    
    // 如果其中一种方法失败，返回另一种方法的结果
    if (groupMethodLevel == -1 && apiMethodLevel != -1) {
      return apiMethodLevel;
    } else if (apiMethodLevel == -1 && groupMethodLevel != -1) {
      return groupMethodLevel;
    } else if (groupMethodLevel == -1 && apiMethodLevel == -1) {
      // 两种方法都失败
      return -1;
    } else if (apiMethodLevel == -2 || groupMethodLevel == -2) {
      // 如果其中一种方法表示隐藏等级且注册时间不足
      return -2;
    }
    
    // 两种方法都成功获取到等级，返回较高的值
    return Math.max(groupMethodLevel, apiMethodLevel);
  }
  
  // 方法二：通过外部API获取QQ等级
  private int getQQLevelFromExternalAPI(long qq, GroupMessageEvent event) {
    String urlString = "https://api.dwo.cc/api/qqnet?qq=" + qq;
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
        
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
          sb.append(output);
        }
        conn.disconnect();
        
        JSONObject json = new JSONObject(sb.toString());
        
        if (json.has("data")) {
          JSONObject dataObject = json.getJSONObject("data");
          if (dataObject != null && dataObject.has("level")) {
            int level = dataObject.getInt("level");
            
            // 如果level为0，表示用户隐藏了QQ等级
            if (level > 0) {
              return level; // 直接返回等级值
            } else if (level == 0 && dataObject.has("regTime")) {
              // level为0，检查注册时间是否超过三年
              long regTime = dataObject.getLong("regTime");
              long currentTime = System.currentTimeMillis() / 1000; // 当前时间戳（秒）
              long threeYearsInSeconds = 3L * 365 * 24 * 60 * 60; // 三年的秒数
              
              if (currentTime - regTime > threeYearsInSeconds) {
                // 注册时间超过三年，允许通过
                return 999; // 返回一个很大的值以确保通过等级检查
              } else {
                // 注册时间不足三年且隐藏了等级
                event.replyMessage("您的QQ等级已隐藏，且注册时间不足三年，无法验证。请关闭QQ等级隐藏后再尝试。");
                return -2; // 特殊值表示隐藏等级且注册时间不足
              }
            }
          }
          
          // 如果没有level字段但有regTime，检查注册时间
          if (dataObject != null && dataObject.has("regTime")) {
            long regTime = dataObject.getLong("regTime");
            long currentTime = System.currentTimeMillis() / 1000; // 当前时间戳（秒）
            long threeYearsInSeconds = 3L * 365 * 24 * 60 * 60; // 三年的秒数
            
            if (currentTime - regTime > threeYearsInSeconds) {
              // 注册时间超过三年，允许通过
              return 999; // 返回一个很大的值以确保通过等级检查
            } else {
              // 注册时间不足三年且可能隐藏了等级
              event.replyMessage("您的QQ等级可能不足，注册时间也不够三年，无法验证。");
              return -2; // 特殊值表示可能隐藏等级且注册时间不足
            }
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        if (attempt == maxRetries) {
          return -1;  // 返回-1表示查询失败
        }
      }
    }
    
    return -1;  // 所有尝试都失败
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
