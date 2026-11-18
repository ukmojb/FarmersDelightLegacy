package com.wdcftgg.farmersdelightlegacy.common.world;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.compat.VillageNamesVillageCompat;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraft.world.biome.BiomeSavanna;
import net.minecraft.world.biome.BiomeTaiga;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.List;
import java.util.Random;

public final class VillageCompostPiece extends StructureVillagePieces.Village implements VillageCompostTemplate.Placement {
    private static boolean registered;
    private Kind kind = Kind.PLAINS;

    public VillageCompostPiece() {
    }

    private VillageCompostPiece(StructureVillagePieces.Start start, int componentType, Kind kind,
                               StructureBoundingBox bounds, EnumFacing facing) {
        super(start, componentType);
        this.kind = kind;
        this.boundingBox = bounds;
        setCoordBaseMode(facing);
    }

    public static void registerAll() {
        if (registered) {
            return;
        }
        MapGenStructureIO.registerStructureComponent(VillageCompostPiece.class, "FDCompostPile");
        if (Configuration.generateVillageCompostHeaps) {
            VillagerRegistry.instance().registerVillageCreationHandler(new Handler());
        }
        registered = true;
    }

    @Override
    public boolean addComponentParts(World world, Random random, StructureBoundingBox bounds) {
        VillageCompostTemplate.TemplateData data = kind.template();
        if (averageGroundLvl < 0) {
            averageGroundLvl = getAverageGroundLevel(world, bounds);
            if (averageGroundLvl < 0) {
                return true;
            }
            boundingBox.offset(0, averageGroundLvl - boundingBox.minY - data.jigsawY, 0);
        }
        VillageCompostTemplate.place(data, kind.foundation, this, world, bounds);
        return true;
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tag) {
        super.writeStructureToNBT(tag);
        tag.setString("FdTemplate", kind.filename);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tag, TemplateManager manager) {
        super.readStructureFromNBT(tag, manager);
        String filename = tag.getString("FdTemplate");
        for (Kind value : Kind.values()) {
            if (value.filename.equals(filename)) {
                kind = value;
                return;
            }
        }
        throw new IllegalArgumentException("未知的堆肥结构模板：" + filename);
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
    protected IBlockState getBiomeSpecificBlockState(IBlockState state) {
        return state;
    }

    @Override
    public BlockPos position(int x, int y, int z) {
        return new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
    }

    private enum Kind {
        PLAINS("plains", 5, Blocks.DIRT.getDefaultState()),
        DESERT("desert", 3, Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH)),
        SAVANNA("savanna", 4, Blocks.DIRT.getDefaultState()),
        SNOWY("snowy", 3, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL)),
        TAIGA("taiga", 4, Blocks.COBBLESTONE.getDefaultState());

        private final String filename;
        private final int weight;
        private final IBlockState foundation;
        private VillageCompostTemplate.TemplateData data;

        Kind(String name, int weight, IBlockState foundation) {
            this.filename = name + "_compost_pile.nbt";
            this.weight = weight;
            this.foundation = foundation;
        }

        private synchronized VillageCompostTemplate.TemplateData template() {
            if (data == null) {
                data = VillageCompostTemplate.load(filename);
            }
            return data;
        }

        private static Kind forBiome(Biome biome) {
            if (biome.isSnowyBiome()) {
                return SNOWY;
            }
            if (biome instanceof BiomeDesert) {
                return DESERT;
            }
            if (biome instanceof BiomeSavanna) {
                return SAVANNA;
            }
            return biome instanceof BiomeTaiga ? TAIGA : PLAINS;
        }
    }

    public static final class Handler implements VillagerRegistry.IVillageCreationHandler {
        @Override
        public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int villageSize) {
            return new StructureVillagePieces.PieceWeight(VillageCompostPiece.class, 5, 1);
        }

        @Override
        public Class<?> getComponentClass() {
            return VillageCompostPiece.class;
        }

        @Override
        public StructureVillagePieces.Village buildComponent(StructureVillagePieces.PieceWeight weight,
                StructureVillagePieces.Start start, List<StructureComponent> pieces, Random random,
                int x, int y, int z, EnumFacing facing, int componentType) {
            if (!Configuration.generateVillageCompostHeaps || Loader.isModLoaded("villagenames")
                    && VillageNamesVillageCompat.isVillageStart(start)) {
                return null;
            }
            Kind kind = Kind.forBiome(start.biome);
            if (random.nextInt(5) >= kind.weight) {
                return null;
            }
            StructureBoundingBox bounds = VillageCompostTemplate.createBoundingBox(x, y, z, facing, kind.template());
            if (!canVillageGoDeeper(bounds) || StructureComponent.findIntersecting(pieces, bounds) != null) {
                return null;
            }
            return new VillageCompostPiece(start, componentType, kind, bounds, facing);
        }
    }
}
