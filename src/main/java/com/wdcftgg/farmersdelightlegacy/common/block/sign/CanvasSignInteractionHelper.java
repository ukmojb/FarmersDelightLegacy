package com.wdcftgg.farmersdelightlegacy.common.block.sign;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import com.wdcftgg.farmersdelightlegacy.common.network.ModNetworkHandler;
import com.wdcftgg.farmersdelightlegacy.common.network.PacketOpenCanvasSignEditor;
import com.wdcftgg.farmersdelightlegacy.common.registry.ItemCategories;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCanvasSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class CanvasSignInteractionHelper {
    private CanvasSignInteractionHelper() {
    }

    public static boolean openEditor(World world, BlockPos pos, EntityPlayer player) {
        if (!Configuration.enableCanvasSignEditing || player.isSpectator() || !player.capabilities.allowEdit) {
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityCanvasSign && player instanceof EntityPlayerMP) {
            TileEntityCanvasSign sign = (TileEntityCanvasSign) tile;
            boolean front = sign.isFrontSide(player);
            if (sign.beginEdit(player, front)) {
                ModNetworkHandler.INSTANCE.sendTo(new PacketOpenCanvasSignEditor(pos, front), (EntityPlayerMP) player);
            }
        }
        return true;
    }

    public static boolean interact(World world, BlockPos pos, EntityPlayer player, EnumHand hand, boolean allowItem) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityCanvasSign) || player.isSpectator()) {
            return false;
        }
        if (allowItem && FluidloggingCompat.canUseBucket(world, pos, player, player.getHeldItem(hand))) {
            return false;
        }
        TileEntityCanvasSign sign = (TileEntityCanvasSign) tile;
        boolean front = sign.isFrontSide(player);
        if (world.isRemote) {
            return true;
        }
        if (sign.executeCommands(player, front)) {
            return true;
        }
        if (sign.isWaxed()) {
            world.playSound(null, pos, SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        if (sign.hasEditor()) {
            return true;
        }
        ItemStack stack = player.getHeldItem(hand);
        if (allowItem && player.capabilities.allowEdit && !stack.isEmpty()) {
            boolean changed = false;
            SoundEvent sound = SoundEvents.ENTITY_SMALL_SLIME_SQUISH;
            if (ItemCategories.contains("fdHoneycomb", stack)) {
                changed = sign.wax();
                sound = SoundEvents.BLOCK_SLIME_PLACE;
            } else if (sign.hasText(front)) {
                if (ItemCategories.contains("fdGlowInkSac", stack)) {
                    changed = sign.setGlowing(front, true);
                } else if (stack.getItem() == Items.DYE && stack.getMetadata() == 0 && sign.isGlowing(front)) {
                    changed = sign.setGlowing(front, false);
                } else {
                    EnumDyeColor color = dyeColor(stack);
                    if (color != null) {
                        changed = sign.setColor(front, color);
                    }
                }
            }
            if (changed) {
                player.addStat(StatList.getObjectUseStats(stack.getItem()));
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
                world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        }
        return openEditor(world, pos, player);
    }

    private static EnumDyeColor dyeColor(ItemStack stack) {
        if (stack.getItem() == Items.DYE) {
            return EnumDyeColor.byDyeDamage(stack.getMetadata());
        }
        for (EnumDyeColor color : EnumDyeColor.values()) {
            String name = color == EnumDyeColor.SILVER ? "lightGray" : color.getTranslationKey();
            if (ItemCategories.contains("dye" + Character.toUpperCase(name.charAt(0)) + name.substring(1), stack)) {
                return color;
            }
        }
        return null;
    }

    @Mod.EventBusSubscriber
    public static final class CanvasSignInteractionEvents {
        private CanvasSignInteractionEvents() {
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickCanvasSign(PlayerInteractEvent.RightClickBlock event) {
            if (event.getUseBlock() == Event.Result.DENY) {
                return;
            }
            EntityPlayer player = event.getEntityPlayer();
            World world = event.getWorld();
            BlockPos pos = event.getPos();
            ItemStack stack = player.getHeldItem(event.getHand());
            boolean signMaterial = ItemCategories.contains("fdHoneycomb", stack)
                    || ItemCategories.contains("fdGlowInkSac", stack) || dyeColor(stack) != null;
            if (player.isSneaking() && !stack.doesSneakBypassUse(world, pos, player) && !signMaterial) {
                return;
            }
            IBlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof BlockCanvasStandingSign)
                    && !(state.getBlock() instanceof BlockCanvasWallSign)
                    && !(state.getBlock() instanceof BlockCanvasHangingSign)
                    && !(state.getBlock() instanceof BlockCanvasWallHangingSign)) {
                return;
            }
            if (interact(world, pos, player, event.getHand(), event.getUseItem() != Event.Result.DENY)) {
                event.setUseBlock(Event.Result.DENY);
                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
        }
    }
}
