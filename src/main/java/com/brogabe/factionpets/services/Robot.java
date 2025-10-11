package com.brogabe.factionpets.services;

import com.brogabe.factionpets.FactionPets;
import com.brogabe.factionpets.menus.FactionsMenu;
import com.brogabe.factionpets.modules.types.pets.PetModule;
import com.brogabe.factionpets.utils.ItemSerializer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Robot {

    private final FactionPets plugin;

    private final FactionsMenu factionsMenu;

    @Getter
    private final String section;

    private final int increment;

    private BukkitTask updateTask;

    private Instant autoSave;

    private double amount = 0.0;

    public Robot(FactionPets plugin, FactionsMenu factionsMenu, String section, int increment) {
        this.plugin = plugin;
        this.factionsMenu = factionsMenu;
        this.section = section;
        this.increment = increment;

        autoSave = Instant.now();

        startTask();

        System.out.println("[ROBOT] Did robot class get initizialized?");
    }

    public void startTask() {
        if(isRunning()) return;

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            amount = amount + increment;

            if(isMenuOpen()) {
                updateRobot();
                autoSave = Instant.now();
                return;
            }

            if(!pastFiveMinutes()) return;

            updateRobot();

            autoSave = Instant.now();

        },(20L * 30), (20L * 30));
    }

    private boolean isMenuOpen() {
        Faction faction = Factions.getInstance().getFactionById(factionsMenu.getId());

        List<Player> openPlayers = faction.getOnlinePlayers().stream().filter(p -> p.getOpenInventory().getTopInventory().equals(factionsMenu.getGui().getInventory())).collect(Collectors.toList());

        return (!openPlayers.isEmpty());
    }

    private boolean pastFiveMinutes() {
        Duration elapsed = Duration.between(autoSave, Instant.now());

        return (elapsed.toMinutes() >= 1);
    }

    public void cancelTask() {
        if(!isRunning()) return;
        updateTask.cancel();
    }

    public void updateRobot() {
        PetModule module = plugin.getModuleManager().getPetModule();

        Optional<ItemStack> itemStackOptional = getItemFromSection();

        if(!itemStackOptional.isPresent()) {
            cancelTask();
            return;
        }

        ItemStack itemStack = itemStackOptional.get();

        if(!module.isRobot(itemStack)) {
            cancelTask();
            return;
        }

        NBTItem nbtItem = new NBTItem(itemStack);

        NBTCompound nbtCompound = nbtItem.getCompound("FactionPets");

        assert nbtCompound != null;
        double currentAmount = nbtCompound.getDouble("amount");
        String type = nbtCompound.getString("type");

        nbtCompound.setDouble("amount", currentAmount + amount);

        ItemStack newItem = nbtItem.getItem();

        FileConfiguration config = factionsMenu.getConfig();

        PetModule petModule = plugin.getModuleManager().getPetModule();
        petModule.updateRobotLore(type, newItem, currentAmount + amount);

        config.set(section, ItemSerializer.itemStackToBase64(newItem));

        factionsMenu.saveConfig();
        factionsMenu.reloadConfig();

        amount = 0.0;
    }

    public boolean isRunning() {
        return (updateTask != null &&
                (Bukkit.getScheduler().isCurrentlyRunning(updateTask.getTaskId())
                        || Bukkit.getScheduler().isQueued(updateTask.getTaskId())));
    }

    private Optional<ItemStack> getItemFromSection() {
        FileConfiguration config = factionsMenu.getConfig();

        ItemStack itemStack;

        try {
            itemStack = ItemSerializer.itemStackFromBase64(config.getString(section));
        } catch (Exception exception) {
            return Optional.empty();
        }

        return Optional.of(itemStack);
    }

}
