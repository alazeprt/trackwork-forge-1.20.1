package com.example.trackwork.rendering;

import com.example.trackwork.TrackworkPartialModels;
import com.example.trackwork.blocks.sproket.SprocketBlockEntity;
import com.example.trackwork.blocks.suspension.SuspensionTrackBlock;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

public class SprocketInstance extends ShaftInstance<SprocketBlockEntity> implements DynamicInstance {
   private final ModelData gantryCogs;
   final Axis axis;
   final Axis rotationAxis;
   final float rotationMult;
   final BlockPos visualPos;
   private final float lastAngle = Float.NaN;

   public SprocketInstance(MaterialManager materialManager, SprocketBlockEntity blockEntity) {
      super(materialManager, blockEntity);
      this.gantryCogs = this.getTransformMaterial().getModel(TrackworkPartialModels.COGS, this.blockState).createInstance();
      this.axis = this.blockState.getValue(SuspensionTrackBlock.AXIS);
      this.rotationAxis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
      if (this.axis == Axis.X) {
         this.rotationMult = -1.0F;
      } else {
         this.rotationMult = 1.0F;
      }

      this.visualPos = blockEntity.getBlockPos();
      this.animateCogs(this.getCogAngle());
   }

   public void beginFrame() {
      float cogAngle = this.getCogAngle();
      if (Mth.equal(cogAngle, this.lastAngle)) return;
      this.animateCogs(cogAngle);
   }

   private float getCogAngle() {
      return SprocketRenderer.getAngleForBE(this.blockEntity, this.visualPos, this.rotationAxis) * this.rotationMult;
   }

   private void animateCogs(float cogAngle) {
      gantryCogs.loadIdentity()
              .translate(getInstancePosition())
              .centre()
              .rotateY(this.axis == Axis.X ? 0.0 : 90.0)
              .rotateX(-cogAngle)
              .translate(0.0, 0.5625, 0.0)
              .unCentre();
   }

   @Override
   public void updateLight() {
      relight(pos, gantryCogs, rotatingModel);
   }


   public void remove() {
      super.remove();
      this.gantryCogs.delete();
   }
}
