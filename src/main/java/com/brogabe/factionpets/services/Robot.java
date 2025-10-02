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
import java.util.stream.Collectors;

public class Robot {

    private final FactionPets plugin;

    @Getter
    private ItemStack itemStack;

    private final FactionsMenu factionsMenu;

    @Getter
    private final String section;

    private BukkitTask updateTask;

    private Instant autoSave;

    private double amount = 0.0;

    public Robot(FactionPets plugin, FactionsMenu factionsMenu, ItemStack itemStack, String section) {
        this.plugin = plugin;
        this.factionsMenu = factionsMenu;
        this.itemStack = itemStack;
        this.section = section;

        autoSave = Instant.now();

        startTask();

        System.out.println("[ROBOT] Did robot class get initizialized?");
    }

    public void startTask() {
        System.out.println("Did start task get called?");
        if(isRunning()) return;

        System.out.println("Did my isRunning method work?");

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            amount = amount + 5000;

            System.out.println("amount is: " + amount);

            System.out.println("This happens every 30 seconds");

            if(isMenuOpen()) {
                updateRobot();
                autoSave = Instant.now();
                System.out.println("Was menu open, causing it to update?");
                return;
            }

            if(!pastFiveMinutes()) return;

            System.out.println("was it past two minutes");

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

        if(!module.isRobot(itemStack)) {
            cancelTask();
            return;
        }

        NBTItem nbtItem = new NBTItem(itemStack);

        NBTCompound nbtCompound = nbtItem.getCompound("FactionPets");

        assert nbtCompound != null;
        double currentAmount = nbtCompound.getDouble("amount");
        System.out.println("current amount is: " + currentAmount);
        String type = nbtCompound.getString("type");

        nbtCompound.setDouble("amount", currentAmount + amount);

        setItemStack(nbtItem.getItem());

        FileConfiguration config = factionsMenu.getConfig();

        PetModule petModule = plugin.getModuleManager().getPetModule();
        petModule.updateRobotLore(type, itemStack, currentAmount + amount);

        config.set(section, ItemSerializer.itemStackToBase64(itemStack));

        factionsMenu.saveConfig();
        factionsMenu.reloadConfig();

        amount = 0.0;
    }

    public boolean isRunning() {
        return (updateTask != null &&
                (Bukkit.getScheduler().isCurrentlyRunning(updateTask.getTaskId())
                        || Bukkit.getScheduler().isQueued(updateTask.getTaskId())));
    }

    private void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

}
