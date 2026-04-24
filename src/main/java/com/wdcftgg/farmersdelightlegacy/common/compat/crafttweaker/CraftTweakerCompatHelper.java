package com.wdcftgg.farmersdelightlegacy.common.compat.crafttweaker;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.oredict.IOreDictEntry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

final class CraftTweakerCompatHelper {

    private CraftTweakerCompatHelper() {
    }

    static Item itemOf(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return null;
        }
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(stripMetadataToken(itemId)));
    }

    static ItemStack stackOf(String itemId, int count) {
        ParsedItemToken parsedItemToken = parseItemToken(itemId);
        Item item = parsedItemToken.itemId == null ? null : itemOf(parsedItemToken.itemId);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        if (parsedItemToken.hasMetadata && parsedItemToken.metadata != OreDictionary.WILDCARD_VALUE) {
            return new ItemStack(item, Math.max(1, count), parsedItemToken.metadata);
        }
        return new ItemStack(item, Math.max(1, count));
    }

    static ItemStack stackOf(String itemId, int count, int metadata) {
        Item item = itemOf(itemId);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, Math.max(1, count), Math.max(0, metadata));
    }

    static ItemStack stackOf(IItemStack stack) {
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        ItemStack nativeStack = CraftTweakerMC.getItemStack(stack);
        return nativeStack == null ? ItemStack.EMPTY : nativeStack.copy();
    }

    static String itemIdOf(IItemStack stack) {
        ItemStack nativeStack = stackOf(stack);
        if (nativeStack.isEmpty()) {
            return null;
        }
        ResourceLocation registryName = nativeStack.getItem().getRegistryName();
        return registryName == null ? null : registryName.toString();
    }

    static String itemTokenOf(IItemStack stack) {
        ItemStack nativeStack = stackOf(stack);
        if (nativeStack.isEmpty()) {
            return null;
        }

        ResourceLocation registryName = nativeStack.getItem().getRegistryName();
        if (registryName == null) {
            return null;
        }

        int metadata = nativeStack.getMetadata();
        if (metadata == OreDictionary.WILDCARD_VALUE) {
            return registryName + "@*";
        }
        return registryName + "@" + metadata;
    }

    static String[] toStrictIngredientTokens(IIngredient[] ingredients) {
        if (ingredients == null || ingredients.length == 0) {
            return null;
        }

        String[] tokens = new String[ingredients.length];
        for (int index = 0; index < ingredients.length; index++) {
            String token = toStrictIngredientToken(ingredients[index]);
            if (token == null || token.isEmpty()) {
                return null;
            }
            tokens[index] = token;
        }
        return tokens;
    }

    static String[] toIngredientTokens(IIngredient[] ingredients) {
        if (ingredients == null || ingredients.length == 0) {
            return null;
        }

        String[] tokens = new String[ingredients.length];
        for (int index = 0; index < ingredients.length; index++) {
            String token = toIngredientToken(ingredients[index]);
            if (token == null || token.isEmpty()) {
                return null;
            }
            tokens[index] = token;
        }
        return tokens;
    }

    static String toIngredientToken(IIngredient ingredient) {
        if (ingredient == null) {
            return null;
        }

        if (ingredient instanceof IOreDictEntry) {
            String oreName = ((IOreDictEntry) ingredient).getName();
            return oreName == null || oreName.isEmpty() ? null : "ore:" + oreName;
        }

        if (ingredient instanceof IItemStack) {
            return itemIdOf((IItemStack) ingredient);
        }

        String commandString = ingredient.toCommandString();
        if (commandString == null || commandString.isEmpty()) {
            return null;
        }
        if (commandString.startsWith("<ore:") && commandString.endsWith(">")) {
            return "ore:" + commandString.substring(5, commandString.length() - 1);
        }
        return null;
    }

    static String toStrictIngredientToken(IIngredient ingredient) {
        if (ingredient == null) {
            return null;
        }

        if (ingredient instanceof IOreDictEntry) {
            String oreName = ((IOreDictEntry) ingredient).getName();
            return oreName == null || oreName.isEmpty() ? null : "ore:" + oreName;
        }

        if (ingredient instanceof IItemStack) {
            return itemTokenOf((IItemStack) ingredient);
        }

        String commandString = ingredient.toCommandString();
        if (commandString == null || commandString.isEmpty()) {
            return null;
        }
        if (commandString.startsWith("<ore:") && commandString.endsWith(">")) {
            return "ore:" + commandString.substring(5, commandString.length() - 1);
        }
        return null;
    }

    static Block blockOf(String blockId) {
        if (blockId == null || blockId.isEmpty()) {
            return null;
        }
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
    }

    private static String stripMetadataToken(String itemId) {
        ParsedItemToken parsedItemToken = parseItemToken(itemId);
        return parsedItemToken.itemId == null ? itemId : parsedItemToken.itemId;
    }

    private static ParsedItemToken parseItemToken(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return new ParsedItemToken(itemId, 0, false);
        }

        int separatorIndex = itemId.lastIndexOf('@');
        if (separatorIndex <= 0 || separatorIndex + 1 >= itemId.length()) {
            return new ParsedItemToken(itemId, 0, false);
        }

        String metadataToken = itemId.substring(separatorIndex + 1);
        if ("*".equals(metadataToken)) {
            return new ParsedItemToken(itemId.substring(0, separatorIndex), OreDictionary.WILDCARD_VALUE, true);
        }

        try {
            return new ParsedItemToken(itemId.substring(0, separatorIndex), Math.max(0, Integer.parseInt(metadataToken)), true);
        } catch (NumberFormatException ignored) {
            return new ParsedItemToken(itemId, 0, false);
        }
    }

    private static final class ParsedItemToken {
        private final String itemId;
        private final int metadata;
        private final boolean hasMetadata;

        private ParsedItemToken(String itemId, int metadata, boolean hasMetadata) {
            this.itemId = itemId;
            this.metadata = metadata;
            this.hasMetadata = hasMetadata;
        }
    }
}

