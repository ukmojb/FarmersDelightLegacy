package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.advancement.ModAdvancements;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;

import java.util.Random;

public class BlockTomatoVine extends BlockCrops {

    public static final PropertyBool ROPELOGGED = PropertyBool.create("ropelogged");
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(2.0D / 16.0D, 0.0D, 2.0D / 16.0D, 14.0D / 16.0D, 1.0D, 14.0D / 16.0D);

    public BlockTomatoVine() {
        this(false);
    }

    protected BlockTomatoVine(boolean hanging) {
        this.setTickRandomly(true);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
        IBlockState state = this.blockState.getBaseState().withProperty(this.getAgeProperty(), 0);
        this.setDefaultState(hanging ? state : state.withProperty(ROPELOGGED, false));
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    protected Item getSeed() {
        return ModItems.ITEMS.get("tomato_seeds");
    }

    @Override
    protected Item getCrop() {
        return ModItems.ITEMS.get("tomato");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{this.getAgeProperty(), ROPELOGGED});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int age = Math.max(0, Math.min(3, this.getAge(state)));
        return age | (state.getValue(ROPELOGGED) ? 4 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int age = meta & 3;
        boolean ropelogged = (meta & 4) != 0;
        return this.withAge(age).withProperty(ROPELOGGED, ropelogged);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (this.getAge(state) < this.getMaxAge()) {
            return false;
        }

        harvestMatureTomatoes(worldIn, pos, state, playerIn);

        return true;
    }

    public static void harvestMatureTomatoes(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn) {
        float soundPitch = 0.8F + worldIn.rand.nextFloat() * 0.4F;
        if (worldIn.isRemote) {
            worldIn.playSound(playerIn, pos, ModSounds.tomatoesPickTomatoes, SoundCategory.BLOCKS, 1.0F, soundPitch);
        } else {
            int quantity = 1 + worldIn.rand.nextInt(2);
            Item tomatoItem = ModItems.ITEMS.get("tomato");
            if (tomatoItem != null) {
                Block.spawnAsEntity(worldIn, pos, new ItemStack(tomatoItem, quantity));
            }

            if (worldIn.rand.nextFloat() < 0.05F) {
                Item rottenTomatoItem = ModItems.ITEMS.get("rotten_tomato");
                if (rottenTomatoItem != null) {
                    Block.spawnAsEntity(worldIn, pos, new ItemStack(rottenTomatoItem));
                }
            }

            worldIn.playSound(playerIn, pos, ModSounds.tomatoesPickTomatoes, SoundCategory.BLOCKS, 1.0F, soundPitch);
            if (((BlockTomatoVine) state.getBlock()).hasRope(state)
                    && playerIn instanceof net.minecraft.entity.player.EntityPlayerMP) {
                ModAdvancements.HARVEST_ROPELOGGED_TOMATO.trigger((net.minecraft.entity.player.EntityPlayerMP) playerIn);
            }
            worldIn.setBlockState(pos, state.withProperty(AGE, 0), 2);
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            worldIn.destroyBlock(pos, true);
            if (state.getPropertyKeys().contains(ROPELOGGED) && state.getValue(ROPELOGGED)) {
                placeRope(worldIn, pos);
            }
        }
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote || !worldIn.isAreaLoaded(pos, 1)) {
            return;
        }
        if (convertLegacyState(worldIn, pos, state)) {
            return;
        }
        if (worldIn.getLightFromNeighbors(pos) >= 9) {
            int age = this.getAge(state);
            if (age < this.getMaxAge()) {
                if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt(6) == 0)) {
                    IBlockState grownState = state.withProperty(this.getAgeProperty(), age + 1);
                    worldIn.setBlockState(pos, grownState, 2);
                    ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
                }
            }
            climbRopeAbove(worldIn, pos);
        }
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        if (worldIn.isRemote || convertLegacyState(worldIn, pos, state)) {
            return;
        }
        int newAge = this.getAge(state) + this.getBonemealAgeIncrease(worldIn) / 2;
        if (newAge <= getMaxAge()) {
            worldIn.setBlockState(pos, state.withProperty(getAgeProperty(), newAge), 3);
            if (rand.nextFloat() < 0.3F) {
                climbRopeAbove(worldIn, pos);
            }
        } else {
            IBlockState above = worldIn.getBlockState(pos.up());
            if (Configuration.isTomatoVineClimbableRope(above.getBlock())) {
                climbRopeAbove(worldIn, pos);
            } else if (above.getBlock() instanceof BlockHangingTomato) {
                BlockHangingTomato hanging = (BlockHangingTomato) above.getBlock();
                if (hanging.canGrow(worldIn, pos.up(), above, false)) {
                    hanging.grow(worldIn, rand, pos.up(), above);
                }
            }
        }
    }

    @Override
    public void grow(World worldIn, BlockPos pos, IBlockState state) {
        grow(worldIn, worldIn.rand, pos, state);
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        if (!isMaxAge(state)) {
            return true;
        }
        for (int height = 1; height <= 2; height++) {
            IBlockState above = worldIn.getBlockState(pos.up(height));
            if (Configuration.isTomatoVineClimbableRope(above.getBlock())) {
                return true;
            }
            if (!(above.getBlock() instanceof BlockHangingTomato)) {
                return false;
            }
            if (!isMaxAge(above)) {
                return true;
            }
        }
        return false;
    }

    private boolean convertLegacyState(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() == ModBlocks.TOMATOES && state.getValue(ROPELOGGED)) {
            world.setBlockState(pos, ModBlocks.TOMATOES_ON_ROPE.getDefaultState()
                    .withProperty(getAgeProperty(), getAge(state)), 3);
            return true;
        }
        return false;
    }

    private void climbRopeAbove(World worldIn, BlockPos pos) {
        BlockPos abovePos = pos.up();
        if (!Configuration.isTomatoVineClimbableRope(worldIn.getBlockState(abovePos).getBlock())) {
            return;
        }

        int height = 1;
        while (worldIn.getBlockState(pos.down(height)).getBlock() == this) {
            height++;
        }
        if (height < 3) {
            worldIn.setBlockState(abovePos, ModBlocks.TOMATOES_ON_ROPE.getDefaultState(), 3);
        }
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return hasRope(state);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, net.minecraft.entity.Entity entityIn) {
        if (hasRope(state) && entityIn instanceof EntityLivingBase) {
            BlockRope.applyClimbingMotion((EntityLivingBase) entityIn);
        }
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn.getBlockState(pos.down()).getBlock() instanceof BlockTomatoVine) {
            return worldIn.getLightFromNeighbors(pos) >= 8 || worldIn.canSeeSky(pos);
        }
        return super.canBlockStay(worldIn, pos, state);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canBlockStay(worldIn, pos, state)) {
            if (!worldIn.isRemote) {
                worldIn.scheduleUpdate(pos, this, 1);
            }
            return;
        }
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (hasRope(state)) {
            onBlockHarvested(world, pos, state, player);
            return placeRope(world, pos);
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    protected boolean hasRope(IBlockState state) {
        return state.getValue(ROPELOGGED);
    }

    protected boolean placeRope(World world, BlockPos pos) {
        Block ropeBlock = Configuration.getDefaultTomatoVineRopeBlock();
        IBlockState rope = ropeBlock == ModBlocks.ROPE
                ? ropeBlock.getStateForPlacement(world, pos, EnumFacing.UP, 0.5F, 0.5F, 0.5F, 0, null, EnumHand.MAIN_HAND)
                : ropeBlock.getDefaultState();
        return world.setBlockState(pos, rope, world.isRemote ? 11 : 3);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random random = world instanceof World ? ((World) world).rand : RANDOM;
        if (!hasRope(state)) {
            drops.add(new ItemStack(getSeed()));
        }
        if (isMaxAge(state)) {
            int count = 1 + random.nextInt(2) + random.nextInt(Math.max(0, fortune) + 1);
            drops.add(new ItemStack(getCrop(), count));
            if (random.nextFloat() < 0.05F) {
                drops.add(new ItemStack(ModItems.get("rotten_tomato")));
            }
        }
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }
}
