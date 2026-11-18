package com.wdcftgg.farmersdelightlegacy.common.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

final class FoodShape {
    private static final AxisAlignedBB[] TRAY = {
            box(1, 0, 1, 15, 1, 15), box(1, 1, 1, 2, 2, 15), box(14, 1, 1, 15, 2, 15),
            box(2, 1, 1, 14, 2, 2), box(2, 1, 14, 14, 2, 15)
    };
    private final AxisAlignedBB[] boxes;
    private final AxisAlignedBB bounds;

    FoodShape(AxisAlignedBB... boxes) {
        this.boxes = boxes.clone();
        AxisAlignedBB combined = boxes[0];
        for (int i = 1; i < boxes.length; i++) {
            combined = combined.union(boxes[i]);
        }
        bounds = combined;
    }

    static AxisAlignedBB box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new AxisAlignedBB(minX / 16.0D, minY / 16.0D, minZ / 16.0D, maxX / 16.0D, maxY / 16.0D, maxZ / 16.0D);
    }

    static FoodShape[][] plated(AxisAlignedBB[][] dishes) {
        FoodShape[][] shapes = new FoodShape[dishes.length + 1][4];
        shapes[0] = new FoodShape(TRAY).rotated();
        for (int i = 0; i < dishes.length; i++) {
            AxisAlignedBB[] boxes = Arrays.copyOf(TRAY, TRAY.length + dishes[i].length);
            System.arraycopy(dishes[i], 0, boxes, TRAY.length, dishes[i].length);
            shapes[i + 1] = new FoodShape(boxes).rotated();
        }
        return shapes;
    }

    FoodShape[] rotated() {
        FoodShape[] shapes = new FoodShape[4];
        FoodShape current = this;
        EnumFacing direction = EnumFacing.NORTH;
        for (int turn = 0; turn < 4; turn++) {
            shapes[direction.getHorizontalIndex()] = current;
            AxisAlignedBB[] next = new AxisAlignedBB[boxes.length];
            for (int i = 0; i < next.length; i++) {
                AxisAlignedBB box = current.boxes[i];
                next[i] = new AxisAlignedBB(1.0D - box.maxZ, box.minY, box.minX,
                        1.0D - box.minZ, box.maxY, box.maxX);
            }
            current = new FoodShape(next);
            direction = direction.rotateY();
        }
        return shapes;
    }

    AxisAlignedBB getBounds() {
        return bounds;
    }

    void addCollisionBoxes(BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> result) {
        for (AxisAlignedBB box : boxes) {
            AxisAlignedBB offset = box.offset(pos);
            if (entityBox.intersects(offset)) {
                result.add(offset);
            }
        }
    }

    @Nullable
    RayTraceResult rayTrace(BlockPos pos, Vec3d start, Vec3d end) {
        Vec3d localStart = start.subtract(pos.getX(), pos.getY(), pos.getZ());
        Vec3d localEnd = end.subtract(pos.getX(), pos.getY(), pos.getZ());
        RayTraceResult nearest = null;
        for (AxisAlignedBB box : boxes) {
            RayTraceResult hit = box.calculateIntercept(localStart, localEnd);
            if (hit != null && (nearest == null
                    || localStart.squareDistanceTo(hit.hitVec) < localStart.squareDistanceTo(nearest.hitVec))) {
                nearest = hit;
            }
        }
        return nearest == null ? null : new RayTraceResult(nearest.hitVec.add(pos.getX(), pos.getY(), pos.getZ()), nearest.sideHit, pos);
    }
}
