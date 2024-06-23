package com.example.trackwork;


import com.example.trackwork.entities.TrackBeltEntity;
import com.example.trackwork.entities.WheelEntity;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.minecraft.world.entity.MobCategory;

public class TrackworkEntities {
    public static final EntityEntry<WheelEntity> WHEEL = Trackwork.REGISTRATE.entity(
            "wheel_entity",
            WheelEntity::new,
            MobCategory.MISC
    ).register();

    public static final EntityEntry<TrackBeltEntity> BELT = Trackwork.REGISTRATE.entity(
            "track_belt_entity",
            TrackBeltEntity::new,
            MobCategory.MISC
    ).register();
    public static void initialize() {

    }
}
