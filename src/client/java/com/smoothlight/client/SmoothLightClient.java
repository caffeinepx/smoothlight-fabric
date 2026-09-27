package com.smoothlight.client;

import com.smoothlight.LightTransitions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.multiplayer.ClientLevel;

public class SmoothLightClient implements ClientModInitializer {
    private static int fadeTick;

    public static int fadeTick() {
        return fadeTick;
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> fadeTick++);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientLevel level = client.level;
            LightTransitions.drop(l -> l instanceof ClientLevel && l != level);
            if (level != null) {
                LightTransitions.tick(level);
            }
            if (client.player instanceof DynamicLightFadeAccess fade) {
                fade.smoothlight$tickDynamicFade();
            }
        });
    }
}
