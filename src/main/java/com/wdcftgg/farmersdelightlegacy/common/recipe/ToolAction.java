package com.wdcftgg.farmersdelightlegacy.common.recipe;

import com.wdcftgg.farmersdelightlegacy.common.item.ItemKnife;
import net.minecraft.item.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public final class ToolAction implements CookingPotRecipe.IngredientStackMatcher {
    private final String action;
    private final String toolClass;

    public ToolAction(String action) {
        this.action = action;
        switch (action) {
            case "axe_dig":
            case "axe_strip":
            case "axe_scrape":
            case "axe_wax_off": toolClass = "axe"; break;
            case "pickaxe_dig": toolClass = "pickaxe"; break;
            case "shovel_dig":
            case "shovel_flatten": toolClass = "shovel"; break;
            case "hoe_dig":
            case "hoe_till": toolClass = "hoe"; break;
            case "sword_dig":
            case "sword_sweep": toolClass = "sword"; break;
            case "shears_dig":
            case "shears_harvest":
            case "shears_carve":
            case "shears_disarm": toolClass = "shears"; break;
            case "shield_block": toolClass = "shield"; break;
            case "fishing_rod_cast": toolClass = "fishingRod"; break;
            case "knife_dig":
            case "knife_cut": toolClass = "knife"; break;
            default: throw new IllegalArgumentException("Unsupported 1.12.2 tool action: " + action);
        }
    }

    public String getAction() {
        return action;
    }

    @Override
    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item.getToolClasses(stack).contains(toolClass)) {
            return true;
        }
        switch (toolClass) {
            case "axe": if (item instanceof ItemAxe) { return true; } break;
            case "pickaxe": if (item instanceof ItemPickaxe) { return true; } break;
            case "shovel": if (item instanceof ItemSpade) { return true; } break;
            case "hoe": if (item instanceof ItemHoe) { return true; } break;
            case "sword": if (item instanceof ItemSword) { return true; } break;
            case "shears": if (item instanceof ItemShears) { return true; } break;
            case "shield": if (item.isShield(stack, null)) { return true; } break;
            case "fishingRod": if (item instanceof ItemFishingRod) { return true; } break;
            case "knife": return ItemKnife.isKnife(stack);
            default: break;
        }
        String oreName = "tool" + Character.toUpperCase(toolClass.charAt(0)) + toolClass.substring(1);
        for (int id : OreDictionary.getOreIDs(stack)) {
            if (oreName.equals(OreDictionary.getOreName(id))) {
                return true;
            }
        }
        return false;
    }

    public List<ItemStack> getDisplayStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS) {
            ItemStack stack = new ItemStack(item);
            if (matches(stack)) {
                stacks.add(stack);
            }
        }
        return stacks;
    }
}
