package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class BlockRope extends BlockPane {

    public static final PropertyBool TIED_TO_BELL = PropertyBool.create("tied_to_bell");

    public BlockRope() {
        super(Material.CLOTH, true);
        this.setHardness(0.2F);
        this.setResistance(0.2F);
        this.setSoundType(SoundType.CLOTH);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(NORTH, false)
                .withProperty(EAST, false)
                .withProperty(SOUTH, false)
                .withProperty(WEST, false)
                .withProperty(TIED_TO_BELL, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{NORTH, EAST, WEST, SOUTH, TIED_TO_BELL});
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        IBlockState actual = super.getActualState(state, worldIn, pos);
        return actual.withProperty(TIED_TO_BELL, isBellBlock(worldIn.getBlockState(pos.up())));
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return true;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> collidingBoxes, @Nullable net.minecraft.entity.Entity entityIn,
                                      boolean isActualState) {
        // 绳子本身不提供实体碰撞盒，允许玩家与实体直接穿过。
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (!(entityIn instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase living = (EntityLivingBase) entityIn;
        if (living instanceof EntityPlayer && ((EntityPlayer) living).isSpectator()) {
            return;
        }

        applyClimbingMotion(living);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldStack = playerIn.getHeldItem(hand);
        if (isHoldingRope(heldStack)) {
            return worldIn.isRemote || tryPlaceRope(worldIn, pos, playerIn, heldStack, facing);
        }

        if (!heldStack.isEmpty()) {
            return false;
        }

        if (playerIn.isSneaking() && Configuration.enableRopeReeling) {
            return reelRope(worldIn, pos, playerIn);
        }

        return ringBellAbove(worldIn, pos);
    }

    public static void applyClimbingMotion(EntityLivingBase living) {
        living.motionX = MathHelper.clamp(living.motionX, -0.15000000596046448D, 0.15000000596046448D);
        living.motionZ = MathHelper.clamp(living.motionZ, -0.15000000596046448D, 0.15000000596046448D);
        living.fallDistance = 0.0F;
        if (living.isSneaking()) {
            if (living.motionY < 0.0D) {
                living.motionY = 0.0D;
            }
            return;
        }

        if (living.isJumping) {
            living.motionY = Math.max(living.motionY, 0.20000000298023224D);
            return;
        }

        living.motionY = Math.max(living.motionY, -0.15D);
    }

    private boolean tryPlaceRope(World worldIn, BlockPos pos, EntityPlayer playerIn, ItemStack heldStack, EnumFacing facing) {
        BlockPos targetPos = playerIn.isSneaking() ? pos.offset(facing) : findLowestRope(pos, worldIn).down();
        if (!playerIn.canPlayerEdit(targetPos, facing, heldStack)) {
            return false;
        }

        IBlockState targetState = worldIn.getBlockState(targetPos);
        if (!targetState.getBlock().isReplaceable(worldIn, targetPos)) {
            return false;
        }

        if (!worldIn.mayPlace(this, targetPos, false, facing, playerIn) || !this.canPlaceBlockAt(worldIn, targetPos)) {
            return false;
        }

        if (!worldIn.isRemote) {
            worldIn.setBlockState(targetPos, this.getDefaultState(), 3);
            worldIn.playSound(null, targetPos, this.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!playerIn.capabilities.isCreativeMode) {
                heldStack.shrink(1);
            }
        }
        return true;
    }

    private BlockPos findLowestRope(BlockPos originPos, World worldIn) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(originPos.getX(), originPos.getY(), originPos.getZ());
        while (worldIn.getBlockState(cursor.down()).getBlock() == this) {
            cursor.move(EnumFacing.DOWN);
        }
        return cursor.toImmutable();
    }

    private boolean isHoldingRope(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(this);
    }

    private boolean reelRope(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        int minY = 0;
        while (cursor.getY() >= minY) {
            if (worldIn.getBlockState(cursor).getBlock() == this) {
                cursor.move(EnumFacing.DOWN);
                continue;
            }
            cursor.move(EnumFacing.UP);
            break;
        }

        if (worldIn.getBlockState(cursor).getBlock() != this) {
            return false;
        }

        if (!worldIn.isRemote) {
            if (!playerIn.capabilities.isCreativeMode) {
                ItemStack ropeStack = new ItemStack(Item.getItemFromBlock(this));
                if (!playerIn.inventory.addItemStackToInventory(ropeStack)) {
                    playerIn.dropItem(ropeStack, false);
                }
            }
            worldIn.destroyBlock(cursor, false);
        }
        return true;
    }

    private boolean ringBellAbove(World worldIn, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < 24; i++) {
            cursor.move(EnumFacing.UP);
            IBlockState stateAbove = worldIn.getBlockState(cursor);
            Block blockAbove = stateAbove.getBlock();
            if (blockAbove == this) {
                continue;
            }
            if (isBellBlock(stateAbove)) {
                worldIn.playSound(null, cursor, SoundEvents.BLOCK_NOTE_BELL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean isBellBlock(IBlockState state) {
        if (state == null || state.getBlock() == null || state.getBlock().getRegistryName() == null) {
            return false;
        }
        String path = state.getBlock().getRegistryName().getPath();
        return path != null && path.contains("bell");
    }
}
