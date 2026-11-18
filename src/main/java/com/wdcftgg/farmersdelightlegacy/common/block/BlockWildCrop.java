package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockWildCrop extends BlockBush implements IGrowable {
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.8125D, 0.875D);

    private final String primaryDropId;
    private final int primaryFortuneMultiplier;
    private final String rareDropId;
    private final float rareDropChance;
    private final String secondaryDropId;
    private final int secondaryDropMin;
    private final int secondaryDropMax;

    public BlockWildCrop(String primaryDropId,
                         int primaryFortuneMultiplier,
                         @Nullable String rareDropId,
                         float rareDropChance,
                         @Nullable String secondaryDropId,
                         int secondaryDropMin,
                         int secondaryDropMax) {
        super(Material.PLANTS);
        this.primaryDropId = primaryDropId;
        this.primaryFortuneMultiplier = Math.max(0, primaryFortuneMultiplier);
        this.rareDropId = rareDropId;
        this.rareDropChance = Math.max(0.0F, rareDropChance);
        this.secondaryDropId = secondaryDropId;
        this.secondaryDropMin = Math.max(0, secondaryDropMin);
        this.secondaryDropMax = Math.max(this.secondaryDropMin, secondaryDropMax);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.setTickRandomly(true);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return BlockCategories.contains("fdTerrain", state);
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
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 60;
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return rand.nextFloat() < 0.8F;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        int nearbyCount = 0;
        for (BlockPos checkPos : BlockPos.getAllInBoxMutable(pos.add(-4, -1, -4), pos.add(4, 1, 4))) {
            if (worldIn.getBlockState(checkPos).getBlock() == this) {
                nearbyCount++;
                if (nearbyCount >= 10) {
                    return;
                }
            }
        }

        BlockPos target = pos.add(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);
        for (int i = 0; i < 4; i++) {
            if (worldIn.isAirBlock(target) && this.canPlaceBlockAt(worldIn, target)) {
                pos = target;
            }
            target = pos.add(rand.nextInt(3) - 1, rand.nextInt(2) - rand.nextInt(2), rand.nextInt(3) - 1);
        }

        if (worldIn.isAirBlock(target) && this.canPlaceBlockAt(worldIn, target)) {
            worldIn.setBlockState(target, this.getDefaultState(), 2);
        }
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity te, ItemStack stack) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
        if (worldIn.isRemote || player.capabilities.isCreativeMode) {
            return;
        }

        boolean usingShears = stack.getItem() == Items.SHEARS;
        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
        NonNullList<ItemStack> drops = NonNullList.create();
        addConfiguredDrops(drops, usingShears, fortune, worldIn.rand);
        for (ItemStack drop : drops) {
            spawnAsEntity(worldIn, pos, drop);
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random random = world instanceof World ? ((World) world).rand : RANDOM;
        addConfiguredDrops(drops, false, fortune, random);
    }

    private void addConfiguredDrops(NonNullList<ItemStack> drops, boolean usingShears, int fortune, Random random) {
        if (usingShears) {
            drops.add(new ItemStack(this));
            return;
        }

        Item primaryItem = resolveItem(primaryDropId);
        if (primaryItem != null) {
            int count = 1;
            if (fortune > 0 && primaryFortuneMultiplier > 0) {
                count += random.nextInt(fortune * primaryFortuneMultiplier + 1);
            }
            drops.add(new ItemStack(primaryItem, Math.max(1, count)));
        }

        if (rareDropId != null && random.nextFloat() < rareDropChance) {
            Item rareItem = resolveItem(rareDropId);
            if (rareItem != null) {
                drops.add(new ItemStack(rareItem));
            }
        }

        if (secondaryDropId != null && secondaryDropMax > 0) {
            Item secondaryItem = resolveItem(secondaryDropId);
            if (secondaryItem != null) {
                int amount = secondaryDropMin;
                if (secondaryDropMax > secondaryDropMin) {
                    amount += random.nextInt(secondaryDropMax - secondaryDropMin + 1);
                }
                if (amount > 0) {
                    drops.add(new ItemStack(secondaryItem, amount));
                }
            }
        }
    }

    @Nullable
    private Item resolveItem(String id) {
        return ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation(id));
    }
}

