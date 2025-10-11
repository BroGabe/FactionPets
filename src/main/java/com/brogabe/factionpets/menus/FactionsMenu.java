package com.brogabe.factionpets.menus;

import com.brogabe.factionpets.FactionPets;
import com.brogabe.factionpets.configuration.FactionsConfig;
import com.brogabe.factionpets.modules.types.pets.PetModule;
import com.brogabe.factionpets.services.PetType;
import com.brogabe.factionpets.services.RobotManager;
import com.brogabe.factionpets.utils.ColorUtil;
import com.brogabe.factionpets.utils.FactionsUtil;
import com.brogabe.factionpets.utils.ItemCreator;
import com.brogabe.factionpets.utils.ItemSerializer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.util.*;

public class FactionsMenu {

    private final FactionPets plugin;

    private final FactionsConfig factionsConfig;

    private final FileConfiguration mainConfig;

    private final RobotManager robotManager;

    @Getter
    private FileConfiguration config;

    @Getter
    private final Gui gui;

    @Getter
    private final String id;

    public FactionsMenu(FactionPets plugin, Player player) {
        this.plugin = plugin;

        id = FactionsUtil.getFactionID(player);

        factionsConfig = plugin.getFactionsConfig();
        mainConfig = plugin.getConfig();
        config = factionsConfig.getFactionConfig(id);
        robotManager = new RobotManager(plugin, this);

        gui = Gui.gui()
                .title(Component.text(ColorUtil.color("&c" + FactionsUtil.getFactionName(player) + "'s Pets")))
                .rows(3)
                .create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        buildMenu();
        initializeRobots();
    }

    public void saveConfig() {
        factionsConfig.saveConfig(id, config);
    }

    public void addRobot(String section, int increment) {
        robotManager.addRobot(section, increment);
    }

    private void rightClickAction(Player player, ItemStack itemStack, String section) {
        robotManager.giveCurrency(player, section, itemStack);
    }

    private ItemStack getBlackGlass() {
        return new ItemCreator(Material.STAINED_GLASS_PANE, "&7", 1, 15, "", "").getItem();
    }

    public void reloadConfig() {
        config = factionsConfig.getFactionConfig(id);
        buildMenu();
        gui.update();
    }

    private ItemStack getAvailableGlass() {
        String name = mainConfig.getString("available-slot.name");
        List<String> lore = mainConfig.getStringList("available-slot.lore");

        return new ItemCreator(Material.STAINED_GLASS_PANE, name, 1, 5, "", lore).getItem();
    }

    private ItemStack getGuideItem() {
        String name = mainConfig.getString("guide-item.name");
        List<String> lore = mainConfig.getStringList("guide-item.lore");

        return new ItemCreator(Material.BOOK_AND_QUILL, name, 1, 0, "", lore).getItem();
    }


    private ItemStack updateName(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        String newName = ColorUtil.color(itemMeta.getDisplayName() + " &7[Active Pet]");

        itemMeta.setDisplayName(newName);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public void openMenu(Player player) {
        if(!FactionsUtil.hasFaction(player)) {
            player.sendMessage(ColorUtil.color("&2&lPETS &aYou do not have a faction."));
            player.playSound(player.getLocation(), Sound.NOTE_BASS, 6, 6);
            return;
        }

        gui.open(player);
    }

    public void closeAll() {
        Faction faction = Factions.getInstance().getFactionById(id);

        for(Player player : faction.getOnlinePlayers()) {
            if(player.getOpenInventory() != gui.getInventory()) continue;

            player.closeInventory();
        }
    }


    public void buildMenu() {
        GuiItem pet1 = getSectionItem("pet-1");
        GuiItem pet2 = getSectionItem("pet-2");
        GuiItem pet3 = getSectionItem("pet-3");

        gui.setItem(11, pet1);
        gui.setItem(13, pet2);
        gui.setItem(15, pet3);
        gui.setItem(26, new GuiItem(getGuideItem()));
        gui.getFiller().fill(new GuiItem(getBlackGlass()));
    }

    private void leftClickAction(Player player, ItemStack itemStack, String section) {
        player.getInventory().addItem(itemStack);
        player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 5, 6);

        config.set(section, "");
        saveConfig();
        reloadConfig();

        gui.update();

        robotManager.removeRobot(section);
    }

    private GuiItem getSectionItem(String section) {
        if(config.getString(section) == null || config.getString(section).isEmpty()) return new GuiItem(getAvailableGlass());

        Optional<ItemStack> stackOptional = getItemFromSection(section);

        if(!stackOptional.isPresent()) return new GuiItem(getAvailableGlass());

        ItemStack itemStack = stackOptional.get();

        GuiItem guiItem = new GuiItem(updateName(itemStack));
        guiItem.setAction(event -> clickAction(event, section));

        return guiItem;
    }

    public Optional<ItemStack> getItemFromSection(String section) {
        ItemStack itemStack;

        String resultString = config.getString(section);

        try {
            itemStack = ItemSerializer.itemStackFromBase64(resultString);
        } catch (IOException e) {
            return Optional.empty();
        }

        return Optional.of(itemStack);
    }

    private void clickAction(InventoryClickEvent event, String section) {
        if(!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if(config.getString(section) == null || config.getString(section).isEmpty()) return;

        Optional<ItemStack> stackOptional = getItemFromSection(section);

        if(!stackOptional.isPresent()) return;

        ItemStack itemStack = stackOptional.get();

        if(event.getClick().isLeftClick()) {
            leftClickAction(player, itemStack, section);
            return;
        }

        rightClickAction(player, itemStack, section);
    }

    private void initializeRobots() {
        Map<String, Optional<ItemStack>> petMap = new HashMap<>();

        petMap.put("pet-1", getItemFromSection("pet-1"));
        petMap.put("pet-2", getItemFromSection("pet-2"));
        petMap.put("pet-3", getItemFromSection("pet-3"));

        for(Map.Entry<String, Optional<ItemStack>> entry : petMap.entrySet()) {
            Optional<ItemStack> optional = entry.getValue();

            if(!optional.isPresent()) continue;

            String section = entry.getKey();

            PetModule module = plugin.getModuleManager().getPetModule();

            if(!module.isRobot(optional.get())) continue;
            if(robotManager.alreadyInitialized(section)) continue;

            NBTItem nbtItem = new NBTItem(optional.get());
            NBTCompound compound = nbtItem.getCompound("FactionPets");
            if(compound == null) continue;

            PetType petType = module.getPetType(compound.getString("type"));

            int increment = module.getIncrement(petType);

            addRobot(section, increment);
        }
    }
}
