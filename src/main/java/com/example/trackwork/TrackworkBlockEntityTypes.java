package com.example.trackwork;

import com.example.trackwork.blocks.sproket.SprocketBlockEntity;
import com.example.trackwork.blocks.suspension.SuspensionTrackBlockEntity;
import com.example.trackwork.rendering.SprocketInstance;
import com.example.trackwork.rendering.SprocketRenderer;
import com.example.trackwork.rendering.SuspensionRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class TrackworkBlockEntityTypes {
    public static final BlockEntityEntry<SuspensionTrackBlockEntity> SUSPENSION_TRACK = Trackwork.REGISTRATE
            .blockEntity("suspension_track", SuspensionTrackBlockEntity::new)
            .validBlocks(TrackworkBlocks.SUSPENSION_TRACK)
            .renderer(() -> SuspensionRenderer::new)
            .register();

    public static final BlockEntityEntry<SprocketBlockEntity> SPROCKET_TRACK_TYPE = Trackwork.REGISTRATE
            .blockEntity("sprocket_track", SprocketBlockEntity::new)
            .instance(() -> SprocketInstance::new)
            .validBlocks(TrackworkBlocks.SPROCKET_TRACK)
            .renderer(() -> SprocketRenderer::new)
            .register();

    public static void initialize() {};
}
