package com.wdcftgg.farmersdelightlegacy.client.model;

import com.wdcftgg.farmersdelightlegacy.client.render.SkilletItemRenderer;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemSkillet;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.BakedModelWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collections;

public final class SkilletBakedModel extends BakedModelWrapper<IBakedModel> {
    private final ItemOverrideList overrides;

    public SkilletBakedModel(IBakedModel original, IBakedModel cooking, SkilletItemRenderer renderer) {
        super(original);
        IBakedModel cookingModel = new BakedModelWrapper<IBakedModel>(cooking) {
            @Override
            public boolean isBuiltInRenderer() {
                return true;
            }

            @Override
            public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) {
                if (type == ItemCameraTransforms.TransformType.GUI) {
                    return original.handlePerspective(type);
                }
                renderer.setTransform(type);
                return Pair.of(this, cooking.handlePerspective(type).getRight());
            }
        };
        overrides = new ItemOverrideList(Collections.emptyList()) {
            @Override
            public IBakedModel handleItemState(IBakedModel model, ItemStack stack,
                                              @Nullable World world, @Nullable EntityLivingBase entity) {
                return ItemSkillet.getCookingStackForRender(stack).isEmpty() ? original : cookingModel;
            }
        };
    }

    @Override
    public ItemOverrideList getOverrides() {
        return overrides;
    }
}
