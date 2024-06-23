package com.example.trackwork.mixin;

import com.example.trackwork.blocks.TrackBaseBlock;
import com.example.trackwork.blocks.TrackBaseBlockEntity;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {RotationPropagator.class},
   remap = false
)
public abstract class MixinRotationPropagator {
   @Inject(
      method = {"getRotationSpeedModifier(Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;)F"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private static void mixinGetRotationSpeedModifier(KineticBlockEntity from, KineticBlockEntity to, CallbackInfoReturnable<Float> cir) {
      BlockState stateFrom = from.getBlockState();
      BlockState stateTo = to.getBlockState();
      Block fromBlock = stateFrom.getBlock();
      Block toBlock = stateTo.getBlock();
      BlockPos diff = to.getBlockPos().subtract(from.getBlockPos());
      Direction direction = Direction.getNearest((float)diff.getX(), (float)diff.getY(), (float)diff.getZ());
      if (fromBlock instanceof TrackBaseBlock<?> && toBlock instanceof TrackBaseBlock) {
         boolean connected = TrackBaseBlock.areBlocksConnected(stateFrom, stateTo, direction) && clockworkdev2$areTracksConnected(from, to);
         cir.setReturnValue(connected ? 1.0F : 0.0F);
      }
   }

   @Unique
   private static boolean clockworkdev2$areTracksConnected(KineticBlockEntity from, KineticBlockEntity to) {
      if (from instanceof TrackBaseBlockEntity te1 && to instanceof TrackBaseBlockEntity te2) {
         return !te1.isDetracked() && !te2.isDetracked();
      }

      return false;
   }
}
