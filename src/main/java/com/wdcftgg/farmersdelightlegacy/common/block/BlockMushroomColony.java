package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemKnife;
import com.wdcftgg.farmersdelightlegacy.common.recipe.ToolAction;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockMushroomColony extends BlockBush implements IGrowable {
    private static final int PLACING_LIGHT_LEVEL = 13;
    private static final int MAX_AGE = 3;
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
    private static final ToolAction SHEARS = new ToolAction("shears_harvest");
    private static final AxisAlignedBB[] SHAPES = new AxisAlignedBB[]{
            new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 0.5D, 0.75D),
            new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.625D, 0.8125D),
            new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.75D, 0.875D),
            new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D)
    };

    private final ResourceLocation mushroomItemId;

    public BlockMushroomColony(String mushroomItemId) {
        super(Material.PLANTS);
        this.mushroomItemId = new ResourceLocation(mushroomItemId);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        this.setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return state.isFullBlock();
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        BlockPos downPos = pos.down();
        IBlockState downState = worldIn.getBlockState(downPos);
        if (isMushroomGrowBlock(downState)) {
            return true;
        }
        return worldIn.getLight(pos) < PLACING_LIGHT_LEVEL
                && downState.getBlock().canSustainPlant(downState, worldIn, downPos, EnumFacing.UP, this);
    }

    private boolean isMushroomGrowBlock(IBlockState groundState) {
        if (groundState.getBlock() == Blocks.MYCELIUM) {
            return true;
        }
        if (groundState.getBlock() == Blocks.DIRT
                && groundState.getValue(net.minecraft.block.BlockDirt.VARIANT) == net.minecraft.block.BlockDirt.DirtType.PODZOL) {
            return true;
        }
        return groundState.getBlock() == ModBlocks.ORGANIC_COMPOST || groundState.getBlock() == ModBlocks.RICH_SOIL;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        int age = state.getValue(AGE);
        ItemStack held = playerIn.getHeldItem(hand);
        boolean shears = SHEARS.matches(held);
        if (age <= 0 || (!shears && !ItemKnife.isKnife(held)) || playerIn.isSpectator()
                || !playerIn.canPlayerEdit(pos, facing, held)) {
            return false;
        }
        if (worldIn.isRemote) {
            return true;
        }
        if (!worldIn.setBlockState(pos, state.withProperty(AGE, shears ? age - 1 : 0), 2)) {
            return false;
        }
        spawnAsEntity(worldIn, pos, new ItemStack(getMushroomItem(), shears ? 1 : age));
        held.damageItem(1, playerIn);
        worldIn.playSound(null, pos, shears ? SoundEvents.ENTITY_SHEEP_SHEAR : getSoundType(state, worldIn, pos, playerIn).getBreakSound(),
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (worldIn instanceof WorldServer) {
            double spread = shears ? 0.1D : 0.2D;
            ((WorldServer) worldIn).spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    shears ? 3 : 10, spread, spread, spread, shears ? 0.001D : 0.1D, getStateId(state));
        }
        return true;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote) {
            return;
        }
        if (!canBlockStay(worldIn, pos, state)) {
            super.updateTick(worldIn, pos, state, rand);
            return;
        }
        int age = state.getValue(AGE);
        IBlockState groundState = worldIn.getBlockState(pos.down());
        if (age < getMaxAge() && (BlockRichSoil.matchesBlockList(groundState, Configuration.mushroomColonyGrowableOn)
                || com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories.contains("fdMushroomColonyGrowableOn", groundState))
                && ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt(4) == 0)) {
            worldIn.setBlockState(pos, state.withProperty(AGE, age + 1), 2);
            ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return state.getValue(AGE) < getMaxAge();
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        int age = Math.min(getMaxAge(), state.getValue(AGE) + 1 + rand.nextInt(2));
        worldIn.setBlockState(pos, state.withProperty(AGE, age), 2);
    }

    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, Math.max(0, Math.min(getMaxAge(), meta)));
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity te, ItemStack stack) {
        player.addStat(StatList.getBlockStats(this));
        player.addExhaustion(0.005F);
        if (worldIn.isRemote || player.capabilities.isCreativeMode) {
            return;
        }

        NonNullList<ItemStack> drops = NonNullList.create();
        addConfiguredDrops(drops, state.getValue(AGE), SHEARS.matches(stack));
        float chance = ForgeEventFactory.fireBlockHarvesting(drops, worldIn, pos, state, 0, 1.0F, false, player);
        for (ItemStack drop : drops) {
            if (worldIn.rand.nextFloat() <= chance) {
                spawnAsEntity(worldIn, pos, drop);
            }
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        addConfiguredDrops(drops, state.getValue(AGE), false);
    }

    private void addConfiguredDrops(NonNullList<ItemStack> drops, int age, boolean usingShears) {
        if (usingShears && age == getMaxAge()) {
            drops.add(new ItemStack(this));
            return;
        }

        drops.add(new ItemStack(getMushroomItem(), age + 2));
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(getMushroomItem());
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(getMushroomItem());
    }

    private Item getMushroomItem() {
        Item item = ForgeRegistries.ITEMS.getValue(mushroomItemId);
        if (item == null) {
            throw new IllegalStateException("Missing mushroom item: " + mushroomItemId);
        }
        return item;
    }
}

