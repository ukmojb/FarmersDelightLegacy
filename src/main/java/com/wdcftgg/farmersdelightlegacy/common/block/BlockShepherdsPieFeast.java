package com.wdcftgg.farmersdelightlegacy.common.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;

public class BlockShepherdsPieFeast extends BlockFeast {

    private static final FoodShape[][] SHAPES = FoodShape.plated(new AxisAlignedBB[][]{
            {FoodShape.box(2, 1, 8, 8, 8, 14)},
            {FoodShape.box(2, 1, 8, 14, 8, 14)},
            {FoodShape.box(8, 1, 2, 14, 8, 8), FoodShape.box(2, 1, 8, 14, 8, 14)},
            {FoodShape.box(2, 1, 2, 14, 8, 14)}
    });

    public BlockShepherdsPieFeast(int maxServings, String servingItemPath, String requiredContainerPath, boolean hasLeftovers) {
        super(maxServings, servingItemPath, requiredContainerPath, hasLeftovers);
    }

    @Override
    protected FoodShape getShape(IBlockState state) {
        return SHAPES[state.getValue(SERVINGS)][state.getValue(FACING).getHorizontalIndex()];
    }
}
