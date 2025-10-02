package com.brogabe.factionpets.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.brogabe.factionpets.FactionPets;
import com.brogabe.factionpets.menus.FactionsMenu;
import com.brogabe.factionpets.modules.types.pets.AquaticPet;
import com.brogabe.factionpets.modules.types.pets.TokenPet;
import com.brogabe.factionpets.utils.ColorUtil;
import com.brogabe.factionpets.utils.FactionsUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

@CommandAlias("fpets|facpets|factionpets")
public class PetCommand extends BaseCommand {

    private final FactionPets plugin;

    public PetCommand(FactionPets plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onOpen(Player player) {
        if(!FactionsUtil.hasFaction(player)) {
            player.sendMessage(ColorUtil.color("&4&lPETS &cYou need a faction to do this!"));
            player.playSound(player.getLocation(), Sound.VILLAGER_NO, 6, 6);
            return;
        }

        FactionsMenu factionsMenu = plugin.getOrCreateFactionsMenu(player);

        factionsMenu.openMenu(player);
    }

    @Subcommand("give")
    @CommandPermission("factionpets.admin")
    public void onGive(Player player) {
        AquaticPet.givePet(player, plugin);
        TokenPet.givePet(player, plugin);
    }
}
