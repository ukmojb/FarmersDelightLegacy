package com.wdcftgg.farmersdelightlegacy.common.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

public final class SkilletFlip {
    private static final String TIMESTAMP = "SkilletFlipTimestamp";
    private static final String FLIPPED = "SkilletFlipped";
    private static final int DURATION = 12;

    private SkilletFlip() {
    }

    public static boolean start(EntityLivingBase player) {
        ItemStack stack = player.getActiveItemStack();
        if (player.world.isRemote || !player.isHandActive() || !(stack.getItem() instanceof ItemSkillet)
                || ItemSkillet.getCookingStackForRender(stack).isEmpty() || isFlipping(stack)) {
            return false;
        }
        stack.getTagCompound().setLong(TIMESTAMP, player.world.getTotalWorldTime());
        return true;
    }

    public static boolean isFlipping(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().hasKey(TIMESTAMP, 4);
    }

    public static boolean isFlipped(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean(FLIPPED);
    }

    public static float progress(ItemStack stack, long gameTime, float partialTicks) {
        return isFlipping(stack)
                ? MathHelper.clamp((gameTime - stack.getTagCompound().getLong(TIMESTAMP) + partialTicks) / DURATION, 0, 1)
                : 0;
    }

    public static boolean isLandingSoundTick(ItemStack stack, long gameTime) {
        return isFlipping(stack) && gameTime - stack.getTagCompound().getLong(TIMESTAMP) == DURATION - 8;
    }

    public static void tick(ItemStack stack, long gameTime) {
        if (isFlipping(stack) && gameTime - stack.getTagCompound().getLong(TIMESTAMP) > DURATION) {
            stack.getTagCompound().setBoolean(FLIPPED, !isFlipped(stack));
            stack.getTagCompound().removeTag(TIMESTAMP);
        }
    }

    public static boolean sameCookingState(ItemStack first, ItemStack second) {
        if (first == second) {
            return true;
        }
        if (!ItemStack.areItemsEqual(first, second) || first.getCount() != second.getCount()) {
            return false;
        }
        NBTTagCompound firstTag = first.hasTagCompound() ? first.getTagCompound().copy() : new NBTTagCompound();
        NBTTagCompound secondTag = second.hasTagCompound() ? second.getTagCompound().copy() : new NBTTagCompound();
        clear(firstTag);
        clear(secondTag);
        return firstTag.equals(secondTag);
    }

    public static void clear(NBTTagCompound tag) {
        tag.removeTag(TIMESTAMP);
        tag.removeTag(FLIPPED);
    }
}
