package com.wdcftgg.farmersdelightlegacy.common.block;

import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import com.wdcftgg.farmersdelightlegacy.common.tile.TileEntityFeast;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.List;

public class BlockFeast extends Block implements ITileEntityProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyInteger SERVINGS = PropertyInteger.create("servings", 0, 4);

    private static final FoodShape[] FEAST_SHAPES = {
            new FoodShape(FoodShape.box(2, 0, 2, 14, 2, 14)),
            new FoodShape(FoodShape.box(2, 0, 2, 14, 2, 14)),
            new FoodShape(FoodShape.box(2, 0, 2, 14, 4, 14)),
            new FoodShape(FoodShape.box(2, 0, 2, 14, 6, 14)),
            new FoodShape(FoodShape.box(2, 0, 2, 14, 8, 14))
    };

    private final int maxServings;
    private final ResourceLocation servingItemId;
    private final boolean hasLeftovers;
    @Nullable
    public final ResourceLocation requiredContainerId;

    public BlockFeast(int maxServings, String servingItemPath, @Nullable String requiredContainerPath) {
        this(maxServings, servingItemPath, requiredContainerPath, true);
    }

    public BlockFeast(int maxServings, String servingItemPath, @Nullable String requiredContainerPath, boolean hasLeftovers) {
        super(Material.CAKE);
        this.maxServings = maxServings;
        this.servingItemId = new ResourceLocation("farmersdelight", servingItemPath);
        this.hasLeftovers = hasLeftovers;
        this.requiredContainerId = requiredContainerPath == null ? null : new ResourceLocation(requiredContainerPath);

        this.setHardness(0.8F);
        this.setResistance(1.0F);
        this.setSoundType(SoundType.WOOD);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(getServingsProperty(), this.maxServings));
    }

    public PropertyInteger getServingsProperty() {
        return SERVINGS;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityFeast();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY,
                                            float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityFeast) {
            TileEntityFeast feastTileEntity = (TileEntityFeast) tileEntity;
            feastTileEntity.initializeFromBlockDefault(this.maxServings);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) {
            return true;
        }

        int servings = getServings(worldIn, pos);
        if (servings <= 0) {
            if (!worldIn.isRemote) {
                worldIn.playSound(null, pos, SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.PLAYERS, 0.8F, 0.8F);
                worldIn.destroyBlock(pos, true);
            }
            return true;
        }

        ItemStack result = getServingStackForServings(servings);
        if (result.isEmpty()) {
            return false;
        }
        ItemStack heldStack = playerIn.getHeldItem(hand);
        ItemStack requiredServingItem = getRequiredServingItem(worldIn, pos, state, playerIn, hand, result, servings);

        if (requiredServingItem != null && !requiredServingItem.isEmpty()) {
            if (heldStack.isEmpty() || heldStack.getItem() != requiredServingItem.getItem()) {
                if (!worldIn.isRemote) {
                    playerIn.sendStatusMessage(new TextComponentTranslation("farmersdelight.block.feast.use_container", requiredServingItem.getDisplayName()), true);
                }
                return true;
            }
        }

        if (!worldIn.isRemote) {
            if (!heldStack.isEmpty() && StatList.getObjectUseStats(heldStack.getItem()) != null) {
                playerIn.addStat(StatList.getObjectUseStats(heldStack.getItem()));
            }
            if (requiredServingItem != null && !requiredServingItem.isEmpty() && !playerIn.capabilities.isCreativeMode) {
                heldStack.shrink(1);
            }
            if (!playerIn.addItemStackToInventory(result)) {
                playerIn.dropItem(result, false);
            }
            int newServings = clampServings(servings - 1);
            setServings(worldIn, pos, state, newServings);
            if (newServings == 0 && !this.hasLeftovers) {
                worldIn.setBlockToAir(pos);
            }
            worldIn.playSound(null, pos, ModSounds.foodTakePortion, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (hasServingParticles() && worldIn instanceof WorldServer) {
                ((WorldServer) worldIn).spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        3, 0.1D, 0.1D, 0.1D, 0.001D, Block.getStateId(state));
            }
        }

        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (fromPos.getY() == pos.getY() - 1 && !worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP)) {
            worldIn.destroyBlock(pos, true);
            return;
        }
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int facingMeta = meta & 3;
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(facingMeta))
                .withProperty(getServingsProperty(), this.maxServings);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int servings = state.getValue(getServingsProperty());
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityFeast) {
            servings = clampServings(((TileEntityFeast) tileEntity).getServings());
        }

        return state.withProperty(getServingsProperty(), servings);
    }

    protected ItemStack getServingStackForServings(int servings) {
        Item servingItem = ForgeRegistries.ITEMS.getValue(this.servingItemId);
        if (servingItem == null || servings <= 0) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(servingItem);
    }

    public ItemStack getCookingPotServingStack() {
        ItemStack servingStack = getServingStackForServings(this.maxServings);
        if (servingStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        servingStack.setCount(1);
        return servingStack;
    }

    @Nullable
    protected ItemStack getRequiredServingItem(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                               EnumHand hand, ItemStack servingResult, int servings) {
        if (servingResult.getItem().hasContainerItem(servingResult)) {
            return servingResult.getItem().getContainerItem(servingResult);
        }
        if (this.requiredContainerId != null) {
            Item fallbackContainer = ForgeRegistries.ITEMS.getValue(this.requiredContainerId);
            if (fallbackContainer != null) {
                return new ItemStack(fallbackContainer);
            }
        }
        return null;
    }

    public int getMaxServings() {
        return this.maxServings;
    }

    public int clampServings(int servings) {
        return Math.max(0, Math.min(this.maxServings, servings));
    }

    private int getServings(World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityFeast) {
            TileEntityFeast feastTileEntity = (TileEntityFeast) tileEntity;
            feastTileEntity.initializeFromBlockDefault(this.maxServings);
            return clampServings(feastTileEntity.getServings());
        }
        return this.maxServings;
    }

    private void setServings(World worldIn, BlockPos pos, IBlockState state, int servings) {
        int clampedServings = clampServings(servings);
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityFeast) {
            ((TileEntityFeast) tileEntity).setServings(clampedServings);
        }
        IBlockState updatedState = state.withProperty(getServingsProperty(), clampedServings);
        worldIn.setBlockState(pos, updatedState, 3);
        worldIn.notifyBlockUpdate(pos, state, updatedState, 3);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, new IProperty[]{FACING, getServingsProperty()});
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return getShape(getActualState(state, source, pos)).getBounds();
    }

    protected FoodShape getShape(IBlockState state) {
        return FEAST_SHAPES[Math.min(4, state.getValue(getServingsProperty()))];
    }

    protected boolean hasServingParticles() {
        return true;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> boxes, @Nullable Entity entity, boolean actualState) {
        getShape(actualState ? state : getActualState(state, world, pos)).addCollisionBoxes(pos, entityBox, boxes);
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        return getShape(getActualState(state, world, pos)).rayTrace(pos, start, end);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        return getServings(worldIn, pos);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int servings = clampServings(state.getValue(getServingsProperty()));
        if (servings == this.maxServings) {
            servings = getStoredServings(world, pos);
        }
        if (servings == this.maxServings) {
            Item selfItem = Item.getItemFromBlock(this);
            if (selfItem != Items.AIR) {
                drops.add(new ItemStack(selfItem));
            }
            return;
        }

        if (this.hasLeftovers) {
            drops.add(new ItemStack(Items.BOWL));
        }
        appendPartialServingsDrops(drops);
    }

    protected void appendPartialServingsDrops(NonNullList<ItemStack> drops) {
    }

    private int getStoredServings(IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityFeast) {
            TileEntityFeast feastTileEntity = (TileEntityFeast) tileEntity;
            feastTileEntity.initializeFromBlockDefault(this.maxServings);
            return clampServings(feastTileEntity.getServings());
        }
        return this.maxServings;
    }
}
