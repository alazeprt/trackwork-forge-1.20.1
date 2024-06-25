package com.example.trackwork.blocks;


import com.example.trackwork.rendering.TrackBeltRenderer;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public abstract class TrackBaseBlockEntity extends KineticBlockEntity implements ITrackPointProvider {
    private boolean detracked = false;
    protected Pair<Float, Float> nextPointVerticalOffset = new Pair(0.0F, 0.0F);
    protected float nextPointHorizontalOffset = 0.0F;
    @NotNull
    private ITrackPointProvider.PointType nextPoint = PointType.NONE;

    public TrackBaseBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public void tick() {
        if (this.level.getBlockEntity(this.getBlockPos().offset(TrackBeltRenderer.getAlong(this.getBlockState()).getNormal())) instanceof ITrackPointProvider track) {
            this.nextPointVerticalOffset = new Pair(track.getPointDownwardOffset(0.0F), track.getPointDownwardOffset(1.0F));
            this.nextPointHorizontalOffset = track.getPointHorizontalOffset();
            this.nextPoint = track.getTrackPointType();
        } else {
            this.nextPoint = PointType.NONE;
        }
    }

    @NotNull
    @Override
    public ITrackPointProvider.PointType getNextPoint() {
        return this.nextPoint;
    }

    public void throwTrack(boolean fixTrack) {
//        World world = this.world;
//        if (!world.isClient && this.detracked == fixTrack) {
//            this.detracked = !fixTrack;
//            this.speed = 0.0F;
//            BlockPos pos = this.getPos();
//
//            for (boolean forward : Iterate.trueAndFalse) {
//                BlockPos currentPos = this.nextTrackPosition(this.getCachedState(), pos, forward);
//                if (currentPos != null && world.getBlockEntity(currentPos) instanceof TrackBaseBlockEntity track_base_be) {
//                    track_base_be.throwTrack(fixTrack);
//                }
//            }
//
//            NetworkManager.sendToAllPlayerTrackingThisBlock(new ThrowTrackPacket(this.getBlockPos(), detracked), this);
//        }
    }

    @Nullable
    private BlockPos nextTrackPosition(BlockState state, BlockPos pos, boolean forward) {
        TrackBaseBlock.TrackPart part = state.getValue(TrackBaseBlock.PART);
        Direction next = Direction.get(Direction.AxisDirection.POSITIVE, around(state.getValue(RotatedPillarKineticBlock.AXIS)));
        int offset = forward ? 1 : -1;
        return (part != TrackBaseBlock.TrackPart.END || !forward) && (part != TrackBaseBlock.TrackPart.START || forward) ? pos.relative(next, offset) : null;
    }

    private static Axis around(Axis axis) {
        if (axis.isHorizontal()) {
            return axis;
        } else {
            return axis == Axis.X ? Axis.Z : Axis.X;
        }
    }

    protected static Vec3 getActionNormal(Axis axis) {
        return switch (axis) {
            case X -> new Vec3(0.0, -1.0, 0.0);
            case Y -> new Vec3(0.0, 0.0, 0.0);
            case Z -> new Vec3(0.0, -1.0, 0.0);
        };
    }

    protected static Vector3d getAxisAsVec(Axis axis) {
        return switch (axis) {
            case X -> new Vector3d(1.0, 0.0, 0.0);
            case Y -> new Vector3d(0.0, 1.0, 0.0);
            case Z -> new Vector3d(0.0, 0.0, 1.0);
        };
    }

    public static Vector3d getActionVec3(Axis axis, float length) {
        return switch (axis) {
            case X -> new Vector3d(0.0, 0.0, length);
            case Y -> new Vector3d(0.0, 0.0, 0.0);
            case Z -> new Vector3d(length, 0.0, 0.0);
        };
    }

    public boolean isDetracked() {
        return this.detracked;
    }

    public void write(CompoundTag compound, boolean clientPacket) {
        compound.putBoolean("Detracked", this.detracked);
        super.write(compound, clientPacket);
    }

    protected void read(CompoundTag compound, boolean clientPacket) {
        if (compound.getBoolean("Detracked")) {
            this.detracked = compound.getBoolean("Detracked");
        }

        super.read(compound, clientPacket);
    }

    /*public void handlePacket(ThrowTrackPacket p) {
        this.detracked = p.detracked;
        if (this.detracked) {
            this.speed = 0.0F;
        }
    }*/
}
