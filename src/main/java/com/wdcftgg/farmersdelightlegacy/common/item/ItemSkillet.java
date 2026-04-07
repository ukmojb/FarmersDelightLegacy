package com.wdcftgg.farmersdelightlegacy.common.item;

import com.google.common.collect.Multimap;
import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockSkillet;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntitySkillet;
import com.wdcftgg.farmersdelightlegacy.common.util.HeatSourceHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class ItemSkillet extends ItemBlock {

    private static final String NBT_COOKING = "Cooking";
    private static final String NBT_COOK_TIME_HANDHELD = "CookTimeHandheld";
    private static final Item.ToolMaterial SKILLET_TOOL_MATERIAL = Item.ToolMaterial.IRON;

    public ItemSkillet(Block block) {
        super(block);
        this.setMaxStackSize(1);
        this.setMaxDamage(SKILLET_TOOL_MATERIAL.getMaxUses());
        this.addPropertyOverride(new ResourceLocation(Tags.MOD_ID, "cooking"),
                (stack, world, entity) -> hasCookingItem(stack) ? 1.0F : 0.0F);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack skilletStack = playerIn.getHeldItem(handIn);
        if (!isPlayerNearHeatSource(playerIn, worldIn)) {
            return new ActionResult<>(EnumActionResult.PASS, skilletStack);
        }

        EnumHand otherHand = handIn == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
        ItemStack cookingStack = playerIn.getHeldItem(otherHand);

        if (hasCookingItem(skilletStack)) {
            playerIn.setActiveHand(handIn);
            return new ActionResult<>(EnumActionResult.PASS, skilletStack);
        }

        CampfireCookingRecipe recipe = CampfireCookingRecipeManager.findRecipe(cookingStack);
        if (recipe == null) {
            playerIn.sendStatusMessage(new TextComponentTranslation("farmersdelight.item.skillet.how_to_cook"), true);
            return new ActionResult<>(EnumActionResult.PASS, skilletStack);
        }

        if (playerIn.isInWater() || playerIn.isWet()) {
            playerIn.sendStatusMessage(new TextComponentTranslation("farmersdelight.item.skillet.underwater"), true);
            return new ActionResult<>(EnumActionResult.PASS, skilletStack);
        }

        ItemStack cookingStackCopy = cookingStack.copy();
        ItemStack cookingStackUnit = cookingStackCopy.splitStack(1);
        NBTTagCompound tag = getOrCreateTag(skilletStack);
        tag.setTag(NBT_COOKING, cookingStackUnit.writeToNBT(new NBTTagCompound()));
        tag.setInteger(NBT_COOK_TIME_HANDHELD, recipe.getCookingTime());
        playerIn.setHeldItem(otherHand, cookingStackCopy);
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, skilletStack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        int fireAspectLevel = net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.FIRE_ASPECT, stack);
        NBTTagCompound tag = stack.getTagCompound();
        int cookingTime = tag != null ? tag.getInteger(NBT_COOK_TIME_HANDHELD) : 0;
        return BlockSkillet.getSkilletCookingTime(cookingTime, fireAspectLevel);
    }


    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        World world = player.world;
        if (world.rand.nextInt(50) == 0) {
            world.playSound(player.posX, player.posY, player.posZ,
                    ModSounds.SKILLET_SIZZLE,
                    SoundCategory.BLOCKS,
                    0.4F,
                    world.rand.nextFloat() * 0.2F + 0.9F,
                    false);
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (!(entityLiving instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) entityLiving;
        ItemStack cookingStack = takeCookingStack(stack);
        if (cookingStack.isEmpty()) {
            return;
        }

        if (!player.inventory.addItemStackToInventory(cookingStack)) {
            player.dropItem(cookingStack, false);
        }
        clearCookingTag(stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        if (!(entityLiving instanceof EntityPlayer)) {
            return stack;
        }

        EntityPlayer player = (EntityPlayer) entityLiving;
        ItemStack cookingStack = takeCookingStack(stack);
        if (cookingStack.isEmpty()) {
            return stack;
        }

        CampfireCookingRecipe recipe = CampfireCookingRecipeManager.findRecipe(cookingStack);
        if (recipe != null) {
            ItemStack resultStack = recipe.getResultStack();
            if (!player.inventory.addItemStackToInventory(resultStack)) {
                player.dropItem(resultStack, false);
            }
        } else if (!player.inventory.addItemStackToInventory(cookingStack)) {
            player.dropItem(cookingStack, false);
        }

        clearCookingTag(stack);
        return stack;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(1, attacker);
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if (!worldIn.isRemote && state.getBlockHardness(worldIn, pos) != 0.0F) {
            stack.damageItem(1, entityLiving);
        }
        return true;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return SKILLET_TOOL_MATERIAL.getRepairItemStack().getItem() == repair.getItem() || super.getIsRepairable(toRepair, repair);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> attributes = super.getItemAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            double attackDamage = 5.0D + SKILLET_TOOL_MATERIAL.getAttackDamage();
            attributes.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", attackDamage, 0));
            attributes.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", -3.1D, 0));
        }
        return attributes;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (!super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState)) {
            return false;
        }

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntitySkillet) {
            NBTTagCompound tileData = tileEntity.writeToNBT(new NBTTagCompound());
            ItemStack skilletData = stack.copy();
            skilletData.setCount(1);
            tileData.setTag("Skillet", skilletData.writeToNBT(new NBTTagCompound()));
            tileEntity.readFromNBT(tileData);
            tileEntity.markDirty();
        }
        return true;
    }

    private static boolean isPlayerNearHeatSource(EntityPlayer player, World world) {
        if (player.isBurning()) {
            return true;
        }
        BlockPos basePos = new BlockPos(player.posX, player.posY, player.posZ);
        return HeatSourceHelper.hasHeatSourceNearby(world, basePos, 1);
    }

    private static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    private static boolean hasCookingItem(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey(NBT_COOKING, 10);
    }

    private static ItemStack takeCookingStack(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(NBT_COOKING, 10)) {
            return ItemStack.EMPTY;
        }

        ItemStack cookingStack = new ItemStack(tag.getCompoundTag(NBT_COOKING));
        return cookingStack;
    }

    private static void clearCookingTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return;
        }
        tag.removeTag(NBT_COOKING);
        tag.removeTag(NBT_COOK_TIME_HANDHELD);
        if (tag.getSize() == 0) {
            stack.setTagCompound(null);
        }
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static class SkilletEvents {

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent event) {
            DamageSource damageSource = event.getSource();
            Entity attacker = damageSource.getImmediateSource();
            if (!(attacker instanceof EntityLivingBase)) {
                return;
            }

            EntityLivingBase living = (EntityLivingBase) attacker;
            ItemStack mainhand = living.getHeldItemMainhand();
            if (!(mainhand.getItem() instanceof ItemSkillet)) {
                return;
            }

            if (living.world.isRemote) {
                return;
            }

            float pitch = 0.9F + (living.getRNG().nextFloat() * 0.2F);
            if (living instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) living;
                float attackPower = player.getCooledAttackStrength(0.0F);
                if (attackPower > 0.8F) {
                    player.world.playSound(null, player.posX, player.posY, player.posZ,
                            ModSounds.SKILLET_ATTACK_STRONG, SoundCategory.PLAYERS, 1.0F, pitch);
                } else {
                    player.world.playSound(null, player.posX, player.posY, player.posZ,
                            ModSounds.SKILLET_ATTACK_WEAK, SoundCategory.PLAYERS, 0.8F, 0.9F);
                }
            } else {
                living.world.playSound(null, living.posX, living.posY, living.posZ,
                        ModSounds.SKILLET_ATTACK_STRONG, SoundCategory.PLAYERS, 1.0F, pitch);
            }
        }
    }
}

