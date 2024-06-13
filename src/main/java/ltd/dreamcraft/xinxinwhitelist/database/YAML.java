package ltd.dreamcraft.xinxinwhitelist.database;

import com.xinxin.BotApi.BotBind;
import ltd.dreamcraft.xinxinwhitelist.BotActionLocal;
import ltd.dreamcraft.xinxinwhitelist.XinxinWhiteList;
import ltd.dreamcraft.xinxinwhitelist.beans.CustomConfig;
import ltd.dreamcraft.xinxinwhitelist.beans.GroupMember;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author haishen668
 */
public class YAML implements PlayerData {
    private static CustomConfig playerData;
    private static CustomConfig mysqlSettings;
    private static Map<String, Long> playerDataCache = new HashMap<>();
    private static List<Runnable> ioTasks = new ArrayList<>();

    public static CustomConfig getPlayerData() {
        return playerData;
    }

    public static void setPlayerData(CustomConfig playerData) {
        YAML.playerData = playerData;
    }

    public static CustomConfig getMysqlSettings() {
        return mysqlSettings;
    }

    public static void setMysqlSettings(CustomConfig mysqlSettings) {
        YAML.mysqlSettings = mysqlSettings;
    }

    public static Map<String, Long> getPlayerDataCache() {
        return playerDataCache;
    }

    public static void setPlayerDataCache(Map<String, Long> playerDataCache) {
        YAML.playerDataCache = playerDataCache;
    }

    public static List<Runnable> getIoTasks() {
        return ioTasks;
    }

    public static void setIoTasks(List<Runnable> ioTasks) {
        YAML.ioTasks = ioTasks;
    }

    public YAML() {
        playerData = new CustomConfig("players.yml", XinxinWhiteList.getInstance());
        XinxinWhiteList.getInstance().getLogger().info("Loaded");
        playerData.save();
        playerData.reload();
        // 异步定时批处理任务
        Bukkit.getScheduler().runTaskTimerAsynchronously(XinxinWhiteList.getInstance(), () -> {
            List<Runnable> tasksToRun = new ArrayList<>(ioTasks);
            ioTasks.clear();
            for (Runnable task : tasksToRun) {
                task.run();
            }
        }, 20L * 60, 20L * 60); // 每分钟运行一次
    }

    public String getPlayerName(String playerName) {
        if (playerDataCache.containsKey(playerName)) {
            return playerDataCache.get(playerName).toString();
        } else {
            Long data = playerData.getConfig().getLong(playerName);
            playerDataCache.put(playerName, data);
            return data.toString();
        }
    }

    public void modifyPlayerData(String playerName, Long newQQ) {
        playerDataCache.put(playerName, newQQ);
        ioTasks.add(() -> {
            playerData.getConfig().set(playerName, newQQ);
            playerData.save();
        });
    }

    public String getPlayerByQQ(String qq) {
        for (String key : playerData.getConfig().getConfigurationSection("").getKeys(false)) {
            if (playerData.getConfig().getString(key).equalsIgnoreCase(qq)) {
                return key;
            }
        }
        return null;
    }

    public void addPlayerData(String playerName, Long qq) {
        playerDataCache.put(playerName, qq);
        ioTasks.add(() -> {
            playerData.getConfig().set(playerName, qq);
            playerData.save();
        });
    }

    public boolean removePlayerByID(String playerName) {
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

    public boolean removePlayerDataByQQ(long qq) {
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

    /**
     * 通过群号删除白名单(如果玩家不在群内但是拥有白名单)
     *
     * @param groupId 群号
     */
    public void removeWhiteListByGroupId(long groupId) {
        Bukkit.getScheduler().runTaskAsynchronously(XinxinWhiteList.getInstance(), () -> {
            AtomicInteger successCount = new AtomicInteger(); // 成功计数器
            List<GroupMember> groupMemberList = BotActionLocal.getGroupMemberList(Long.parseLong(String.valueOf(groupId)));
            ArrayList<Long> memberIdList = new ArrayList<>();
            for (GroupMember member : groupMemberList) {
                long userId = member.getUserId();
                memberIdList.add(userId);
            }
            FileConfiguration config = playerData.getConfig();
            for (String playerName : config.getKeys(false)) {
                long qqId = config.getLong(playerName.toLowerCase());
                if (!memberIdList.contains(qqId)) {
                    boolean b = removePlayerByID(playerName.toLowerCase());
                    if (b) {
                        successCount.getAndIncrement(); // 每次成功删除时 递增计数器
                        XinxinWhiteList.getInstance().getLogger().info("§a[XXW] §c玩家" + playerName + "的绑定数据已经成功删除");
                    } else {
                        XinxinWhiteList.getInstance().getLogger().warning("§a[XXW] §c玩家" + playerName + "的绑定数据删除失败");
                    }
                }
            }
            if (successCount.get() > 0) {
                XinxinWhiteList.getInstance().getLogger().info("§a[XXW] §c成功删除了" + successCount.get() + "个玩家的绑定数据");
            }
        });
    }

    public Map<Boolean, String> tryBind(long qq, String name) {
        FileConfiguration playerData = getPlayerData().getConfig();
        FileConfiguration config = XinxinWhiteList.getInstance().getConfig();
        Map<Boolean, String> map = new HashMap<>();
        String bind = config.getString("messages.bind").replace("%name%", name);
        Set<String> section = playerData.getConfigurationSection("").getKeys(false);
        for (String playerKey : section) {
            if (playerData.getLong(playerKey) == qq) {
                String binded = config.getString("messages.binded").replace("%name%", playerKey);
                map.put(false, binded);
                return map;
            }
        }
        playerData.set(name.toLowerCase(), qq);
        getPlayerData().save();
        map.put(true, bind);
        //强制绑定
        BotBind.setBind(String.valueOf(qq), name);
        return map;
    }

    @Override
    public void reload() {
        if ("YAML".equalsIgnoreCase(XinxinWhiteList.getInstance().getConfig().getString("database.type"))) {
            playerData.reload();
        }
    }

}
