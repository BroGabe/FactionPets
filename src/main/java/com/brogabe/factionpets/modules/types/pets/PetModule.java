package com.brogabe.factionpets.modules.types.pets;

import com.brogabe.factionpets.FactionPets;
import com.brogabe.factionpets.configuration.FactionsConfig;
import com.brogabe.factionpets.services.PetType;
import com.brogabe.factionpets.utils.ColorUtil;
import com.brogabe.factionpets.utils.FactionsUtil;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PetModule {

    private final FactionPets plugin;

    public PetModule(FactionPets plugin) {
        this.plugin = plugin;
    }

    public boolean hasPetSlots(Player player) {
        if(!FactionsUtil.hasFaction(player)) return false;

        String factionID = FactionsUtil.getFactionID(player);

        FactionsConfig factionsConfig = plugin.getFactionsConfig();
        FileConfiguration config = factionsConfig.getFactionConfig(factionID);

        return config.getString("pet-1").isEmpty() || config.getString("pet-2").isEmpty()
                || config.getString("pet-3").isEmpty();
    }

    public Optional<String> getAvailableSection(Player player) {
        String factionID = FactionsUtil.getFactionID(player);

        FactionsConfig factionsConfig = plugin.getFactionsConfig();
        FileConfiguration config = factionsConfig.getFactionConfig(factionID);

        return config.getKeys(false).stream().filter(s -> isSectionAvailable(s, config)).findFirst();
    }

    public boolean isSectionAvailable(String section, FileConfiguration config) {
        return config.getString(section) != null && config.getString(section).isEmpty();
    }

    public boolean isRobot(ItemStack itemStack) {
        if(itemStack == null || itemStack.getType() == Material.AIR) return false;

        NBTItem nbtItem = new NBTItem(itemStack);

        if(nbtItem.getCompound("FactionPets") == null) return false;

        NBTCompound compound = nbtItem.getCompound("FactionPets");
        String type = compound.getString("type");

        return (getPetType(type) == PetType.MONEY || getPetType(type) == PetType.TOKEN || getPetType(type) == PetType.EXPERIENCE);
    }

    public void updateRobotLore(String type, ItemStack itemStack, double amount) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        PetType petType = getPetType(type);

        switch (petType) {
            case TOKEN:
                List<String> lore = plugin.getConfig().getStringList("token-pet.lore")
                        .stream()
                        .map(s -> ColorUtil.color(s
                                        .replace("%tokens%", String.valueOf((int) amount)))).collect(Collectors.toList());

                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                return;
        }
    }

    public PetType getPetType(String type) {
        switch (type.toUpperCase()) {
            case "AQUATIC": return PetType.AQUATIC;
            case "TOKEN": return PetType.TOKEN;
            case "MONEY": return PetType.MONEY;
            case "EXPERIENCE": return PetType.EXPERIENCE;
            default: return PetType.INVALID;
        }
    }
}
