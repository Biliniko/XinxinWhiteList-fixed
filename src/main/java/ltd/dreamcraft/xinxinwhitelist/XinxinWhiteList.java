package ltd.dreamcraft.xinxinwhitelist;

import com.xinxin.BotApi.BotBind;
import ltd.dreamcraft.xinxinwhitelist.beans.CustomConfig;
import ltd.dreamcraft.xinxinwhitelist.beans.GroupMember;
import ltd.dreamcraft.xinxinwhitelist.listeners.onGroup;
import ltd.dreamcraft.xinxinwhitelist.listeners.onJoin;
import ltd.dreamcraft.xinxinwhitelist.listeners.onLogin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class XinxinWhiteList extends JavaPlugin {
  private static XinxinWhiteList instance;
  private static CustomConfig playerData;
  private static CustomConfig mysqlSettings;
  private static Map<String, Long> playerDataCache = new HashMap<>();
  private static List<Runnable> ioTasks = new ArrayList<>();

  public static XinxinWhiteList getInstance() {
    return instance;
  }

  public static CustomConfig getPlayerData() {
    return playerData;
  }

  public static CustomConfig getMysqlSettings() {
    return mysqlSettings;
  }

  @Override
  public void onDisable() {
    for (String name : onGroup.playersMap) {
      onGroup.removePlayer(name);
    }
    // 批量保存缓存中的数据
//    flushCache();
  }

  @Override
  public void onEnable() {
    instance = this;
    saveDefaultConfig();
    registerEvent(new onJoin());
    registerEvent(new onGroup());
    registerEvent(new onLogin());
    playerData = new CustomConfig("players.yml", this);
    getLogger().info("Loaded");
    playerData.save();
    playerData.reload();

    Bukkit.getScheduler().runTaskTimer(this, () -> {
      if (System.currentTimeMillis() - onLogin.last > 2000L) {
        onLogin.attacks = 0;
      }
    }, 40L, 40L);

    for (Player p : Bukkit.getOnlinePlayers()) {
      if (p.hasPermission("admin")) {
        onLogin.admins.add(p.getUniqueId());
      }
    }

    // 异步定时批处理任务
    Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
      List<Runnable> tasksToRun = new ArrayList<>(ioTasks);
      ioTasks.clear();
      for (Runnable task : tasksToRun) {
        task.run();
      }
    }, 20L * 60, 20L * 60); // 每分钟运行一次
  }

  private void registerEvent(Listener l) {
    Bukkit.getServer().getPluginManager().registerEvents(l, this);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
      reloadConfig();
      playerData.reload();
      sender.sendMessage("§a[XXW] 配置文件已经重新载入");
      return true;
    }
    if (args.length == 2 && "check".equalsIgnoreCase(args[0])) {
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
        String qq = getPlayerData(args[1].toLowerCase());
        sender.sendMessage("§a[XXW] 此玩家的QQ为: " + qq);
      });
      return true;
    }
    if (args.length == 2 && "qq".equalsIgnoreCase(args[0])) {
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
        String player = getPlayerByQQ(args[1]);
        if (player != null) {
          sender.sendMessage("§a[XXW] 此QQ用户所绑定的玩家为: " + player);
        } else {
          sender.sendMessage("§a[XXW] 此QQ没有申请白名单的记录");
        }
      });
      return true;
    }
    if (args.length == 2 && "bdqq".equalsIgnoreCase(args[0])) {
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
        try {
          String playerName = BotBind.getBindPlayerName(args[1]);
          sender.sendMessage("§a[XXW] 此QQ所绑定的玩家为: " + Bukkit.getOfflinePlayer(playerName).getName());
        } catch (Exception e) {
          sender.sendMessage("§a[XXW] §c发生错误!");
        }
      });
      return true;
    }
    if (args.length == 2 && "bdcheck".equalsIgnoreCase(args[0])) {
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
        if (offlinePlayer.getName() != null) {
          sender.sendMessage("§a[XXW] 此玩家绑定的QQ为: " + BotBind.getBindQQ(offlinePlayer.getName()));
        } else {
          sender.sendMessage("§a[XXW] §c没有这个玩家!");
        }
      });
      return true;
    }
    if (args.length == 3 && "modify".equalsIgnoreCase(args[0])) {
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
        try {
          String name = args[1].toLowerCase();
          Long newQQ = Long.parseLong(args[2]);
          modifyPlayerData(name, newQQ);
          sender.sendMessage("§a[XXW] §a玩家" + name + "的QQ已经成功更改为: " + newQQ);
        } catch (NumberFormatException e) {
          sender.sendMessage("§a[XXW] §c请输入数字");
        }
      });
      return true;
    }
    if (args.length == 3 && "forcebind".equalsIgnoreCase(args[0])) {
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
        try {
          String name = args[1].toLowerCase();
          Long qq = Long.parseLong(args[2]);
          if (getPlayerByQQ(qq.toString()) == null) {
            addPlayerData(name, qq);
            sender.sendMessage("§a[XXW] §a玩家" + name + "和QQ: " + qq + "已经成功绑定");
          } else {
            sender.sendMessage("§a[XXW] §c此QQ已经绑定玩家");
          }
        } catch (NumberFormatException e) {
          sender.sendMessage("§a[XXW] §c请输入数字");
        }
      });
      return true;
    }
    if (args.length == 2 && "delete".equalsIgnoreCase(args[0])) {
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
        String name = args[1].toLowerCase();
        if (removePlayerData(name)) {
          sender.sendMessage("§a[XXW] §c玩家" + name + "的绑定数据已经成功删除");
        } else {
          sender.sendMessage("§a[XXW] §c玩家" + name + "没有绑定过QQ");
        }
      });
      return true;
    }
    if (args.length == 2 && "deleteByQQ".equalsIgnoreCase(args[0])) {
      Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
        try {
          long qq = Long.parseLong(args[1]);
          if (removePlayerDataByQQ(qq)) {
            sender.sendMessage("§a[XXW] §cQQ" + qq + "绑定的玩家数据已经成功删除");
          } else {
            sender.sendMessage("§a[XXW] §cQQ" + qq + "没有绑定过玩家");
          }
        } catch (NumberFormatException e) {
          sender.sendMessage("§a[XXW] §c请输入有效的QQ号码");
        }
      });
      return true;
    }
    if (args.length == 2 && "checkGroup".equalsIgnoreCase(args[0])) {
      AtomicInteger successCount = new AtomicInteger(); // 成功计数器
      if (getConfig().getBoolean("kick_unbind")) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
          try {
            List<GroupMember> groupMemberList = BotActionLocal.getGroupMemberList(Long.parseLong(args[1]));
            ArrayList<Long> memberIdList = new ArrayList<>();
            for (GroupMember member : groupMemberList) {
              long userId = member.getUserId();
              memberIdList.add(userId);
            }
            FileConfiguration config = playerData.getConfig();
            for (String playerName : config.getKeys(false)) {
              long qqId = config.getLong(playerName.toLowerCase());
              if (!memberIdList.contains(qqId)) {
                boolean b = removePlayerData(playerName.toLowerCase());
                if (b) {
                  successCount.getAndIncrement(); // 每次成功删除时 递增计数器
                  getLogger().info("§a[XXW] §c玩家" + playerName + "的绑定数据已经成功删除");
                } else {
                  getLogger().warning("§a[XXW] §c玩家" + playerName + "的绑定数据删除失败");
                }
              }
            }
          } catch (NumberFormatException e) {
            sender.sendMessage("§a[XXW] §c请输入有效的QQ群号码");
          }
          getLogger().info("§a[XXW] §c成功删除的玩家数据总数: " + successCount);
        });
      }
      return true;
    }
    sender.sendMessage("§a/xxw reload —— 重新载入配置文件");
    sender.sendMessage("§a/xxw check [玩家] —— 查看玩家QQ");
    sender.sendMessage("§a/xxw qq [QQ号码] —— 查看QQ所绑定的玩家");
    sender.sendMessage("§a/xxw bdqq [QQ号码] —— [bot内]查看QQ所绑定的玩家");
    sender.sendMessage("§a/xxw bdcheck [玩家] —— [bot内]查看玩家绑定的QQ");
    sender.sendMessage("§a/xxw modify [玩家] [QQ] —— 更改玩家所绑定的QQ");
    sender.sendMessage("§a/xxw forcebind [玩家] [QQ] —— 手动增加新的玩家和QQ数据");
    sender.sendMessage("§c/xxw delete [玩家] —— 删除玩家绑定数据");
    sender.sendMessage("§c/xxw deleteByQQ [QQ] —— 删除玩家绑定数据");
    sender.sendMessage("§c注意！！！所有涉及绑定解绑白名单的操作都会有延迟(1min)");
    return true;
  }

  private void flushCache() {
    // 批量保存缓存中的数据
    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
      for (Map.Entry<String, Long> entry : playerDataCache.entrySet()) {
        playerData.getConfig().set(entry.getKey(), entry.getValue());
      }
      playerData.save();
    });
  }

  private String getPlayerData(String playerName) {
    if (playerDataCache.containsKey(playerName)) {
      return playerDataCache.get(playerName).toString();
    } else {
      Long data = playerData.getConfig().getLong(playerName);
      playerDataCache.put(playerName, data);
      return data.toString();
    }
  }

  private void modifyPlayerData(String playerName, Long newQQ) {
    playerDataCache.put(playerName, newQQ);
    ioTasks.add(() -> {
      playerData.getConfig().set(playerName, newQQ);
      playerData.save();
    });
  }

  private String getPlayerByQQ(String qq) {
    for (String key : playerData.getConfig().getConfigurationSection("").getKeys(false)) {
      if (playerData.getConfig().getString(key).equalsIgnoreCase(qq)) {
        return key;
      }
    }
    return null;
  }

  public static void addPlayerData(String playerName, Long qq) {
    playerDataCache.put(playerName, qq);
    ioTasks.add(() -> {
      playerData.getConfig().set(playerName, qq);
      playerData.save();
    });
  }

  public static boolean removePlayerData(String playerName) {
    for (String key : playerData.getConfig().getConfigurationSection("").getKeys(false)) {
      if (key.equalsIgnoreCase(playerName)) {
        playerDataCache.remove(key);
        ioTasks.add(() -> {
          playerData.getConfig().set(key, null);
          playerData.save();
        });
        return true;
      }
    }
    return false;
  }

  private boolean removePlayerDataByQQ(long qq) {
    for (String key : playerData.getConfig().getConfigurationSection("").getKeys(false)) {
      if (playerData.getConfig().getLong(key) == qq) {
        playerDataCache.remove(key);
        ioTasks.add(() -> {
          playerData.getConfig().set(key, null);
          playerData.save();
        });
        return true;
      }
    }
    return false;
  }
}
