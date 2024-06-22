package com.example.trackwork;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "trackwork";
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID);
    public ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);
        REGISTRATE.registerEventListeners(REGISTRATE.getModEventBus());
        TrackworkBlocks.initialize();
        TrackworkItemGroups.initialize();
        TrackworkEntities.initialize();
        TrackworkBlockEntityTypes.initialize();
        TrackworkTabs.REGISTRY.register(REGISTRATE.getModEventBus());
    }
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            TrackworkPartialModels.init();
            TrackworkSpriteShifts.init();
        }
    }
    public static ResourceLocation getResource(String path) {
        return new ResourceLocation("trackwork", path);
    }
}