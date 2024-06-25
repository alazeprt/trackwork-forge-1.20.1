package com.example.trackwork.blocks.sproket;


import com.example.trackwork.TrackworkBlockEntityTypes;
import com.example.trackwork.blocks.TrackBaseBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SprocketBlock extends TrackBaseBlock<SprocketBlockEntity> {
    public SprocketBlock(Properties properties) {
        super(properties);
    }
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }
    public Class<SprocketBlockEntity> getBlockEntityClass() {
        return SprocketBlockEntity.class;
    }

    public BlockEntityType<? extends SprocketBlockEntity> getBlockEntityType() {
        return TrackworkBlockEntityTypes.SPROCKET_TRACK_TYPE.get();
    }
}