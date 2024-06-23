package com.example.trackwork.blocks;

import com.simibubi.create.content.contraptions.ITransformableBlock;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public abstract class TrackBaseBlock<BE extends TrackBaseBlockEntity> extends RotatedPillarKineticBlock implements ITransformableBlock, IBE<BE> {
    public static final EnumProperty<TrackPart> PART = EnumProperty.create("part", TrackPart.class);
    public static final BooleanProperty CONNECTED_ALONG_FIRST_COORDINATE = DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;

    public TrackBaseBlock(Properties properties) {
        super(properties);
    }
    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }
    @Override
    public Class<BE> getBlockEntityClass() {
        return null;
    }
    @Override
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }
    @Override
    public BlockEntityType<? extends BE> getBlockEntityType() {
        return null;
    }
    public static boolean isValidAxis(Axis axis) {
        return !axis.isHorizontal();
    }
    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        return false;
    }

    //    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
//        if ((Boolean)TrackworkConfigs.server().enableTrackThrow.get()) {
//            this.withBlockEntityDo(level, pos, be -> be.throwTrack(false));
//        }
//
//        super.onBlockExploded(state, level, pos, explosion);
//    }

    @NotNull
    public PushReaction pistonBehavior(@NotNull BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
        builder.add(CONNECTED_ALONG_FIRST_COORDINATE);
        super.createBlockStateDefinition(builder);
    }

    public static void updateTrackSystem(BlockPos pos) {
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Axis placedAxis = context.getClickedFace().getAxis();
        Axis axis = context.getPlayer() != null && context.getPlayer().isShiftKeyDown() ? placedAxis : getPreferredAxis(context);
        if (axis == null) {
            axis = placedAxis;
        }

        if (axis == Axis.Y) {
            axis = Axis.X;
        }

        BlockState state = this.defaultBlockState().setValue(AXIS, axis);

        for (Direction facing : Iterate.directions) {
            if (facing.getAxis() != axis) {
                BlockPos pos = context.getClickedPos();
                BlockPos offset = pos.offset(facing.getNormal());
                state = this.updateShape(state, facing, context.getLevel().getBlockState(offset), context.getLevel(), pos, offset);
            }
        }

        return state;
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction face, BlockState neighbour, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        TrackPart part = stateIn.getValue(PART);
        Axis axis = stateIn.getValue(AXIS);
        boolean connectionAlongFirst = stateIn.getValue(CONNECTED_ALONG_FIRST_COORDINATE);
        Axis connectionAxis = connectionAlongFirst ? (axis == Axis.X ? Axis.Y : Axis.X) : (axis == Axis.Z ? Axis.Y : Axis.Z);
        Axis faceAxis = face.getAxis();
        boolean facingAlongFirst = axis == Axis.X ? faceAxis.isHorizontal() : faceAxis == Axis.X;
        boolean positive = face.getAxisDirection() == AxisDirection.POSITIVE;
        if (axis == faceAxis) {
            return stateIn;
        } else if (!(neighbour.getBlock() instanceof TrackBaseBlock)) {
            if (facingAlongFirst != connectionAlongFirst || part == TrackPart.NONE) {
                return stateIn;
            } else if (part == TrackPart.MIDDLE) {
                return stateIn.setValue(PART, positive ? TrackPart.END : TrackPart.START);
            } else {
                return part == TrackPart.START == positive ? stateIn.setValue(PART, TrackPart.NONE) : stateIn;
            }
        } else {
            TrackPart otherPart = neighbour.getValue(PART);
            Axis otherAxis = neighbour.getValue(AXIS);
            boolean otherConnection = neighbour.getValue(CONNECTED_ALONG_FIRST_COORDINATE);
            Axis otherConnectionAxis = otherConnection ? (otherAxis == Axis.X ? Axis.Y : Axis.X) : (otherAxis == Axis.Z ? Axis.Y : Axis.Z);
            if (neighbour.getValue(AXIS) == faceAxis) {
                return stateIn;
            } else if (otherPart != TrackPart.NONE && otherConnectionAxis != faceAxis) {
                return stateIn;
            } else {
                if (part == TrackPart.NONE) {
                    part = positive ? TrackPart.START : TrackPart.END;
                    connectionAlongFirst = axis == Axis.X ? faceAxis.isHorizontal() : faceAxis == Axis.X;
                } else if (connectionAxis != faceAxis) {
                    return stateIn;
                }

                if (part == TrackPart.START != positive) {
                    part = TrackPart.MIDDLE;
                }

                return stateIn.setValue(PART, part).setValue(CONNECTED_ALONG_FIRST_COORDINATE, connectionAlongFirst);
            }
        }
    }

    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState.getValue(PART) == TrackPart.NONE
                ? super.getRotatedBlockState(originalState, targetedFace)
                : super.getRotatedBlockState(originalState, Direction.get(AxisDirection.POSITIVE, getConnectionAxis(originalState)));
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    public static boolean areBlocksConnected(BlockState state, BlockState other, Direction facing) {
        TrackPart part = state.getValue(PART);
        Axis connectionAxis = getConnectionAxis(state);
        Axis otherConnectionAxis = getConnectionAxis(other);
        if (otherConnectionAxis != connectionAxis) {
            return false;
        } else if (facing.getAxis() != connectionAxis) {
            return false;
        } else {
            return facing.getAxisDirection() == AxisDirection.POSITIVE && (part == TrackPart.MIDDLE || part == TrackPart.START) || facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE && (part == TrackPart.MIDDLE || part == TrackPart.END);
        }
    }

    protected static Axis getConnectionAxis(BlockState state) {
        Axis axis = state.getValue(AXIS);
        boolean connectionAlongFirst = state.getValue(CONNECTED_ALONG_FIRST_COORDINATE);
        return connectionAlongFirst ? (axis == Axis.X ? Axis.Y : Axis.X) : (axis == Axis.Z ? Axis.Y : Axis.Z);
    }

    public BlockState m_6843_(BlockState state, Rotation rot) {
        return this.rotate(state, rot, Axis.Y);
    }

    protected BlockState rotate(BlockState pState, Rotation rot, Axis rotAxis) {
        Axis connectionAxis = getConnectionAxis(pState);
        Direction direction = Direction.fromAxisAndDirection(connectionAxis, AxisDirection.POSITIVE);
        Direction normal = Direction.fromAxisAndDirection(pState.getValue(AXIS), AxisDirection.POSITIVE);

        for (int i = 0; i < rot.ordinal(); i++) {
            direction = direction.getClockWise(rotAxis);
            normal = normal.getClockWise(rotAxis);
        }

        if (direction.getAxisDirection() == AxisDirection.NEGATIVE) {
            pState = this.reversePart(pState);
        }

        Axis newAxis = normal.getAxis();
        Axis newConnectingDirection = direction.getAxis();
        boolean alongFirst = newAxis == Axis.X && newConnectingDirection == Axis.Y || newAxis != Axis.X && newConnectingDirection == Axis.X;
        return pState.setValue(AXIS, newAxis).setValue(CONNECTED_ALONG_FIRST_COORDINATE, alongFirst);
    }

    @NotNull
    public BlockState mirror(@NotNull BlockState pState, Mirror pMirror) {
        Axis connectionAxis = getConnectionAxis(pState);
        return pMirror.mirror(Direction.fromAxisAndDirection(connectionAxis, AxisDirection.POSITIVE)).getAxisDirection() == AxisDirection.POSITIVE
                ? pState
                : this.reversePart(pState);
    }

    protected BlockState reversePart(BlockState pState) {
        TrackPart part = pState.getValue(PART);
        if (part == TrackPart.START) {
            return pState.setValue(PART, TrackPart.END);
        } else {
            return part == TrackPart.END ? pState.setValue(PART, TrackPart.START) : pState;
        }
    }

    public BlockState transform(BlockState state, StructureTransform transform) {
        return this.rotate(this.mirror(state, transform.mirror), transform.rotation, transform.rotationAxis);
    }

    public enum TrackPart implements StringRepresentable {
        START,
        MIDDLE,
        END,
        NONE;
        @Override
        public String getSerializedName() {
            return Lang.asId(this.name());
        }
    }
}
