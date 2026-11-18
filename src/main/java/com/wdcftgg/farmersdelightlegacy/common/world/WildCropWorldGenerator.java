package com.wdcftgg.farmersdelightlegacy.common.world;

import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockMushroomColony;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockWildRice;
import com.wdcftgg.farmersdelightlegacy.common.compat.FluidloggingCompat;
import com.wdcftgg.farmersdelightlegacy.common.compat.fluidlogged.BlockFluidloggedWildRice;
import com.wdcftgg.farmersdelightlegacy.common.compat.VillageNamesVillageCompat;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModBlocks;
import com.wdcftgg.farmersdelightlegacy.common.registry.BlockCategories;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Loader;

import java.util.*;

public class WildCropWorldGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        BlockPos biomePos = new BlockPos((chunkX << 4) + 8, 0, (chunkZ << 4) + 8);
        Biome biome = world.getBiome(biomePos);
        int dimension = world.provider.getDimension();
        ResourceLocation biomeId = biome.getRegistryName();

        generateWildCabbages(world, random, chunkX, chunkZ, biome, biomeId, dimension);
        generateWildOnions(world, random, chunkX, chunkZ, biome, biomeId, dimension);
        generateWildTomatoes(world, random, chunkX, chunkZ, biome, biomeId, dimension);
        generateWildCarrots(world, random, chunkX, chunkZ, biome, biomeId, dimension);
        generateWildPotatoes(world, random, chunkX, chunkZ, biome, biomeId, dimension);
        generateWildBeetroots(world, random, chunkX, chunkZ, biome, biomeId, dimension);
        generateWildRice(world, random, chunkX, chunkZ, biome, biomeId, dimension);
        if (dimension != 0) {
            return;
        }
        generateMushroomColonies(world, random, chunkX, chunkZ, biome, true);
        generateMushroomColonies(world, random, chunkX, chunkZ, biome, false);
        if (Loader.isModLoaded("villagenames")) {
            VillageNamesVillageCompat.replaceVillageFarmCrops(world, random, chunkX, chunkZ);
        } else {
            generateVillageFarmCrops(world, random, chunkX, chunkZ, chunkGenerator);
        }
    }

    private void generateVillageFarmCrops(World world, Random random, int chunkX, int chunkZ, IChunkGenerator chunkGenerator) {
        if (!Configuration.generateVillageFarmFDCrops || chunkGenerator == null) {
            return;
        }

        Set<BlockPos> visitedPositions = new HashSet<>();
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                replaceVillageFarmColumn(world, startX + localX, startZ + localZ, chunkGenerator, visitedPositions);
            }
        }
    }

    private void replaceVillageFarmColumn(World world, int x, int z, IChunkGenerator chunkGenerator, Set<BlockPos> visitedPositions) {
        int topY = Math.min(255, world.getHeight(x, z));
        for (int y = 48; y <= topY; y++) {
            BlockPos cropPos = new BlockPos(x, y, z);
            if (visitedPositions.contains(cropPos)) {
                continue;
            }

            IBlockState cropState = world.getBlockState(cropPos);
            if (!isVillageFarmTarget(world, cropPos, cropState, chunkGenerator) || !hasVillageFarmNeighbors(world, cropPos)) {
                continue;
            }

            List<BlockPos> cropBlockPositions = collectVillageFarmCropBlock(world, cropPos, chunkGenerator, visitedPositions);
            if (cropBlockPositions.size() < 4) {
                continue;
            }

            VillageCropRules.replace(world, cropBlockPositions);
        }
    }

    private boolean isVillageFarmTarget(World world, BlockPos cropPos, IBlockState cropState, IChunkGenerator chunkGenerator) {
        IBlockState farmlandState = world.getBlockState(cropPos.down());
        return farmlandState.getBlock() == Blocks.FARMLAND
                && isVillageFarmCrop(cropState)
                && chunkGenerator.isInsideStructure(world, "Village", cropPos);
    }

    private List<BlockPos> collectVillageFarmCropBlock(World world, BlockPos origin, IChunkGenerator chunkGenerator, Set<BlockPos> visitedPositions) {
        List<BlockPos> cropBlockPositions = new ArrayList<>();
        Queue<BlockPos> pendingPositions = new ArrayDeque<>();
        visitedPositions.add(origin);
        pendingPositions.add(origin);

        while (!pendingPositions.isEmpty() && cropBlockPositions.size() < 128) {
            BlockPos currentPos = pendingPositions.remove();
            IBlockState currentState = world.getBlockState(currentPos);
            if (!isVillageFarmTarget(world, currentPos, currentState, chunkGenerator)) {
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


    private boolean isVillageFarmCrop(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.WHEAT
                || block == Blocks.CARROTS
                || block == Blocks.POTATOES
                || block == Blocks.BEETROOTS
                || block == ModBlocks.CABBAGES
                || block == ModBlocks.ONIONS
                || block == ModBlocks.BUDDING_TOMATOES;
    }

    private boolean hasVillageFarmNeighbors(World world, BlockPos cropPos) {
        int farmCropCount = 0;
        for (int offsetX = -2; offsetX <= 2; offsetX++) {
            for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                BlockPos nearbyPos = cropPos.add(offsetX, 0, offsetZ);
                if (world.getBlockState(nearbyPos.down()).getBlock() == Blocks.FARMLAND && isVillageFarmCrop(world.getBlockState(nearbyPos))) {
                    farmCropCount++;
                }
            }
        }
        return farmCropCount >= 4;
    }


    private void generateWildCabbages(World world, Random random, int chunkX, int chunkZ, Biome biome, ResourceLocation biomeId, int dimension) {
        if (!canGenerateWildCrop(Configuration.wildCabbagesGeneration, biomeId, dimension)) {
            return;
        }
        if (!Configuration.wildCabbagesGeneration.usesBiomeWhitelist() && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH)) {
            return;
        }
        generatePatch(world, random, chunkX, chunkZ, Configuration.wildCabbagesGeneration.getChance(), 64, 6, 3,
                this::placeSandyShrubFloor,
                this::placeWildCabbages,
                this::placeSandyShrub);
    }

    private void generateWildOnions(World world, Random random, int chunkX, int chunkZ, Biome biome, ResourceLocation biomeId, int dimension) {
        if (!canGenerateWildCrop(Configuration.wildOnionsGeneration, biomeId, dimension)) {
            return;
        }
        if (!Configuration.wildOnionsGeneration.usesBiomeWhitelist() && BiomeDictionary.hasType(biome, BiomeDictionary.Type.MUSHROOM)) {
            return;
        }
        if (!Configuration.wildOnionsGeneration.usesBiomeWhitelist()) {
            float temperature = biome.getDefaultTemperature();
            if (temperature < 0.4F || temperature > 0.9F) {
                return;
            }
        }
        generatePatch(world, random, chunkX, chunkZ, Configuration.wildOnionsGeneration.getChance(), 64, 6, 3,
                null,
                this::placeWildOnions,
                this::placeAllium);
    }

    private void generateWildTomatoes(World world, Random random, int chunkX, int chunkZ, Biome biome, ResourceLocation biomeId, int dimension) {
        if (!canGenerateWildCrop(Configuration.wildTomatoesGeneration, biomeId, dimension)) {
            return;
        }
        if (!Configuration.wildTomatoesGeneration.usesBiomeWhitelist() && (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET))) {
            return;
        }
        generatePatch(world, random, chunkX, chunkZ, Configuration.wildTomatoesGeneration.getChance(), 64, 6, 3,
                null,
                this::placeWildTomatoes,
                this::placeDeadBush);
    }

    private void generateWildCarrots(World world, Random random, int chunkX, int chunkZ, Biome biome, ResourceLocation biomeId, int dimension) {
        if (!canGenerateWildCrop(Configuration.wildCarrotsGeneration, biomeId, dimension)) {
            return;
        }
        if (!Configuration.wildCarrotsGeneration.usesBiomeWhitelist() && BiomeDictionary.hasType(biome, BiomeDictionary.Type.MUSHROOM)) {
            return;
        }
        if (!Configuration.wildCarrotsGeneration.usesBiomeWhitelist()) {
            float temperature = biome.getDefaultTemperature();
            if (temperature < 0.4F || temperature > 0.9F) {
                return;
            }
        }
        generatePatch(world, random, chunkX, chunkZ, Configuration.wildCarrotsGeneration.getChance(), 64, 6, 3,
                this::placeCoarseDirtFloor,
                this::placeWildCarrots,
                this::placeGrass);
    }

    private void generateWildPotatoes(World world, Random random, int chunkX, int chunkZ, Biome biome, ResourceLocation biomeId, int dimension) {
        if (!canGenerateWildCrop(Configuration.wildPotatoesGeneration, biomeId, dimension)) {
            return;
        }
        if (!Configuration.wildPotatoesGeneration.usesBiomeWhitelist()) {
            float temperature = biome.getDefaultTemperature();
            if (temperature < 0.1F || temperature > 0.3F) {
                return;
            }
        }
        generatePatch(world, random, chunkX, chunkZ, Configuration.wildPotatoesGeneration.getChance(), 64, 6, 3,
                null,
                this::placeWildPotatoes,
                this::placeFern);
    }

    private void generateWildBeetroots(World world, Random random, int chunkX, int chunkZ, Biome biome, ResourceLocation biomeId, int dimension) {
        if (!canGenerateWildCrop(Configuration.wildBeetrootsGeneration, biomeId, dimension)) {
            return;
        }
        if (!Configuration.wildBeetrootsGeneration.usesBiomeWhitelist() && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH)) {
            return;
        }
        generatePatch(world, random, chunkX, chunkZ, Configuration.wildBeetrootsGeneration.getChance(), 64, 6, 3,
                this::placeSandyShrubFloor,
                this::placeWildBeetroots,
                this::placeSandyShrub);
    }

    private void generateWildRice(World world, Random random, int chunkX, int chunkZ, Biome biome, ResourceLocation biomeId, int dimension) {
        if (!canGenerateWildCrop(Configuration.wildRiceGeneration, biomeId, dimension)) {
            return;
        }
        if (!Configuration.wildRiceGeneration.usesBiomeWhitelist() && !BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET)) {
            return;
        }
        int wildRiceChance = Configuration.wildRiceGeneration.getChance();
        if (wildRiceChance <= 0 || random.nextInt(wildRiceChance) != 0) {
            return;
        }

        boolean ffd = FluidloggingCompat.usesFutureDelightWaterlogging(ModBlocks.WILD_RICE.getDefaultState());
        if (ffd || Loader.isModLoaded("fluidlogged_api")) {
            generateWildRiceInWater(world, random, chunkX, chunkZ, ffd);
            return;
        }

        generateWildRiceWithoutFluidlogged(world, random, chunkX, chunkZ);
    }

    private boolean canGenerateWildCrop(Configuration.WildCropGenerationSettings settings, ResourceLocation biomeId, int dimension) {
        return settings.canGenerateInDimension(dimension) && settings.canGenerateInBiome(biomeId);
    }


    private void generateWildRiceInWater(World world, Random random, int chunkX, int chunkZ, boolean ffd) {
        BlockPos origin = randomSurfaceOrigin(world, random, chunkX, chunkZ);
        for (int i = 0; i < 96; i++) {
            BlockPos pos = origin.add(randomOffset(random, 8), randomOffset(random, 4), randomOffset(random, 8));
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() != Blocks.WATER || state.getValue(BlockLiquid.LEVEL) != 0 || !world.isAirBlock(pos.up())) {
                continue;
            }
            if (!ModBlocks.WILD_RICE.canPlaceBlockAt(world, pos)) {
                continue;
            }
            if (ffd) {
                FluidloggingCompat.placeBlock(world, pos, ModBlocks.WILD_RICE.getDefaultState(), 2);
            } else {
                BlockFluidloggedWildRice.placeInWorld(world, pos);
            }
        }
    }

    private void generateWildRiceWithoutFluidlogged(World world, Random random, int chunkX, int chunkZ) {
        BlockPos origin = randomSurfaceOrigin(world, random, chunkX, chunkZ);
        for (int i = 0; i < 20; i++) {
            BlockPos pos = origin.add(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
            if (!world.isAirBlock(pos) || !world.isAirBlock(pos.up())) {
                continue;
            }
            if (!FluidloggingCompat.hasAdjacentWater(world, pos.down()) || !ModBlocks.WILD_RICE.canPlaceBlockAt(world, pos)) {
                continue;
            }

            world.setBlockState(pos, ModBlocks.WILD_RICE.getDefaultState().withProperty(BlockWildRice.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), 2);
            world.setBlockState(pos.up(), ModBlocks.WILD_RICE.getDefaultState().withProperty(BlockWildRice.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 2);
        }
    }

    private void generateMushroomColonies(World world, Random random, int chunkX, int chunkZ, Biome biome, boolean brown) {
        if (!BiomeDictionary.hasType(biome, BiomeDictionary.Type.MUSHROOM)) {
            return;
        }

        if ((brown && !Configuration.generateBrownMushroomColonies) || (!brown && !Configuration.generateRedMushroomColonies)) {
            return;
        }

        int rarity = brown ? Configuration.chanceBrownMushroomColonies : Configuration.chanceRedMushroomColonies;
        if (rarity <= 0 || random.nextInt(rarity) != 0) {
            return;
        }

        Block primaryBlock = brown ? ModBlocks.BROWN_MUSHROOM_COLONY : ModBlocks.RED_MUSHROOM_COLONY;
        Block secondaryBlock = brown ? Blocks.BROWN_MUSHROOM : Blocks.RED_MUSHROOM;
        BlockPos origin = randomSurfaceOrigin(world, random, chunkX, chunkZ);

        int spread = 7;
        for (int i = 0; i < 64; i++) {
            BlockPos pos = origin.add(randomOffset(random, spread - 2), randomOffset(random, 4), randomOffset(random, spread - 2));
            if (!world.isAirBlock(pos) || world.getBlockState(pos.down()).getBlock() != Blocks.MYCELIUM) {
                continue;
            }
            IBlockState state = primaryBlock.getDefaultState().withProperty(BlockMushroomColony.AGE, random.nextInt(4));
            if (primaryBlock.canPlaceBlockAt(world, pos)) {
                world.setBlockState(pos, state, 2);
            }
        }

        for (int i = 0; i < 64; i++) {
            BlockPos pos = origin.add(randomOffset(random, spread), randomOffset(random, 4), randomOffset(random, spread));
            if (!world.isAirBlock(pos) || world.getBlockState(pos.down()).getBlock() != Blocks.MYCELIUM) {
                continue;
            }
            if (secondaryBlock.canPlaceBlockAt(world, pos)) {
                world.setBlockState(pos, secondaryBlock.getDefaultState(), 2);
            }
        }
    }

    private void generatePatch(World world, Random random, int chunkX, int chunkZ,
                               int rarity, int tries, int xzSpread, int ySpread,
                               PatchPlacer floorPlacer,
                               PatchPlacer primaryPlacer,
                               PatchPlacer secondaryPlacer) {
        if (rarity <= 0 || random.nextInt(rarity) != 0) {
            return;
        }

        BlockPos origin = randomSurfaceOrigin(world, random, chunkX, chunkZ);
        int spread = xzSpread + 1;

        if (floorPlacer != null) {
            for (int i = 0; i < tries; i++) {
                BlockPos pos = origin.add(randomOffset(random, spread), randomOffset(random, ySpread + 1), randomOffset(random, spread));
                floorPlacer.place(world, random, pos);
            }
        }

        int primarySpread = Math.max(1, spread - 2);
        for (int i = 0; i < tries; i++) {
            BlockPos pos = origin.add(randomOffset(random, primarySpread), randomOffset(random, ySpread + 1), randomOffset(random, primarySpread));
            primaryPlacer.place(world, random, pos);
        }

        for (int i = 0; i < tries; i++) {
            BlockPos pos = origin.add(randomOffset(random, spread), randomOffset(random, ySpread + 1), randomOffset(random, spread));
            secondaryPlacer.place(world, random, pos);
        }
    }

    private BlockPos randomSurfaceOrigin(World world, Random random, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + random.nextInt(16) + 8;
        int z = (chunkZ << 4) + random.nextInt(16) + 8;
        return world.getHeight(new BlockPos(x, 0, z));
    }

    private int randomOffset(Random random, int spread) {
        return random.nextInt(spread) - random.nextInt(spread);
    }

    private boolean placeWildCabbages(World world, Random random, BlockPos pos) {
        return placeWildCrop(world, pos, ModBlocks.WILD_CABBAGES, this::isSandLike);
    }

    private boolean placeWildOnions(World world, Random random, BlockPos pos) {
        return placeWildCrop(world, pos, ModBlocks.WILD_ONIONS, this::isDirtLike);
    }

    private boolean placeWildTomatoes(World world, Random random, BlockPos pos) {
        return placeWildCrop(world, pos, ModBlocks.WILD_TOMATOES, this::isTomatoSoil);
    }

    private boolean placeWildCarrots(World world, Random random, BlockPos pos) {
        return placeWildCrop(world, pos, ModBlocks.WILD_CARROTS, this::isDirtLike);
    }

    private boolean placeWildPotatoes(World world, Random random, BlockPos pos) {
        return placeWildCrop(world, pos, ModBlocks.WILD_POTATOES, this::isDirtLike);
    }

    private boolean placeWildBeetroots(World world, Random random, BlockPos pos) {
        return placeWildCrop(world, pos, ModBlocks.WILD_BEETROOTS, this::isSandLike);
    }

    private boolean placeSandyShrub(World world, Random random, BlockPos pos) {
        return placeBlock(world, pos, ModBlocks.SANDY_SHRUB.getDefaultState(), this::isSandLike);
    }

    private boolean placeAllium(World world, Random random, BlockPos pos) {
        IBlockState allium = Blocks.RED_FLOWER.getStateFromMeta(BlockFlower.EnumFlowerType.ALLIUM.getMeta());
        return placeBlock(world, pos, allium, this::isDirtLike);
    }

    private boolean placeDeadBush(World world, Random random, BlockPos pos) {
        return placeBlock(world, pos, Blocks.DEADBUSH.getDefaultState(), this::isTomatoSoil);
    }

    private boolean placeGrass(World world, Random random, BlockPos pos) {
        IBlockState grass = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
        return placeBlock(world, pos, grass, this::isDirtLike);
    }

    private boolean placeFern(World world, Random random, BlockPos pos) {
        IBlockState fern = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.FERN);
        return placeBlock(world, pos, fern, this::isDirtLike);
    }

    private boolean placeSandyShrubFloor(World world, Random random, BlockPos pos) {
        return false;
    }

    private boolean placeCoarseDirtFloor(World world, Random random, BlockPos pos) {
        if (!world.getBlockState(pos).getMaterial().isReplaceable()) {
            return false;
        }
        BlockPos floorPos = pos.down();
        IBlockState floorState = world.getBlockState(floorPos);
        if (!isDirtLike(floorState)) {
            return false;
        }

        IBlockState coarseDirt = Blocks.DIRT.getDefaultState().withProperty(net.minecraft.block.BlockDirt.VARIANT, net.minecraft.block.BlockDirt.DirtType.COARSE_DIRT);
        world.setBlockState(floorPos, coarseDirt, 2);
        return true;
    }

    private boolean placeWildCrop(World world, BlockPos pos, Block block, SoilPredicate soilPredicate) {
        return placeBlock(world, pos, block.getDefaultState(), soilPredicate);
    }

    private boolean placeBlock(World world, BlockPos pos, IBlockState state, SoilPredicate soilPredicate) {
        if (!world.isAirBlock(pos)) {
            return false;
        }
        IBlockState soil = world.getBlockState(pos.down());
        if (!soilPredicate.matches(soil)) {
            return false;
        }
        Block block = state.getBlock();
        if (!block.canPlaceBlockAt(world, pos)) {
            return false;
        }
        world.setBlockState(pos, state, 2);
        return true;
    }

    private boolean isDirtLike(IBlockState state) {
        return BlockCategories.contains("fdDirt", state);
    }

    private boolean isSandLike(IBlockState state) {
        return BlockCategories.contains("fdSand", state);
    }

    private boolean isTomatoSoil(IBlockState state) {
        return BlockCategories.contains("fdTerrain", state);
    }

    private interface PatchPlacer {
        boolean place(World world, Random random, BlockPos pos);
    }

    private interface SoilPredicate {
        boolean matches(IBlockState state);
    }

}

