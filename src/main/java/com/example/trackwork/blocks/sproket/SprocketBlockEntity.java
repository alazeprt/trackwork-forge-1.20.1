package com.example.trackwork.blocks.sproket;

import com.example.trackwork.TrackworkEntities;
import com.example.trackwork.blocks.ITrackPointProvider;
import com.example.trackwork.blocks.TrackBaseBlock;
import com.example.trackwork.blocks.TrackBaseBlockEntity;
import com.example.trackwork.data.PhysEntityTrackData;
import com.example.trackwork.entities.TrackBeltEntity;
import com.example.trackwork.entities.WheelEntity;
import com.example.trackwork.forces.PhysicsEntityTrackController;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.properties.ShipTransform;
import org.valkyrienskies.core.apigame.constraints.VSAttachmentConstraint;
import org.valkyrienskies.core.apigame.constraints.VSConstraintAndId;
import org.valkyrienskies.core.apigame.constraints.VSHingeOrientationConstraint;
import org.valkyrienskies.core.apigame.physics.PhysicsEntityData;
import org.valkyrienskies.core.impl.game.ships.ShipTransformImpl;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class SprocketBlockEntity extends TrackBaseBlockEntity implements ITrackPointProvider {
    private float wheelRadius;
    protected final Supplier<Ship> ship;
    private Integer trackID;
    private UUID wheelID;
    @NotNull
    private WeakReference<WheelEntity> wheel;
    public boolean assembled;
    public boolean assembleNextTick = true;

    public SprocketBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.assembled = false;
        this.wheelRadius = 0.5F;
        this.ship = () -> VSGameUtilsKt.getShipObjectManagingPos(this.getLevel(), pos);
        this.wheel = new WeakReference<>(null);
        this.setLazyTickRate(40);
    }

    public static SprocketBlockEntity large(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        SprocketBlockEntity be = new SprocketBlockEntity(type, pos, state);
        be.wheelRadius = 1.0F;
        return be;
    }

    public static SprocketBlockEntity med(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        SprocketBlockEntity be = new SprocketBlockEntity(type, pos, state);
        be.wheelRadius = 0.75F;
        return be;
    }

    public void destroy() {
        super.destroy();
        if (this.getLevel() != null && !this.getLevel().isClientSide && this.assembled) {
            ServerShip ship = (ServerShip)this.ship.get();
            if (ship != null) {
                PhysicsEntityTrackController controller = PhysicsEntityTrackController.getOrCreate(ship);
                controller.removeTrackBlock((ServerLevel)this.getLevel(), this.trackID);
                Objects.requireNonNull(this.wheel.get()).kill();
            }
        }
    }

    public void onLoad() {
        super.onLoad();
        if (!this.getLevel().isClientSide && this.assembled) {
            Entity e = ((ServerLevel)this.getLevel()).getEntity(this.wheelID);
            ServerShip ship = (ServerShip)this.ship.get();
            if (ship != null) {
                if (!(e instanceof WheelEntity wheel)) {
                    this.assemble();
                    return;
                }

                if (this.constrainWheel(ship, wheel.getShipId(), VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf(this.getBlockPos()))) != null) {
                    return;
                }

                this.wheel = new WeakReference<>(wheel);
            }

            this.assembled = false;
            this.assembleNextTick = true;
        }
    }

    @Deprecated
    public boolean summonBelt() {
        if (!this.getLevel().isClientSide) {
        TrackBeltEntity e = TrackBeltEntity.create(this.getLevel(), this.getBlockPos());
            e.setPos(Vec3.atLowerCornerOf(this.getBlockPos()));
            this.getLevel().addFreshEntity(e);
        }

        return true;
    }

    private void assemble() {
        if (this.getLevel() != null && !this.getLevel().isClientSide) {
            if (!TrackBaseBlock.isValidAxis(this.getBlockState().getValue(RotatedPillarKineticBlock.AXIS))) {
                return;
            }

            ServerLevel slevel = (ServerLevel)this.getLevel();
            ServerShip ship = (ServerShip)this.ship.get();
            if (ship != null) {
                PhysicsEntityTrackController controller = PhysicsEntityTrackController.getOrCreate(ship);
                if (this.assembled) {
                    controller.removeTrackBlock((ServerLevel)this.getLevel(), this.trackID);
                }

                this.assembled = true;
                Vector3dc trackLocalPos = VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf(this.getBlockPos()));
                WheelEntity wheel = TrackworkEntities.WHEEL.create(slevel);
                long wheelId = VSGameUtilsKt.getShipObjectWorld(slevel).allocateShipId(VSGameUtilsKt.getDimensionId(slevel));
                double wheelRadius = this.wheelRadius;
                Vector3dc wheelGlobalPos = ship.getTransform().getShipToWorld().transformPosition(trackLocalPos, new Vector3d());
                ShipTransform transform = ShipTransformImpl.Companion.create(wheelGlobalPos, new Vector3d());
                PhysicsEntityData wheelData = WheelEntity.DataBuilder.createBasicData(wheelId, transform, wheelRadius, 1000.0);
                wheel.setPhysicsEntityData(wheelData);
                wheel.setPos(VectorConversionsMCKt.toMinecraft(wheelGlobalPos));
                slevel.addFreshEntity(wheel);
                PhysEntityTrackData.CreateData createData = this.constrainWheel(ship, wheelId, trackLocalPos);
                this.trackID = controller.addTrackBlock(createData);
                this.wheelID = wheel.getUUID();
                this.wheel = new WeakReference<>(wheel);
                this.sendData();
            }
        }
    }

    private PhysEntityTrackData.CreateData constrainWheel(ServerShip ship, long wheelId, Vector3dc trackLocalPos) {
        ServerLevel slevel = (ServerLevel)this.getLevel();
        double attachCompliance = 1.0E-8;
        double attachMaxForce = 1.0E150;
        double hingeMaxForce = 1.0E75;
        Vector3dc axis = getAxisAsVec(this.getBlockState().getValue(RotatedPillarKineticBlock.AXIS));
        VSAttachmentConstraint slider = new VSAttachmentConstraint(
                ship.getId(), wheelId, attachCompliance, trackLocalPos, new Vector3d(0.0, 0.0, 0.0), attachMaxForce, 0.0
        );
        VSHingeOrientationConstraint axle = new VSHingeOrientationConstraint(
                ship.getId(),
                wheelId,
                attachCompliance,
                new Quaterniond().fromAxisAngleDeg(axis, 0.0),
                new Quaterniond().fromAxisAngleDeg(new Vector3d(0.0, 0.0, 1.0), 0.0),
                hingeMaxForce
        );
        Integer sliderId = VSGameUtilsKt.getShipObjectWorld(slevel).createNewConstraint(slider);
        Integer axleId = VSGameUtilsKt.getShipObjectWorld(slevel).createNewConstraint(axle);
        return sliderId != null && axleId != null
                ? new PhysEntityTrackData.CreateData(
                trackLocalPos, axis, wheelId, 0.0, 0.0, new VSConstraintAndId(sliderId, slider), new VSConstraintAndId(axleId, axle), this.getSpeed()
        )
                : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.ship.get() != null && this.assembleNextTick && !this.assembled && this.getLevel() != null) {
            this.assemble();
            this.assembleNextTick = false;
        }else if (this.getLevel() != null) {
            if (this.assembled && !this.getLevel().isClientSide) {
                ServerShip ship = (ServerShip)this.ship.get();
                if (ship != null) {
                    WheelEntity wheel = this.wheel.get();
                    if (wheel == null) {
                        this.assemble();
                        wheel = this.wheel.get();
                    }

                    wheel.keepAlive();
                    PhysicsEntityTrackController controller = PhysicsEntityTrackController.getOrCreate(ship);
                    PhysEntityTrackData.UpdateData data = new PhysEntityTrackData.UpdateData(0.0, 0.0, this.getSpeed());
                    controller.updateTrackBlock(this.trackID, data);
                }
            }
        }
    }

    /*public void addMassStats(List<MutableComponent> tooltip, float mass) {
        Lang.text("Total Mass").style(ChatFormatting.GRAY).forGoggles(tooltip);
        Lang.number(mass).text(" kg").style(ChatFormatting.WHITE).forGoggles(tooltip, 1);
    }*/

    @Override
    public float getPointDownwardOffset(float partialTicks) {
        return (float)((double)this.wheelRadius - 0.5);
    }

    @Override
    public float getPointHorizontalOffset() {
        return 0.0F;
    }

    @Override
    public boolean isBeltLarge() {
        return (double)this.wheelRadius > 0.75;
    }

    @Override
    public Vec3 getTrackPointSlope(float partialTicks) {
        return new Vec3(
                0.0,
                Mth.lerp(partialTicks, this.nextPointVerticalOffset.getFirst(), this.nextPointVerticalOffset.getSecond())
                        - this.getPointDownwardOffset(partialTicks),
                this.nextPointHorizontalOffset
        );
    }

    @NotNull
    @Override
    public ITrackPointProvider.PointType getTrackPointType() {
        return ITrackPointProvider.PointType.WRAP;
    }

    @Override
    public float getWheelRadius() {
        return this.wheelRadius;
    }

    public float getSpeed() {
        return super.getSpeed();
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        compound.putBoolean("Assembled", this.assembled);
        if (this.trackID != null) {
            compound.putInt("trackBlockID", this.trackID);
        }

        if (this.wheelID != null) {
            compound.putUUID("wheelID", this.wheelID);
        }

        super.write(compound, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        this.assembled = compound.getBoolean("Assembled");
        if (this.trackID == null && compound.contains("trackBlockID")) {
            this.trackID = compound.getInt("trackBlockID");
        }

        if (this.wheelID == null && compound.contains("wheelID")) {
            this.wheelID = compound.getUUID("wheelID");
        }

        super.read(compound, clientPacket);
    }

    public float calculateStressApplied() {
        if (!this.getLevel().isClientSide
                && this.assembled
                && this.getBlockState().getValue(TrackBaseBlock.PART) == TrackBaseBlock.TrackPart.START) {
            Ship ship = this.ship.get();
            if (ship == null) {
                return super.calculateStressApplied();
            } else {
                double mass = ((ServerShip)ship).getInertiaData().getMass();
                float impact = this.calculateStressApplied((float)mass);
                this.lastStressApplied = impact;
                return impact;
            }
        } else {
            return super.calculateStressApplied();
        }
    }

    public float calculateStressApplied(float mass) {
        float impact = (float)((double)mass * 0.1 * (double)(2.0F * this.wheelRadius));
        if (impact < 0.0F) {
            impact = 0.0F;
        }

        return impact;
    }
}
