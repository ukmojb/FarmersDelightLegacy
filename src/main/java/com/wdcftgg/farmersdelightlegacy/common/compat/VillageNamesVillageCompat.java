package com.wdcftgg.farmersdelightlegacy.common.compat;

import astrotibs.villagenames.config.village.VillageGeneratorConfigHandler;
import astrotibs.villagenames.utility.FunctionsVN.MaterialType;
import astrotibs.villagenames.utility.FunctionsVN.VillageType;
import astrotibs.villagenames.village.StructureVillageVN;
import com.google.common.base.Optional;
import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockOrganicCompost;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWoodSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemAxe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight;
import net.minecraft.world.gen.structure.StructureVillagePieces.Village;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public final class VillageNamesVillageCompat {

    private static final String STRUCTURE_ROOT = "/data/farmersdelightlegacy/structures/village/houses/";
    private static final String OBSOLETE_GENERIC_COMPONENT_CLASS = "com.wdcftgg.farmersdelightlegacy.common.compat.VillageNamesVillageCompat$GenericCompostPiece";
    private static final TemplateDefinition PLAINS = new TemplateDefinition(
            "plains_compost_pile.nbt",
            VillageType.PLAINS,
            MaterialType.OAK,
            "plains",
            5,
            0,
            Blocks.DIRT.getDefaultState(),
            PlainsCompostPiece.class
    );
    private static final TemplateDefinition DESERT = new TemplateDefinition(
            "desert_compost_pile.nbt",
            VillageType.DESERT,
            MaterialType.SAND,
            "desert",
            3,
            1,
            Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH),
            DesertCompostPiece.class
    );
    private static final TemplateDefinition SAVANNA = new TemplateDefinition(
            "savanna_compost_pile.nbt",
            VillageType.SAVANNA,
            MaterialType.ACACIA,
            "savanna",
            4,
            2,
            Blocks.DIRT.getDefaultState(),
            SavannaCompostPiece.class
    );
    private static final TemplateDefinition SNOWY = new TemplateDefinition(
            "snowy_compost_pile.nbt",
            VillageType.SNOWY,
            MaterialType.SNOW,
            "snowy",
            3,
            3,
            Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL),
            SnowyCompostPiece.class
    );
    private static final TemplateDefinition TAIGA = new TemplateDefinition(
            "taiga_compost_pile.nbt",
            VillageType.TAIGA,
            MaterialType.SPRUCE,
            "taiga",
            4,
            3,
            Blocks.COBBLESTONE.getDefaultState(),
            TaigaCompostPiece.class
    );
    private static final List<TemplateDefinition> DEFINITIONS = Arrays.asList(PLAINS, DESERT, SAVANNA, SNOWY, TAIGA);
    private static final Set<String> REPORTED_MISSING_BLOCKS = new LinkedHashSet<>();

    private static boolean registered = false;

    private VillageNamesVillageCompat() {
    }

    public static void registerAll() {
        if (registered || !Configuration.generateVillageCompostHeaps || !Loader.isModLoaded("villagenames")) {
            return;
        }

        registerStructureComponents();
        registerVillageHandlers();
        registerVillageTypeMappings();
        registered = true;
        FarmersDelightLegacy.LOGGER.info("已注册 Village Names 村庄堆肥场兼容结构。");
    }

    private static void registerStructureComponents() {
        MapGenStructureIO.registerStructureComponent(EmptyCompostPiece.class, "FDVNEmptyCompost");
        MapGenStructureIO.registerStructureComponent(PlainsCompostPiece.class, "FDVNPlainsCompost");
        MapGenStructureIO.registerStructureComponent(DesertCompostPiece.class, "FDVNDesertCompost");
        MapGenStructureIO.registerStructureComponent(SavannaCompostPiece.class, "FDVNSavannaCompost");
        MapGenStructureIO.registerStructureComponent(SnowyCompostPiece.class, "FDVNSnowyCompost");
        MapGenStructureIO.registerStructureComponent(TaigaCompostPiece.class, "FDVNTaigaCompost");
    }

    private static void registerVillageHandlers() {
        VillagerRegistry registry = VillagerRegistry.instance();
        registry.registerVillageCreationHandler(new CompostHandler(PLAINS, new PieceFactory() {
            @Override
            public Village create(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
                return new PlainsCompostPiece(startPiece, componentType, boundingBox, facing);
            }
        }));
        registry.registerVillageCreationHandler(new CompostHandler(DESERT, new PieceFactory() {
            @Override
            public Village create(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
                return new DesertCompostPiece(startPiece, componentType, boundingBox, facing);
            }
        }));
        registry.registerVillageCreationHandler(new CompostHandler(SAVANNA, new PieceFactory() {
            @Override
            public Village create(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
                return new SavannaCompostPiece(startPiece, componentType, boundingBox, facing);
            }
        }));
        registry.registerVillageCreationHandler(new CompostHandler(SNOWY, new PieceFactory() {
            @Override
            public Village create(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
                return new SnowyCompostPiece(startPiece, componentType, boundingBox, facing);
            }
        }));
        registry.registerVillageCreationHandler(new CompostHandler(TAIGA, new PieceFactory() {
            @Override
            public Village create(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
                return new TaigaCompostPiece(startPiece, componentType, boundingBox, facing);
            }
        }));
    }

    private static void registerVillageTypeMappings() {
        Set<String> mappings = new LinkedHashSet<>();
        String[] existingMappings = VillageGeneratorConfigHandler.componentVillageTypes;
        if (existingMappings == null || existingMappings.length == 0) {
            existingMappings = VillageGeneratorConfigHandler.MODERN_VANILLA_COMPONENT_VILLAGE_TYPE_DEFAULTS;
        }

        if (existingMappings != null) {
            for (String mapping : existingMappings) {
                if (!isObsoleteVillageTypeMapping(mapping)) {
                    mappings.add(mapping);
                }
            }
        }

        for (TemplateDefinition definition : DEFINITIONS) {
            mappings.add(definition.pieceClass.getName() + "|" + definition.villageTypeKey);
        }

        VillageGeneratorConfigHandler.componentVillageTypes = mappings.toArray(new String[0]);
    }

    private static boolean isObsoleteVillageTypeMapping(String mapping) {
        return mapping != null && mapping.startsWith(OBSOLETE_GENERIC_COMPONENT_CLASS + "|");
    }

    public static void replaceVillageFarmCrops(World world, Random random, int chunkX, int chunkZ) {
        if (!Configuration.generateVillageFarmFDCrops || !Loader.isModLoaded("villagenames")) {
            return;
        }

        Set<BlockPos> visitedPositions = new HashSet<>();
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                replaceVillageFarmColumn(world, startX + localX, startZ + localZ, visitedPositions);
            }
        }
    }

    private static void replaceVillageFarmColumn(World world, int x, int z, Set<BlockPos> visitedPositions) {
        int topY = Math.min(255, world.getHeight(x, z));
        for (int y = 48; y <= topY; y++) {
            BlockPos cropPos = new BlockPos(x, y, z);
            if (visitedPositions.contains(cropPos)) {
                continue;
            }

            IBlockState cropState = world.getBlockState(cropPos);
            if (!isVillageFarmTarget(world, cropPos, cropState) || !hasVillageNamesFarmFootprint(world, cropPos)) {
                continue;
            }

            List<BlockPos> cropBlockPositions = collectVillageFarmCropBlock(world, cropPos, visitedPositions);
            if (cropBlockPositions.size() < 4) {
                continue;
            }

            int replacementChoice = getVillageFarmReplacementChoice(world, cropBlockPositions);
            replaceVillageFarmCropBlock(world, cropBlockPositions, replacementChoice);
        }
    }

    private static boolean isVillageFarmTarget(World world, BlockPos cropPos, IBlockState cropState) {
        return world.getBlockState(cropPos.down()).getBlock() == Blocks.FARMLAND && isVillageFarmCrop(cropState);
    }

    private static List<BlockPos> collectVillageFarmCropBlock(World world, BlockPos origin, Set<BlockPos> visitedPositions) {
        List<BlockPos> cropBlockPositions = new ArrayList<>();
        Queue<BlockPos> pendingPositions = new ArrayDeque<>();
        visitedPositions.add(origin);
        pendingPositions.add(origin);

        while (!pendingPositions.isEmpty() && cropBlockPositions.size() < 128) {
            BlockPos currentPos = pendingPositions.remove();
            IBlockState currentState = world.getBlockState(currentPos);
            if (!isVillageFarmTarget(world, currentPos, currentState)) {
                continue;
            }

            cropBlockPositions.add(currentPos);
            for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
                BlockPos nextPos = currentPos.offset(facing);
                if (visitedPositions.add(nextPos)) {
                    pendingPositions.add(nextPos);
                }
            }
        }

        return cropBlockPositions;
    }

    private static void replaceVillageFarmCropBlock(World world, List<BlockPos> cropBlockPositions, int replacementChoice) {
        for (BlockPos cropPos : cropBlockPositions) {
            IBlockState cropState = world.getBlockState(cropPos);
            IBlockState replacementState = getVillageFarmReplacement(replacementChoice, cropState);
            if (replacementState != null && canVillageReplacementStay(world, cropPos, replacementState)) {
                world.setBlockState(cropPos, replacementState, 2);
            }
        }
    }

    private static boolean isVillageFarmCrop(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.WHEAT
                || block == Blocks.CARROTS
                || block == Blocks.POTATOES
                || block == Blocks.BEETROOTS
                || block == ModBlocks.CABBAGES
                || block == ModBlocks.ONIONS
                || block == ModBlocks.BUDDING_TOMATOES;
    }

    private static boolean hasVillageNamesFarmFootprint(World world, BlockPos cropPos) {
        int farmCropCount = 0;
        int villageBlockCount = 0;
        for (int offsetX = -3; offsetX <= 3; offsetX++) {
            for (int offsetZ = -3; offsetZ <= 3; offsetZ++) {
                BlockPos nearbyPos = cropPos.add(offsetX, 0, offsetZ);
                IBlockState nearbyCrop = world.getBlockState(nearbyPos);
                IBlockState nearbySoil = world.getBlockState(nearbyPos.down());
                if (nearbySoil.getBlock() == Blocks.FARMLAND && isVillageFarmCrop(nearbyCrop)) {
                    farmCropCount++;
                }

                IBlockState surfaceState = world.getBlockState(nearbyPos.down());
                if (isVillageNamesSurfaceBlock(surfaceState) || isVillageNamesSurfaceBlock(world.getBlockState(nearbyPos))) {
                    villageBlockCount++;
                }
            }
        }
        return farmCropCount >= 6 && villageBlockCount >= 2;
    }

    private static boolean isVillageNamesSurfaceBlock(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRAVEL
                || block == Blocks.COBBLESTONE
                || block == Blocks.PLANKS
                || block == Blocks.LOG
                || block == Blocks.LOG2
                || block == Blocks.WATER
                || block == Blocks.FLOWING_WATER
                || block instanceof BlockFarmland
                || block instanceof BlockFence
                || block instanceof BlockFenceGate;
    }

    private static int getVillageFarmReplacementChoice(World world, List<BlockPos> cropBlockPositions) {
        BlockPos anchorPos = cropBlockPositions.get(0);
        for (BlockPos cropPos : cropBlockPositions) {
            if (cropPos.getX() < anchorPos.getX()
                    || cropPos.getX() == anchorPos.getX() && cropPos.getZ() < anchorPos.getZ()
                    || cropPos.getX() == anchorPos.getX() && cropPos.getZ() == anchorPos.getZ() && cropPos.getY() < anchorPos.getY()) {
                anchorPos = cropPos;
            }
        }

        long blockSeed = world.getSeed();
        blockSeed ^= anchorPos.getX() * 341873128712L;
        blockSeed ^= anchorPos.getZ() * 132897987541L;
        blockSeed ^= anchorPos.getY() * 42317861L;
        return (int) Math.floorMod(blockSeed, 7L);
    }

    private static IBlockState getVillageFarmReplacement(int choice, IBlockState sourceState) {
        int sourceAge = getVillageCropAge(sourceState);
        if (choice == 0) {
            return getCropState(Blocks.WHEAT, sourceAge);
        }
        if (choice == 1) {
            return getCropState(Blocks.CARROTS, sourceAge);
        }
        if (choice == 2) {
            return getCropState(Blocks.POTATOES, sourceAge);
        }
        if (choice == 3) {
            return getCropState(Blocks.BEETROOTS, sourceAge);
        }
        if (choice == 4) {
            return getCropState(ModBlocks.CABBAGES, sourceAge);
        }
        if (choice == 5) {
            return getCropState(ModBlocks.ONIONS, sourceAge);
        }
        return getCropState(ModBlocks.BUDDING_TOMATOES, sourceAge);
    }

    private static IBlockState getCropState(Block cropBlock, int sourceAge) {
        BlockCrops crop = (BlockCrops) cropBlock;
        return crop.withAge(Math.min(sourceAge, crop.getMaxAge()));
    }

    private static boolean canVillageReplacementStay(World world, BlockPos cropPos, IBlockState replacementState) {
        Block block = replacementState.getBlock();
        if (block instanceof BlockBush) {
            return ((BlockBush) block).canBlockStay(world, cropPos, replacementState);
        }
        return block.canPlaceBlockAt(world, cropPos);
    }

    private static int getVillageCropAge(IBlockState sourceState) {
        Block block = sourceState.getBlock();
        if (block instanceof BlockCrops) {
            return block.getMetaFromState(sourceState) & 7;
        }
        return 0;
    }

    private interface PieceFactory {
        Village create(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing);
    }

    private static final class CompostHandler implements VillagerRegistry.IVillageCreationHandler {
        private final TemplateDefinition definition;
        private final PieceFactory pieceFactory;

        private CompostHandler(TemplateDefinition definition, PieceFactory pieceFactory) {
            this.definition = definition;
            this.pieceFactory = pieceFactory;
        }

        @Override
        public PieceWeight getVillagePieceWeight(java.util.Random random, int villageSize) {
            return new PieceWeight(this.definition.pieceClass, this.definition.weight, 1);
        }

        @Override
        public Class<?> getComponentClass() {
            return this.definition.pieceClass;
        }

        @Override
        public Village buildComponent(PieceWeight villagePiece, StructureVillagePieces.Start startPiece, List<StructureComponent> pieces, java.util.Random random, int x, int y, int z, EnumFacing facing, int componentType) {
            if (!(startPiece instanceof StructureVillageVN.StartVN)) {
                return new EmptyCompostPiece(startPiece, componentType, createEmptyBoundingBox(x, z), facing);
            }

            StructureVillageVN.StartVN villageStart = (StructureVillageVN.StartVN) startPiece;
            if (villageStart.villageType != this.definition.villageType) {
                return new EmptyCompostPiece(startPiece, componentType, createEmptyBoundingBox(x, z), facing);
            }

            Village villagePieceInstance = CompostPiece.createPiece(villageStart, pieces, x, y, z, facing, componentType, this.definition, this.pieceFactory);
            if (villagePieceInstance == null) {
                return new EmptyCompostPiece(startPiece, componentType, createEmptyBoundingBox(x, z), facing);
            }
            return villagePieceInstance;
        }
    }

    private static StructureBoundingBox createEmptyBoundingBox(int x, int z) {
        return new StructureBoundingBox(x, 1, z, x, 1, z);
    }

    private abstract static class CompostPiece extends StructureVillageVN.VNComponent {
        private TemplateDefinition definition;

        protected CompostPiece() {
            this(null);
        }

        protected CompostPiece(TemplateDefinition definition) {
            this.averageGroundLvl = -1;
            this.averageGroundLevel = -1;
            setDefinition(definition);
        }

        protected CompostPiece(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing, TemplateDefinition definition) {
            this(definition);
            this.boundingBox = boundingBox;
            this.componentType = componentType;
            this.startPiece = startPiece;
            this.setCoordBaseMode(facing);
            this.ascertainVillageStatsFromStartPiece(startPiece);
        }

        protected void setDefinition(TemplateDefinition definition) {
            this.definition = definition;
            applyDefinitionContext();
        }

        private void applyDefinitionContext() {
            if (this.definition == null) {
                return;
            }

            this.villageType = this.definition.villageType;
            this.materialType = this.definition.materialType;
            this.setStructureType(this.definition.structureType);
        }

        @Override
        public boolean addComponentParts(World worldIn, java.util.Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            if (this.definition == null) {
                this.setDefinition(findDefinition(this.getClass()));
            }
            if (this.definition == null && this.startPiece instanceof StructureVillageVN.StartVN) {
                this.setDefinition(resolveDefinition((StructureVillageVN.StartVN) this.startPiece));
            }
            if (this.definition == null) {
                FarmersDelightLegacy.LOGGER.error("Village Names 村庄堆肥场缺少结构定义：{}", this.getClass().getName());
                return false;
            }

            TemplateData templateData = this.definition.getTemplate();
            this.populateVillageFields(worldIn);
            applyDefinitionContext();

            if (this.averageGroundLvl < 0) {
                this.averageGroundLvl = this.getAverageGroundLevel(worldIn, this.boundingBox);
                this.averageGroundLevel = this.averageGroundLvl;
                if (this.averageGroundLvl < 0) {
                    return true;
                }

                int yOffset = this.averageGroundLvl - (this.boundingBox.minY + templateData.jigsawY);
                this.boundingBox.offset(0, yOffset, 0);
            }

            fillSupportBlocks(worldIn, structureBoundingBoxIn, templateData);
            Map<String, TemplateBlock> templateBlocksByPos = indexTemplateBlocks(templateData);
            Set<String> placedTemplateBlocks = new HashSet<>();

            for (TemplateBlock templateBlock : templateData.blocks) {
                if (isDoorBlock(templateBlock.blockState) || isAttachmentSensitive(templateBlock.blockState)) {
                    continue;
                }

                placeTemplateBlock(worldIn, structureBoundingBoxIn, templateBlock);
                placedTemplateBlocks.add(toTemplateKey(templateBlock.localX, templateBlock.localY, templateBlock.localZ));
            }

            for (TemplateBlock templateBlock : templateData.blocks) {
                if (!isDoorLower(templateBlock.blockState)) {
                    continue;
                }

                String lowerKey = toTemplateKey(templateBlock.localX, templateBlock.localY, templateBlock.localZ);
                if (placedTemplateBlocks.contains(lowerKey)) {
                    continue;
                }

                TemplateBlock upperBlock = templateBlocksByPos.get(toTemplateKey(templateBlock.localX, templateBlock.localY + 1, templateBlock.localZ));
                if (upperBlock != null && isMatchingDoorUpper(templateBlock.blockState, upperBlock.blockState)) {
                    placeDoorPair(worldIn, structureBoundingBoxIn, templateBlock, upperBlock);
                    placedTemplateBlocks.add(lowerKey);
                    placedTemplateBlocks.add(toTemplateKey(upperBlock.localX, upperBlock.localY, upperBlock.localZ));
                    continue;
                }

                placeTemplateBlock(worldIn, structureBoundingBoxIn, templateBlock);
                placedTemplateBlocks.add(lowerKey);
            }

            for (TemplateBlock templateBlock : templateData.blocks) {
                if (!isDoorUpper(templateBlock.blockState)) {
                    continue;
                }

                String upperKey = toTemplateKey(templateBlock.localX, templateBlock.localY, templateBlock.localZ);
                if (placedTemplateBlocks.contains(upperKey)) {
                    continue;
                }

                placeTemplateBlock(worldIn, structureBoundingBoxIn, templateBlock);
                placedTemplateBlocks.add(upperKey);
            }

            for (TemplateBlock templateBlock : templateData.blocks) {
                if (!isAttachmentSensitive(templateBlock.blockState)) {
                    continue;
                }

                String blockKey = toTemplateKey(templateBlock.localX, templateBlock.localY, templateBlock.localZ);
                if (placedTemplateBlocks.contains(blockKey)) {
                    continue;
                }

                placeTemplateBlock(worldIn, structureBoundingBoxIn, templateBlock);
                placedTemplateBlocks.add(blockKey);
            }
            return true;
        }

        @Override
        protected void writeStructureToNBT(NBTTagCompound tagCompound) {
            super.writeStructureToNBT(tagCompound);
            if (this.definition != null) {
                tagCompound.setString("FdTemplate", this.definition.filename);
            }
        }

        @Override
        protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager templateManager) {
            super.readStructureFromNBT(tagCompound, templateManager);
            if (tagCompound.hasKey("FdTemplate", 8)) {
                this.setDefinition(findDefinition(tagCompound.getString("FdTemplate")));
            } else {
                this.setDefinition(findDefinition(this.getClass()));
            }
        }

        private void placeTemplateBlock(World worldIn, StructureBoundingBox structureBoundingBoxIn, TemplateBlock templateBlock) {
            this.setBlockState(worldIn, templateBlock.blockState, templateBlock.localX, templateBlock.localY, templateBlock.localZ, structureBoundingBoxIn);
            applyTileEntityData(worldIn, structureBoundingBoxIn, templateBlock);
        }

        private void placeDoorPair(World worldIn, StructureBoundingBox structureBoundingBoxIn, TemplateBlock lowerBlock, TemplateBlock upperBlock) {
            this.setBlockState(worldIn, lowerBlock.blockState, lowerBlock.localX, lowerBlock.localY, lowerBlock.localZ, structureBoundingBoxIn);
            this.setBlockState(worldIn, upperBlock.blockState, upperBlock.localX, upperBlock.localY, upperBlock.localZ, structureBoundingBoxIn);
            applyTileEntityData(worldIn, structureBoundingBoxIn, lowerBlock);
            applyTileEntityData(worldIn, structureBoundingBoxIn, upperBlock);
        }

        private void applyTileEntityData(World worldIn, StructureBoundingBox structureBoundingBoxIn, TemplateBlock templateBlock) {
            if (templateBlock.tileEntityData == null) {
                return;
            }

            BlockPos worldPos = toWorldPos(templateBlock.localX, templateBlock.localY, templateBlock.localZ);
            if (!structureBoundingBoxIn.isVecInside(worldPos)) {
                return;
            }

            TileEntity tileEntity = worldIn.getTileEntity(worldPos);
            if (tileEntity == null) {
                return;
            }

            NBTTagCompound data = templateBlock.tileEntityData.copy();
            data.setInteger("x", worldPos.getX());
            data.setInteger("y", worldPos.getY());
            data.setInteger("z", worldPos.getZ());

            if (tileEntity instanceof TileEntityBanner) {
                data.setInteger("Base", EnumDyeColor.BROWN.getDyeDamage());
            }

            tileEntity.readFromNBT(data);
            tileEntity.markDirty();
            worldIn.notifyBlockUpdate(worldPos, templateBlock.blockState, templateBlock.blockState, 3);
        }

        private void fillSupportBlocks(World worldIn, StructureBoundingBox structureBoundingBoxIn, TemplateData templateData) {
            TemplateBlock[][] supportBlocks = new TemplateBlock[templateData.sizeX][templateData.sizeZ];

            for (TemplateBlock templateBlock : templateData.blocks) {
                if (templateBlock.localY > 1 || templateBlock.blockState.getMaterial() == Material.AIR) {
                    continue;
                }

                TemplateBlock existing = supportBlocks[templateBlock.localX][templateBlock.localZ];
                if (existing == null || templateBlock.localY < existing.localY) {
                    supportBlocks[templateBlock.localX][templateBlock.localZ] = templateBlock;
                }
            }

            for (int x = 0; x < supportBlocks.length; x++) {
                for (int z = 0; z < supportBlocks[x].length; z++) {
                    TemplateBlock support = supportBlocks[x][z];
                    if (support == null) {
                        continue;
                    }

                    this.replaceAirAndLiquidDownwards(worldIn, this.definition.foundationState, support.localX, support.localY - 1, support.localZ, structureBoundingBoxIn);
                }
            }
        }

        private BlockPos toWorldPos(int localX, int localY, int localZ) {
            return new BlockPos(
                    this.getXWithOffset(localX, localZ),
                    this.getYWithOffset(localY),
                    this.getZWithOffset(localX, localZ)
            );
        }

        private static boolean isDoorBlock(IBlockState blockState) {
            return blockState.getBlock() instanceof BlockDoor;
        }

        private static boolean isDoorLower(IBlockState blockState) {
            return isDoorBlock(blockState) && blockState.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
        }

        private static boolean isDoorUpper(IBlockState blockState) {
            return isDoorBlock(blockState) && blockState.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER;
        }

        private static boolean isMatchingDoorUpper(IBlockState lowerState, IBlockState upperState) {
            return lowerState.getBlock() == upperState.getBlock() && isDoorUpper(upperState);
        }

        private static boolean isAttachmentSensitive(IBlockState blockState) {
            Block block = blockState.getBlock();
            if (block instanceof BlockTorch || block instanceof BlockTrapDoor || block instanceof net.minecraft.block.BlockBanner) {
                return true;
            }

            ResourceLocation registryName = block.getRegistryName();
            if (registryName == null || registryName.getPath() == null) {
                return false;
            }

            String path = registryName.getPath();
            return path.contains("lantern") || path.contains("trapdoor") || path.contains("banner");
        }

        private static Map<String, TemplateBlock> indexTemplateBlocks(TemplateData templateData) {
            Map<String, TemplateBlock> templateBlocksByPos = new HashMap<>();
            for (TemplateBlock templateBlock : templateData.blocks) {
                templateBlocksByPos.put(toTemplateKey(templateBlock.localX, templateBlock.localY, templateBlock.localZ), templateBlock);
            }
            return templateBlocksByPos;
        }

        private static String toTemplateKey(int localX, int localY, int localZ) {
            return localX + ":" + localY + ":" + localZ;
        }

        private static Village createPiece(StructureVillageVN.StartVN startPiece, List<StructureComponent> pieces, int x, int y, int z, EnumFacing facing, int componentType, TemplateDefinition definition, PieceFactory pieceFactory) {
            TemplateData templateData = definition.getTemplate();
            StructureBoundingBox boundingBox = createBoundingBox(x, y, z, facing, templateData);
            boolean canGoDeeper = canVillageGoDeeper(boundingBox);
            StructureComponent intersectingPiece = findBlockingIntersecting(pieces, boundingBox, definition);
            if (!canGoDeeper || intersectingPiece != null) {
                return null;
            }

            return pieceFactory.create(startPiece, componentType, boundingBox, facing);
        }

        private static StructureComponent findBlockingIntersecting(List<StructureComponent> pieces, StructureBoundingBox boundingBox, TemplateDefinition definition) {
            for (StructureComponent piece : pieces) {
                if (piece.getBoundingBox() != null && piece.getBoundingBox().intersectsWith(boundingBox)) {
                    if (isVillageRoadPiece(piece)) {
                        continue;
                    }
                    return piece;
                }
            }
            return null;
        }

        private static boolean isVillageRoadPiece(StructureComponent piece) {
            String className = piece.getClass().getName();
            return piece instanceof StructureVillagePieces.Path || "astrotibs.villagenames.village.StructureVillageVN$PathVN".equals(className);
        }
    }

    public static class EmptyCompostPiece extends Village {
        public EmptyCompostPiece() {
        }

        public EmptyCompostPiece(StructureVillagePieces.Start startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
            super(startPiece, componentType);
            this.boundingBox = boundingBox;
            this.setCoordBaseMode(facing);
        }

        @Override
        public boolean addComponentParts(World worldIn, java.util.Random randomIn, StructureBoundingBox structureBoundingBoxIn) {
            return true;
        }
    }

    public static class PlainsCompostPiece extends CompostPiece {
        public PlainsCompostPiece() {
            super(PLAINS);
        }

        public PlainsCompostPiece(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
            super(startPiece, componentType, boundingBox, facing, PLAINS);
        }
    }

    public static class DesertCompostPiece extends CompostPiece {
        public DesertCompostPiece() {
            super(DESERT);
        }

        public DesertCompostPiece(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
            super(startPiece, componentType, boundingBox, facing, DESERT);
        }
    }

    public static class SavannaCompostPiece extends CompostPiece {
        public SavannaCompostPiece() {
            super(SAVANNA);
        }

        public SavannaCompostPiece(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
            super(startPiece, componentType, boundingBox, facing, SAVANNA);
        }
    }

    public static class SnowyCompostPiece extends CompostPiece {
        public SnowyCompostPiece() {
            super(SNOWY);
        }

        public SnowyCompostPiece(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
            super(startPiece, componentType, boundingBox, facing, SNOWY);
        }
    }

    public static class TaigaCompostPiece extends CompostPiece {
        public TaigaCompostPiece() {
            super(TAIGA);
        }

        public TaigaCompostPiece(StructureVillageVN.StartVN startPiece, int componentType, StructureBoundingBox boundingBox, EnumFacing facing) {
            super(startPiece, componentType, boundingBox, facing, TAIGA);
        }
    }

    private static StructureBoundingBox createBoundingBox(int x, int y, int z, EnumFacing facing, TemplateData templateData) {
        return StructureBoundingBox.getComponentToAddBoundingBox(
                x,
                y,
                z,
                -templateData.jigsawX,
                -templateData.jigsawY,
                getZOffset(templateData.jigsawZ, facing),
                templateData.sizeX,
                templateData.sizeY,
                templateData.sizeZ,
                facing
        );
    }

    private static int getZOffset(int jigsawZ, EnumFacing facing) {
        return facing == EnumFacing.NORTH || facing == EnumFacing.WEST ? jigsawZ : -jigsawZ;
    }

    private static TemplateDefinition findDefinition(Class<?> pieceClass) {
        for (TemplateDefinition definition : DEFINITIONS) {
            if (definition.pieceClass == pieceClass) {
                return definition;
            }
        }
        return null;
    }

    private static TemplateDefinition findDefinition(String filename) {
        for (TemplateDefinition definition : DEFINITIONS) {
            if (definition.filename.equals(filename)) {
                return definition;
            }
        }
        return null;
    }

    private static TemplateDefinition resolveDefinition(StructureVillageVN.StartVN startPiece) {
        if (startPiece == null || startPiece.villageType == null) {
            return null;
        }

        for (TemplateDefinition definition : DEFINITIONS) {
            if (definition.villageType == startPiece.villageType) {
                return definition;
            }
        }
        return null;
    }

    private static PlacementState mapState(NBTTagCompound paletteState) {
        String name = paletteState.getString("Name");
        NBTTagCompound properties = paletteState.hasKey("Properties", 10) ? paletteState.getCompoundTag("Properties") : new NBTTagCompound();

        IBlockState blockState;
        if ("farmersdelight:organic_compost".equals(name)) {
            blockState = ModBlocks.ORGANIC_COMPOST.getDefaultState();
            blockState = blockState.withProperty(BlockOrganicCompost.LEVEL, parseInt(properties, "composting", 0));
            return PlacementState.place(name, blockState);
        }
        if ("farmersdelight:rich_soil".equals(name)) {
            return PlacementState.place(name, ModBlocks.RICH_SOIL.getDefaultState());
        }
        if ("farmersdelight:rich_soil_farmland".equals(name)) {
            blockState = ModBlocks.RICH_SOIL_FARMLAND.getDefaultState();
            return PlacementState.place(name, applyNamedProperties(blockState, properties, "waterlogged"));
        }
        if ("farmersdelight:cabbages".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(ModBlocks.CABBAGES.getDefaultState(), properties));
        }
        if ("farmersdelight:tomatoes".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(ModBlocks.TOMATOES.getDefaultState(), properties));
        }
        if ("farmersdelight:onions".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(ModBlocks.ONIONS.getDefaultState(), properties));
        }
        if ("farmersdelight:brown_mushroom_colony".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(ModBlocks.BROWN_MUSHROOM_COLONY.getDefaultState(), properties));
        }

        if ("minecraft:grass_block".equals(name)) {
            return PlacementState.place(name, Blocks.GRASS.getDefaultState());
        }
        if ("minecraft:podzol".equals(name)) {
            return PlacementState.place(name, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL));
        }
        if ("minecraft:coarse_dirt".equals(name)) {
            return PlacementState.place(name, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT));
        }
        if ("minecraft:oak_planks".equals(name)) {
            return PlacementState.place(name, Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.OAK));
        }
        if ("minecraft:oak_log".equals(name)) {
            blockState = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
            return PlacementState.place(name, applyNamedProperties(blockState, properties));
        }
        if ("minecraft:oak_fence".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(Blocks.OAK_FENCE.getDefaultState(), properties, "waterlogged"));
        }
        if ("minecraft:spruce_fence".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(Blocks.SPRUCE_FENCE.getDefaultState(), properties, "waterlogged"));
        }
        if ("minecraft:acacia_fence".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(Blocks.ACACIA_FENCE.getDefaultState(), properties, "waterlogged"));
        }
        if ("minecraft:oak_fence_gate".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(Blocks.OAK_FENCE_GATE.getDefaultState(), properties, "waterlogged", "in_wall"));
        }
        if ("minecraft:spruce_fence_gate".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(Blocks.SPRUCE_FENCE_GATE.getDefaultState(), properties, "waterlogged", "in_wall"));
        }
        if ("minecraft:acacia_fence_gate".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(Blocks.ACACIA_FENCE_GATE.getDefaultState(), properties, "waterlogged", "in_wall"));
        }
        if ("minecraft:oak_door".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(Blocks.OAK_DOOR.getDefaultState(), properties));
        }
        if ("minecraft:spruce_door".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(Blocks.SPRUCE_DOOR.getDefaultState(), properties));
        }
        if ("minecraft:acacia_door".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(Blocks.ACACIA_DOOR.getDefaultState(), properties));
        }
        if ("minecraft:spruce_log".equals(name)) {
            blockState = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
            return PlacementState.place(name, applyNamedProperties(blockState, properties));
        }
        if ("minecraft:acacia_log".equals(name)) {
            blockState = Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA);
            return PlacementState.place(name, applyNamedProperties(blockState, properties));
        }
        if ("minecraft:acacia_wood".equals(name)) {
            Block futureBlock = lookupBlock("futuremc:acacia_wood");
            if (futureBlock != null) {
                return PlacementState.place(name, applyNamedProperties(futureBlock.getDefaultState(), properties, "waterlogged"));
            }
            blockState = Blocks.LOG2.getDefaultState()
                    .withProperty(BlockNewLog.VARIANT, BlockPlanks.EnumType.ACACIA)
                    .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.NONE);
            return PlacementState.place(name, blockState);
        }
        if ("minecraft:dirt_path".equals(name)) {
            return PlacementState.place(name, Blocks.GRASS_PATH.getDefaultState());
        }
        if ("minecraft:oak_slab".equals(name)) {
            return PlacementState.place(name, createWoodSlabState(BlockPlanks.EnumType.OAK, properties.getString("type")));
        }
        if ("minecraft:acacia_slab".equals(name)) {
            return PlacementState.place(name, createWoodSlabState(BlockPlanks.EnumType.ACACIA, properties.getString("type")));
        }
        if ("minecraft:smooth_sandstone".equals(name)) {
            return PlacementState.place(name, Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH));
        }
        if ("minecraft:cut_sandstone".equals(name)) {
            Block futureBlock = lookupBlock("futuremc:cut_sandstone");
            if (futureBlock != null) {
                return PlacementState.place(name, futureBlock.getDefaultState());
            }
            return PlacementState.place(name, Blocks.SANDSTONE.getDefaultState());
        }
        if ("minecraft:smooth_sandstone_slab".equals(name)) {
            Block futureBlock = lookupBlock("futuremc:smooth_sandstone_slab");
            if (futureBlock != null) {
                return PlacementState.place(name, applyNamedProperties(futureBlock.getDefaultState(), properties, "waterlogged"));
            }
            return PlacementState.place(name, createSandstoneSlabState(properties.getString("type")));
        }
        if ("minecraft:sandstone_wall".equals(name)) {
            Block futureBlock = lookupBlock("futuremc:sandstone_wall");
            blockState = futureBlock != null ? futureBlock.getDefaultState() : Blocks.COBBLESTONE_WALL.getDefaultState();
            return PlacementState.place(name, applyWallProperties(blockState, properties));
        }
        if ("minecraft:stripped_spruce_log".equals(name)) {
            Block futureBlock = lookupBlock("futuremc:stripped_spruce_log");
            if (futureBlock != null) {
                return PlacementState.place(name, applyNamedProperties(futureBlock.getDefaultState(), properties));
            }
            blockState = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
            return PlacementState.place(name, applyNamedProperties(blockState, properties));
        }
        if ("minecraft:lantern".equals(name)) {
            Block futureBlock = lookupBlock("futuremc:lantern");
            if (futureBlock != null) {
                return PlacementState.place(name, applyNamedProperties(futureBlock.getDefaultState(), properties, "waterlogged"));
            }
            return PlacementState.place(name, Blocks.TORCH.getDefaultState());
        }
        if ("minecraft:spruce_trapdoor".equals(name)) {
            Block futureBlock = lookupBlock("futuremc:spruce_trapdoor");
            blockState = futureBlock != null ? futureBlock.getDefaultState() : Blocks.TRAPDOOR.getDefaultState();
            return PlacementState.place(name, applyNamedProperties(blockState, properties, "waterlogged"));
        }
        if ("minecraft:brown_wall_banner".equals(name)) {
            return PlacementState.place(name, applyNamedProperties(Blocks.WALL_BANNER.getDefaultState(), properties, "waterlogged"));
        }
        if ("minecraft:jigsaw".equals(name)) {
            return PlacementState.skip(name);
        }

        Block directBlock = lookupBlock(name);
        if (directBlock == null) {
            reportMissingBlock(name);
            return PlacementState.skip(name);
        }

        if (directBlock == Blocks.COBBLESTONE_WALL) {
            return PlacementState.place(name, applyWallProperties(directBlock.getDefaultState(), properties));
        }

        return PlacementState.place(name, applyNamedProperties(directBlock.getDefaultState(), properties, "waterlogged", "orientation", "in_wall", "snowy"));
    }

    private static PlacementState mapFinalState(String finalState) {
        if (finalState == null || finalState.isEmpty() || "minecraft:structure_void".equals(finalState)) {
            return PlacementState.skip("minecraft:structure_void");
        }

        String stateName = finalState;
        NBTTagCompound properties = new NBTTagCompound();
        int bracketIndex = finalState.indexOf('[');
        if (bracketIndex >= 0 && finalState.endsWith("]")) {
            stateName = finalState.substring(0, bracketIndex);
            String propertiesBody = finalState.substring(bracketIndex + 1, finalState.length() - 1);
            if (!propertiesBody.isEmpty()) {
                String[] tokens = propertiesBody.split(",");
                for (String token : tokens) {
                    String[] keyValue = token.split("=", 2);
                    if (keyValue.length == 2) {
                        properties.setString(keyValue[0], keyValue[1]);
                    }
                }
            }
        }

        NBTTagCompound stateTag = new NBTTagCompound();
        stateTag.setString("Name", stateName);
        if (!properties.getKeySet().isEmpty()) {
            stateTag.setTag("Properties", properties);
        }
        return mapState(stateTag);
    }

    private static IBlockState applyNamedProperties(IBlockState blockState, NBTTagCompound properties, String... ignoredKeys) {
        Set<String> ignored = new LinkedHashSet<>(Arrays.asList(ignoredKeys));
        IBlockState result = blockState;
        for (String key : properties.getKeySet()) {
            if (ignored.contains(key)) {
                continue;
            }
            result = withPropertyIfPresent(result, key, properties.getString(key));
        }
        return result;
    }

    private static IBlockState applyWallProperties(IBlockState blockState, NBTTagCompound properties) {
        IBlockState result = blockState;
        result = withPropertyIfPresent(result, "north", hasWallConnection(properties, "north") ? "true" : "false");
        result = withPropertyIfPresent(result, "east", hasWallConnection(properties, "east") ? "true" : "false");
        result = withPropertyIfPresent(result, "south", hasWallConnection(properties, "south") ? "true" : "false");
        result = withPropertyIfPresent(result, "west", hasWallConnection(properties, "west") ? "true" : "false");
        return result;
    }

    private static boolean hasWallConnection(NBTTagCompound properties, String key) {
        if (!properties.hasKey(key, 8)) {
            return false;
        }
        String value = properties.getString(key);
        return !"none".equals(value) && !"false".equals(value);
    }

    private static IBlockState createWoodSlabState(BlockPlanks.EnumType plankType, String typeName) {
        if ("double".equals(typeName)) {
            return Blocks.DOUBLE_WOODEN_SLAB.getDefaultState().withProperty(BlockWoodSlab.VARIANT, plankType);
        }

        IBlockState blockState = Blocks.WOODEN_SLAB.getDefaultState().withProperty(BlockWoodSlab.VARIANT, plankType);
        if ("top".equals(typeName)) {
            blockState = blockState.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
        }
        return blockState;
    }

    private static IBlockState createSandstoneSlabState(String typeName) {
        if ("double".equals(typeName)) {
            return Blocks.DOUBLE_STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SAND);
        }

        IBlockState blockState = Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SAND);
        if ("top".equals(typeName)) {
            blockState = blockState.withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.TOP);
        }
        return blockState;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withPropertyIfPresent(IBlockState blockState, String propertyName, String value) {
        for (IProperty property : blockState.getPropertyKeys()) {
            if (!property.getName().equals(propertyName)) {
                continue;
            }

            Optional parsedValue = property.parseValue(value);
            if (parsedValue.isPresent()) {
                return withParsedProperty(blockState, property, (Comparable) parsedValue.get());
            }
            break;
        }
        return blockState;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState withParsedProperty(IBlockState blockState, IProperty property, Comparable value) {
        return blockState.withProperty(property, value);
    }

    private static int parseInt(NBTTagCompound properties, String key, int defaultValue) {
        if (!properties.hasKey(key, 8)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(properties.getString(key));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static Block lookupBlock(String registryName) {
        ResourceLocation location = new ResourceLocation(registryName);
        return ForgeRegistries.BLOCKS.getValue(location);
    }

    private static void reportMissingBlock(String registryName) {
        if (REPORTED_MISSING_BLOCKS.add(registryName)) {
            FarmersDelightLegacy.LOGGER.warn("Village Names 结构方块无法映射，已跳过：{}", registryName);
        }
    }

    private static final class TemplateDefinition {
        private final String filename;
        private final VillageType villageType;
        private final MaterialType materialType;
        private final String villageTypeKey;
        private final int weight;
        private final int structureType;
        private final IBlockState foundationState;
        private final Class<? extends CompostPiece> pieceClass;
        private TemplateData cachedTemplate;

        private TemplateDefinition(String filename, VillageType villageType, MaterialType materialType, String villageTypeKey, int weight, int structureType, IBlockState foundationState, Class<? extends CompostPiece> pieceClass) {
            this.filename = filename;
            this.villageType = villageType;
            this.materialType = materialType;
            this.villageTypeKey = villageTypeKey;
            this.weight = weight;
            this.structureType = structureType;
            this.foundationState = foundationState;
            this.pieceClass = pieceClass;
        }

        private synchronized TemplateData getTemplate() {
            if (this.cachedTemplate == null) {
                this.cachedTemplate = loadTemplate(this);
            }
            return this.cachedTemplate;
        }
    }

    private static final class TemplateData {
        private final int sizeX;
        private final int sizeY;
        private final int sizeZ;
        private final int jigsawX;
        private final int jigsawY;
        private final int jigsawZ;
        private final List<TemplateBlock> blocks;

        private TemplateData(int sizeX, int sizeY, int sizeZ, int jigsawX, int jigsawY, int jigsawZ, List<TemplateBlock> blocks) {
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.jigsawX = jigsawX;
            this.jigsawY = jigsawY;
            this.jigsawZ = jigsawZ;
            this.blocks = Collections.unmodifiableList(blocks);
        }
    }

    private static final class TemplateBlock {
        private final int localX;
        private final int localY;
        private final int localZ;
        private final IBlockState blockState;
        private final NBTTagCompound tileEntityData;

        private TemplateBlock(int localX, int localY, int localZ, IBlockState blockState, NBTTagCompound tileEntityData) {
            this.localX = localX;
            this.localY = localY;
            this.localZ = localZ;
            this.blockState = blockState;
            this.tileEntityData = tileEntityData;
        }
    }

    private static final class PlacementState {
        private final IBlockState blockState;
        private final boolean shouldPlace;

        private PlacementState(IBlockState blockState, boolean shouldPlace) {
            this.blockState = blockState;
            this.shouldPlace = shouldPlace;
        }

        private static PlacementState place(String sourceName, IBlockState blockState) {
            return new PlacementState(blockState, true);
        }

        private static PlacementState skip(String sourceName) {
            return new PlacementState(Blocks.AIR.getDefaultState(), false);
        }
    }

    private static TemplateData loadTemplate(TemplateDefinition definition) {
        try (InputStream inputStream = VillageNamesVillageCompat.class.getResourceAsStream(STRUCTURE_ROOT + definition.filename)) {
            if (inputStream == null) {
                throw new IllegalStateException("缺少结构模板资源：" + STRUCTURE_ROOT + definition.filename);
            }

            NBTTagCompound root = CompressedStreamTools.readCompressed(inputStream);
            NBTTagList sizeTag = root.getTagList("size", 3);
            int sizeX = sizeTag.getIntAt(0);
            int sizeY = sizeTag.getIntAt(1);
            int sizeZ = sizeTag.getIntAt(2);

            NBTTagList paletteTag = root.getTagList("palette", 10);
            List<NBTTagCompound> paletteEntries = new ArrayList<>(paletteTag.tagCount());
            for (int i = 0; i < paletteTag.tagCount(); i++) {
                paletteEntries.add(paletteTag.getCompoundTagAt(i));
            }

            int jigsawX = -1;
            int jigsawY = -1;
            int jigsawZ = -1;
            List<TemplateBlock> blocks = new ArrayList<>();
            NBTTagList blockTag = root.getTagList("blocks", 10);
            for (int i = 0; i < blockTag.tagCount(); i++) {
                NBTTagCompound blockData = blockTag.getCompoundTagAt(i);
                NBTTagList posTag = blockData.getTagList("pos", 3);
                int localX = posTag.getIntAt(0);
                int localY = posTag.getIntAt(1);
                int localZ = posTag.getIntAt(2);

                NBTTagCompound paletteState = paletteEntries.get(blockData.getInteger("state"));
                String stateName = paletteState.getString("Name");
                NBTTagCompound tileEntityData = blockData.hasKey("nbt", 10) ? blockData.getCompoundTag("nbt").copy() : null;

                PlacementState placementState;
                if ("minecraft:jigsaw".equals(stateName)) {
                    if (tileEntityData != null && "minecraft:building_entrance".equals(tileEntityData.getString("name"))) {
                        jigsawX = localX;
                        jigsawY = localY;
                        jigsawZ = localZ;
                    }
                    placementState = mapFinalState(tileEntityData != null ? tileEntityData.getString("final_state") : "");
                    tileEntityData = null;
                } else {
                    placementState = mapState(paletteState);
                }

                if (!placementState.shouldPlace) {
                    continue;
                }

                if (placementState.blockState.getBlock() == Blocks.WALL_BANNER && tileEntityData == null) {
                    tileEntityData = new NBTTagCompound();
                    tileEntityData.setString("id", "minecraft:banner");
                }

                blocks.add(new TemplateBlock(localX, localY, localZ, placementState.blockState, tileEntityData));
            }

            if (jigsawX < 0 || jigsawY < 0 || jigsawZ < 0) {
                throw new IllegalStateException("结构模板缺少 village 入口 jigsaw：" + definition.filename);
            }

            return new TemplateData(sizeX, sizeY, sizeZ, jigsawX, jigsawY, jigsawZ, blocks);
        } catch (IOException exception) {
            throw new IllegalStateException("读取结构模板失败：" + definition.filename, exception);
        }
    }
}
