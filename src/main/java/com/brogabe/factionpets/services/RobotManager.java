package com.brogabe.factionpets.services;

import com.brogabe.factionpets.FactionPets;
import com.brogabe.factionpets.menus.FactionsMenu;
import com.brogabe.factionpets.modules.types.CurrencyModule;
import com.brogabe.factionpets.modules.types.pets.PetModule;
import com.brogabe.factionpets.utils.ItemSerializer;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RobotManager {

    private final FactionPets plugin;

    private final FactionsMenu factionsMenu;

    private final Set<Robot> robotSet = new HashSet<>();

    // Loops through the robot set to do methods inside
    public RobotManager(FactionPets plugin, FactionsMenu factionsMenu) {
        this.plugin = plugin;
        this.factionsMenu = factionsMenu;
    }

    public void addRobot(String section, int increment) {
        Robot robot = new Robot(plugin, factionsMenu, section, increment);
        robotSet.add(robot);
    }

    public void removeRobot(String section) {
        List<Robot> robotList = robotSet.stream().filter(robot -> matchesClass(robot, section)).collect(Collectors.toList());

        if(robotList.isEmpty()) return;

        for(Robot robot : robotList) {
            robot.cancelTask();
        }

        robotList.forEach(robotSet::remove);
    }

    private boolean matchesClass(Robot robot, String section) {
        return (robot.getSection().equalsIgnoreCase(section));
    }

    public boolean alreadyInitialized(String section) {
        return (robotSet.stream().anyMatch(robot -> robot.getSection().equalsIgnoreCase(section)));
    }

    public void giveCurrency(Player player, String section, ItemStack itemStack) {
        CurrencyModule currencyModule = plugin.getModuleManager().getCurrencyModule();
        PetModule petModule = plugin.getModuleManager().getPetModule();

        if(!petModule.isRobot(itemStack)) return;

        NBTItem nbtItem = new NBTItem(itemStack);

        NBTCompound nbtCompound = nbtItem.getCompound("FactionPets");

        assert nbtCompound != null;
        String type = nbtCompound.getString("type");
        double amount = nbtCompound.getDouble("amount");

        PetType petType = petModule.getPetType(type);

        switch (petType) {
            case TOKEN:
                currencyModule.giveTokens(player, amount);
                resetAmount(nbtItem, section);
                return;
            case EXPERIENCE:
                currencyModule.addExperience(player, (int) amount);
                resetAmount(nbtItem, section);
                return;
            case MONEY:
                currencyModule.addMoney(player, amount);
                resetAmount(nbtItem, section);
        }
    }

    private void resetAmount(NBTItem nbtItem, String section) {
        NBTCompound nbtCompound = nbtItem.getCompound("FactionPets");
        assert nbtCompound != null;
        nbtCompound.setDouble("amount", 0.0);

        ItemStack itemStack = nbtItem.getItem();

        PetModule petModule = plugin.getModuleManager().getPetModule();

        petModule.updateRobotLore(nbtCompound.getString("type"), itemStack, 0.0);

        factionsMenu.getConfig().set(section, ItemSerializer.itemStackToBase64(itemStack));
        factionsMenu.saveConfig();
        factionsMenu.reloadConfig();
    }
}
