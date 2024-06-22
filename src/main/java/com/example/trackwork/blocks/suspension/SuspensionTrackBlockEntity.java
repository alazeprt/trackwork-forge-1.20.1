package com.example.trackwork.blocks.suspension;

import com.example.trackwork.blocks.ITrackPointProvider;
import com.example.trackwork.blocks.TrackBaseBlockEntity;
import com.example.trackwork.data.PhysTrackData;
import com.example.trackwork.forces.PhysicsTrackController;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

public class SuspensionTrackBlockEntity extends TrackBaseBlockEntity implements ITrackPointProvider {
   private float wheelRadius;
   private float suspensionTravel = 1.5F;
   protected final Random random = new Random();
   @NotNull
   protected final Supplier<Ship> ship;
   private Integer trackID;
   public boolean assembled = false;
   public boolean assembleNextTick = true;
   private float wheelTravel;
   private float prevWheelTravel;
   private double suspensionScale = 1.0;
   private float horizontalOffset;

   public SuspensionTrackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state);
      this.assembled = false;
      this.wheelRadius = 0.5F;
      this.suspensionTravel = 1.5F;
      this.ship = () -> VSGameUtilsKt.getShipObjectManagingPos(this.level, pos);
      this.setLazyTickRate(40);
   }

   public static SuspensionTrackBlockEntity large(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      SuspensionTrackBlockEntity be = new SuspensionTrackBlockEntity(type, pos, state);
      be.wheelRadius = 1.0F;
      be.suspensionTravel = 2.0F;
      return be;
   }

   public static SuspensionTrackBlockEntity med(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      SuspensionTrackBlockEntity be = new SuspensionTrackBlockEntity(type, pos, state);
      be.wheelRadius = 0.75F;
      be.suspensionTravel = 1.5F;
      return be;
   }

   public void onLoad() {
      super.onLoad();
   }

   public void remove() {
      if (this.level != null && !this.level.isClientSide && this.assembled) {
         ServerShip ship = (ServerShip)this.ship.get();
         if (ship != null) {
            PhysicsTrackController controller = PhysicsTrackController.getOrCreate(ship);
            controller.removeTrackBlock(this.trackID);
         }
      }
      super.remove();
   }

   private void assemble() {

   }

   public void disassemble() {
   }

