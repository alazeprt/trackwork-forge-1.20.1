package com.example.trackwork.blocks;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public interface ITrackPointProvider {
    float getPointDownwardOffset(float var1);

    boolean isBeltLarge();

    float getPointHorizontalOffset();

    Vec3 getTrackPointSlope(float var1);

    @NotNull
    ITrackPointProvider.PointType getTrackPointType();

    @NotNull
    ITrackPointProvider.PointType getNextPoint();

    float getWheelRadius();

    public static enum PointType {
        WRAP,
        GROUND,
        BLANK,
        NONE;
    }
}
