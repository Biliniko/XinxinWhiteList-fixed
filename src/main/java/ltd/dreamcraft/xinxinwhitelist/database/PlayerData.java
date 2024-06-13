package ltd.dreamcraft.xinxinwhitelist.database;

import java.util.Map;

/**
 * @author shuyi
 */
public interface PlayerData {
    String getPlayerName(String playerName);

    void modifyPlayerData(String playerName, Long newQQ);

    String getPlayerByQQ(String qq);

    void addPlayerData(String playerName, Long qq);

    boolean removePlayerByID(String playerName);

    boolean removePlayerDataByQQ(long qq);

    void removeWhiteListByGroupId(long groupId);

    void reload();

    Map<Boolean, String> tryBind(long qq, String name);
}
