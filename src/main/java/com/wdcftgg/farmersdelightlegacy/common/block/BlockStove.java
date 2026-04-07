package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipe;
import com.wdcftgg.farmersdelightlegacy.common.recipe.CampfireCookingRecipeManager;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityStove;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockStove extends BlockHorizontal implements ITileEntityProvider {

    public static final PropertyBool LIT = PropertyBool.create("lit");

    public BlockStove() {
        super(Material.ROCK);
        this.setHardness(2.0F);
        this.setResistance(3.5F);
        this.setSoundType(SoundType.STONE);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(LIT, false));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityStove();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(LIT, true);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldStack = playerIn.getHeldItem(hand);
        Item heldItem = heldStack.getItem();

        if (state.getValue(LIT)) {
            if (heldItem instanceof ItemSpade) {
                extinguish(state, worldIn, pos);
                heldStack.damageItem(1, playerIn);
                return true;
            }
            if (heldItem == Items.WATER_BUCKET) {
                if (!worldIn.isRemote) {
                    worldIn.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    if (!playerIn.capabilities.isCreativeMode) {
                        playerIn.setHeldItem(hand, new ItemStack(Items.BUCKET));
                    }
                }
                extinguish(state, worldIn, pos);
                return true;
            }
        } else {
            if (heldItem instanceof ItemFlintAndSteel) {
                worldIn.playSound(playerIn, pos, net.minecraft.init.SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, worldIn.rand.nextFloat() * 0.4F + 0.8F);
                worldIn.setBlockState(pos, state.withProperty(LIT, true), 11);
                heldStack.damageItem(1, playerIn);
                return true;
            }
            if (heldItem instanceof ItemFireball) {
                worldIn.playSound(null, pos, net.minecraft.init.SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 1.0F, (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.2F + 1.0F);
                worldIn.setBlockState(pos, state.withProperty(LIT, true), 11);
                if (!playerIn.capabilities.isCreativeMode) {
                    heldStack.shrink(1);
                }
                return true;
            }
        }

        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityStove) {
            TileEntityStove stove = (TileEntityStove) tileEntity;
            int stoveSlot = stove.getNextEmptySlot();
            if (stoveSlot < 0 || stove.isStoveBlockedAbove()) {
                return false;
            }

            CampfireCookingRecipe recipe = CampfireCookingRecipeManager.findRecipe(heldStack);
            if (recipe != null) {
                if (!worldIn.isRemote) {
                    ItemStack inputStack = playerIn.capabilities.isCreativeMode ? heldStack.copy() : heldStack;
                    if (stove.addItem(inputStack, recipe, stoveSlot)) {
                        return true;
                    }
                }
                return true;
            }
        }

        return false;
    }

    public void extinguish(IBlockState state, World world, BlockPos pos) {
        world.setBlockState(pos, state.withProperty(LIT, false), 2);
        world.playSound(null, pos, net.minecraft.init.SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if (worldIn.getBlockState(pos).getValue(LIT)
                && entityIn instanceof EntityLivingBase
                && !entityIn.isImmuneToFire()
                && !EnchantmentHelper.hasFrostWalkerEnchantment((EntityLivingBase) entityIn)) {
            entityIn.attackEntityFrom(DamageSource.HOT_FLOOR, 1.0F);
        }
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityStove) {
            net.minecraft.inventory.InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityStove) tileEntity);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (!stateIn.getValue(LIT)) {
            return;
        }

        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        if (rand.nextInt(10) == 0) {
            worldIn.playSound(x, y, z, ModSounds.STOVE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }

        EnumFacing direction = stateIn.getValue(FACING);
        EnumFacing.Axis axis = direction.getAxis();
        double horizontalOffset = rand.nextDouble() * 0.6D - 0.3D;
        double xOffset = axis == EnumFacing.Axis.X ? direction.getXOffset() * 0.52D : horizontalOffset;
        double yOffset = rand.nextDouble() * 6.0D / 16.0D;
        double zOffset = axis == EnumFacing.Axis.Z ? direction.getZOffset() * 0.52D : horizontalOffset;
        worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + xOffset, y + yOffset, z + zOffset, 0.0D, 0.0D, 0.0D);
        worldIn.spawnParticle(EnumParticleTypes.FLAME, x + xOffset, y + yOffset, z + zOffset, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(LIT)) {
            meta |= 4;
        }
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(LIT, (meta & 4) != 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, LIT);
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }
}
