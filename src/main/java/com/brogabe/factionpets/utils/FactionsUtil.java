package com.brogabe.factionpets.utils;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import org.bukkit.entity.Player;

public class FactionsUtil {

    public static String getFactionID(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        if(!fPlayer.hasFaction()) return "";

        return fPlayer.getFactionId();
    }

    public static boolean hasFaction(Player player) {
        return FPlayers.getInstance().getByPlayer(player).hasFaction();
    }

    public static String getFactionName(Player player) {
        FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);

        return fPlayer.getFaction().getTag();
    }
}
