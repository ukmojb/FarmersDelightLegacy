package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCookingPot;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCookingPot extends ItemBlock {

    public static final String MEAL_TOOLTIP_PREFIX = TextFormatting.BLUE + "     ";

    public ItemCookingPot(Block block) {
        super(block);
        this.setMaxStackSize(1);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getServingCount(stack) > 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        int servings = getServingCount(stack);
        if (servings <= 0) {
            return 1.0D;
        }
        return 1.0D - (Math.min(servings, 64) / 64.0D);
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return 0x6666FF;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        ItemStack meal = TileEntityCookingPot.getMealFromItem(stack);
        if (meal.isEmpty()) {
            tooltip.add(TextFormatting.GRAY + new TextComponentTranslation("farmersdelight.tooltip.cooking_pot.empty").getFormattedText());
            return;
        }

        int servings = meal.getCount();
        if (servings > 1) {
            tooltip.add(TextFormatting.GRAY + new TextComponentTranslation("farmersdelight.tooltip.cooking_pot.many_servings", servings)
                    .getFormattedText());
        } else {
            tooltip.add(TextFormatting.GRAY + new TextComponentTranslation("farmersdelight.tooltip.cooking_pot.single_serving")
                    .getFormattedText());
        }

        tooltip.add(MEAL_TOOLTIP_PREFIX + meal.getDisplayName());
        tooltip.add("");

        ItemStack container = inferContainer(stack, meal);
        if (!container.isEmpty()) {
            tooltip.add(TextFormatting.GRAY + new TextComponentTranslation("farmersdelight.container.cooking_pot.served_on", container.getDisplayName())
                    .getFormattedText());
        }
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
                                float hitX, float hitY, float hitZ, net.minecraft.block.state.IBlockState newState) {
        if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            return false;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCookingPot) {
            TileEntityCookingPot pot = (TileEntityCookingPot) tileEntity;
            pot.readFromItem(stack);
        }
        return true;
    }

    public static ItemStack inferContainer(ItemStack cookingPotStack, ItemStack mealStack) {
        ItemStack configured = TileEntityCookingPot.getContainerFromItem(cookingPotStack);
        boolean useDefault = TileEntityCookingPot.useDefaultContainerFromItem(cookingPotStack);
        return TileEntityCookingPot.inferServingContainerForMeal(mealStack, configured, useDefault);
    }

    private static int getServingCount(ItemStack stack) {
        ItemStack mealStack = TileEntityCookingPot.getMealFromItem(stack);
        return mealStack.isEmpty() ? 0 : mealStack.getCount();
    }
}

