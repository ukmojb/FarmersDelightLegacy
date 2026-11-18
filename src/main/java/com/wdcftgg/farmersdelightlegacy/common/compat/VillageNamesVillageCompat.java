package com.wdcftgg.farmersdelightlegacy.common.compat;

import com.wdcftgg.farmersdelightlegacy.common.world.VillageCropRules;
import com.wdcftgg.farmersdelightlegacy.common.world.VillageCompostTemplate;
import com.wdcftgg.farmersdelightlegacy.common.world.VillageCompostTemplate.TemplateData;

import astrotibs.villagenames.config.village.VillageGeneratorConfigHandler;
import astrotibs.villagenames.utility.FunctionsVN.MaterialType;
import astrotibs.villagenames.utility.FunctionsVN.VillageType;
import astrotibs.villagenames.village.StructureVillageVN;
import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
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
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.*;

public final class VillageNamesVillageCompat {

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

    private static boolean registered = false;

    private VillageNamesVillageCompat() {
    }

    public static boolean isVillageStart(StructureVillagePieces.Start start) {
        return start instanceof StructureVillageVN.StartVN;
    }

    public static void registerAll() {
        if (registered || !Loader.isModLoaded("villagenames")) {
            return;
        }

        registerStructureComponents();
        if (Configuration.generateVillageCompostHeaps) {
            registerVillageHandlers();
            registerVillageTypeMappings();
        }
        registered = true;
        FarmersDelightLegacy.LOGGER.info("Registered the Village Names village compost pile compatibility structure.");
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

            VillageCropRules.replace(world, cropBlockPositions);
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
                return null;
            }

            StructureVillageVN.StartVN villageStart = (StructureVillageVN.StartVN) startPiece;
            if (villageStart.villageType != this.definition.villageType) {
                return null;
            }

            Village villagePieceInstance = CompostPiece.createPiece(villageStart, pieces, x, y, z, facing, componentType, this.definition, this.pieceFactory);
            if (villagePieceInstance == null) {
                return null;
            }
            return villagePieceInstance;
        }
    }

    private static StructureBoundingBox createEmptyBoundingBox(int x, int z) {
        return new StructureBoundingBox(x, 1, z, x, 1, z);
    }

    private abstract static class CompostPiece extends StructureVillageVN.VNComponent implements VillageCompostTemplate.Placement {
        private TemplateDefinition definition;
        private int layoutVersion = 1;

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
                FarmersDelightLegacy.LOGGER.error("Village Names 堆肥结构缺少模板定义：{}", this.getClass().getName());
                return false;
            }

            TemplateData templateData = this.definition.getTemplate(layoutVersion != 0);
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

            VillageCompostTemplate.place(templateData, this.definition.foundationState, this, worldIn, structureBoundingBoxIn);
            return true;
        }

        @Override
        protected void writeStructureToNBT(NBTTagCompound tagCompound) {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setInteger("FdLayoutVersion", layoutVersion);
            if (this.definition != null) {
                tagCompound.setString("FdTemplate", this.definition.filename);
            }
        }

        @Override
        protected void readStructureFromNBT(NBTTagCompound tagCompound, TemplateManager templateManager) {
            super.readStructureFromNBT(tagCompound, templateManager);
            layoutVersion = tagCompound.getInteger("FdLayoutVersion");
            if (layoutVersion < 0 || layoutVersion > 1) {
                throw new IllegalArgumentException("未知的堆肥结构布局版本：" + layoutVersion);
            }
            if (tagCompound.hasKey("FdTemplate", 8)) {
                this.setDefinition(findDefinition(tagCompound.getString("FdTemplate")));
            } else {
                this.setDefinition(findDefinition(this.getClass()));
            }
        }

        @Override
        public void setBlock(World world, IBlockState state, int x, int y, int z, StructureBoundingBox bounds) {
            setBlockState(world, state, x, y, z, bounds);
        }

        @Override
        public void fill(World world, IBlockState state, int x, int y, int z, StructureBoundingBox bounds) {
            replaceAirAndLiquidDownwards(world, state, x, y, z, bounds);
        }

        @Override
        public BlockPos position(int x, int y, int z) {
            return new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
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
        return VillageCompostTemplate.createBoundingBox(x, y, z, facing, templateData);
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

    static Block lookupBackportBlock(String path) {
        return VillageCompostTemplate.lookupBackportBlock(path);
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
        private TemplateData legacyTemplate;

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
            return getTemplate(true);
        }

        private synchronized TemplateData getTemplate(boolean normalizedLayout) {
            if (!normalizedLayout) {
                if (legacyTemplate == null) {
                    legacyTemplate = VillageCompostTemplate.load(filename, false);
                }
                return legacyTemplate;
            }
            if (this.cachedTemplate == null) {
                this.cachedTemplate = VillageCompostTemplate.load(filename);
            }
            return this.cachedTemplate;
        }
    }

}
