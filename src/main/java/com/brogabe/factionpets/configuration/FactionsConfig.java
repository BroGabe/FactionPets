package com.brogabe.factionpets.configuration;

import com.brogabe.factionpets.FactionPets;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FactionsConfig {

    private final FactionPets plugin;

    public FactionsConfig(FactionPets plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getFactionConfig(String id) {
        File file = getFactionFile(id);

        if(!file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(getFactionFile(id));
            setupConfig(id, config);
            return config;
        }

        return YamlConfiguration.loadConfiguration(getFactionFile(id));
    }

    public File getFactionFile(String id) {
        File folder = new File(plugin.getDataFolder(), "factions");
        if(!folder.exists()) folder.mkdirs();

        return new File(folder, id +".yml");
    }

    public void saveConfig(String id, FileConfiguration config) {
        try {
            config.save(getFactionFile(id));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(String id) {
        File folder = new File(plugin.getDataFolder(), "factions");
        if(!folder.exists()) folder.mkdirs();

        File file = new File(folder, id +".yml");

        if(!file.exists()) return;

        System.out.println((file.delete()) ? "[FactionPets] Faction file deleted." : "[FactionPets] Error deleting file.");
    }

    private void setupConfig(String id, FileConfiguration config) {
        config.set("pet-1", "");
        config.set("pet-2", "");
        config.set("pet-3", "");

        saveConfig(id, config);
    }
}
