package ltd.dreamcraft.xinxinwhitelist.test;

import ltd.dreamcraft.xinxinwhitelist.BotActionLocal;
import ltd.dreamcraft.xinxinwhitelist.beans.GroupMember;
import org.bukkit.command.CommandSender;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * QQ等级测试工具类
 * 用于测试不同方法获取QQ等级的情况
 */
public class QQLevelTest {

    /**
     * 测试QQ等级
     * @param sender 命令发送者
     * @param qq 要测试的QQ号
     * @param groupId 群号（可选）
     */
    public static void testQQLevel(CommandSender sender, long qq, Long groupId) {
        // 测试结果汇总
        boolean groupMethodSuccess = false;
        boolean apiMethodSuccess = false;
        int groupLevel = -1;
        int apiLevel = -1;
        
        sender.sendMessage("§a[测试] §b开始测试QQ: " + qq + " 的等级获取方法");
        
        // 测试方法一：通过群成员信息获取
        if (groupId != null) {
            try {
                sender.sendMessage("§a[测试] §b===== 群成员API测试结果 =====");
                GroupMember groupMemberInfo = BotActionLocal.getGroupMemberInfo(groupId, qq, true);
                if (groupMemberInfo != null) {
                    String levelStr = groupMemberInfo.getLevel();
                    sender.sendMessage("§a[测试] §b昵称: " + groupMemberInfo.getNickname());
                    sender.sendMessage("§a[测试] §b群名片: " + groupMemberInfo.getCard());
                    if (levelStr != null && !levelStr.isEmpty()) {
                        groupLevel = Integer.parseInt(levelStr);
                        sender.sendMessage("§a[测试] §bQQ等级: " + groupLevel);
                        if (groupLevel > 0) {
                            sender.sendMessage("§a[测试] §a该用户的QQ等级可通过群成员API获取");
                            groupMethodSuccess = true;
                        } else {
                            sender.sendMessage("§a[测试] §c该用户的QQ等级为0或无法通过群成员API获取");
                        }
                    } else {
                        sender.sendMessage("§a[测试] §c无法获取该用户的QQ等级信息");
                    }
                } else {
                    sender.sendMessage("§a[测试] §c获取群成员信息失败，该用户可能不在指定群内");
                }
            } catch (Exception e) {
                sender.sendMessage("§a[测试] §c群成员API测试过程中发生错误: " + e.getMessage());
            }
        } else {
            sender.sendMessage("§a[测试] §c未提供群号，跳过群成员API测试");
        }
        
        // 测试方法二：通过外部API获取
        try {
            sender.sendMessage("§a[测试] §b===== 外部API测试结果 =====");
            String urlString = "https://api.dwo.cc/api/qqnet?qq=" + qq;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            if (conn.getResponseCode() != 200) {
                sender.sendMessage("§a[测试] §c外部API请求失败，错误码: " + conn.getResponseCode());
                return;
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
                
                if (dataObject != null) {
                    sender.sendMessage("§a[测试] §b昵称: " + dataObject.optString("nickname", "未知"));
                    
                    if (dataObject.has("level")) {
                        apiLevel = dataObject.getInt("level");
                        sender.sendMessage("§a[测试] §bQQ等级: " + apiLevel);
                        
                        if (apiLevel > 0) {
                            sender.sendMessage("§a[测试] §a该用户的QQ等级可通过外部API获取");
                            apiMethodSuccess = true;
                        } else if (apiLevel == 0) {
                            sender.sendMessage("§a[测试] §c该QQ号可能隐藏了等级");
                            
                            // 检查注册时间
                            if (dataObject.has("regTime")) {
                                long regTime = dataObject.getLong("regTime");
                                long currentTime = System.currentTimeMillis() / 1000;
                                long threeYearsInSeconds = 3L * 365 * 24 * 60 * 60;
                                
                                sender.sendMessage("§a[测试] §b注册时间: " + new Date(regTime * 1000L));
                                
                                if (currentTime - regTime > threeYearsInSeconds) {
                                    sender.sendMessage("§a[测试] §a注册时间超过三年，允许通过验证");
                                    apiMethodSuccess = true;
                                } else {
                                    sender.sendMessage("§a[测试] §c注册时间不足三年，不允许通过验证");
                                }
                            } else {
                                sender.sendMessage("§a[测试] §c未找到注册时间信息");
                            }
                        }
                    } else {
                        sender.sendMessage("§a[测试] §c未找到等级信息");
                    }
                    
                    // 输出其他有用信息
                    if (dataObject.has("regTime")) {
                        sender.sendMessage("§a[测试] §b注册时间: " + new Date(dataObject.getLong("regTime") * 1000L));
                    }
                    if (dataObject.has("sing")) {
                        sender.sendMessage("§a[测试] §b个性签名: " + dataObject.getString("sing"));
                    }
                }
            } else {
                sender.sendMessage("§a[测试] §c外部API返回数据格式错误");
            }
        } catch (Exception e) {
            sender.sendMessage("§a[测试] §c外部API测试过程中发生错误: " + e.getMessage());
        }
        
        // 测试结论
        sender.sendMessage("§a[测试] §b===== 测试结论 =====");
        if (groupMethodSuccess && apiMethodSuccess) {
            if (groupLevel == apiLevel) {
                sender.sendMessage("§a[测试] §a两种方法均可获取该QQ等级，且结果一致: " + groupLevel);
            } else {
                sender.sendMessage("§a[测试] §e两种方法均可获取该QQ等级，但结果不一致");
                sender.sendMessage("§a[测试] §e群成员API获取结果: " + groupLevel);
                sender.sendMessage("§a[测试] §e外部API获取结果: " + apiLevel);
                sender.sendMessage("§a[测试] §e建议优先使用数值较高的结果");
            }
        } else if (groupMethodSuccess) {
            sender.sendMessage("§a[测试] §b仅群成员API可获取该QQ等级: " + groupLevel);
            sender.sendMessage("§a[测试] §b建议使用群成员API获取等级");
        } else if (apiMethodSuccess) {
            sender.sendMessage("§a[测试] §b仅外部API可获取该QQ等级或通过注册时间判定");
            sender.sendMessage("§a[测试] §b建议使用外部API获取等级");
        } else {
            sender.sendMessage("§a[测试] §c两种方法均无法获取该QQ等级，可能需要其他方法");
        }
    }
    
