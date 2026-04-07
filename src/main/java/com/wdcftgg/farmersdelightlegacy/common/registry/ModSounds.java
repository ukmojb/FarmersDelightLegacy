package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelight.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;

public final class ModSounds {

    public static SoundEvent CABINET_OPEN;
    public static SoundEvent CABINET_CLOSE;
    public static SoundEvent STOVE_CRACKLE;
    public static SoundEvent COOKING_POT_BOIL;
    public static SoundEvent COOKING_POT_BOIL_SOUP;
    public static SoundEvent CUTTING_BOARD_KNIFE;
    public static SoundEvent SKILLET_SIZZLE;
    public static SoundEvent SKILLET_ADD_FOOD;
    public static SoundEvent SKILLET_ATTACK_STRONG;
    public static SoundEvent SKILLET_ATTACK_WEAK;

    private ModSounds() {
    }

    public static void registerAll(RegistryEvent.Register<SoundEvent> event) {
        CABINET_OPEN = register(event, "block.cabinet.open");
        CABINET_CLOSE = register(event, "block.cabinet.close");
        STOVE_CRACKLE = register(event, "block.stove.crackle");
        COOKING_POT_BOIL = register(event, "block.cooking_pot.boil");
        COOKING_POT_BOIL_SOUP = register(event, "block.cooking_pot.boil_soup");
        CUTTING_BOARD_KNIFE = register(event, "block.cutting_board.knife");
        SKILLET_SIZZLE = register(event, "block.skillet.sizzle");
        SKILLET_ADD_FOOD = register(event, "block.skillet.add_food");
        SKILLET_ATTACK_STRONG = register(event, "item.skillet.attack.strong");
        SKILLET_ATTACK_WEAK = register(event, "item.skillet.attack.weak");
    }

    private static SoundEvent register(RegistryEvent.Register<SoundEvent> event, String soundPath) {
        ResourceLocation soundId = new ResourceLocation(Tags.MOD_ID, soundPath);
        SoundEvent soundEvent = new SoundEvent(soundId);
        soundEvent.setRegistryName(soundId);
        event.getRegistry().register(soundEvent);
        return soundEvent;
    }
}

