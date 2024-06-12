package ltd.dreamcraft.xinxinwhitelist;

import com.xinxin.PluginBasicTool.BotData;
import com.xinxin.PluginBasicTool.JsonAction;
import ltd.dreamcraft.xinxinwhitelist.beans.GroupMember;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * @author haishen668
 * @version 1.0
 * @description: TODO
 * @date 2024/4/11 9:56
 */
public class BotActionLocal {
    public long sendPrivateMessage(long user_id, String message, boolean... auto_escape) {
        return this.getData(BotData.getClient().sendData(
                        (new JsonAction("send_private_msg"))
                                .add("user_id", user_id)
                                .add("message", message)
                                .add("auto_escape", auto_escape), true))
                .getInt("message_id");
    }

    /**
     * 获取群成员信息
     *
     * @param group_id    群号
     * @param user_id     群成员QQ号
     * @param auto_escape 是否缓存
     * @return GroupMember对象
     */
    public static GroupMember getGroupMemberInfo(long group_id, long user_id, boolean... auto_escape) {
        JSONObject data = getData(BotData.getClient().sendData(
                (new JsonAction("get_group_member_info"))
                        .add("group_id", group_id)
                        .add("user_id", user_id)
                        .add("no_cache", auto_escape), true));
        return createGroupMember(data);
    }

    public static List<GroupMember> getGroupMemberList(long group_id) {
        JSONArray data = getJsonArray(BotData.getClient().sendData(
                (new JsonAction("get_group_member_list"))
                        .add("group_id", group_id), true));
        List<GroupMember> groupMembers = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i);
            GroupMember groupMember = createGroupMember(jsonObject);
            groupMembers.add(groupMember);
        }
        return groupMembers;
    }

    private static GroupMember createGroupMember(JSONObject data) {
        long groupId = data.getLong("group_id");
        long userId = data.getLong("user_id");
        String nickname = data.getString("nickname");
        String card = data.getString("card");
        String sex = data.getString("sex");
        int age = data.getInt("age");
        String area = data.has("area") ? data.getString("area") : null;
        int joinTime = data.getInt("join_time");
        int lastSentTime = data.getInt("last_sent_time");
        String level = data.getString("qq_level");
        String role = data.getString("role");
        boolean unfriendly = data.getBoolean("unfriendly");
        String title = data.has("title") ? data.getString("title") : null;
        int titleExpireTime = data.has("title_expire_time") ? data.getInt("title_expire_time") : 0;
        boolean cardChangeable = data.getBoolean("card_changeable");
        return new GroupMember(groupId, userId, nickname, card, sex, age, area, joinTime, lastSentTime, level, role, unfriendly, title, titleExpireTime, cardChangeable);
    }


    public static JSONObject getData(JSONObject json) {
        return JSONObject.fromObject(json.getString("data"));
    }

    public static JSONArray getJsonArray(JSONObject json) {
        return JSONArray.fromObject(json.getString("data"));
    }
}
