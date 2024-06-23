/*package com.example.trackwork;

import com.simibubi.create.AllBlocks;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;

public class TrackworkItemGroups {
    public static final ItemGroup TRACKWORK_ITEMGROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(AllBlocks.BELT))
            .displayName(Text.literal("Trackwork"))
            .entries((displayContext, entries) -> {
                entries.add(TrackworkBlocks.SUSPENSION_TRACK.asItem());
                entries.add(TrackworkBlocks.SPROCKET_TRACK.asItem());
            })
            .build();

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, new Identifier(Trackwork.MOD_ID, "item_group"), TRACKWORK_ITEMGROUP);
    }
}*/
