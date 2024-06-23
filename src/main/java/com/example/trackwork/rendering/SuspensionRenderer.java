package com.example.trackwork.rendering;

import com.example.trackwork.TrackworkPartialModels;
import com.example.trackwork.blocks.TrackBaseBlock;
import com.example.trackwork.blocks.suspension.SuspensionTrackBlock;
import com.example.trackwork.blocks.suspension.SuspensionTrackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;

public class SuspensionRenderer  extends KineticBlockEntityRenderer<SuspensionTrackBlockEntity> {

    public SuspensionRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SuspensionTrackBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        Axis rotationAxis = getRotationAxisOf(be);
        BlockPos visualPos = be.getBlockPos();
        float angleForBE = getAngleForBE(be, visualPos, rotationAxis);
        Axis trackAxis = state.getValue(TrackBaseBlock.AXIS);
        if (trackAxis == Axis.X) {
            angleForBE *= -1.0F;
        }

        float yRot = trackAxis == Axis.X ? 0.0F : 90.0F;
        if (state.hasProperty(SuspensionTrackBlock.WHEEL_VARIANT) && state.getValue(SuspensionTrackBlock.WHEEL_VARIANT) != SuspensionTrackBlock.TrackVariant.BLANK) {
            SuperByteBuffer wheels = be.getWheelRadius() < 0.6F
                    ? CachedBufferer.partial(TrackworkPartialModels.SUSPENSION_WHEEL, state)
                    : (be.getWheelRadius() > 0.8F
                    ? CachedBufferer.partial(TrackworkPartialModels.LARGE_SUSPENSION_WHEEL, state)
                    : CachedBufferer.partial(TrackworkPartialModels.MED_SUSPENSION_WHEEL, state));

            wheels.centre()
                    .rotateY(yRot)
                    .translate(0.0, be.getWheelRadius() - 0.5, 0.0)
                    .translate(0.0, -be.getWheelTravel(partialTicks), be.getPointHorizontalOffset())
                    .rotateX(-angleForBE)
                    .translate(0.0, 0.5625, 0.0)
                    .unCentre();

            wheels.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }

        if (be.assembled) {
            TrackBeltRenderer.renderBelt(
                    be,
                    partialTicks,
                    ms,
                    buffer,
                    light,
                    new TrackBeltRenderer.ScalableScroll(be, (float)(be.getSpeed() * (be.getWheelRadius() / 0.5)), trackAxis)
            );
        }
    }

    public static float getAngleForBE(KineticBlockEntity be, BlockPos pos, Axis axis) {
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float offset = getRotationOffsetForPosition(be, pos, axis);
        return (time * be.getSpeed() * 3.0F / 10.0F + offset) % 360.0F;
        }
    @Override
    protected BlockState getRenderedBlockState(SuspensionTrackBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    @Override
    public int getViewDistance() {
        return 256;    }
}
