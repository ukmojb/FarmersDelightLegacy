package com.wdcftgg.farmersdelightlegacy.common.world;

import com.google.common.base.Optional;
import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockOrganicCompost;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class VillageCompostTemplate {
    private static final String STRUCTURE_ROOT = "/data/farmersdelightlegacy/structures/village/houses/";
    private static final Set<String> REPORTED_MISSING_BLOCKS = new LinkedHashSet<>();
    private final TemplateData templateData;
    private final IBlockState foundationState;
    private final Placement placement;

    private VillageCompostTemplate(TemplateData templateData, IBlockState foundationState, Placement placement) {
        this.templateData = templateData;
        this.foundationState = foundationState;
        this.placement = placement;
    }

    public interface Placement {
        void setBlock(World world, IBlockState state, int x, int y, int z, StructureBoundingBox bounds);
        void fill(World world, IBlockState state, int x, int y, int z, StructureBoundingBox bounds);
        BlockPos position(int x, int y, int z);
    }

    public static void place(TemplateData data, IBlockState foundation, Placement placement, World world, StructureBoundingBox bounds) {
        new VillageCompostTemplate(data, foundation, placement).generate(world, bounds);
    }

    private void generate(World worldIn, StructureBoundingBox structureBoundingBoxIn) {
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

    }

    private void placeTemplateBlock(World worldIn, StructureBoundingBox structureBoundingBoxIn, TemplateBlock templateBlock) {
        placement.setBlock(worldIn, localState(templateBlock.blockState), templateBlock.localX, templateBlock.localY, templateBlock.localZ, structureBoundingBoxIn);
        applyTileEntityData(worldIn, structureBoundingBoxIn, templateBlock);
    }

    private void placeDoorPair(World worldIn, StructureBoundingBox structureBoundingBoxIn, TemplateBlock lowerBlock, TemplateBlock upperBlock) {
        placement.setBlock(worldIn, localState(lowerBlock.blockState), lowerBlock.localX, lowerBlock.localY, lowerBlock.localZ, structureBoundingBoxIn);
        placement.setBlock(worldIn, localState(upperBlock.blockState), upperBlock.localX, upperBlock.localY, upperBlock.localZ, structureBoundingBoxIn);
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

    private IBlockState localState(IBlockState state) {
        return templateData.normalizedLayout ? state.withMirror(Mirror.LEFT_RIGHT) : state;
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

                placement.fill(worldIn, foundationState, support.localX, support.localY - 1, support.localZ, structureBoundingBoxIn);
            }
        }
    }

    private BlockPos toWorldPos(int localX, int localY, int localZ) {
        return placement.position(localX, localY, localZ);
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

    public static StructureBoundingBox createBoundingBox(int x, int y, int z, EnumFacing facing, TemplateData templateData) {
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
            Block futureBlock = lookupBackportBlock("acacia_wood");
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
            Block futureBlock = lookupBackportBlock("stripped_spruce_log");
            if (futureBlock != null) {
                return PlacementState.place(name, applyNamedProperties(futureBlock.getDefaultState(), properties));
            }
            blockState = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.SPRUCE);
            return PlacementState.place(name, applyNamedProperties(blockState, properties));
        }
        if ("minecraft:lantern".equals(name)) {
            Block futureBlock = lookupBackportBlock("lantern");
            if (futureBlock != null) {
                return PlacementState.place(name, applyNamedProperties(futureBlock.getDefaultState(), properties, "waterlogged"));
            }
            return PlacementState.place(name, Blocks.TORCH.getDefaultState());
        }
        if ("minecraft:spruce_trapdoor".equals(name)) {
            Block futureBlock = lookupBackportBlock("spruce_trapdoor");
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
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("无效的堆肥模板属性：" + key + "=" + properties.getString(key), exception);
        }
    }

    public static Block lookupBackportBlock(String path) {
        Block block = lookupBlock("futuremc:" + path);
        return block != null ? block : lookupBlock("farmers_future_delight:" + path);
    }

    private static Block lookupBlock(String registryName) {
        ResourceLocation location = new ResourceLocation(registryName);
        return ForgeRegistries.BLOCKS.containsKey(location) ? ForgeRegistries.BLOCKS.getValue(location) : null;
    }

    private static void reportMissingBlock(String registryName) {
        if (REPORTED_MISSING_BLOCKS.add(registryName)) {
            FarmersDelightLegacy.LOGGER.warn("无法映射堆肥结构方块，跳过该方块：{}", registryName);
        }
    }

    public static final class TemplateData {
        public final int sizeX;
        public final int sizeY;
        public final int sizeZ;
        public final int jigsawX;
        public final int jigsawY;
        public final int jigsawZ;
        private final List<TemplateBlock> blocks;
        private final boolean normalizedLayout;

        private TemplateData(int sizeX, int sizeY, int sizeZ, int jigsawX, int jigsawY, int jigsawZ, List<TemplateBlock> blocks, boolean normalizedLayout) {
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.jigsawX = jigsawX;
            this.jigsawY = jigsawY;
            this.jigsawZ = jigsawZ;
            this.blocks = Collections.unmodifiableList(blocks);
            this.normalizedLayout = normalizedLayout;
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

    public static TemplateData load(String filename) {
        return load(filename, true);
    }

    public static TemplateData load(String filename, boolean normalizedLayout) {
        try (InputStream inputStream = VillageCompostTemplate.class.getResourceAsStream(STRUCTURE_ROOT + filename)) {
            if (inputStream == null) {
                throw new IllegalStateException("缺少结构模板资源：" + STRUCTURE_ROOT + filename);
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
            String entranceOrientation = "";
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
                        entranceOrientation = paletteState.getCompoundTag("Properties").getString("orientation");
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
                throw new IllegalStateException("结构模板缺少 village 入口 jigsaw：" + filename);
            }

            if (!normalizedLayout) {
                return new TemplateData(sizeX, sizeY, sizeZ, jigsawX, jigsawY, jigsawZ, blocks, false);
            }

            Rotation rotation;
            BlockPos offset;
            switch (entranceOrientation) {
                case "north_up":
                    return new TemplateData(sizeX, sizeY, sizeZ, jigsawX, jigsawY, jigsawZ, blocks, true);
                case "west_up":
                    rotation = Rotation.CLOCKWISE_90;
                    offset = new BlockPos(sizeZ - 1, 0, 0);
                    break;
                case "east_up":
                    rotation = Rotation.COUNTERCLOCKWISE_90;
                    offset = new BlockPos(0, 0, sizeX - 1);
                    break;
                case "south_up":
                    rotation = Rotation.CLOCKWISE_180;
                    offset = new BlockPos(sizeX - 1, 0, sizeZ - 1);
                    break;
                default:
                    throw new IllegalStateException("无效的堆肥模板入口方向：" + filename + ": " + entranceOrientation);
            }
            List<TemplateBlock> rotatedBlocks = new ArrayList<>(blocks.size());
            for (TemplateBlock block : blocks) {
                BlockPos pos = new BlockPos(block.localX, block.localY, block.localZ).rotate(rotation).add(offset);
                rotatedBlocks.add(new TemplateBlock(pos.getX(), pos.getY(), pos.getZ(),
                        block.blockState.withRotation(rotation), block.tileEntityData));
            }
            BlockPos entrance = new BlockPos(jigsawX, jigsawY, jigsawZ).rotate(rotation).add(offset);
            boolean swapAxes = rotation != Rotation.CLOCKWISE_180;
            return new TemplateData(swapAxes ? sizeZ : sizeX, sizeY, swapAxes ? sizeX : sizeZ,
                    entrance.getX(), entrance.getY(), entrance.getZ(), rotatedBlocks, true);
        } catch (IOException exception) {
            throw new IllegalStateException("读取结构模板失败：" + filename, exception);
        }
    }
}
