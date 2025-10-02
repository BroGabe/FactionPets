package com.brogabe.factionpets.listeners;

import com.brogabe.factionpets.FactionPets;
import com.brogabe.factionpets.configuration.FactionsConfig;
import com.brogabe.factionpets.menus.FactionsMenu;
import com.brogabe.factionpets.modules.types.pets.PetModule;
import com.brogabe.factionpets.utils.ColorUtil;
import com.brogabe.factionpets.utils.FactionsUtil;
import com.brogabe.factionpets.utils.ItemSerializer;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class PlayerListener implements Listener {

    private final FactionPets plugin;

    public PlayerListener(FactionPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        NBTItem nbtItem = new NBTItem(itemStack);

        if(nbtItem.getCompound("FactionPets") == null) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onRedeem(PlayerInteractEvent event) {
        if(event.getPlayer().getItemInHand() == null
                || event.getPlayer().getItemInHand().getType() == Material.AIR) return;

        Player player = event.getPlayer();
        ItemStack itemStack = event.getPlayer().getItemInHand();
        NBTItem nbtItem = new NBTItem(itemStack);

        if(nbtItem.getCompound("FactionPets") == null) return;

        PetModule module = plugin.getModuleManager().getPetModule();

        if(!module.hasPetSlots(event.getPlayer())) {
            player.sendMessage(ColorUtil.color("&4&lPETS &cYou do not have available slots!"));
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 6, 6);
            return;
        }

        Optional<String> sectionOptional = module.getAvailableSection(player);

        if(!sectionOptional.isPresent()) return;

        String section = sectionOptional.get();
        String factionID = FactionsUtil.getFactionID(player);

        FactionsConfig factionsConfig = plugin.getFactionsConfig();
        FileConfiguration config = factionsConfig.getFactionConfig(factionID);

        config.set(section, ItemSerializer.itemStackToBase64(itemStack));

        factionsConfig.saveConfig(factionID, config);

        FactionsMenu factionsMenu = plugin.getOrCreateFactionsMenu(player);

        factionsMenu.reloadConfig();

        if(module.isRobot(itemStack)) {
            factionsMenu.addRobot(itemStack, section);
        } else {
            Bukkit.broadcastMessage("Not a robot");
        }

        player.sendMessage(ColorUtil.color("&2&lPETS &aYou have activated a pet!"));
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 6, 6);

        if(itemStack.getAmount() <= 1) {
            player.setItemInHand(null);
            return;
        }

        itemStack.setAmount(itemStack.getAmount() -1);
        player.setItemInHand(itemStack);
    }
}
