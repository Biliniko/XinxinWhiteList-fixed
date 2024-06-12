package ltd.dreamcraft.xinxinwhitelist;

/**
 * @Author: haishen668
 * @CreateTime: 2024-06-01
 * @Description:
 * @Version: 1.0
 */

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private File configFile;
    private FileConfiguration config;

    public ConfigManager(String fileName) {
        XinxinWhiteList plugin = XinxinWhiteList.getInstance();
        configFile = new File(plugin.getDataFolder(), fileName);

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}
