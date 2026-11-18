package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.compat.NyxSandyShrubBonemealCompat;
import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;
import com.wdcftgg.farmersdelightlegacy.common.registry.ItemCategories;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.HarvestDropRecipeManager;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.Enchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.common.IShearable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockSandyShrub extends BlockBush implements IGrowable, IShearable {
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.8125D, 0.875D);

    public BlockSandyShrub() {
        super(Material.PLANTS);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.setTickRandomly(true);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return BlockCategories.contains("fdSand", state);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().isReplaceable(world, pos) && canSustainBush(world.getBlockState(pos.down()));
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return canSustainBush(world.getBlockState(pos.down()));
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(this));
        if (world instanceof World && ItemCategories.contains("fdStrawHarvesters", item)
                && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, item) == 0) {
            HarvestDropRecipeManager.addStrawDrops(world.getBlockState(pos), fortune, ((World) world).rand, drops);
        }
        return drops;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random random = world instanceof World ? ((World) world).rand : RANDOM;
        if (random.nextFloat() < 0.125F) {
            drops.add(new ItemStack(Items.BEETROOT_SEEDS, 1 + random.nextInt(2 * Math.max(0, fortune) + 1)));
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return !Loader.isModLoaded(NyxSandyShrubBonemealCompat.NyxModId);
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        for (int i = 0; i < 6; i++) {
            BlockPos target = pos.add(rand.nextInt(5) - 2, 0, rand.nextInt(5) - 2);
            if (worldIn.isAirBlock(target) && this.canPlaceBlockAt(worldIn, target)) {
                worldIn.setBlockState(target, this.getDefaultState(), 2);
            }
        }
    }
}

