package com.example.trackwork.rendering;

import com.example.trackwork.TrackworkPartialModels;
import com.example.trackwork.TrackworkSpriteShifts;
import com.example.trackwork.blocks.ITrackPointProvider;
import com.example.trackwork.blocks.TrackBaseBlock;
import com.example.trackwork.blocks.TrackBaseBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TrackBeltRenderer {
    public static void renderBelt(
            TrackBaseBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, ScalableScroll scroll
    ) {
        if (!be.isDetracked()) {
            BlockState state = be.getBlockState();
            float yRot = getYRotFromState(state);
            renderLink(be, partialTicks, state, yRot, light, ms, buffer, scroll);
        }
    }

    private static void renderLink(
            TrackBaseBlockEntity fromTrack,
            float partialTicks,
            BlockState state,
            float yRot,
            int light,
            PoseStack ms,
            MultiBufferSource buf,
            ScalableScroll scroll
    ) {
        boolean isLarge = fromTrack.isBeltLarge();
        float largeScale = isLarge ? 2.0F : 2.0F * fromTrack.getWheelRadius();
        TrackBaseBlock.TrackPart part = state.getValue(TrackBaseBlock.PART);
        if (part == TrackBaseBlock.TrackPart.MIDDLE) {
            SuperByteBuffer topLink = getLink(state);
            topLink.centre().rotateY(yRot).rotateX(180.0)
                    .translate(0.0, -0.53125 * (double)largeScale, -0.5)
                    .scale(1.0F, largeScale, 1.0F)
                    .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(1.0F))
                    .unCentre();
            topLink.light(light).renderInto(ms, buf.getBuffer(RenderType.cutoutMipped()));
            SuperByteBuffer flatlink = getLink(state);
            flatlink.centre().rotateY(yRot)
                    .translate(0.0, -0.5, -0.25)
                    .translate(0.0, -fromTrack.getPointDownwardOffset(partialTicks), fromTrack.getPointHorizontalOffset())
                    .scale(1.0F, largeScale, 0.5F)
                    .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(0.5F))
                    .unCentre();
            flatlink.light(light).renderInto(ms, buf.getBuffer(RenderType.cutoutMipped()));
        } else if (fromTrack.getTrackPointType() == ITrackPointProvider.PointType.WRAP) {
            float flip = part == TrackBaseBlock.TrackPart.END ? -1.0F : 1.0F;
            SuperByteBuffer topLink = getLink(state);
            topLink.centre().rotateY(yRot).rotateX(180.0)
                    .translate(0.0, -0.53125 * (double)largeScale, -0.5)
                    .scale(1.0F, largeScale, 0.75F + largeScale / 16.0F)
                    .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(flip))
                    .unCentre();

            topLink.light(light).renderInto(ms, buf.getBuffer(RenderType.cutoutMipped()));
            SuperByteBuffer wrapLink = CachedBufferer.partial(TrackworkPartialModels.TRACK_WRAP, state);
            wrapLink.centre().rotateY(yRot)
                    .scale(1.0F, largeScale, largeScale)
                    .translate(0.0, 0.5625, -0.0625)
                    .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(flip))
                    .unCentre();
            wrapLink.light(light).renderInto(ms, buf.getBuffer(RenderType.cutoutMipped()));
        }

        if (fromTrack.getNextPoint() != ITrackPointProvider.PointType.NONE) {
            Vec3 offset = fromTrack.getTrackPointSlope(partialTicks);
            float opposite = (float) offset.z;
            float adjacent = 1.0F + (float) offset.y;
            SuperByteBuffer link = getLink(state);
            if (fromTrack.getNextPoint() == fromTrack.getTrackPointType()) {
                float cut_adjacent = 0.5F + (float)offset.y;
                float length = (float)Math.sqrt(opposite * opposite + cut_adjacent * cut_adjacent);
                float angleOffset = (float)Math.atan2(opposite, cut_adjacent);
                link.centre().rotateY(yRot)
                        .translate(0.0, -0.5, 0.25)
                        .translate(0.0, -fromTrack.getPointDownwardOffset(partialTicks), fromTrack.getPointHorizontalOffset())
                        .rotateX((double)(angleOffset * 180.0F) / Math.PI)
                        .scale(1.0F, largeScale, length)
                        .shiftUVScrolling(TrackworkSpriteShifts.BELT, scroll.getAtScale(length))
                        .unCentre();
                link.light(light).renderInto(ms, buf.getBuffer(RenderType.cutoutMipped()));
            }
            else {
                // this is where the code for the angled belt thingy goes, i couldn't figure out the math
                // for some reason the opposite variable is just 0 all the time, I pulled my hair out but i still couldn't figure out how to make it non zero
            }
        }
    }

    private static SuperByteBuffer getLink(BlockState state) {
        return CachedBufferer.partial(TrackworkPartialModels.TRACK_LINK, state);
    }

    public static Direction getAlong(BlockState state) {
        return state.getValue(RotatedPillarKineticBlock.AXIS) == Axis.X ? Direction.SOUTH : Direction.EAST;
    }

    public static float getYRotFromState(BlockState state) {
        Axis trackAxis = state.getValue(RotatedPillarKineticBlock.AXIS);
        boolean flip = state.getValue(TrackBaseBlock.PART) == TrackBaseBlock.TrackPart.END;
        return (float)((trackAxis == Axis.X ? 0 : 90) + (flip ? 180 : 0));
    }

    public static class ScalableScroll {
        private final float trueSpeed;
        private final float time;
        private final float spriteSize;
        private final float scrollMult;

        public ScalableScroll(KineticBlockEntity be, float speed, Axis axis) {
            this.trueSpeed = axis == Axis.X ? speed : -speed;
            this.time = AnimationTickHolder.getRenderTime(be.getLevel()) * 1.0F;
            this.scrollMult = 0.5F;
            SpriteShiftEntry spriteShift = TrackworkSpriteShifts.BELT;
            this.spriteSize = spriteShift.getTarget().getV1() - spriteShift.getTarget().getV0();
        }

        public float getAtScale(float scale) {
            float speed = this.trueSpeed / scale;
            if (speed != 0.0F) {
                double scroll = (double)(speed * this.time) / 504.0;
                scroll -= Math.floor(scroll);
                scroll = scroll * (double)this.spriteSize * (double)this.scrollMult;
                return (float)scroll;
            } else {
                return 0.0F;
            }
        }
    }
}
