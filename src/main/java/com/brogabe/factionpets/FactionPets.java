package com.brogabe.factionpets;

import co.aikar.commands.PaperCommandManager;
import com.brogabe.factionpets.commands.PetCommand;
import com.brogabe.factionpets.configuration.FactionsConfig;
import com.brogabe.factionpets.listeners.FactionListeners;
import com.brogabe.factionpets.listeners.PlayerListener;
import com.brogabe.factionpets.menus.FactionsMenu;
import com.brogabe.factionpets.modules.ModuleManager;
import com.brogabe.factionpets.utils.FactionsUtil;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class FactionPets extends JavaPlugin {

    @Getter
    private FactionsConfig factionsConfig;

    @Getter
    private ModuleManager moduleManager;

    @Getter
    private Economy economy;

    private final Map<String, FactionsMenu> factionsMenuMap = new HashMap<>();

    /**
     * Auto-save feature to save robots every 5 minutes. Loops through every faction
     * file. On InventoryOpen, auto-save for that singular faction. Great for performance
     */

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        factionsConfig = new FactionsConfig(this);

        if (!setupEconomy() ) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        moduleManager = new ModuleManager(this);

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FactionListeners(this), this);

        // Register the Commands
        PaperCommandManager manager = new PaperCommandManager(this);

        manager.registerCommand(new PetCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public Optional<FactionsMenu> getOrCreateFactionsMenu(Player player) {
        if(!FactionsUtil.hasFaction(player)) {
            return Optional.empty();
        }

        String id = FactionsUtil.getFactionID(player);

        if(factionsMenuMap.containsKey(id)) return Optional.of(factionsMenuMap.get(id));

        FactionsMenu factionsMenu = new FactionsMenu(this, player);

        factionsMenuMap.put(id, factionsMenu);

        return Optional.of(factionsMenu);
    }

    public void removeFactionsMenu(String id) {
        if(!factionsMenuMap.containsKey(id)) return;

        FactionsMenu factionsMenu = factionsMenuMap.get(id);
        factionsMenu.closeAll();

        factionsMenuMap.remove(id);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
}
