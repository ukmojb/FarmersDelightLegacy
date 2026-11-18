package com.wdcftgg.farmersdelightlegacy.client.model;

import com.wdcftgg.farmersdelightlegacy.common.item.SkilletFlip;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

public final class SkilletArmPose extends ModelRenderer {
    private final ModelRenderer original;
    private final float angle;

    private SkilletArmPose(ModelBiped model, ModelRenderer original, float angle) {
        super(model);
        model.boxList.remove(this);
        this.original = original;
        this.angle = angle;
        cubeList = original.cubeList;
        childModels = original.childModels;
        showModel = original.showModel;
        isHidden = original.isHidden;
        offsetX = original.offsetX;
        offsetY = original.offsetY;
        offsetZ = original.offsetZ;
        ModelBase.copyModelAngles(original, this);
    }

    public static void prepare(ModelBiped model, EntityLivingBase entity, float partialTicks) {
        restore(model);
        ItemStack stack = entity.getActiveItemStack();
        if (!entity.isHandActive() || !SkilletFlip.isFlipping(stack)) {
            return;
        }
        float progress = SkilletFlip.progress(stack, entity.world.getTotalWorldTime(), partialTicks);
        float angle = (-MathHelper.sin(progress * (float) Math.PI * 2) * 15 - 20) * (float) Math.PI / 180;
        EnumHandSide side = entity.getActiveHand() == EnumHand.MAIN_HAND
                ? entity.getPrimaryHand() : entity.getPrimaryHand().opposite();
        if (side == EnumHandSide.RIGHT) {
            model.bipedRightArm = new SkilletArmPose(model, model.bipedRightArm, angle);
            if (model instanceof ModelPlayer) {
                ModelPlayer player = (ModelPlayer) model;
                player.bipedRightArmwear = new SkilletArmPose(player, player.bipedRightArmwear, angle);
            }
        } else {
            model.bipedLeftArm = new SkilletArmPose(model, model.bipedLeftArm, angle);
            if (model instanceof ModelPlayer) {
                ModelPlayer player = (ModelPlayer) model;
                player.bipedLeftArmwear = new SkilletArmPose(player, player.bipedLeftArmwear, angle);
            }
        }
    }

    public static void restore(ModelBiped model) {
        model.bipedRightArm = unwrap(model.bipedRightArm);
        model.bipedLeftArm = unwrap(model.bipedLeftArm);
        if (model instanceof ModelPlayer) {
            ModelPlayer player = (ModelPlayer) model;
            player.bipedRightArmwear = unwrap(player.bipedRightArmwear);
            player.bipedLeftArmwear = unwrap(player.bipedLeftArmwear);
        }
    }

    private static ModelRenderer unwrap(ModelRenderer part) {
        return part instanceof SkilletArmPose ? ((SkilletArmPose) part).original : part;
    }

    private void applyPose() {
        ModelBase.copyModelAngles(this, original);
        original.rotateAngleX = angle;
        original.offsetX = offsetX;
        original.offsetY = offsetY;
        original.offsetZ = offsetZ;
        original.showModel = showModel;
        original.isHidden = isHidden;
    }

    @Override
    public void render(float scale) {
        applyPose();
        original.render(scale);
    }

    @Override
    public void renderWithRotation(float scale) {
        applyPose();
        original.renderWithRotation(scale);
    }

    @Override
    public void postRender(float scale) {
        applyPose();
        original.postRender(scale);
    }
}
