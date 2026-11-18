package com.wdcftgg.farmersdelightlegacy.common.tile;

import com.wdcftgg.farmersdelightlegacy.FarmersDelightLegacy;
import com.wdcftgg.farmersdelightlegacy.common.Configuration;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasHangingSign;
import com.wdcftgg.farmersdelightlegacy.common.block.sign.BlockCanvasWallHangingSign;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;

import java.util.Arrays;

public class TileEntityCanvasSign extends TileEntitySign implements ITickable {
    private final ITextComponent[] backText = emptyText();
    private EnumDyeColor frontColor;
    private EnumDyeColor backColor;
    private boolean frontGlowing;
    private boolean backGlowing;
    private boolean waxed;
    private EntityPlayer editor;
    private boolean editingFront;

    @Override
    public boolean canRenderBreaking() {
        return true;
    }

    public ITextComponent[] getText(boolean front) {
        return Arrays.stream(front ? signText : backText).map(ITextComponent::createCopy).toArray(ITextComponent[]::new);
    }

    public EnumDyeColor getColor(boolean front) {
        EnumDyeColor color = front ? frontColor : backColor;
        if (color != null) {
            return color;
        }
        Block block = world == null ? null : getBlockType();
        if ((front || isHanging()) && block != null && block.getRegistryName() != null) {
            String path = block.getRegistryName().getPath();
            for (String suffix : new String[]{"_wall_hanging_canvas_sign", "_hanging_canvas_sign", "_canvas_wall_sign", "_canvas_sign"}) {
                if (path.endsWith(suffix) && Configuration.isCanvasSignDarkBackground(path.substring(0, path.length() - suffix.length()))) {
                    return EnumDyeColor.WHITE;
                }
            }
        }
        return EnumDyeColor.BLACK;
    }

    public boolean setColor(boolean front, EnumDyeColor color) {
        if (waxed || getColor(front) == color) {
            return false;
        }
        if (front) {
            frontColor = color;
        } else {
            backColor = color;
        }
        markDirty();
        return true;
    }

    public boolean isGlowing(boolean front) {
        return front ? frontGlowing : backGlowing;
    }

    public boolean setGlowing(boolean front, boolean glowing) {
        if (waxed || isGlowing(front) == glowing) {
            return false;
        }
        if (front) {
            frontGlowing = glowing;
        } else {
            backGlowing = glowing;
        }
        markDirty();
        return true;
    }

    public boolean isWaxed() {
        return waxed;
    }

    public boolean wax() {
        if (waxed) {
            return false;
        }
        waxed = true;
        editor = null;
        markDirty();
        return true;
    }