//   public void tick() {
//      super.tick();
//      if (this.ship.get() != null && this.assembleNextTick && !this.assembled && this.world != null) {
//         if (!this.world.isClient) {
//            ServerShip ship = (ServerShip)this.ship.get();
//            if (ship != null) {
//               this.assembled = true;
//               Trackwork.LOGGER.info("tried to assemble, currently assembled: " + this.assembled);
//
//               PhysicsTrackController controller = PhysicsTrackController.getOrCreate(ship);
//               PhysTrackData.PhysTrackCreateData data = new PhysTrackData.PhysTrackCreateData(
//                       VectorConversionsMCKt.toJOML(Vec3d.of(this.getPos()))
//               );
//               this.trackID = controller.addTrackBlock(data);
//               this.sendData();
//               if (this.trackID != null) {
//                  return;
//               }
//            }
//         }
//         this.assembleNextTick = false;
//      } else {
//         if (!this.world.isClient) {
//            if (this.assembled) {
//               Vec3d start = Vec3d.of(this.getPos());
//               Axis axis = (Axis)this.getCachedState().get(RotatedPillarKineticBlock.AXIS);
//               double restOffset = (double)(this.wheelRadius - 0.5F);
//               float trackRPM = this.getSpeed();
//               double susScaled = (double)this.suspensionTravel * this.suspensionScale;
//               ServerShip ship = (ServerShip)this.ship.get();
//               if (ship != null) {
//                  Vec3d worldSpaceNormal = VectorConversionsMCKt.toMinecraft(
//                          ship.getTransform()
//                                  .getShipToWorldRotation()
//                                  .transform(VectorConversionsMCKt.toJOML(getActionNormal(axis)), new Vector3d())
//                                  .mul(susScaled + 0.5)
//                  );
//                  Vec3d worldSpaceStart = VectorConversionsMCKt.toMinecraft(
//                          VSGameUtilsKt.getWorldCoordinates(this.world, this.getPos(), VectorConversionsMCKt.toJOML(start.add(0.0, -restOffset, 0.0)))
//                  );
//                  Vector3dc worldSpaceForward = ship.getTransform().getShipToWorldRotation().transform(getActionVec3d(axis, 1.0F), new Vector3d());
//                  Vec3d worldSpaceFutureOffset = VectorConversionsMCKt.toMinecraft(
//                          worldSpaceForward.mul(0.1 * ship.getVelocity().dot(worldSpaceForward), new Vector3d())
//                  );
//                  Vec3d worldSpaceHorizontalOffset = VectorConversionsMCKt.toMinecraft(
//                          worldSpaceForward.mul((double)this.getPointHorizontalOffset(), new Vector3d())
//                  );
//
//                  Vec3d combinedOffset = worldSpaceFutureOffset.add(worldSpaceHorizontalOffset);
//                  Vec3d worldSpaceStartCorrected = worldSpaceStart.add(combinedOffset);
//
//                  ClipResult clipResult = this.clipAndResolve(
//                          ship, axis, worldSpaceStartCorrected, worldSpaceNormal
//                  );
//                  Vector3dc forceVec = clipResult.trackTangent.mul((double)this.wheelRadius / 0.5, new Vector3d());
//                  if (forceVec.lengthSquared() == 0.0) {
//                     BlockState b = this.world.getBlockState(BlockPos.ofFloored(worldSpaceStart));
//                     if (b.getFluidState().isIn(FluidTags.WATER)) {
//                        forceVec = ship.getTransform().getShipToWorldRotation().transform(getActionVec3d(axis, 1.0F)).mul(0.1);
//                     }
//                  }
//
//                  double suspensionTravel = clipResult.suspensionLength.lengthSquared() == 0.0 ? susScaled : clipResult.suspensionLength.length() - 0.5;
//                  Vector3dc suspensionForce = VectorConversionsMCKt.toJOML(worldSpaceNormal.multiply(susScaled - suspensionTravel)).negate();
//                  PhysicsTrackController controller = PhysicsTrackController.getOrCreate(ship);
//                  if (this.trackID == null) {
//                     Trackwork.LOGGER.info("track id is null");
//                     return;
//                  }
//
//                  PhysTrackData.PhysTrackUpdateData data = new PhysTrackData.PhysTrackUpdateData(
//                          VectorConversionsMCKt.toJOML(worldSpaceStart),
//                          forceVec,
//                          VectorConversionsMCKt.toJOML(worldSpaceNormal),
//                          suspensionForce,
//                          clipResult.groundShipId,
//                          clipResult.suspensionLength.lengthSquared() != 0.0,
//                          trackRPM
//                  );
//                  this.suspensionScale = controller.updateTrackBlock(this.trackID, data);
//                  this.prevWheelTravel = this.wheelTravel;
//                  this.wheelTravel = (float)(suspensionTravel + restOffset);
//
//                  PacketByteBuf buf = PacketByteBufs.create();
//                  buf.writeBlockPos(this.getPos());
//                  buf.writeFloat(this.wheelTravel);
//
//                  for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, this.getPos())) {
//                     ServerPlayNetworking.send(player, TrackworkPackets.SUSPENSION_PACKET_ID, buf);
//                  }
//               }
//            }
//         }
//      }
//   }

   @Override
   public void tick() {
      super.tick();
      if (this.ship.get() != null && this.assembleNextTick && !this.assembled && this.level != null) {
         if (!this.level.isClientSide) {
            ServerShip ship = (ServerShip)this.ship.get();
            if (ship != null) {
               this.assembled = true;
               PhysicsTrackController controller = PhysicsTrackController.getOrCreate(ship);
               PhysTrackData.PhysTrackCreateData data = new PhysTrackData.PhysTrackCreateData(
                       VectorConversionsMCKt.toJOML(Vec3.atLowerCornerOf(this.getBlockPos()))
               );
               this.trackID = controller.addTrackBlock(data);
               this.sendData();
               if (this.trackID != null) {
                  return;
               }
            }
         }
         this.assembleNextTick = false;
      } else {
         // TODO: Spawn running particles

         if (!this.level.isClientSide) {
            if (this.assembled) {
               Vec3 start = Vec3.atCenterOf(this.getBlockPos());
               Axis axis = this.getBlockState().getValue(RotatedPillarKineticBlock.AXIS);
               double restOffset = this.wheelRadius - 0.5F;
               float trackRPM = this.getSpeed();
               double susScaled = (double)this.suspensionTravel * this.suspensionScale;
               ServerShip ship = (ServerShip)this.ship.get();
               if (ship != null) {
                  Vec3 worldSpaceNormal = VectorConversionsMCKt.toMinecraft(
                          ship.getTransform()
                                  .getShipToWorldRotation()
                                  .transform(VectorConversionsMCKt.toJOML(getActionNormal(axis)), new Vector3d())
                                  .mul(susScaled + 0.5)
                  );
                  Vec3 worldSpaceStart = VectorConversionsMCKt.toMinecraft(
                          VSGameUtilsKt.getWorldCoordinates(this.level, this.getBlockPos(), VectorConversionsMCKt.toJOML(start.subtract(0.0, -restOffset, 0.0)))
                  );
                  Vector3dc worldSpaceForward = ship.getTransform().getShipToWorldRotation().transform(getActionVec3d(axis, 1.0F), new Vector3d());
                  Vec3 worldSpaceFutureOffset = VectorConversionsMCKt.toMinecraft(
                          worldSpaceForward.mul(0.1 * ship.getVelocity().dot(worldSpaceForward), new Vector3d())
                  );
                  Vec3 worldSpaceHorizontalOffset = VectorConversionsMCKt.toMinecraft(
                          worldSpaceForward.mul(this.getPointHorizontalOffset(), new Vector3d())
                  );
                  SuspensionTrackBlockEntity.ClipResult clipResult = this.clipAndResolve(
                          ship, axis, worldSpaceStart.add(worldSpaceFutureOffset).add(worldSpaceHorizontalOffset), worldSpaceNormal
                  );
                  Vector3dc forceVec = clipResult.trackTangent.mul((double)this.wheelRadius / 0.5, new Vector3d());
                  if (forceVec.lengthSquared() == 0.0) {
                     BlockState b = this.level.getBlockState(BlockPos.containing(worldSpaceStart));
                     if (b.getFluidState().is(FluidTags.WATER)) {
                        forceVec = ship.getTransform().getShipToWorldRotation().transform(getActionVec3d(axis, 1.0F)).mul(0.1);
                     }
                  }

                  double suspensionTravel = clipResult.suspensionLength.length() == 0.0 ? susScaled : clipResult.suspensionLength.length() - 0.5;
                  Vector3dc suspensionForce = VectorConversionsMCKt.toJOML(worldSpaceNormal.scale(susScaled - suspensionTravel)).negate();
                  PhysicsTrackController controller = PhysicsTrackController.getOrCreate(ship);
                  if (this.trackID == null) {
                     return;
                  }

                  PhysTrackData.PhysTrackUpdateData data = new PhysTrackData.PhysTrackUpdateData(
                          VectorConversionsMCKt.toJOML(worldSpaceStart),
                          forceVec,
                          VectorConversionsMCKt.toJOML(worldSpaceNormal),
                          suspensionForce,
                          clipResult.groundShipId,
                          clipResult.suspensionLength.length() != 0.0,
                          trackRPM
                  );
                  this.suspensionScale = controller.updateTrackBlock(this.trackID, data);
                  this.prevWheelTravel = this.wheelTravel;
                  this.wheelTravel = (float)(suspensionTravel + restOffset);
               }
            }
         }
      }
   }


   @NotNull
   private SuspensionTrackBlockEntity.ClipResult clipAndResolve(ServerShip ship, Axis axis, Vec3 start, Vec3 dir) {
      BlockHitResult bResult = this.level.clip(new ClipContext(start, start.add(dir), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
      if (bResult.getType() != HitResult.Type.BLOCK) {
         return new SuspensionTrackBlockEntity.ClipResult(new Vector3d(0.0), Vec3.ZERO, null);
      } else {
         Ship hitShip = VSGameUtilsKt.getShipObjectManagingPos(this.level, bResult.getBlockPos());
         Long hitShipId = null;
         if (hitShip != null) {
            if (hitShip.equals(ship)) {
               return new SuspensionTrackBlockEntity.ClipResult(new Vector3d(0.0), Vec3.ZERO, null);
            }
            hitShipId = hitShip.getId();
         }

         Vec3 worldSpaceHitExact = bResult.getLocation();
         Vec3 forceNormal = start.subtract(worldSpaceHitExact);
         Vec3 worldSpaceAxis = VectorConversionsMCKt.toMinecraft(ship.getTransform().getShipToWorldRotation().transform(getAxisAsVec(axis)));

         Vector3d tangent = VectorConversionsMCKt.toJOML(worldSpaceAxis.cross(forceNormal).normalize());

         return new SuspensionTrackBlockEntity.ClipResult(
                 tangent,
                 forceNormal,
                 hitShipId
         );
      }
   }

   public void setHorizontalOffset(Vector3dc offset) {
      Axis axis = (Axis)this.getBlockState().getValue(RotatedPillarKineticBlock.AXIS);
      double factor = offset.dot(getActionVec3d(axis, 1.0F));
      this.horizontalOffset = Math.clamp(-0.5F, 0.5F, (float)Math.round(factor * 8.0) / 8.0F);
//      FIXME: tf is this??
//      this.m_6596_();
   }

   @Override
   public float getPointDownwardOffset(float partialTicks) {
      return this.getWheelTravel(partialTicks);
   }

   @Override
   public float getPointHorizontalOffset() {
      return this.horizontalOffset;
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
                 - this.getWheelTravel(partialTicks),
              this.nextPointHorizontalOffset - this.horizontalOffset
      );
   }

   @NotNull
   @Override
   public ITrackPointProvider.PointType getTrackPointType() {
      return ITrackPointProvider.PointType.GROUND;
   }

   @Override
   public float getWheelRadius() {
      return this.wheelRadius;
   }

   public float getSpeed() {
      return !this.assembled ? 0.0F : super.getSpeed();
   }

   @Override
   public void write(CompoundTag compound, boolean clientPacket) {
      compound.putBoolean("Assembled", this.assembled);
      if (this.trackID != null) {
         compound.putInt("trackBlockID", this.trackID);
      }

      compound.putFloat("WheelTravel", this.wheelTravel);
      compound.putFloat("horizontalOffset", this.horizontalOffset);

      super.write(compound, clientPacket);
   }

   @Override
   protected void read(CompoundTag compound, boolean clientPacket) {
      this.assembled = compound.getBoolean("Assembled");
      if (this.trackID == null && compound.contains("trackBlockID")) {
         this.trackID = compound.getInt("trackBlockID");
      }

      this.wheelTravel = compound.getFloat("WheelTravel");
      if (compound.contains("horizontalOffset")) {
         this.horizontalOffset = compound.getFloat("horizontalOffset");
      }

      this.prevWheelTravel = this.wheelTravel;
      super.read(compound, clientPacket);
   }

   public float getWheelTravel(float partialTicks) {
      return Mth.lerp(partialTicks, this.prevWheelTravel, this.wheelTravel);
   }

   public void setWheelTravel(float wheelTravel) {
      this.prevWheelTravel = this.wheelTravel;
      this.wheelTravel = wheelTravel;
   }

   public static record ClipResult(Vector3dc trackTangent, Vec3 suspensionLength, @Nullable Long groundShipId) {
   }
}
