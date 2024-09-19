package ltd.dreamcraft.xinxinwhitelist.beans;

/**
 * @author haishen668
 * @version 1.0
 * @date 2024/4/11 10:17
 */
public class GroupMember {
    private long groupId;
    private long userId;
    private String nickname;
    private String card;
    private String sex;
    private int age;
    private String area;
    private int joinTime;
    private int lastSentTime;
    private String level;
    private String role;
    private boolean unfriendly;
    private String title;
    private int titleExpireTime;
    private boolean cardChangeable;

    public GroupMember(long groupId, long userId, String nickname, String card, String sex, int age, String area, int joinTime, int lastSentTime, String level, String role, boolean unfriendly, String title, int titleExpireTime, boolean cardChangeable) {
        this.groupId = groupId;
        this.userId = userId;
        this.nickname = nickname;
        this.card = card;
        this.sex = sex;
        this.age = age;
        this.area = area;
        this.joinTime = joinTime;
        this.lastSentTime = lastSentTime;
        this.level = level;
        this.role = role;
        this.unfriendly = unfriendly;
        this.title = title;
        this.titleExpireTime = titleExpireTime;
        this.cardChangeable = cardChangeable;
    }

    public long getGroupId() {
        return groupId;
    }

    public long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getCard() {
        return card;
    }

    public String getSex() {
        return sex;
    }

    public int getAge() {
        return age;
    }

    public String getArea() {
        return area;
    }

    public int getJoinTime() {
        return joinTime;
    }

    public int getLastSentTime() {
        return lastSentTime;
    }

    public String getLevel() {
        return level;
    }

    public String getRole() {
        return role;
    }

    public boolean isUnfriendly() {
        return unfriendly;
    }

    public String getTitle() {
        return title;
    }

    public int getTitleExpireTime() {
        return titleExpireTime;
    }

    public boolean isCardChangeable() {
        return cardChangeable;
    }
// Getter and Setter methods for each field
}
