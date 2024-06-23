package com.example.trackwork;

import com.example.trackwork.blocks.sproket.SprocketBlock;
import com.example.trackwork.blocks.suspension.SuspensionTrackBlock;
import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.simibubi.create.content.kinetics.chainDrive.ChainDriveGenerator;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockModel;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class TrackworkBlocks {
    public static final BlockEntry<SuspensionTrackBlock> SUSPENSION_TRACK =
        Trackwork.REGISTRATE.block("suspension_track", SuspensionTrackBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_BROWN))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(BlockStressDefaults.setNoImpact())
            .transform(axeOrPickaxe())
            .blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
                .getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
            //.addLayer(() -> RenderType::cutoutMipped)
            .item()
            .transform(customItemModel())
            .register();

    public static final BlockEntry<SprocketBlock> SPROCKET_TRACK =
            Trackwork.REGISTRATE.block("sprocket_track", SprocketBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_BROWN))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(BlockStressDefaults.setNoImpact())
                    .transform(axeOrPickaxe())
                    .blockstate((c, p) -> new ChainDriveGenerator((state, suffix) -> p.models()
                            .getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p)
                    )
                    .onRegister(CreateRegistrate.blockModel(() -> BracketedKineticBlockModel::new))
                    //.addLayer(() -> RenderType::cutoutMipped)
                    .item()
                    .transform(customItemModel())
                    .register();

    /*public static <T extends Block> T register(T block, String name, boolean shouldRegisterItem) {
        ResourceLocation id = new ResourceLocation(Trackwork.MODID, name);

        if(shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Properties());
            Registry.register(Registries.Item, id, blockItem);
        }

        return Registry.register(Registries.BLOCK, id, block);
    }*/

    public static void initialize() {}
}