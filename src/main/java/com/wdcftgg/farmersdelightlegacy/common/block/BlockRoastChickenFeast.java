package com.wdcftgg.farmersdelightlegacy.common.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;

public class BlockRoastChickenFeast extends BlockFeast {

    private static final FoodShape[][] SHAPES = FoodShape.plated(new AxisAlignedBB[][]{
            {FoodShape.box(4, 1, 10, 12, 8, 12)},
            {FoodShape.box(4, 1, 8, 12, 8, 12)},
            {FoodShape.box(4, 1, 6, 12, 8, 12)},
            {FoodShape.box(4, 1, 4, 12, 8, 12)}
    });

    public BlockRoastChickenFeast(int maxServings, String servingItemPath, String requiredContainerPath, boolean hasLeftovers) {
        super(maxServings, servingItemPath, requiredContainerPath, hasLeftovers);
    }

    @Override
    protected FoodShape getShape(IBlockState state) {
        return SHAPES[state.getValue(SERVINGS)][state.getValue(FACING).getHorizontalIndex()];
    }

    @Override
    protected void appendPartialServingsDrops(NonNullList<ItemStack> drops) {
        drops.add(new ItemStack(Items.DYE, 1, 15));
    }
}
