package ltd.dreamcraft.xinxinwhitelist.database;

import com.xinxin.BotApi.BotBind;
import ltd.dreamcraft.xinxinwhitelist.XinxinWhiteList;
import org.bukkit.configuration.file.FileConfiguration;

public class yaml {
    /**
     * 将该qq号码对应的玩家绑定信息删除 包括amb和amw
     * 删除成功返回true qq号获取不到
     * 玩家信息就返回false
     *
     * @param qq qq号码
     */
    public static boolean removePlayerBindDataByQQ(long qq) {
        FileConfiguration playerData = XinxinWhiteList.getPlayerData().getConfig();
        //获取玩家名字，通过这个qq
        String playerName = BotBind.getBindPlayerName(String.valueOf(qq));
        if (playerName == null) {
            return false;
        }
        //amw解绑
        playerData.set(playerName.toLowerCase(), null);
        //amb解绑
        BotBind.unBind(String.valueOf(qq));
        XinxinWhiteList.getPlayerData().save();
        return true;
    }

    /**
     * 将该玩家绑定信息删除 包括amb和amw
     * 删除成功返回true qq号获取不到玩家信息
     * 就返回false
     *
     * @param name 玩家id
     */
    public static boolean removePlayerBindDataByName(String name) {
        if (yaml.getQQ(name.toLowerCase()) == null) {
            return false;
        }
        FileConfiguration playerData = XinxinWhiteList.getPlayerData().getConfig();
        //amw解绑
        playerData.set(name.toLowerCase(), null);
        //amb解绑
        String bindQQ = BotBind.getBindQQ(name);
        BotBind.unBind(bindQQ);
        XinxinWhiteList.getPlayerData().save();
        return true;
    }

    /**
     * 给玩家绑定QQ
     * 删除成功返回true qq号获取不到
     * 玩家信息就返回false
     *
     * @param qq qq号码
     */
    public static boolean addPlayerBindDataByQQ(long qq, String name) {
        FileConfiguration playerData = XinxinWhiteList.getPlayerData().getConfig();
        /*
获取玩家名字，通过这个qq
        UUID uuid = Bot.getApi().getPlayer(qq);
        if (!(uuid == null)) {
            return false;
        }
         */
        //amw 绑定 不含amb绑定
        playerData.set(name.toLowerCase(), qq);
        XinxinWhiteList.getPlayerData().save();
        return true;
    }

    /**
     * 修改玩家所绑定的qq号码
     *
     * @param nqq  新的qq号
     * @param name 需要修改绑定qq账号的玩家id
     * @return 修改成功返回true
     */
    public static void editPlayerBindDataByQQ(long nqq, String name) {
        FileConfiguration playerData = XinxinWhiteList.getPlayerData().getConfig();
        //amw绑定
        playerData.set(name.toLowerCase(), nqq);
        XinxinWhiteList.getPlayerData().save();
    }

    public static Long getQQ(String name) {
        FileConfiguration playerData = XinxinWhiteList.getPlayerData().getConfig();
        long qq = playerData.getLong(name);//获取到玩家的qq号
        if (qq == 0) {
            return null;
        }
        return qq;
    }
}
