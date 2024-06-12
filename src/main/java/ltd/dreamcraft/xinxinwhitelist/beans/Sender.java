package ltd.dreamcraft.xinxinwhitelist.beans;

/**
 * @author haishen668
 * @version 1.0
 * @date 2024/4/9 16:59
 */
public class Sender {
    protected long user_id;
    protected String nickname;
    protected String sex;
    protected int age = 0;
    protected String card;
    protected String area;
    protected String level;
    protected String role;
    protected String title;

    public Sender() {
    }

    public Sender(long userId, String senderNickname, String senderCard) {
        this.user_id = userId;
        this.nickname = senderNickname;
        this.card = senderCard;
    }

    public long getUserID() {
        return this.user_id;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getSex() {
        return this.sex;
    }

    public int getAge() {
        return this.age;
    }

    public String getTitle() {
        return this.title;
    }

    public String getRole() {
        return this.role;
    }

    public String getLevel() {
        return this.level;
    }

    public String getArea() {
        return this.area;
    }

    public String getCard() {
        return this.card;
    }

    public String getName() {
        return this.card.trim().isEmpty() ? this.nickname : this.card;
    }

    public String getDisplayName() {
        return this.card.isEmpty() ? this.nickname : this.card;
    }
}