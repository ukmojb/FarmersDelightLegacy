package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.compat.BellCompat;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
public class ItemRope extends ItemBlock {
    public ItemRope(Block block) {
        super(block);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onUseBell(PlayerInteractEvent.RightClickBlock event) {
        if (event.getItemStack().getItem() instanceof ItemRope
                && BellCompat.isBell(event.getWorld().getBlockState(event.getPos()))) {
            event.setUseBlock(Event.Result.DENY);
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing face, float hitX, float hitY, float hitZ) {
        if (world.getBlockState(pos).getBlock() != block && !BellCompat.isBell(world.getBlockState(pos))) {
            return super.onItemUse(player, world, pos, hand, face, hitX, hitY, hitZ);
        }
        EnumFacing direction = player.isSneaking() ? face : EnumFacing.DOWN;
        BlockPos target = findTarget(world, pos, direction);
        if (target == null) {
            return EnumActionResult.FAIL;
        }
        return super.onItemUse(player, world, target.offset(direction.getOpposite()), hand,
                direction, hitX, hitY, hitZ);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing face, EntityPlayer player, ItemStack stack) {
        if (world.getBlockState(pos).getBlock() != block && !BellCompat.isBell(world.getBlockState(pos))) {
            return super.canPlaceBlockOnSide(world, pos, face, player, stack);
        }
        EnumFacing direction = player.isSneaking() ? face : EnumFacing.DOWN;
        BlockPos target = findTarget(world, pos, direction);
        return target != null && player.canPlayerEdit(target, direction, stack)
                && world.mayPlace(block, target, false, direction, player);
    }

    @Nullable
    private BlockPos findTarget(World world, BlockPos origin, EnumFacing direction) {
        BlockPos target = origin.offset(direction);
        while (target.getY() >= 0 && target.getY() < world.getHeight()) {
            if (world.getBlockState(target).getBlock() != block) {
                Material material = world.getBlockState(target).getMaterial();
                return (!material.isLiquid() || material == Material.WATER)
                        && world.getBlockState(target).getBlock().isReplaceable(world, target) ? target : null;
            }
            if (direction != EnumFacing.DOWN) {
                return null;
            }
            target = target.down();
        }
        return null;
    }
}