    /**
     * 获取QQ等级（综合判断方法）
     * @param qq QQ号
     * @param groupId 群号（可选）
     * @return QQ等级，-1表示获取失败，-2表示隐藏等级且注册时间不足
     */
    public static int getQQLevel(long qq, Long groupId) {
        int groupMethodLevel = -1;
        int apiMethodLevel = -1;
        
        // 方法一：通过群成员信息获取等级
        if (groupId != null) {
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
        }
        
        // 方法二：通过外部API获取
        try {
            String urlString = "https://api.dwo.cc/api/qqnet?qq=" + qq;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            if (conn.getResponseCode() != 200) {
                apiMethodLevel = -1;
            } else {
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
                            apiMethodLevel = level; // 保存等级值
                        } else if (level == 0 && dataObject.has("regTime")) {
                            // level为0，检查注册时间是否超过三年
                            long regTime = dataObject.getLong("regTime");
                            long currentTime = System.currentTimeMillis() / 1000;
                            long threeYearsInSeconds = 3L * 365 * 24 * 60 * 60;
                            
                            if (currentTime - regTime > threeYearsInSeconds) {
                                // 注册时间超过三年，允许通过
                                apiMethodLevel = 999; // 返回一个很大的值以确保通过等级检查
                            } else {
                                // 注册时间不足三年且隐藏了等级
                                apiMethodLevel = -2; // 特殊值表示隐藏等级且注册时间不足
                            }
                        }
                    }
                    
                    // 如果没有level字段但有regTime，检查注册时间
                    if ((apiMethodLevel == -1) && dataObject != null && dataObject.has("regTime")) {
                        long regTime = dataObject.getLong("regTime");
                        long currentTime = System.currentTimeMillis() / 1000;
                        long threeYearsInSeconds = 3L * 365 * 24 * 60 * 60;
                        
                        if (currentTime - regTime > threeYearsInSeconds) {
                            // 注册时间超过三年，允许通过
                            apiMethodLevel = 999;
                        } else {
                            // 注册时间不足三年
                            apiMethodLevel = -2;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            apiMethodLevel = -1;
        }
        
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
} 