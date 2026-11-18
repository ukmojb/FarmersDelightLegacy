package com.wdcftgg.farmersdelightlegacy.client;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.client.model.SkilletBakedModel;
import com.wdcftgg.farmersdelightlegacy.client.render.SkilletItemRenderer;
import com.wdcftgg.farmersdelightlegacy.client.particle.CookingPotParticleBridge;
import com.wdcftgg.farmersdelightlegacy.client.render.TileEntityCanvasSignRenderer;
import com.wdcftgg.farmersdelightlegacy.client.render.TileEntityCuttingBoardRenderer;
import com.wdcftgg.farmersdelightlegacy.client.render.TileEntitySkilletRenderer;
import com.wdcftgg.farmersdelightlegacy.client.render.TileEntityStoveRenderer;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasWallHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.entity.EntityRottenTomato;
import com.wdcftgg.farmersdelightlegacy.common.entity.EntityFireproofItem;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModItems;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCanvasSign;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityCuttingBoard;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntitySkillet;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityStove;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = FarmersDelightLegacy.MOD_ID, value = Side.CLIENT)
public final class ClientRegistryHandler {

    private static boolean tileRenderersBound;
    private static boolean particleFactoriesBound;
    private static boolean entityRenderersBound;

    private ClientRegistryHandler() {
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        if (!tileRenderersBound) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCuttingBoard.class, new TileEntityCuttingBoardRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySkillet.class, new TileEntitySkilletRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStove.class, new TileEntityStoveRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCanvasSign.class, new TileEntityCanvasSignRenderer());
            tileRenderersBound = true;
        }

        if (!entityRenderersBound) {
            RenderingRegistry.registerEntityRenderingHandler(EntityRottenTomato.class,
                    renderManager -> new RenderSnowball<>(renderManager, ModItems.ITEMS.get("rotten_tomato"),
                            net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));
            RenderingRegistry.registerEntityRenderingHandler(EntityFireproofItem.class,
                    renderManager -> new RenderEntityItem(renderManager, net.minecraft.client.Minecraft.getMinecraft().getRenderItem()));
            entityRenderersBound = true;
        }

        for (Block block : ModBlocks.BLOCKS.values()) {
            if (block instanceof BlockStandingSign) {
                ModelLoader.setCustomStateMapper(block, new StateMap.Builder().ignore(BlockStandingSign.ROTATION).build());
            } else if (block instanceof BlockWallSign) {
                ModelLoader.setCustomStateMapper(block, new StateMap.Builder().ignore(BlockWallSign.FACING).build());
            } else if (block instanceof BlockCanvasHangingSign) {
                ModelLoader.setCustomStateMapper(block, new StateMap.Builder().ignore(BlockCanvasHangingSign.ROTATION).build());
            } else if (block instanceof BlockCanvasWallHangingSign) {
                ModelLoader.setCustomStateMapper(block, new StateMap.Builder().ignore(BlockCanvasWallHangingSign.FACING).build());
            }
        }

        ModelBakery.registerItemVariants(Item.getItemFromBlock(ModBlocks.SKILLET),
                new ResourceLocation(FarmersDelightLegacy.MOD_ID, "skillet_cooking"));

        for (Item item : ModItems.ITEMS.values()) {
            if (item.getRegistryName() == null) {
                continue;
            }
            ResourceLocation location = item.getRegistryName();
            ModelLoader.setCustomModelResourceLocation(item, 0, new net.minecraft.client.renderer.block.model.ModelResourceLocation(location, "inventory"));
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        ModelResourceLocation skilletInventory = new ModelResourceLocation(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "skillet"), "inventory");
        IBakedModel skilletBaseModel = event.getModelRegistry().getObject(skilletInventory);
        ModelResourceLocation skilletCookingInventory = new ModelResourceLocation(new ResourceLocation(FarmersDelightLegacy.MOD_ID, "skillet_cooking"), "inventory");
        IBakedModel skilletCookingModel = event.getModelRegistry().getObject(skilletCookingInventory);

        if (skilletBaseModel != null && skilletCookingModel != null) {
            SkilletItemRenderer renderer = new SkilletItemRenderer(skilletCookingModel);
            Item.getItemFromBlock(ModBlocks.SKILLET).setTileEntityItemStackRenderer(renderer);
            event.getModelRegistry().putObject(skilletInventory, new SkilletBakedModel(skilletBaseModel, skilletCookingModel, renderer));
        }
    }

    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        particleFactoriesBound = false;
        CookingPotParticleBridge.registerTextures(event.getMap());
    }

    @SubscribeEvent
    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
        particleFactoriesBound = CookingPotParticleBridge.registerFactories();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || particleFactoriesBound) {
            return;
        }

        particleFactoriesBound = CookingPotParticleBridge.registerFactories();
    }
}

