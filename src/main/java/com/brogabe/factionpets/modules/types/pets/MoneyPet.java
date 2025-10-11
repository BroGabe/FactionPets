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

public class MoneyPet {

    public static void givePet(Player player, FactionPets plugin) {
        FileConfiguration config = plugin.getConfig();

        List<String> lore = config.getStringList("money-pet.lore");
        lore.replaceAll(s -> s.replace("%amount%", "0"));

        Material material = Material.SKULL_ITEM;
        String name = config.getString("money-pet.name");
        String texture = config.getString("money-pet.texture");

        ItemStack itemStack = new ItemCreator(material, name, 1, 3, texture, lore).getItem();
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound compound = nbtItem.getOrCreateCompound("FactionPets");

        compound.setString("type", "money");
        compound.setDouble("amount", 0.0);

        player.getInventory().addItem(nbtItem.getItem());
    }
}
