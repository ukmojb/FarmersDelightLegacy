package com.wdcftgg.farmersdelightlegacy.common.compat.futuremc;

import com.wdcftgg.farmersdelightlegacy.common.compat.Compostables;
import thedarkcolour.futuremc.block.villagepillage.ComposterBlock;

public final class FutureMcComposterCompat {
    private FutureMcComposterCompat() {
    }

    public static void registerAll() {
        Compostables.forEach((stack, chance) -> ComposterBlock.ItemsForComposter.INSTANCE.add(stack, chance.intValue()));
    }
}
