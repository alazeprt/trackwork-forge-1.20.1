package com.example.trackwork;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

public class TrackworkTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ExampleMod.MODID);
	public static final RegistryObject<CreativeModeTab> TRACKWORK_TAB = REGISTRY.register("trackwork",
		() -> CreativeModeTab.builder().title(Component.translatable("trackwork")).icon(() -> new
			ItemStack(TrackworkBlocks.SPROCKET_TRACK)).displayItems((parameters, tabData) -> {
			tabData.accept(TrackworkBlocks.SPROCKET_TRACK.get().asItem());
			tabData.accept(TrackworkBlocks.SUSPENSION_TRACK.get().asItem());
		}
	)
	.build());
}
