package com.example.trackwork;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public class TrackworkItemGroups {
    public static final CreativeModeTab TRACKWORK_ITEMGROUP = CreativeModeTab
            .builder()
            .displayItems((CreativeModeTab.ItemDisplayParameters var1, CreativeModeTab.Output var2)->{
                var2.accept(TrackworkBlocks.SPROCKET_TRACK.asItem());
                var2.accept(TrackworkBlocks.SUSPENSION_TRACK.asItem());
            })
            .title(Component.literal("Trackwork"))
            .build();

    public static void initialize() {
    }
}
