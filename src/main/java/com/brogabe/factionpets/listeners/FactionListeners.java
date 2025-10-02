package com.brogabe.factionpets.listeners;

import com.brogabe.factionpets.FactionPets;
import com.brogabe.factionpets.menus.FactionsMenu;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionCreateEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class FactionListeners implements Listener {

    private final FactionPets plugin;

    public FactionListeners(FactionPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreate(FactionCreateEvent event) {
        plugin.getOrCreateFactionsMenu(event.getFPlayer().getPlayer());
    }

    @EventHandler
    public void onDisband(FactionDisbandEvent event) {
        plugin.removeFactionsMenu(event.getFaction().getId());
    }

    @EventHandler
    public void onLeave(FPlayerLeaveEvent event) {
        Player player = event.getfPlayer().getPlayer();

        FactionsMenu factionsMenu = plugin.getOrCreateFactionsMenu(player);

        if(player.getOpenInventory() != factionsMenu.getGui().getInventory()) return;
        player.closeInventory();
    }
}
