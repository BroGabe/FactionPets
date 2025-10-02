package com.brogabe.factionpets.modules.types.pets;


import com.brogabe.factionpets.FactionPets;
import com.brogabe.factionpets.utils.ItemCreator;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class AquaticPet {

    public static void givePet(Player player, FactionPets plugin) {
        FileConfiguration config = plugin.getConfig();

        List<String> lore = config.getStringList("aquatic-pet.lore");

        Material material = Material.SKULL_ITEM;
        String name = config.getString("aquatic-pet.name");
        String texture = config.getString("aquatic-pet.texture");

        ItemStack itemStack = new ItemCreator(material, name, 1, 3, texture, lore).getItem();
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound compound = nbtItem.getOrCreateCompound("FactionPets");

        compound.setString("type", "aquatic");

        player.getInventory().addItem(nbtItem.getItem());
    }
}