    public boolean hasText(boolean front) {
        for (ITextComponent line : front ? signText : backText) {
            if (!line.getUnformattedText().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isHanging() {
        return world != null && (getBlockType() instanceof BlockCanvasHangingSign || getBlockType() instanceof BlockCanvasWallHangingSign);
    }

    public float getTextRotation(IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockStandingSign) {
            return -state.getValue(BlockStandingSign.ROTATION) * 22.5F;
        }
        if (block instanceof BlockCanvasHangingSign) {
            return -state.getValue(BlockCanvasHangingSign.ROTATION) * 22.5F;
        }
        if (block instanceof BlockCanvasWallHangingSign) {
            return -state.getValue(BlockCanvasWallHangingSign.FACING).getOpposite().getHorizontalAngle();
        }
        return -state.getValue(BlockWallSign.FACING).getHorizontalAngle();
    }

    public boolean isFrontSide(EntityPlayer player) {
        double radians = Math.toRadians(getTextRotation(world.getBlockState(pos)));
        return (player.posX - pos.getX() - 0.5D) * Math.sin(radians)
                + (player.posZ - pos.getZ() - 0.5D) * Math.cos(radians) >= 0.0D;
    }

    public boolean hasEditor() {
        clearDistantEditor();
        return editor != null;
    }

    public boolean beginEdit(EntityPlayer player, boolean front) {
        clearDistantEditor();
        if (waxed || editor != null && editor != player || player.isSpectator() || !player.capabilities.allowEdit) {
            return false;
        }
        for (ITextComponent line : front ? signText : backText) {
            if (!(line instanceof TextComponentString) || line.getStyle().getClickEvent() != null) {
                return false;
            }
        }
        editor = player;
        editingFront = front;
        return true;
    }

    public boolean applyText(EntityPlayer player, boolean front, String[] lines) {
        clearDistantEditor();
        if (waxed || editor != player || editingFront != front || lines.length != 4 || !Configuration.enableCanvasSignEditing
                || player.isSpectator() || !player.capabilities.allowEdit) {
            return false;
        }
        ITextComponent[] text = front ? signText : backText;
        for (int index = 0; index < text.length; index++) {
            String line = ChatAllowedCharacters.filterAllowedCharacters(TextFormatting.getTextWithoutFormattingCodes(lines[index]));
            text[index] = new TextComponentString(line.substring(0, Math.min(line.length(), 384)));
        }
        editor = null;
        markDirty();
        return true;
    }

    public boolean executeCommands(EntityPlayer player, boolean front) {
        if (!waxed) {
            return false;
        }
        ICommandSender sender = new ICommandSender() {
            @Override
            public String getName() {
                return player.getName();
            }

            @Override
            public ITextComponent getDisplayName() {
                return player.getDisplayName();
            }

            @Override
            public boolean canUseCommand(int permissionLevel, String command) {
                return permissionLevel <= 2;
            }

            @Override
            public BlockPos getPosition() {
                return pos;
            }

            @Override
            public Vec3d getPositionVector() {
                return new Vec3d(pos).add(0.5D, 0.5D, 0.5D);
            }

            @Override
            public World getEntityWorld() {
                return world;
            }

            @Override
            public Entity getCommandSenderEntity() {
                return player;
            }

            @Override
            public MinecraftServer getServer() {
                return world.getMinecraftServer();
            }

            @Override
            public void setCommandStat(CommandResultStats.Type type, int amount) {
                getStats().setCommandStatForSender(getServer(), this, type, amount);
            }
        };
        boolean executed = false;
        for (ITextComponent line : getText(front)) {
            ClickEvent click = line.getStyle().getClickEvent();
            if (click != null && click.getAction() == ClickEvent.Action.RUN_COMMAND) {
                sender.getServer().getCommandManager().executeCommand(sender, click.getValue());
                executed = true;
            }
        }
        return executed;
    }

    @Override
    public boolean getIsEditable() {
        return false;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            clearDistantEditor();
        }
    }

    private void clearDistantEditor() {
        if (editor != null && (editor.isDead || editor.world != world || editor.getDistanceSq(pos) > 64.0D)) {
            editor = null;
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos,
                                 IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagCompound back = new NBTTagCompound();
        for (int index = 0; index < backText.length; index++) {
            back.setString("Text" + (index + 1), ITextComponent.Serializer.componentToJson(backText[index]));
        }
        compound.setTag("BackText", back);
        compound.setString("FrontColor", getColor(true).getName());
        compound.setString("BackColor", getColor(false).getName());
        compound.setBoolean("FrontGlowing", frontGlowing);
        compound.setBoolean("BackGlowing", backGlowing);
        compound.setBoolean("Waxed", waxed);
        compound.removeTag("HangingTextOnBack");
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagCompound signData = compound.copy();
        migrateSignText(signData);
        for (int index = 1; index <= 4; index++) {
            if (!signData.hasKey("Text" + index, 8)) {
                signData.setString("Text" + index, ITextComponent.Serializer.componentToJson(new TextComponentString("")));
            }
        }
        super.readFromNBT(signData);
        NBTTagCompound back = signData.getCompoundTag("BackText");
        for (int index = 0; index < backText.length; index++) {
            String serialized = back.getString("Text" + (index + 1));
            if (serialized.isEmpty()) {
                backText[index] = new TextComponentString("");
            } else {
                try {
                    ITextComponent line = ITextComponent.Serializer.jsonToComponent(serialized);
                    backText[index] = line == null ? new TextComponentString("") : line;
                } catch (com.google.gson.JsonParseException exception) {
                    FarmersDelightLegacy.LOGGER.error("Invalid canvas sign text at {} on back line {}", pos, index + 1, exception);
                    backText[index] = new TextComponentString(serialized);
                }
            }
        }
        if (!signData.hasKey("BackText", 10) && compound.getBoolean("HangingTextOnBack")) {
            for (int index = 0; index < signText.length; index++) {
                backText[index] = signText[index];
                signText[index] = new TextComponentString("");
            }
        }
        frontColor = readColor(signData, "FrontColor");
        backColor = readColor(signData, "BackColor");
        if (frontColor == null) {
            frontColor = legacyTextColor(signText);
        }
        if (backColor == null) {
            backColor = legacyTextColor(backText);
        }
        frontGlowing = signData.getBoolean("FrontGlowing");
        backGlowing = signData.getBoolean("BackGlowing");
        waxed = signData.getBoolean("Waxed");
        editor = null;
    }

    private static void migrateSignText(NBTTagCompound compound) {
        NBTTagCompound data = compound.getCompoundTag("ForgeData");
        for (String[] keys : new String[][]{{"Waxed", "waxedText"}, {"FrontGlowing", "glowingText"}, {"BackGlowing", "glowingBackText"}}) {
            if (!compound.hasKey(keys[0]) && data.hasKey(keys[1])) {
                compound.setBoolean(keys[0], data.getBoolean(keys[1]));
            }
            data.removeTag(keys[1]);
        }
        NBTTagCompound back = new NBTTagCompound();
        for (int index = 1; index <= 4; index++) {
            String key = "backText" + index;
            if (data.hasKey(key, 8)) {
                back.setString("Text" + index, data.getString(key));
                data.removeTag(key);
            }
        }
        if (!compound.hasKey("BackText", 10) && !back.isEmpty()) {
            compound.setTag("BackText", back);
        }
    }

    private static EnumDyeColor legacyTextColor(ITextComponent[] text) {
        for (ITextComponent line : text) {
            TextFormatting color = line.getStyle().getColor();
            if (color == null) {
                continue;
            }
            switch (color) {
                case WHITE: return EnumDyeColor.WHITE;
                case GOLD: return EnumDyeColor.ORANGE;
                case AQUA: return EnumDyeColor.MAGENTA;
                case BLUE: return EnumDyeColor.LIGHT_BLUE;
                case YELLOW: return EnumDyeColor.YELLOW;
                case GREEN: return EnumDyeColor.LIME;
                case LIGHT_PURPLE: return EnumDyeColor.PINK;
                case DARK_GRAY: return EnumDyeColor.GRAY;
                case GRAY: return EnumDyeColor.SILVER;
                case DARK_AQUA: return EnumDyeColor.CYAN;
                case DARK_PURPLE: return EnumDyeColor.PURPLE;
                case DARK_BLUE: return EnumDyeColor.BLUE;
                case DARK_GREEN: return EnumDyeColor.GREEN;
                case DARK_RED: return EnumDyeColor.RED;
                case BLACK: return EnumDyeColor.BLACK;
                default: break;
            }
        }
        return null;
    }

    private EnumDyeColor readColor(NBTTagCompound compound, String key) {
        if (!compound.hasKey(key, 8)) {
            return null;
        }
        for (EnumDyeColor color : EnumDyeColor.values()) {
            if (color.getName().equals(compound.getString(key))) {
                return color;
            }
        }
        FarmersDelightLegacy.LOGGER.error("Invalid canvas sign color {} at {}", compound.getString(key), pos);
        return null;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    private static ITextComponent[] emptyText() {
        return new ITextComponent[]{new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")};
    }
}

