package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;

public final class ModSounds {

    public static SoundEvent cabinetOpen;
    public static SoundEvent CABINET_CLOSE;
    public static SoundEvent STOVE_CRACKLE;
    public static SoundEvent COOKING_POT_BOIL;
    public static SoundEvent COOKING_POT_BOIL_SOUP;
    public static SoundEvent CUTTING_BOARD_KNIFE;
    public static SoundEvent cuttingBoardPlace;
    public static SoundEvent cuttingBoardRemove;
    public static SoundEvent cuttingBoardCarve;
    public static SoundEvent SKILLET_SIZZLE;
    public static SoundEvent SKILLET_ADD_FOOD;
    public static SoundEvent SKILLET_ATTACK_STRONG;
    public static SoundEvent SKILLET_ATTACK_WEAK;
    public static SoundEvent foodTakePortion;
    public static SoundEvent foodSlice;
    public static SoundEvent tomatoesPickTomatoes;
    public static SoundEvent ROTTEN_TOMATO_THROW;
    public static SoundEvent ROTTEN_TOMATO_HIT;
    public static SoundEvent ropeFenceGateOpen;
    public static SoundEvent ropeFenceGateClose;

    private ModSounds() {
    }

    public static void registerAll(RegistryEvent.Register<SoundEvent> event) {
        cabinetOpen = register(event, "block.cabinet.open");
        CABINET_CLOSE = register(event, "block.cabinet.close");
        STOVE_CRACKLE = register(event, "block.stove.crackle");
        COOKING_POT_BOIL = register(event, "block.cooking_pot.boil");
        COOKING_POT_BOIL_SOUP = register(event, "block.cooking_pot.boil_soup");
        CUTTING_BOARD_KNIFE = register(event, "block.cutting_board.knife");
        cuttingBoardPlace = register(event, "block.cutting_board.place_item");
        cuttingBoardRemove = register(event, "block.cutting_board.remove_item");
        cuttingBoardCarve = register(event, "block.cutting_board.carve_tool");
        SKILLET_SIZZLE = register(event, "block.skillet.sizzle");
        SKILLET_ADD_FOOD = register(event, "block.skillet.add_food");
        SKILLET_ATTACK_STRONG = register(event, "item.skillet.attack.strong");
        SKILLET_ATTACK_WEAK = register(event, "item.skillet.attack.weak");
        foodTakePortion = register(event, "block.food.take_portion");
        foodSlice = register(event, "block.food.slice");
        tomatoesPickTomatoes = register(event, "block.tomato_bush.pick_tomatoes");
        ROTTEN_TOMATO_THROW = register(event, "entity.rotten_tomato.throw");
        ROTTEN_TOMATO_HIT = register(event, "entity.rotten_tomato.hit");
        ropeFenceGateOpen = register(event, "block.rope_fence_gate.open");
        ropeFenceGateClose = register(event, "block.rope_fence_gate.close");
    }

    private static SoundEvent register(RegistryEvent.Register<SoundEvent> event, String soundPath) {
        ResourceLocation soundId = new ResourceLocation(FarmersDelightLegacy.MOD_ID, soundPath);
        SoundEvent soundEvent = new SoundEvent(soundId);
        soundEvent.setRegistryName(soundId);
        event.getRegistry().register(soundEvent);
        return soundEvent;
    }
}
