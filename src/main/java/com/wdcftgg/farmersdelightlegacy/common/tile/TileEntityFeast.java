package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelightlegacy.common.block.BlockFeast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityFeast extends TileEntity {

    private int servings = -1;

    public int getServings() {
        return this.servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
        markDirty();
    }

    public void initializeFromBlockDefault(int defaultServings) {
        if (this.servings < 0) {
            this.servings = defaultServings;
            markDirty();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        syncBlockStateFromTile();
    }

    private void syncBlockStateFromTile() {
        if (this.world == null || this.pos == null) {
            return;
        }

        IBlockState state = this.world.getBlockState(this.pos);
        if (!(state.getBlock() instanceof BlockFeast)) {
            return;
        }

        BlockFeast feast = (BlockFeast) state.getBlock();
        int clampedServings = feast.clampServings(this.servings < 0 ? feast.getMaxServings() : this.servings);
        if (this.servings != clampedServings) {
            this.servings = clampedServings;
            markDirty();
        }

        IBlockState updatedState = state.withProperty(feast.getServingsProperty(), clampedServings);
        if (state != updatedState) {
            this.world.setBlockState(this.pos, updatedState, 3);
            state = updatedState;
        }

        this.world.checkLight(this.pos);
        this.world.notifyBlockUpdate(this.pos, state, state, 3);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Servings", this.servings);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.servings = compound.hasKey("Servings", 3) ? compound.getInteger("Servings") : -1;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
        this.syncBlockStateFromTile();
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
        this.syncBlockStateFromTile();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null) {
            this.world.checkLight(this.pos);
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public boolean shouldRefresh(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}

