package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.block.BlockCabinet;
import com.wdcftgg.farmersdelightlegacy.common.inventory.ContainerCabinet;
import com.wdcftgg.farmersdelightlegacy.common.registry.ModSounds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class TileEntityCabinet extends TileEntityLockableLoot {

    private static final int SLOT_COUNT = 27;

    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int openerCount;

    @Override
    public int getSizeInventory() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        this.fillWithLoot(null);
        for (ItemStack stack : this.itemStacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (this.world == null || this.world.getTileEntity(this.pos) != this) {
            return false;
        }
        return player.getDistanceSq((double) this.pos.getX() + 0.5D,
                (double) this.pos.getY() + 0.5D,
                (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (player.isSpectator() || this.world == null || this.world.isRemote) {
            return;
        }

        if (this.openerCount < 0) {
            this.openerCount = 0;
        }

        this.fillWithLoot(player);
        this.openerCount++;
        if (this.openerCount == 1) {
            updateOpenState(true);
            playCabinetSound(ModSounds.cabinetOpen);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (player.isSpectator() || this.world == null || this.world.isRemote) {
            return;
        }

        if (this.openerCount > 0) {
            this.openerCount--;
        }

        if (this.openerCount <= 0) {
            this.openerCount = 0;
            updateOpenState(false);
            playCabinetSound(ModSounds.CABINET_CLOSE);
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.itemStacks;
    }

    @Override
    public String getGuiID() {
        return "minecraft:chest";
    }

    @Override
    public Container createContainer(InventoryPlayer inventory, EntityPlayer player) {
        this.fillWithLoot(player);
        return new ContainerCabinet(inventory, this);
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : FarmersDelightLegacy.MOD_ID + ".container.cabinet";
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.itemStacks);
        }

        if (this.hasCustomName()) {
            compound.setString("CustomName", this.customName);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        this.itemStacks.clear();
        this.lootTable = null;
        this.lootTableSeed = 0L;
        if (!this.checkLootAndRead(compound)) {
            ItemStackHelper.loadAllItems(compound, this.itemStacks);
        }
        this.customName = compound.hasKey("CustomName", 8) ? compound.getString("CustomName") : null;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tag = super.getUpdateTag();
        if (hasCustomName()) {
            tag.setString("CustomName", this.customName);
        }
        return tag;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.customName = tag.hasKey("CustomName", 8) ? tag.getString("CustomName") : null;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null && this.pos != null) {
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.updateComparatorOutputLevel(this.pos, this.getBlockType());
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    private void updateOpenState(boolean open) {
        if (this.world == null) {
            return;
        }

        IBlockState state = this.world.getBlockState(this.pos);
        if (state.getBlock() instanceof BlockCabinet && state.getValue(BlockCabinet.OPEN) != open) {
            this.world.setBlockState(this.pos, state.withProperty(BlockCabinet.OPEN, open), 3);
        }
    }

    private void playCabinetSound(net.minecraft.util.SoundEvent soundEvent) {
        if (this.world == null || soundEvent == null) {
            return;
        }

        IBlockState state = this.world.getBlockState(this.pos);
        EnumFacing facing = state.getBlock() instanceof BlockCabinet ? state.getValue(BlockCabinet.FACING) : EnumFacing.NORTH;
        Vec3i facingVector = facing.getDirectionVec();
        double x = this.pos.getX() + 0.5D + facingVector.getX() / 2.0D;
        double y = this.pos.getY() + 0.5D + facingVector.getY() / 2.0D;
        double z = this.pos.getZ() + 0.5D + facingVector.getZ() / 2.0D;
        this.world.playSound(null, x, y, z, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
    }
}

