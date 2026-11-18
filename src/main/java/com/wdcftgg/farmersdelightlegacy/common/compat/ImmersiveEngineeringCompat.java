package com.wdcftgg.farmersdelightlegacy.common.compat;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;

public final class ImmersiveEngineeringCompat {
    private ImmersiveEngineeringCompat() {
    }

    public static void registerAll() {
        CropCloche vegetables = new CropCloche(800);
        vegetables.register(stack("cabbage_seeds", 1), new ItemStack[]{stack("cabbage", 2), stack("cabbage_seeds", 1)},
                new ItemStack(Blocks.DIRT), ModBlocks.CABBAGES.getDefaultState());
        vegetables.register(stack("onion", 1), new ItemStack[]{stack("onion", 2)},
                new ItemStack(Blocks.DIRT), ModBlocks.ONIONS.getDefaultState());
        vegetables.register(stack("tomato_seeds", 1), new ItemStack[]{stack("tomato", 2)},
                new ItemStack(Blocks.DIRT), ModBlocks.TOMATOES.getDefaultState());
        CropCloche rice = new CropCloche(1000);
        rice.register(stack("rice", 1), new ItemStack[]{stack("rice_panicle", 2)},
                new ItemStack(Blocks.DIRT), ModBlocks.RICE_PANICLES.getDefaultState());
        BelljarHandler.registerHandler(vegetables);
        BelljarHandler.registerHandler(rice);

        CrusherRecipe.addRecipe(stack("rice", 1), stack("rice_panicle", 1), 1600)
                .addToSecondaryOutput(stack("straw", 1), 1.0F);
        CrusherRecipe.addRecipe(new ItemStack(Items.BEETROOT_SEEDS), new ItemStack(ModBlocks.WILD_BEETROOTS), 1600)
                .addToSecondaryOutput(dye(EnumDyeColor.RED, 1), 1.0F);
        CrusherRecipe.addRecipe(stack("cabbage_seeds", 1), new ItemStack(ModBlocks.WILD_CABBAGES), 3200)
                .addToSecondaryOutput(dye(EnumDyeColor.YELLOW, 1), 1.0F);
        CrusherRecipe.addRecipe(dye(EnumDyeColor.SILVER, 2), new ItemStack(ModBlocks.WILD_CARROTS), 3200)
                .addToSecondaryOutput(dye(EnumDyeColor.LIME, 1), 0.1F);
        CrusherRecipe.addRecipe(dye(EnumDyeColor.MAGENTA, 1), new ItemStack(ModBlocks.WILD_ONIONS), 3200)
                .addToSecondaryOutput(dye(EnumDyeColor.MAGENTA, 3), 0.2F, dye(EnumDyeColor.LIME, 1), 0.1F);
        CrusherRecipe.addRecipe(dye(EnumDyeColor.PURPLE, 2), new ItemStack(ModBlocks.WILD_POTATOES), 3200)
                .addToSecondaryOutput(dye(EnumDyeColor.LIME, 1), 0.1F);
        CrusherRecipe.addRecipe(stack("rice", 1), new ItemStack(ModBlocks.WILD_RICE), 3200)
                .addToSecondaryOutput(dye(EnumDyeColor.GREEN, 2), 1.0F);
        CrusherRecipe.addRecipe(stack("tomato_seeds", 1), new ItemStack(ModBlocks.WILD_TOMATOES), 3200)
                .addToSecondaryOutput(dye(EnumDyeColor.GREEN, 1), 1.0F);

        FermenterRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("ethanol"), 80), ItemStack.EMPTY, "cropTomato", 6400);
        Item mold = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "mold"));
        MetalPressRecipe.addRecipe(stack("pumpkin_slice", 4), new ItemStack(Blocks.PUMPKIN),
                new ItemStack(mold, 1, 7), 3200);
        SqueezerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("plantoil"), 80), ItemStack.EMPTY, stack("cabbage_seeds", 1), 6400);
        SqueezerRecipe.addRecipe(new FluidStack(FluidRegistry.getFluid("plantoil"), 60), ItemStack.EMPTY, stack("tomato_seeds", 1), 6400);
    }

    private static ItemStack stack(String path, int count) {
        return new ItemStack(ModItems.get(path), count);
    }

    private static ItemStack dye(EnumDyeColor color, int count) {
        return new ItemStack(Items.DYE, count, color.getDyeDamage());
    }

    private static final class CropCloche extends BelljarHandler.DefaultPlantHandler {
        private final HashSet<ComparableItemStack> seeds = new HashSet<>();
        private final int growingTime;

        private CropCloche(int growingTime) {
            this.growingTime = growingTime;
        }

        @Override
        protected HashSet<ComparableItemStack> getSeedSet() {
            return seeds;
        }

        @Override
        public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer, boolean render) {
            return fertilizer / growingTime;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IBlockState[] getRenderedPlant(ItemStack seed, ItemStack soil, float growth, TileEntity tile) {
            return BelljarHandler.cropHandler.getRenderedPlant(seed, soil, growth, tile);
        }
    }
}

