package com.wdcftgg.farmersdelightlegacy.common.registry;

import com.wdcftgg.farmersdelight.Tags;
import com.wdcftgg.farmersdelightlegacy.common.ModCreativeTab;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockBasket;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCabinet;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCookingPot;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCuttingBoard;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockFeast;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockSkillet;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockStove;
import com.wdcftgg.farmersdelightlegacy.common.item.ItemSkillet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ModBlocks {

    public static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    public static final List<Item> BLOCK_ITEMS = new ArrayList<>();

    public static final Block TOMATOES = registerBlockOnly("tomatoes", new ModCropBlock("tomato_seeds", "tomato"));
    public static final Block RICE = registerBlockOnly("rice", new ModCropBlock("rice", "rice_panicle"));

    public static final Block COOKING_POT = register("cooking_pot", new BlockCookingPot());
    public static final Block CUTTING_BOARD = register("cutting_board", new BlockCuttingBoard());
    public static final Block BASKET = register("basket", new BlockBasket());
    public static final Block ACACIA_CABINET = register("acacia_cabinet", new BlockCabinet());
    public static final Block BAMBOO_CABINET = register("bamboo_cabinet", new BlockCabinet());
    public static final Block BIRCH_CABINET = register("birch_cabinet", new BlockCabinet());
    public static final Block CHERRY_CABINET = register("cherry_cabinet", new BlockCabinet());
    public static final Block CRIMSON_CABINET = register("crimson_cabinet", new BlockCabinet());
    public static final Block DARK_OAK_CABINET = register("dark_oak_cabinet", new BlockCabinet());
    public static final Block JUNGLE_CABINET = register("jungle_cabinet", new BlockCabinet());
    public static final Block MANGROVE_CABINET = register("mangrove_cabinet", new BlockCabinet());
    public static final Block OAK_CABINET = register("oak_cabinet", new BlockCabinet());
    public static final Block SPRUCE_CABINET = register("spruce_cabinet", new BlockCabinet());
    public static final Block WARPED_CABINET = register("warped_cabinet", new BlockCabinet());
    public static final Block STOVE = register("stove", new BlockStove());
    public static final Block SKILLET = register("skillet", new BlockSkillet(), ItemSkillet::new);
    public static final Block ROAST_CHICKEN_BLOCK = register("roast_chicken_block", new BlockFeast(4, "roast_chicken", null));
    public static final Block HONEY_GLAZED_HAM_BLOCK = register("honey_glazed_ham_block", new BlockFeast(4, "honey_glazed_ham", null));
    public static final Block SHEPHERDS_PIE_BLOCK = register("shepherds_pie_block", new BlockFeast(4, "shepherds_pie", "minecraft:bowl"));
    public static final Block STUFFED_PUMPKIN_BLOCK = register("stuffed_pumpkin_block", new BlockFeast(4, "stuffed_pumpkin", null));
    public static final Block RICE_ROLL_MEDLEY_BLOCK = register("rice_roll_medley_block", new BlockFeast(8, "kelp_roll_slice", null));
    public static final Block ORGANIC_COMPOST = register("organic_compost", basicBlock(Material.GROUND, SoundType.GROUND, 0.7F));
    public static final Block RICH_SOIL_FARMLAND = register("rich_soil_farmland", basicBlock(Material.GROUND, SoundType.GROUND, 0.7F));
    public static final Block ROPE = register("rope", basicBlock(Material.CLOTH, SoundType.CLOTH, 0.3F));
    public static final Block CANVAS_RUG = register("canvas_rug", basicBlock(Material.CLOTH, SoundType.CLOTH, 0.2F));
    public static final Block TATAMI = register("tatami", basicBlock(Material.CLOTH, SoundType.CLOTH, 0.2F));
    public static final Block FULL_TATAMI_MAT = register("full_tatami_mat", basicBlock(Material.CLOTH, SoundType.CLOTH, 0.2F));

    private ModBlocks() {
    }

    public static void registerAll(RegistryEvent.Register<Block> event) {
        for (Block block : BLOCKS.values()) {
            event.getRegistry().register(block);
        }
    }

    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        for (Item itemBlock : BLOCK_ITEMS) {
            event.getRegistry().register(itemBlock);
        }
    }

    private static Block register(String path, Block block) {
        return register(path, block, ItemBlock::new);
    }

    private static Block register(String path, Block block, Function<Block, ItemBlock> itemFactory) {
        block.setRegistryName(new ResourceLocation(Tags.MOD_ID, path));
        block.setTranslationKey(Tags.MOD_ID + "." + path);
        block.setCreativeTab(ModCreativeTab.TAB);
        BLOCKS.put(path, block);

        ItemBlock itemBlock = itemFactory.apply(block);
        itemBlock.setRegistryName(block.getRegistryName());
        itemBlock.setTranslationKey(block.getTranslationKey());
        itemBlock.setCreativeTab(ModCreativeTab.TAB);
        BLOCK_ITEMS.add(itemBlock);
        return block;
    }

    private static Block registerBlockOnly(String path, Block block) {
        block.setRegistryName(new ResourceLocation(Tags.MOD_ID, path));
        block.setTranslationKey(Tags.MOD_ID + "." + path);
        BLOCKS.put(path, block);
        return block;
    }

    private static Block basicBlock(Material material, SoundType soundType, float hardness) {
        return new BasicBlock(material, soundType, hardness);
    }

    private static final class BasicBlock extends Block {
        private BasicBlock(Material material, SoundType soundType, float hardness) {
            super(material);
            this.setHardness(hardness);
            this.setResistance(hardness + 2.0F);
            this.setSoundType(soundType);
        }
    }

    private static final class ModCropBlock extends BlockCrops {
        private final ResourceLocation seedName;
        private final ResourceLocation cropName;

        private ModCropBlock(String seedPath, String cropPath) {
            this.seedName = new ResourceLocation(Tags.MOD_ID, seedPath);
            this.cropName = new ResourceLocation(Tags.MOD_ID, cropPath);
            this.setTickRandomly(true);
            this.setHardness(0.0F);
            this.setSoundType(SoundType.PLANT);
        }

        @Override
        protected Item getSeed() {
            return ForgeRegistries.ITEMS.getValue(seedName);
        }

        @Override
        protected Item getCrop() {
            return ForgeRegistries.ITEMS.getValue(cropName);
        }

        @Override
        protected boolean canSustainBush(net.minecraft.block.state.IBlockState state) {
            return state.getBlock() == Blocks.FARMLAND || state.getBlock() == this;
        }
    }
}

