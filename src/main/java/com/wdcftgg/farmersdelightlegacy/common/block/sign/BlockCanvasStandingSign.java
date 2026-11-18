package com.wdcftgg.farmersdelightlegacy.common.block.sign;

import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCanvasSign;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockCanvasStandingSign extends BlockStandingSign {

    @Override
    protected BlockStateContainer createBlockState() {
        return FluidloggingCompat.createBlockState(this, ROTATION);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos fromPos) {
        if (!world.getBlockState(pos.down()).getMaterial().isSolid()) {
            dropBlockAsItem(world, pos, state, 0);
            FluidloggingCompat.removeBlock(world, pos);
        }
    }

    private final ResourceLocation textureLocation;

    public BlockCanvasStandingSign(ResourceLocation textureLocation) {
        this.textureLocation = textureLocation;
        this.setHardness(1.0F);
        this.setSoundType(SoundType.WOOD);
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCanvasSign();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return CanvasSignInteractionHelper.interact(worldIn, pos, playerIn, hand, true);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, net.minecraft.block.state.IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this));
    }

    @Override
    public Item getItemDropped(net.minecraft.block.state.IBlockState state, java.util.Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }
}
