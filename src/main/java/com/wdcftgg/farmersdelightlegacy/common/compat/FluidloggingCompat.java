package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockBasket;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCookingPot;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCuttingBoard;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockRope;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockRopeFence;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockSafetyNet;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockSkillet;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasStandingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasWallHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasWallSign;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.FluidActionResult;
import git.jbredwards.fluidlogged_api.mod.common.config.FluidloggedAPIConfig;
import git.jbredwards.fluidlogged_api.api.event.FluidloggableEvent;
import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;

public final class FluidloggingCompat {
    private FluidloggingCompat() {
    }

    public static void registerAll() {
        if (Loader.isModLoaded("fluidlogged_api")) {
            MinecraftForge.EVENT_BUS.register(Handler.class);
        }
        if (Loader.isModLoaded(FarmersFutureDelightCompat.MOD_ID)) {
            FarmersFutureDelightWaterlogging.registerAll();
        }
        MinecraftForge.EVENT_BUS.register(BucketInteraction.class);
    }

    public static BlockStateContainer createBlockState(Block block, IProperty<?>... properties) {
        return Loader.isModLoaded(FarmersFutureDelightCompat.MOD_ID)
                ? FarmersFutureDelightWaterlogging.createBlockState(block, properties)
                : new BlockStateContainer(block, properties);
    }

    public static boolean placeBlock(World world, BlockPos pos, IBlockState state, int flags) {
        return Loader.isModLoaded(FarmersFutureDelightCompat.MOD_ID)
                ? FarmersFutureDelightWaterlogging.placeBlock(world, pos, state, flags)
                : world.setBlockState(pos, state, flags);
    }

    public static boolean removeBlock(World world, BlockPos pos) {
        return usesFutureDelightWaterlogging(world.getBlockState(pos))
                ? FarmersFutureDelightWaterlogging.removeBlock(world, pos)
                : world.setBlockToAir(pos);
    }

    public static boolean containsWater(IBlockAccess world, BlockPos pos) {
        return world != null && (world.getBlockState(pos).getMaterial() == Material.WATER
                || Loader.isModLoaded("fluidlogged_api") && Handler.containsWater(world, pos)
                || Loader.isModLoaded(FarmersFutureDelightCompat.MOD_ID)
                && FarmersFutureDelightWaterlogging.containsWater(world, pos));
    }

    public static boolean hasAdjacentWater(IBlockAccess world, BlockPos soilPos) {
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            if (containsWater(world, soilPos.offset(facing))) {
                return true;
            }
        }
        return false;
    }

    public static boolean canUseBucket(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
        if (usesFutureDelightWaterlogging(world.getBlockState(pos))) {
            return FarmersFutureDelightWaterlogging.canUseBucket(world, pos, stack);
        }
        return Loader.isModLoaded("fluidlogged_api") && Handler.canUseBucket(world, pos, player, stack);
    }

    public static boolean usesFutureDelightWaterlogging(IBlockState state) {
        return Loader.isModLoaded(FarmersFutureDelightCompat.MOD_ID)
                && FarmersFutureDelightWaterlogging.supportsState(state);
    }

    static boolean supportsWater(Block block) {
        return block instanceof BlockBasket || block instanceof BlockCookingPot || block instanceof BlockCuttingBoard
                || block instanceof BlockRope || block instanceof BlockRopeFence || block instanceof BlockSafetyNet
                || block instanceof BlockSkillet || block instanceof BlockCanvasStandingSign
                || block instanceof BlockCanvasWallSign || block instanceof BlockCanvasHangingSign
                || block instanceof BlockCanvasWallHangingSign;
    }

    public static final class BucketInteraction {
        private BucketInteraction() {
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (event.getUseItem() != Event.Result.DENY
                    && supportsWater(event.getWorld().getBlockState(event.getPos()).getBlock())
                    && canUseBucket(event.getWorld(), event.getPos(), event.getEntityPlayer(), event.getItemStack())) {
                event.setUseBlock(Event.Result.DENY);
            }
        }
    }

    public static final class Handler {
        private Handler() {
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onFillBucket(FillBucketEvent event) {
            if (event.getResult() != Event.Result.DEFAULT || event.getEmptyBucket().getItem() != Items.WATER_BUCKET
                    || !FluidloggedAPIConfig.bucketFluidlogging.test(event.getEntityPlayer())) {
                return;
            }
            EntityPlayer player = event.getEntityPlayer();
            World world = event.getWorld();
            Vec3d eyes = player.getPositionEyes(1.0F);
            Vec3d end = eyes.add(player.getLookVec().scale(player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()));
            RayTraceResult target = world.rayTraceBlocks(eyes, end, false, false, false);
            if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK
                    || event.getTarget() != null && target.getBlockPos().equals(event.getTarget().getBlockPos())) {
                return;
            }
            BlockPos pos = target.getBlockPos();
            IBlockState state = world.getBlockState(pos);
            if (!supportsWater(state.getBlock()) || state.getCollisionBoundingBox(world, pos) != Block.NULL_AABB
                    || !FluidloggedUtils.isStateFluidloggable(state, world, pos, FluidState.of(FluidRegistry.WATER))) {
                return;
            }
            ItemStack bucket = event.getEmptyBucket();
            if (!world.isBlockModifiable(player, pos) || !player.canPlayerEdit(pos, target.sideHit, bucket)) {
                event.setCanceled(true);
                return;
            }
            FillBucketEvent targeted = new FillBucketEvent(player, bucket, world, target);
            if (MinecraftForge.EVENT_BUS.post(targeted) || targeted.getResult() == Event.Result.DENY) {
                event.setCanceled(true);
                return;
            }
            if (targeted.getResult() == Event.Result.ALLOW) {
                event.setFilledBucket(targeted.getFilledBucket());
                event.setResult(Event.Result.ALLOW);
                return;
            }
            FluidActionResult result = FluidUtil.tryPlaceFluid(player, world, pos, bucket, new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME));
            if (result.isSuccess()) {
                if (player instanceof EntityPlayerMP) {
                    CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, bucket);
                }
                player.addStat(StatList.getObjectUseStats(Items.WATER_BUCKET));
                event.setFilledBucket(result.getResult());
                event.setResult(Event.Result.ALLOW);
            } else {
                event.setCanceled(true);
            }
        }

        private static boolean canUseBucket(World world, BlockPos pos, EntityPlayer player, ItemStack stack) {
            return stack.getItem() == Items.WATER_BUCKET && FluidloggedAPIConfig.bucketFluidlogging.test(player)
                    || stack.getItem() == Items.BUCKET && containsWater(world, pos);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onFluidloggable(FluidloggableEvent event) {
            if (event.getResult() == Event.Result.DEFAULT
                    && supportsWater(event.state.getBlock())) {
                event.setResult(!usesFutureDelightWaterlogging(event.state)
                        && isWater(event.fluidState) && event.fluidState.isSource()
                        ? Event.Result.ALLOW : Event.Result.DENY);
            }
        }

        private static boolean containsWater(IBlockAccess world, BlockPos pos) {
            return isWater(FluidState.get(world, pos));
        }

        private static boolean isWater(FluidState fluid) {
            return !fluid.isEmpty() && FluidloggedUtils.isCompatibleFluid(FluidRegistry.WATER, fluid.getFluid());
        }
    }
}
