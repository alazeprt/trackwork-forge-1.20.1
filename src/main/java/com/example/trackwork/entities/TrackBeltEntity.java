package com.example.trackwork.entities;

import com.example.trackwork.TrackworkEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class TrackBeltEntity extends Entity {
    private static final EntityDataAccessor<BlockPos> PARENT = SynchedEntityData.defineId(TrackBeltEntity.class, EntityDataSerializers.BLOCK_POS);
    private boolean wow = false;
    private BlockPos parentPos;
    private int timeout = 0;

    public TrackBeltEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(PARENT, null);
    }

    public static TrackBeltEntity create(Level level, BlockPos pos) {
        TrackBeltEntity e = TrackworkEntities.BELT.create(level);
        e.parentPos = pos;
        return e;
    }

    public void tick() {
        super.tick();
        if (!this.wow && !this.level().isClientSide) {
            if (!this.wow) {
                this.entityData.set(PARENT, this.parentPos);
                this.wow = true;
            }
            this.timeout++;
            if (this.timeout > 60) {
                this.kill();
            }
        }
    }


    public BlockPos getParentPos() {
        return this.entityData.get(PARENT);
    }
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.getBoolean("ParentPos")) {
            this.parentPos = NbtUtils.readBlockPos(compound.getCompound("ParentPos"));
        }

        this.entityData.set(PARENT, this.parentPos);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("ParentPos", NbtUtils.writeBlockPos(this.parentPos));
    }
}