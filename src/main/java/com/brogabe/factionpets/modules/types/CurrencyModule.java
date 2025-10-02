package com.brogabe.factionpets.modules.types;

import com.brogabe.factionpets.FactionPets;
import me.fullpage.mantichoes.data.MPlayers;
import me.fullpage.mantichoes.wrappers.MPlayer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class CurrencyModule {

    private final FactionPets plugin;

    public CurrencyModule(FactionPets plugin) {
        this.plugin = plugin;
    }

    public void giveTokens(Player player, double amount) {
        MPlayer mPlayer = MPlayers.get().getByName(player.getName());
        mPlayer.addTokens(amount);
    }

    public void addExperience(Player player, int amount) {
        player.giveExp(amount);
    }

    public void addMoney(Player player, double amount) {
        Economy economy = plugin.getEconomy();

        economy.depositPlayer(player, amount);
    }
}
