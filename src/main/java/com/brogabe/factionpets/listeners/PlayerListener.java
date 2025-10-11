package com.brogabe.factionpets.listeners;

import com.brogabe.factionpets.FactionPets;
import com.brogabe.factionpets.configuration.FactionsConfig;
import com.brogabe.factionpets.menus.FactionsMenu;
import com.brogabe.factionpets.modules.types.pets.PetModule;
import com.brogabe.factionpets.services.PetType;
import com.brogabe.factionpets.utils.ColorUtil;
import com.brogabe.factionpets.utils.FactionsUtil;
import com.brogabe.factionpets.utils.ItemSerializer;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class PlayerListener implements Listener {

    private final FactionPets plugin;

    public PlayerListener(FactionPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        NBTItem nbtItem = new NBTItem(itemStack);

        if(nbtItem.getCompound("FactionPets") == null) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        plugin.getOrCreateFactionsMenu(event.getPlayer());
    }

    @EventHandler
    public void onRedeem(PlayerInteractEvent event) {
        if(event.getPlayer().getItemInHand() == null
                || event.getPlayer().getItemInHand().getType() == Material.AIR) return;


        Player player = event.getPlayer();

        Optional<FactionsMenu> optional = plugin.getOrCreateFactionsMenu(player);
        if(!optional.isPresent()) return;

        FactionsMenu factionsMenu = optional.get();

        ItemStack itemStack = event.getPlayer().getItemInHand();
        NBTItem nbtItem = new NBTItem(itemStack);

        if(nbtItem.getCompound("FactionPets") == null) return;

        NBTCompound compound = nbtItem.getCompound("FactionPets");

        PetModule module = plugin.getModuleManager().getPetModule();

        PetType petType = module.getPetType(compound.getString("type"));

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

        factionsMenu.reloadConfig();

        if(module.isRobot(itemStack)) {
            int increment = module.getIncrement(petType);
            factionsMenu.addRobot(section, increment);
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
