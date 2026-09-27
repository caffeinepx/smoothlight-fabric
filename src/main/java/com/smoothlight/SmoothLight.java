package com.smoothlight;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;

public class SmoothLight implements ModInitializer {
    public static final String MOD_ID = "smoothlight";

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel level : server.getAllLevels()) {
                LightTransitions.tick(level);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server ->
                LightTransitions.drop(level -> level instanceof ServerLevel));
    }
}
