package com.brogabe.factionpets.modules;

import com.brogabe.factionpets.FactionPets;
import com.brogabe.factionpets.modules.types.CurrencyModule;
import com.brogabe.factionpets.modules.types.pets.PetModule;
import lombok.Getter;

@Getter
public class ModuleManager {

    private final PetModule petModule;

    private final CurrencyModule currencyModule;

    public ModuleManager(FactionPets plugin) {
        currencyModule = new CurrencyModule(plugin);
        petModule = new PetModule(plugin);
    }

}
