package com.wdcftgg.farmersdelightlegacy.client.jei;

import com.google.common.collect.ImmutableList;
import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.api.recipe.knife.HuntingDropOutput;
import com.wdcftgg.farmersdelightlegacy.common.recipe.manager.HuntingDropRecipeManager;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class HuntingDropJeiRecipe implements IRecipeWrapper {

    private static final double entityBoxWidth = 30.0D;
    private static final double entityBoxHeight = 20.0D;
    private static final int outputGridX = 98;
    private static final int outputGridY = 19;
    private static final int outputGridColumns = 3;
    private static final int outputGridMaxSlots = 9;
    private static final int outputStep = 19;
    private static final long displayTextIntervalTicks = 100L;
    private static final int displayTextX = 45;
    private static final int displayTextY = 71;
    private static final int clockX = 34;
    private static final int clockY = 70;
    private static final int outputClockX = outputGridX - 9;
    private static final int outputClockY = outputGridY - 9 - 7;
    private static final int clockFrameWidth = 10;
    private static final int clockFrameHeight = 10;
    private static final int clockFrameCount = 12;
    private static final int clockTextureHeight = clockFrameHeight * clockFrameCount;
    private static final ResourceLocation clockTexture = new ResourceLocation(FarmersDelightLegacy.MOD_ID,
            "textures/gui/jei/clock/clock.png");
    private static final List<String> clockTooltipKeys = ImmutableList.of(
            "farmersdelight.jei.clock.pause",
            "farmersdelight.jei.clock.resume",
            "farmersdelight.jei.clock.previous_page",
            "farmersdelight.jei.clock.next_page"
    );

    private final String recipeId;
    private final ResourceLocation entityId;
    private final List<ItemStack> toolOptions;
    private final List<HuntingDropOutput> outputs;
    private final List<ItemStack> outputStacks;
    private final boolean burningVariant;
    private final Consumer<EntityLivingBase> jeiEntityConfigurator;
    private final boolean runJeiEntityConfiguratorEveryTick;
    private final List<String> displayTexts;

    private World currentWorld;
    private JeiPreviewWorld previewWorld;
    private EntityLivingBase entityInstance;
    private boolean entityErrored;
    private double renderScale = -1.0D;
    private boolean deathAnimationStarted;
    private boolean entityConfigured;
    private long lastDrawTime;
    private long lastEntityUpdateTime;
    private boolean displayTextClockPaused;
    private long displayTextClockTickOffset;
    private long displayTextPausedAnimationTick;
    private boolean outputClockPaused;
    private long outputClockTickOffset;
    private long outputPausedAnimationTick;
    private IGuiItemStackGroup outputItemStacks;
    private IDrawable outputSlotBackground;
    private IDrawable outputChanceSlotBackground;

    private HuntingDropJeiRecipe(String recipeId, ResourceLocation entityId, List<ItemStack> toolOptions,
                                 List<HuntingDropOutput> outputs, boolean burningVariant,
                                 Consumer<EntityLivingBase> jeiEntityConfigurator,
                                 boolean runJeiEntityConfiguratorEveryTick,
                                 List<String> displayTexts) {
        this.recipeId = recipeId;
        this.entityId = entityId;
        this.toolOptions = ImmutableList.copyOf(toolOptions);
        this.outputs = ImmutableList.copyOf(outputs);
        this.outputStacks = buildOutputStacks(outputs);
        this.burningVariant = burningVariant;
        this.jeiEntityConfigurator = jeiEntityConfigurator;
        this.runJeiEntityConfiguratorEveryTick = runJeiEntityConfiguratorEveryTick;
        this.displayTexts = ImmutableList.copyOf(displayTexts);
    }

    public static HuntingDropJeiRecipe of(HuntingDropRecipeManager.HuntingDropRecipeView recipe) {
        return new HuntingDropJeiRecipe(recipe.getKey(), recipe.getEntityId(), recipe.getToolOptions(),
                recipe.getOutputs(), recipe.isBurningVariant(), recipe.getJeiEntityConfigurator(),
                recipe.shouldRunJeiEntityConfiguratorEveryTick(), recipe.getJeiDisplayTexts());
    }

    private static List<ItemStack> buildOutputStacks(List<HuntingDropOutput> outputs) {
        List<ItemStack> stacks = new ArrayList<>();
        for (HuntingDropOutput output : outputs) {
            stacks.add(output.getOutputStack());
        }
        return ImmutableList.copyOf(stacks);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setOutputs(VanillaTypes.ITEM, this.outputStacks);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        updateOutputSlots();
        resetEntityAfterRecipeSwitch(minecraft);
        this.lastDrawTime = Minecraft.getSystemTime();
        refreshEntityInstance(minecraft);
        drawEntity(minecraft);
        drawAnimatedTool(minecraft);
        drawClockOutput(minecraft);
        drawText(minecraft);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (isCursorInsideActiveClock(mouseX, mouseY)) {
            return getClockTooltip();
        }
        if (isCursorInsideBounds(19, 8, 33, 49, mouseX, mouseY)) {
            if (this.entityInstance != null) {
                ModContainer mod = Loader.instance().getIndexedModList().get(this.entityId.getNamespace());
                return ImmutableList.of(
                        this.entityInstance.getName(),
                        TextFormatting.BLUE + "" + TextFormatting.ITALIC + (mod == null ? "Unknown" : mod.getName())
                );
            }
            return Collections.singletonList(this.entityId == null ? "Unknown" : this.entityId.toString());
        }
        for (int outputIndex = 0; outputIndex < getOutputCount(); outputIndex++) {
            if (!isCursorInsideBounds(getOutputSlotX(outputIndex), getOutputSlotY(outputIndex), 18, 18, mouseX, mouseY)) {
                continue;
            }
            HuntingDropOutput output = getCurrentOutput(outputIndex);
            return output == null ? Collections.emptyList() : getOutputTooltip(output);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        if (isCursorInsideDisplayTextClock(mouseX, mouseY)) {
            return handleDisplayTextClockClick(minecraft, mouseButton);
        }
        if (isCursorInsideOutputClock(mouseX, mouseY)) {
            return handleOutputClockClick(minecraft, mouseButton);
        }
        return false;
    }

    private List<String> getClockTooltip() {
        List<String> tooltip = new ArrayList<>();
        for (String tooltipKey : clockTooltipKeys) {
            tooltip.add(I18n.format(tooltipKey));
        }
        return tooltip;
    }

    public int getOutputCount() {
        return Math.min(this.outputStacks.size(), outputGridMaxSlots);
    }

    public void bindOutputSlots(IGuiItemStackGroup itemStacks, IDrawable slotBackground, IDrawable chanceSlotBackground) {
        this.outputItemStacks = itemStacks;
        this.outputSlotBackground = slotBackground;
        this.outputChanceSlotBackground = chanceSlotBackground;
        updateOutputSlots();
    }

    public int getOutputSlotX(int outputIndex) {
        int size = getOutputCount();
        int columns = Math.min(size, outputGridColumns);
        int centerX = columns <= 1 ? outputStep : columns == 2 ? 10 : 1;
        return outputGridX + centerX + ((outputIndex % outputGridColumns) * outputStep);
    }

    public int getOutputSlotY(int outputIndex) {
        int size = getOutputCount();
        int rows = (size + outputGridColumns - 1) / outputGridColumns;
        int centerY = rows <= 1 ? 10 : rows == 2 ? 1 : -8;
        return outputGridY + centerY + ((outputIndex / outputGridColumns) * outputStep);
    }

    public float getOutputChance(int outputIndex) {
        HuntingDropOutput output = getCurrentOutput(outputIndex);
        if (output == null) {
            return 1.0F;
        }
        return output.getChance();
    }

    public HuntingDropOutput getCurrentOutput(int outputSlotIndex) {
        int outputIndex = getCurrentOutputPage() * outputGridMaxSlots + outputSlotIndex;
        if (outputIndex < 0 || outputIndex >= this.outputs.size()) {
            return null;
        }
        return this.outputs.get(outputIndex);
    }

    public int getOutputPageCount() {
        return Math.max(1, (this.outputs.size() + outputGridMaxSlots - 1) / outputGridMaxSlots);
    }

    private void updateOutputSlots() {
        if (this.outputItemStacks == null) {
            return;
        }
        for (int outputSlotIndex = 0; outputSlotIndex < getOutputCount(); outputSlotIndex++) {
            HuntingDropOutput output = getCurrentOutput(outputSlotIndex);
            if (output == null) {
                this.outputItemStacks.set(outputSlotIndex, ItemStack.EMPTY);
                this.outputItemStacks.setBackground(outputSlotIndex, null);
                continue;
            }
            this.outputItemStacks.set(outputSlotIndex, output.getOutputStack());
            this.outputItemStacks.setBackground(outputSlotIndex,
                    output.getChance() < 1.0F ? this.outputChanceSlotBackground : this.outputSlotBackground);
        }
    }

    private int getCurrentOutputPage() {
        int pageCount = getOutputPageCount();
        if (pageCount <= 1) {
            return 0;
        }
        return getPageIndexFromAnimationTicks(getOutputAnimationTicks(Minecraft.getMinecraft()), pageCount);
    }

    public String getRecipeId() {
        return this.recipeId;
    }

    private List<String> getOutputTooltip(HuntingDropOutput output) {
        ItemStack stack = output.getOutputStack();
        if (stack.isEmpty()) {
            return Collections.emptyList();
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        ITooltipFlag.TooltipFlags tooltipFlag = minecraft != null && minecraft.gameSettings != null
                && minecraft.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
        List<String> tooltip = new ArrayList<>(stack.getTooltip(minecraft == null ? null : minecraft.player, tooltipFlag));
        float chance = output.getChance();
        if (chance < 1.0F) {
            String chancePercent = chance < 0.01F ? "<1" : Integer.toString((int) (chance * 100.0F));
            tooltip.add(Math.min(1, tooltip.size()), TextFormatting.GOLD + I18n.format("farmersdelight.jei.chance", chancePercent));
        }
        JeiTooltipUtil.addRecipeIdTooltip(tooltip, this.recipeId);
        return tooltip;
    }

    private void refreshEntityInstance(Minecraft minecraft) {
        if (minecraft.world != null && minecraft.world != this.currentWorld) {
            this.currentWorld = minecraft.world;
            this.previewWorld = null;
            this.entityInstance = null;
            this.entityErrored = false;
            this.renderScale = -1.0D;
            this.deathAnimationStarted = false;
            this.entityConfigured = false;
        }

        if (this.entityInstance != null || this.entityErrored || minecraft.world == null || this.entityId == null) {
            return;
        }

        try {
            this.previewWorld = JeiPreviewWorld.create(minecraft);
            this.previewWorld.resetTo(minecraft);
            Entity entity = EntityList.createEntityByIDFromName(this.entityId, this.previewWorld);
            if (entity instanceof EntityLivingBase) {
                this.entityInstance = (EntityLivingBase) entity;
                forceAdultDisplay(this.entityInstance);
                this.entityInstance.noClip = true;
                this.entityInstance.setPosition(0.0D, 64.0D, 0.0D);
                this.previewWorld.addPreviewEntity(this.entityInstance);
                this.deathAnimationStarted = false;
                this.entityConfigured = false;
            } else {
                this.entityErrored = true;
            }
        } catch (Exception exception) {
            this.entityErrored = true;
            FarmersDelightLegacy.LOGGER.error("创建 JEI 狩猎实体预览失败，配方：{}，实体：{}", this.recipeId, this.entityId, exception);
        }
    }

    private void resetEntityAfterRecipeSwitch(Minecraft minecraft) {
        long currentTime = Minecraft.getSystemTime();
        if (this.entityInstance != null && this.lastDrawTime > 0L && currentTime - this.lastDrawTime > 250L) {
            this.previewWorld = null;
            this.entityInstance = null;
            this.entityErrored = false;
            this.renderScale = -1.0D;
            this.deathAnimationStarted = false;
            this.entityConfigured = false;
            this.lastEntityUpdateTime = 0L;
        }
    }

    private void drawEntity(Minecraft minecraft) {
        if (this.entityInstance == null) {
            return;
        }

        configureEntityOnce();
        updateGuiDisplayEntity(minecraft);
        updateDeathAnimationState();
        keepNonAdvancedBurningEntityOnFire();

        if (this.renderScale < 0.0D) {
            double width = Math.max(0.1D, this.entityInstance.width);
            double height = Math.max(0.1D, this.entityInstance.height);
            this.renderScale = Math.min(entityBoxWidth / width, entityBoxHeight / height);
        }

        float previousBrightnessX = OpenGlHelper.lastBrightnessX;
        float previousBrightnessY = OpenGlHelper.lastBrightnessY;
        GlStateManager.enableDepth();
        GlStateManager.pushMatrix();
        try {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            GlStateManager.translate(35, 52, 0);
            GuiInventory.drawEntityOnScreen(0, 0, (int) Math.round(this.renderScale), -100, 0, this.entityInstance);
            drawPreviewParticles(minecraft);
        } finally {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, previousBrightnessX, previousBrightnessY);
            GlStateManager.popMatrix();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
    }

    private void drawPreviewParticles(Minecraft minecraft) {
        if (this.previewWorld == null) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.renderScale, this.renderScale, this.renderScale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        this.previewWorld.renderParticles(this.entityInstance, minecraft.getRenderPartialTicks());
        GlStateManager.popMatrix();
    }

    private void configureEntityOnce() {
        if (this.entityConfigured) {
            return;
        }
        if (this.jeiEntityConfigurator != null) {
            this.jeiEntityConfigurator.accept(this.entityInstance);
        } else if (this.burningVariant) {
            this.entityInstance.setFire(1000000);
        } else {
            this.entityInstance.extinguish();
        }
        this.entityConfigured = true;
        updateDeathAnimationState();
    }

    private void updateGuiDisplayEntity(Minecraft minecraft) {
        if (this.previewWorld == null || this.entityInstance == null) {
            return;
        }
        long currentTime = Minecraft.getSystemTime();
        if (this.lastEntityUpdateTime > 0L && currentTime - this.lastEntityUpdateTime < 50L) {
            return;
        }
        this.lastEntityUpdateTime = currentTime;
        configureEntityEveryTick();
        this.previewWorld.updatePreviewEntity(this.entityInstance);
        keepNonAdvancedBurningEntityOnFire();
    }

    private void configureEntityEveryTick() {
        if (!this.runJeiEntityConfiguratorEveryTick || this.jeiEntityConfigurator == null) {
            return;
        }
        this.jeiEntityConfigurator.accept(this.entityInstance);
        updateDeathAnimationState();
    }

    private void keepNonAdvancedBurningEntityOnFire() {
        if (this.jeiEntityConfigurator != null || !this.burningVariant || this.entityInstance == null) {
            return;
        }
        this.entityInstance.setFire(1000000);
    }

    private void updateDeathAnimationState() {
        if (!this.entityInstance.isDead || this.deathAnimationStarted) {
            return;
        }
        this.entityInstance.setHealth(0.0F);
        this.entityInstance.deathTime = 1;
        this.entityInstance.hurtTime = Math.max(this.entityInstance.hurtTime, 10);
        this.entityInstance.maxHurtTime = Math.max(this.entityInstance.maxHurtTime, 10);
        this.deathAnimationStarted = true;
    }

    private void drawAnimatedTool(Minecraft minecraft) {
        ItemStack toolStack = getAnimatedTool(minecraft);
        if (toolStack.isEmpty()) {
            return;
        }

        long time = minecraft.world == null ? Minecraft.getSystemTime() : minecraft.world.getTotalWorldTime();
        float partialTicks = minecraft.world == null ? 0.0F : minecraft.getRenderPartialTicks();
        float theta = (float) (Math.PI / 2 * ((time + partialTicks) / 10.0F + 5.0F));
        double movementX = Math.sin(theta);
        double movementY = Math.cos(theta);
        double toolRenderX = 46.0D + movementX * 10.0D;
        double toolRenderY = 42.0D + movementY * 6.0D;

        GlStateManager.pushMatrix();
        GlStateManager.translate(toolRenderX, toolRenderY, 0);
        GlStateManager.rotate((float) (120.0D * movementX), 0, 0, 1);
        GlStateManager.disableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        minecraft.getRenderItem().renderItemAndEffectIntoGUI(toolStack, 0, -16);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void drawText(Minecraft minecraft) {
        if (this.displayTexts.size() > 1) {
            drawClock(minecraft);
        }

        if (this.displayTexts.isEmpty()) {
            return;
        }
        minecraft.fontRenderer.drawString(getCurrentDisplayText(minecraft), displayTextX,
                displayTextY, 0xAA3333);
    }

    private String getCurrentDisplayText(Minecraft minecraft) {
        String displayText = this.displayTexts.get(getPageIndexFromAnimationTicks(getDisplayTextAnimationTicks(minecraft), this.displayTexts.size()));
        return I18n.hasKey(displayText) ? I18n.format(displayText) : displayText;
    }

    private void drawClock(Minecraft minecraft) {
        drawClock(minecraft, clockX, clockY, getDisplayTextAnimationTicks(minecraft));
    }

    private void drawClockOutput(Minecraft minecraft) {
        if (getOutputPageCount() <= 1) {
            return;
        }
        drawClock(minecraft, outputClockX, outputClockY, getOutputAnimationTicks(minecraft));
    }

    private void drawClock(Minecraft minecraft, int clockPositionX, int clockPositionY, long animationTicks) {
        long frameTicks = getPositiveRemainder(animationTicks, displayTextIntervalTicks);
        int frameIndex = (int) (frameTicks * clockFrameCount / displayTextIntervalTicks);
        minecraft.getTextureManager().bindTexture(clockTexture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(clockPositionX, clockPositionY, 0.0F, frameIndex * clockFrameHeight,
                clockFrameWidth, clockFrameHeight, clockFrameWidth, clockTextureHeight);
    }

    private boolean handleDisplayTextClockClick(Minecraft minecraft, int mouseButton) {
        return handleClockClick(minecraft, mouseButton, this.displayTexts.size(), true);
    }

    private boolean handleOutputClockClick(Minecraft minecraft, int mouseButton) {
        boolean handled = handleClockClick(minecraft, mouseButton, getOutputPageCount(), false);
        updateOutputSlots();
        return handled;
    }

    private boolean handleClockClick(Minecraft minecraft, int mouseButton, int pageCount, boolean displayTextClock) {
        if (mouseButton != 0 && mouseButton != 1) {
            return false;
        }
        if (GuiScreen.isCtrlKeyDown()) {
            moveClockPage(minecraft, pageCount, displayTextClock, mouseButton == 0 ? -1 : 1);
            return true;
        }
        if (mouseButton == 0) {
            pauseClock(minecraft, displayTextClock);
            return true;
        }
        resumeClock(minecraft, displayTextClock);
        return true;
    }

    private void pauseClock(Minecraft minecraft, boolean displayTextClock) {
        if (displayTextClock) {
            this.displayTextPausedAnimationTick = getDisplayTextAnimationTicks(minecraft);
            this.displayTextClockPaused = true;
            return;
        }
        this.outputPausedAnimationTick = getOutputAnimationTicks(minecraft);
        this.outputClockPaused = true;
    }

    private void resumeClock(Minecraft minecraft, boolean displayTextClock) {
        long animationTicks = getAnimationTicks(minecraft);
        if (displayTextClock) {
            this.displayTextClockTickOffset = this.displayTextPausedAnimationTick - animationTicks;
            this.displayTextClockPaused = false;
            return;
        }
        this.outputClockTickOffset = this.outputPausedAnimationTick - animationTicks;
        this.outputClockPaused = false;
    }

    private void moveClockPage(Minecraft minecraft, int pageCount, boolean displayTextClock, int pageOffset) {
        long animationTicks = displayTextClock ? getDisplayTextAnimationTicks(minecraft) : getOutputAnimationTicks(minecraft);
        long frameTicks = getPositiveRemainder(animationTicks, displayTextIntervalTicks);
        int currentPageIndex = getPageIndexFromAnimationTicks(animationTicks, pageCount);
        int pageIndex = wrapPageIndex(currentPageIndex + pageOffset, pageCount);
        long pausedAnimationTick = pageIndex * displayTextIntervalTicks + frameTicks;
        if (displayTextClock) {
            this.displayTextPausedAnimationTick = pausedAnimationTick;
            this.displayTextClockPaused = true;
            return;
        }
        this.outputPausedAnimationTick = pausedAnimationTick;
        this.outputClockPaused = true;
    }

    private long getDisplayTextAnimationTicks(Minecraft minecraft) {
        return this.displayTextClockPaused ? this.displayTextPausedAnimationTick : getAnimationTicks(minecraft) + this.displayTextClockTickOffset;
    }

    private long getOutputAnimationTicks(Minecraft minecraft) {
        return this.outputClockPaused ? this.outputPausedAnimationTick : getAnimationTicks(minecraft) + this.outputClockTickOffset;
    }

    private int getPageIndexFromAnimationTicks(long animationTicks, int pageCount) {
        if (pageCount <= 1) {
            return 0;
        }
        return (int) getPositiveRemainder(Math.floorDiv(animationTicks, displayTextIntervalTicks), pageCount);
    }

    private int wrapPageIndex(int pageIndex, int pageCount) {
        if (pageCount <= 1) {
            return 0;
        }
        return (int) getPositiveRemainder(pageIndex, pageCount);
    }

    private static long getPositiveRemainder(long value, long divisor) {
        long remainder = value % divisor;
        return remainder < 0L ? remainder + divisor : remainder;
    }

    private boolean isCursorInsideActiveClock(int mouseX, int mouseY) {
        return isCursorInsideDisplayTextClock(mouseX, mouseY) || isCursorInsideOutputClock(mouseX, mouseY);
    }

    private boolean isCursorInsideDisplayTextClock(int mouseX, int mouseY) {
        return this.displayTexts.size() > 1
                && isCursorInsideBounds(clockX, clockY, clockFrameWidth, clockFrameHeight, mouseX, mouseY);
    }

    private boolean isCursorInsideOutputClock(int mouseX, int mouseY) {
        return getOutputPageCount() > 1
                && isCursorInsideBounds(outputClockX, outputClockY, clockFrameWidth, clockFrameHeight, mouseX, mouseY);
    }

    private static long getAnimationTicks(Minecraft minecraft) {
        return minecraft == null || minecraft.world == null ? Minecraft.getSystemTime() / 50L : minecraft.world.getTotalWorldTime();
    }

    private ItemStack getAnimatedTool(Minecraft minecraft) {
        if (this.toolOptions.isEmpty()) {
            return ItemStack.EMPTY;
        }
        long time = minecraft.world == null ? Minecraft.getSystemTime() : minecraft.world.getTotalWorldTime();
        return this.toolOptions.get((int) ((time / 20L) % this.toolOptions.size()));
    }

    private static void forceAdultDisplay(EntityLivingBase entity) {
        if (entity instanceof EntityAgeable) {
            ((EntityAgeable) entity).setGrowingAge(0);
        }
        if (entity instanceof EntityZombie) {
            ((EntityZombie) entity).setChild(false);
        }
    }

    private static boolean isCursorInsideBounds(int boundsX, int boundsY, int width, int height, int mouseX, int mouseY) {
        return mouseX >= boundsX && mouseX < boundsX + width && mouseY >= boundsY && mouseY < boundsY + height;
    }
}
