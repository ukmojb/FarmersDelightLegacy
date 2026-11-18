package com.wdcftgg.farmersdelightlegacy.common.block;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BlockRiceRollMedley extends BlockFeast {

    public static final PropertyInteger ROLL_SERVINGS = PropertyInteger.create("servings", 0, 8);

    private static final FoodShape[] SHAPES = new FoodShape(
            FoodShape.box(1, 1, 2, 15, 3, 14), FoodShape.box(3, 0, 2, 5, 2, 14),
            FoodShape.box(11, 0, 2, 13, 2, 14)).rotated();

    private static final ResourceLocation[] SERVING_ITEMS = new ResourceLocation[]{
            new ResourceLocation("farmersdelight", "cod_roll"),
            new ResourceLocation("farmersdelight", "cod_roll"),
            new ResourceLocation("farmersdelight", "salmon_roll"),
            new ResourceLocation("farmersdelight", "salmon_roll"),
            new ResourceLocation("farmersdelight", "salmon_roll"),
            new ResourceLocation("farmersdelight", "kelp_roll_slice"),
            new ResourceLocation("farmersdelight", "kelp_roll_slice"),
            new ResourceLocation("farmersdelight", "kelp_roll_slice")
    };

    public BlockRiceRollMedley() {
        super(8, "salmon_roll", null, true);
    }

    @Override
    public PropertyInteger getServingsProperty() {
        return ROLL_SERVINGS;
    }

    @Override
    protected ItemStack getServingStackForServings(int servings) {
        int servingIndex = servings - 1;
        if (servingIndex < 0 || servingIndex >= SERVING_ITEMS.length) {
            return ItemStack.EMPTY;
        }
        Item item = ForgeRegistries.ITEMS.getValue(SERVING_ITEMS[servingIndex]);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    protected ItemStack getRequiredServingItem(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                               EnumHand hand, ItemStack servingResult, int servings) {
        return null;
    }

    @Override
    protected FoodShape getShape(IBlockState state) {
        return SHAPES[state.getValue(FACING).getHorizontalIndex()];
    }

    @Override
    protected boolean hasServingParticles() {
        return false;
    }
}

