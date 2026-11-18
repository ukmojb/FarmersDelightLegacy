package com.wdcftgg.farmersdelightlegacy.common.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;

public class BlockHoneyGlazedHamFeast extends BlockFeast {

    private static final FoodShape[][] SHAPES = FoodShape.plated(new AxisAlignedBB[][]{
            {FoodShape.box(4, 1, 3, 12, 3, 11)},
            {FoodShape.box(4, 1, 6, 12, 9, 10)},
            {FoodShape.box(4, 2, 4, 12, 10, 10)},
            {FoodShape.box(4, 2, 2, 12, 10, 10)}
    });

    public BlockHoneyGlazedHamFeast(int maxServings, String servingItemPath, String requiredContainerPath, boolean hasLeftovers) {
        super(maxServings, servingItemPath, requiredContainerPath, hasLeftovers);
    }

    @Override
    protected FoodShape getShape(IBlockState state) {
        return SHAPES[state.getValue(SERVINGS)][state.getValue(FACING).getHorizontalIndex()];
    }

    @Override
    protected void appendPartialServingsDrops(NonNullList<ItemStack> drops) {
        drops.add(new ItemStack(Items.BONE));
    }
}
