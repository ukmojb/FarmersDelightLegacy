package com.wdcftgg.farmersdelightlegacy.common.item;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.api.knife.ItemKnifeBase;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockPie;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.HarvestDropRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.HuntingDropRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.KnifeItemManager;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;
import com.wdcftgg.farmersdelightlegacy.common.registry.ItemCategories;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCake;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.Map;
import java.util.WeakHashMap;

public class ItemKnife extends ItemKnifeBase {

    private static final float knockbackReduction = 0.1F;

    public ItemKnife(Item.ToolMaterial material, double attackDamage) {
        super(material, attackDamage);
    }

    public static boolean isKnife(ItemStack stack) {
        return KnifeItemManager.isKnife(stack);
    }

    @Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID)
    public static final class KnifeEvents {

        private static final Map<EntityPlayer, HarvestTool> HARVEST_TOOLS = new WeakHashMap<>();

        private KnifeEvents() {
        }

        @SubscribeEvent
        public static void onCakeInteraction(PlayerInteractEvent.RightClickBlock event) {
            EntityPlayer player = event.getEntityPlayer();
            ItemStack tool = event.getItemStack();
            World world = event.getWorld();
            BlockPos pos = event.getPos();
            IBlockState state = world.getBlockState(pos);
            boolean toppedCake = BlockCategories.contains("fdDropsCakeSlice", state);
            EnumFacing face = event.getFace() == null ? EnumFacing.UP : event.getFace();
            if (event.isCanceled() || event.getUseBlock() == Event.Result.DENY || event.getUseItem() == Event.Result.DENY
                    || player.isSpectator() || !ItemKnife.isKnife(tool)
                    || state.getBlock() != Blocks.CAKE && !toppedCake || !player.canPlayerEdit(pos, face, tool)) {
                return;
            }
            if (!world.isRemote) {
                int bites = toppedCake ? 0 : state.getValue(BlockCake.BITES);
                NonNullList<ItemStack> toppings = NonNullList.create();
                if (toppedCake) {
                    state.getBlock().getDrops(toppings, world, pos, state, 0);
                }
                IBlockState cake = toppedCake ? Blocks.CAKE.getDefaultState() : state;
                boolean changed = bites == 6 ? world.setBlockToAir(pos)
                        : world.setBlockState(pos, cake.withProperty(BlockCake.BITES, bites + 1), 3);
                if (!changed) {
                    event.setCancellationResult(EnumActionResult.FAIL);
                    event.setCanceled(true);
                    return;
                }
                for (ItemStack topping : toppings) {
                    Block.spawnAsEntity(world, pos, topping);
                }
                EntityItem slice = new EntityItem(world, pos.getX() + bites * 0.1D,
                        pos.getY() + 0.2D, pos.getZ() + 0.5D, new ItemStack(ModItems.get("cake_slice")));
                slice.motionX = -0.05D;
                slice.motionY = 0.0D;
                slice.motionZ = 0.0D;
                slice.setDefaultPickupDelay();
                world.spawnEntity(slice);
                world.playSound(null, pos, ModSounds.foodSlice, SoundCategory.PLAYERS, 0.8F, 0.8F);
                StatBase stat = StatList.getObjectUseStats(tool.getItem());
                if (stat != null) {
                    player.addStat(stat);
                }
            }
            event.setCancellationResult(EnumActionResult.SUCCESS);
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onKnifeKnockback(LivingKnockBackEvent event) {
            Entity attacker = event.getEntityLiving().getAttackingEntity();
            if (!(attacker instanceof EntityLivingBase)) {
                attacker = event.getEntityLiving().getRevengeTarget();
            }
            if (!(attacker instanceof EntityLivingBase)) {
                return;
            }

            ItemStack toolStack = ((EntityLivingBase) attacker).getHeldItemMainhand();
            if (!ItemKnife.isKnife(toolStack)) {
                return;
            }

            event.setStrength(Math.max(0.0F, event.getOriginalStrength() - knockbackReduction));
        }

        @SubscribeEvent
        public static void onLivingDrops(LivingDropsEvent event) {
            Entity source = event.getSource().getTrueSource();
            if (!(source instanceof EntityLivingBase)) {
                return;
            }

            EntityLivingBase attacker = (EntityLivingBase) source;
            ItemStack toolStack = attacker.getHeldItemMainhand();
            if (!ItemKnife.isKnife(toolStack)) {
                return;
            }

            HuntingDropRecipeManager.addDrops(event, attacker, toolStack);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            EntityPlayer player = event.getPlayer();
            ItemStack tool = player.getHeldItemMainhand();
            if (!event.isCanceled() && (ItemKnife.isKnife(tool)
                    || ItemCategories.contains("fdStrawHarvesters", tool))) {
                HARVEST_TOOLS.put(player, new HarvestTool(tool.copy(), event.getPos(), event.getState(),
                        event.getWorld().getTotalWorldTime()));
            } else {
                HARVEST_TOOLS.remove(player);
            }
        }

        @SubscribeEvent
        public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
            EntityPlayer player = event.getHarvester();
            if (player == null) {
                return;
            }

            ItemStack toolStack = player.getHeldItemMainhand();
            HarvestTool recorded = HARVEST_TOOLS.get(player);
            if (recorded != null && recorded.pos.equals(event.getPos()) && recorded.state == event.getState()
                    && recorded.time == event.getWorld().getTotalWorldTime()) {
                toolStack = recorded.stack;
                HARVEST_TOOLS.remove(player);
            }
            if (!ItemKnife.isKnife(toolStack)) {
                if (!event.isSilkTouching()
                        && ItemCategories.contains("fdStrawHarvesters", toolStack)) {
                    HarvestDropRecipeManager.addStrawDrops(event);
                }
                return;
            }

            IBlockState state = event.getState();
            if (state.getBlock() == Blocks.CAKE) {
                event.getDrops().add(new ItemStack(ModItems.get("cake_slice"), 7 - state.getValue(BlockCake.BITES)));
            } else if (state.getBlock() instanceof BlockPie) {
                ItemStack slices = ((BlockPie) state.getBlock()).getPieSlice();
                slices.setCount(4 - state.getValue(BlockPie.BITES));
                event.getDrops().add(slices);
            }
            if (event.isSilkTouching()) {
                return;
            }
            if (state.getBlock() == Blocks.PUMPKIN) {
                for (int i = 0; i < event.getDrops().size(); i++) {
                    ItemStack drop = event.getDrops().get(i);
                    if (drop.getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) {
                        event.getDrops().set(i, new ItemStack(ModItems.get("pumpkin_slice"), drop.getCount() * 4));
                    }
                }
            }
            HarvestDropRecipeManager.addDrops(event, toolStack);
        }

        private static final class HarvestTool {
            private final ItemStack stack;
            private final BlockPos pos;
            private final IBlockState state;
            private final long time;

            private HarvestTool(ItemStack stack, BlockPos pos, IBlockState state, long time) {
                this.stack = stack;
                this.pos = pos.toImmutable();
                this.state = state;
                this.time = time;
            }
        }
    }
}
