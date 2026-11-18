package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import com.wdcftgg.farmersdelightlegacy.common.compat.BellCompat;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockWall;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
        return FluidloggingCompat.createBlockState(this, NORTH, EAST, WEST, SOUTH, TIED_TO_BELL);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            PropertyBool property = connectionProperty(direction);
            IBlockState neighbor = worldIn.getBlockState(pos.offset(direction));
            state = state.withProperty(property, tiesToRopeOrWall(neighbor)
                    || state.getValue(property) && tiesToSolid(worldIn, pos.offset(direction), direction.getOpposite()));
        }
        return state.withProperty(TIED_TO_BELL, BellCompat.isBell(worldIn.getBlockState(pos.up())));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing face, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState state = getDefaultState();
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.offset(direction);
            state = state.withProperty(connectionProperty(direction), tiesToRopeOrWall(world.getBlockState(neighbor))
                    || face.getAxis().isHorizontal() && tiesToSolid(world, neighbor, direction.getOpposite()));
        }
        return state;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            if (pos.offset(direction).equals(fromPos)) {
                IBlockState updated = state.withProperty(connectionProperty(direction), tiesToRopeOrWall(world.getBlockState(fromPos)));
                if (updated != state) {
                    world.setBlockState(pos, updated, 2);
                }
                return;
            }
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int metadata = 0;
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            if (state.getValue(connectionProperty(direction))) {
                metadata |= 1 << direction.getHorizontalIndex();
            }
        }
        return metadata;
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        IBlockState state = getDefaultState();
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            state = state.withProperty(connectionProperty(direction), (metadata & 1 << direction.getHorizontalIndex()) != 0);
        }
        return state;
    }

    private static PropertyBool connectionProperty(EnumFacing direction) {
        switch (direction) {
            case NORTH: return NORTH;
            case SOUTH: return SOUTH;
            case EAST: return EAST;
            case WEST: return WEST;
            default: throw new IllegalArgumentException("Rope connections require a horizontal direction");
        }
    }

    private static boolean tiesToRopeOrWall(IBlockState state) {
        return state.getBlock() instanceof BlockPane || state.getBlock() instanceof BlockWall;
    }

    private static boolean tiesToSolid(IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState state = world.getBlockState(pos);
        return !isExcepBlockForAttachWithPiston(state.getBlock())
                && state.getBlockFaceShape(world, pos, face) == BlockFaceShape.SOLID;
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
            return false;
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

    private BlockPos findLowestRope(BlockPos originPos, World worldIn) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(originPos.getX(), originPos.getY(), originPos.getZ());
        while (cursor.getY() > 0 && worldIn.getBlockState(cursor.down()).getBlock() == this) {
            cursor.move(EnumFacing.DOWN);
        }
        return cursor.toImmutable();
    }

    private boolean isHoldingRope(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(this);
    }

    private boolean reelRope(World worldIn, BlockPos pos, EntityPlayer playerIn) {
        BlockPos target = findLowestRope(pos, worldIn);
        if (!playerIn.capabilities.allowEdit || !playerIn.canPlayerEdit(target, EnumFacing.UP, ItemStack.EMPTY)) {
            return false;
        }
        ItemStack ropeStack = new ItemStack(Item.getItemFromBlock(this));
        if (!playerIn.capabilities.isCreativeMode && playerIn.inventory.storeItemStack(ropeStack) < 0
                && playerIn.inventory.getFirstEmptyStack() < 0) {
            return false;
        }
        if (!worldIn.isRemote) {
            worldIn.playEvent(2001, target, Block.getStateId(worldIn.getBlockState(target)));
            if (!FluidloggingCompat.removeBlock(worldIn, target)) {
                return false;
            }
            if (!playerIn.capabilities.isCreativeMode) {
                playerIn.inventory.addItemStackToInventory(ropeStack);
            }
        }
        return true;
    }

    private boolean ringBellAbove(World worldIn, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        for (int i = 0; i < 24 && cursor.getY() < worldIn.getHeight() - 1; i++) {
            cursor.move(EnumFacing.UP);
            IBlockState stateAbove = worldIn.getBlockState(cursor);
            Block blockAbove = stateAbove.getBlock();
            if (blockAbove == this) {
                continue;
            }
            return BellCompat.ring(worldIn, cursor.toImmutable(), stateAbove);
        }
        return false;
    }

}
