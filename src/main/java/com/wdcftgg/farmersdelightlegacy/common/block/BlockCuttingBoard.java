package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.advancement.ModAdvancements;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CuttingBoardRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCuttingBoard;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class BlockCuttingBoard extends BlockHorizontal implements ITileEntityProvider {

    private static final AxisAlignedBB BOARD_SHAPE = new AxisAlignedBB(1.0D / 16.0D, 0.0D, 1.0D / 16.0D, 15.0D / 16.0D, 1.0D / 16.0D, 15.0D / 16.0D);

    public BlockCuttingBoard() {
        super(Material.WOOD);
        this.setHardness(1.0F);
        this.setResistance(2.0F);
        this.setSoundType(SoundType.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumFacing = EnumFacing.byHorizontalIndex(meta & 3);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING});
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCuttingBoard();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntityCuttingBoard)) {
            return false;
        }
        TileEntityCuttingBoard cuttingBoard = (TileEntityCuttingBoard) tileEntity;
        ItemStack heldStack = playerIn.getHeldItem(hand);

        if (cuttingBoard.isEmpty()) {
            if (heldStack.isEmpty() || CuttingBoardRecipeManager.isUsedAsRecipeTool(heldStack)) {
                return false;
            }

            if (!worldIn.isRemote) {
                ItemStack storedStack = heldStack.copy();
                storedStack.setCount(1);
                if (cuttingBoard.setStoredItem(storedStack) && !playerIn.capabilities.isCreativeMode) {
                    heldStack.shrink(1);
                }
                worldIn.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 0.8F, 0.95F);
            }
            return true;
        }

        if (heldStack.isEmpty() && hand == EnumHand.MAIN_HAND) {
            if (CuttingBoardRecipeManager.hasRecipe(cuttingBoard.getStoredItem(), ItemStack.EMPTY)) {
                if (!worldIn.isRemote) {
                    processStoredItem(worldIn, pos, state, playerIn, cuttingBoard, ItemStack.EMPTY);
                }
                return true;
            }

            if (!worldIn.isRemote) {
                ItemStack removedStack = cuttingBoard.removeStoredItem();
                if (!removedStack.isEmpty() && !playerIn.inventory.addItemStackToInventory(removedStack)) {
                    playerIn.dropItem(removedStack, false);
                }
                worldIn.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 0.25F, 0.5F);
            }
            return true;
        }

        if (!heldStack.isEmpty()) {
            if (CuttingBoardRecipeManager.hasRecipe(cuttingBoard.getStoredItem(), heldStack)) {
                if (!worldIn.isRemote) {
                    processStoredItem(worldIn, pos, state, playerIn, cuttingBoard, heldStack);
                }
                return true;
            }

            // 与上游 CONSUME 语义对齐：砧板占用时吞掉无效交互，防止误放置方块。
            return true;
        }

        return false;
    }

    private static void processStoredItem(World world, BlockPos pos, IBlockState state, EntityPlayer player, TileEntityCuttingBoard cuttingBoard, ItemStack toolStack) {
        processStoredItem(world, pos, state, player, cuttingBoard, toolStack, true);
    }

    public static boolean processStoredItemFromDispenser(World world, BlockPos pos, IBlockState state, TileEntityCuttingBoard cuttingBoard, ItemStack toolStack) {
        if (cuttingBoard == null || cuttingBoard.isEmpty() || !CuttingBoardRecipeManager.hasRecipe(cuttingBoard.getStoredItem(), toolStack)) {
            return false;
        }
        processStoredItem(world, pos, state, null, cuttingBoard, toolStack, true);
        return true;
    }

    private static void processStoredItem(World world, BlockPos pos, IBlockState state, EntityPlayer player, TileEntityCuttingBoard cuttingBoard, ItemStack toolStack, boolean damageTool) {
        ItemStack particleStack = cuttingBoard.getStoredItem();
        List<ItemStack> craftedStacks = cuttingBoard.processStoredItem(toolStack);
        EnumFacing dropFacing = state.getValue(FACING).rotateYCCW();
        for (ItemStack craftedStack : craftedStacks) {
            if (craftedStack.isEmpty()) {
                continue;
            }
            EntityItem drop = new EntityItem(
                    world,
                    pos.getX() + 0.5D + (dropFacing.getXOffset() * 0.2D),
                    pos.getY() + 0.2D,
                    pos.getZ() + 0.5D + (dropFacing.getZOffset() * 0.2D),
                    craftedStack.copy());
            drop.motionX = dropFacing.getXOffset() * 0.2D;
            drop.motionY = 0.0D;
            drop.motionZ = dropFacing.getZOffset() * 0.2D;
            world.spawnEntity(drop);
        }
        if (damageTool && !toolStack.isEmpty()) {
            damageTool(world, player, toolStack);
        }
        world.playSound(null, pos, ModSounds.CUTTING_BOARD_KNIFE, SoundCategory.BLOCKS, 0.9F, 1.0F);
        spawnCuttingParticles(world, pos, particleStack, 5);
        if (player instanceof net.minecraft.entity.player.EntityPlayerMP) {
            ModAdvancements.USE_CUTTING_BOARD.trigger((net.minecraft.entity.player.EntityPlayerMP) player);
        }
    }

    private static void damageTool(World world, EntityPlayer player, ItemStack toolStack) {
        if (player != null) {
            toolStack.damageItem(1, player);
            return;
        }
        if (toolStack.attemptDamageItem(1, world.rand, null)) {
            toolStack.shrink(1);
            toolStack.setItemDamage(0);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCuttingBoard) {
            ItemStack storedItem = ((TileEntityCuttingBoard) tileEntity).getStoredItem();
            if (!storedItem.isEmpty()) {
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), storedItem);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOARD_SHAPE;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return BOARD_SHAPE;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, net.minecraft.block.Block blockIn, BlockPos fromPos) {
        if (fromPos.getY() == pos.getY() - 1 && !worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP)) {
            worldIn.destroyBlock(pos, true);
            return;
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityCuttingBoard) {
            return ((TileEntityCuttingBoard) tileEntity).isEmpty() ? 0 : 15;
        }
        return 0;
    }


    public static void spawnCuttingParticles(World world, BlockPos pos, ItemStack stack, int count) {
        for (int i = 0; i < count; i++) {
            double motionX = (world.rand.nextFloat() - 0.5D) * 0.1D;
            double motionY = Math.random() * 0.1D + 0.1D;
            double motionZ = (world.rand.nextFloat() - 0.5D) * 0.1D;
            if (world instanceof WorldServer) {
                ((WorldServer) world).spawnParticle(
                        EnumParticleTypes.ITEM_CRACK,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.1D,
                        pos.getZ() + 0.5D,
                        1,
                        motionX,
                        motionY + 0.05D,
                        motionZ,
                        0.0D,
                        Item.getIdFromItem(stack.getItem()),
                        stack.getMetadata());
            } else {
                world.spawnParticle(
                        EnumParticleTypes.ITEM_CRACK,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.1D,
                        pos.getZ() + 0.5D,
                        motionX,
                        motionY + 0.05D,
                        motionZ,
                        Item.getIdFromItem(stack.getItem()),
                        stack.getMetadata());
            }
        }
    }

    private static boolean isCarvingTool(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof ItemTool
                || stack.getItem() instanceof ItemSword
                || stack.getItem() instanceof ItemShears;
    }

    @Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
    public static class ToolCarvingEvent {

        @SubscribeEvent
        public static void onSneakPlaceTool(PlayerInteractEvent.RightClickBlock event) {
            EntityPlayer player = event.getEntityPlayer();
            if (player == null || !player.isSneaking() || event.getHand() != EnumHand.MAIN_HAND) {
                return;
            }

            World world = event.getWorld();
            BlockPos pos = event.getPos();
            IBlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof BlockCuttingBoard)) {
                return;
            }

            ItemStack heldStack = player.getHeldItemMainhand();
            if (!isCarvingTool(heldStack) || heldStack.getItem() instanceof ItemBlock) {
                return;
            }

            TileEntity tileEntity = world.getTileEntity(pos);
            if (!(tileEntity instanceof TileEntityCuttingBoard)) {
                return;
            }

            TileEntityCuttingBoard cuttingBoard = (TileEntityCuttingBoard) tileEntity;
            if (!cuttingBoard.isEmpty()) {
                return;
            }

            if (!world.isRemote) {
                ItemStack toPlace = player.capabilities.isCreativeMode ? heldStack.copy() : heldStack;
                if (cuttingBoard.carveToolOnBoard(toPlace)) {
                    if (!player.capabilities.isCreativeMode) {
                        heldStack.shrink(1);
                    }
                    world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
                }
            }

            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
}

