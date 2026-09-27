package com.smoothlight.client.mixin;

import com.smoothlight.LightTransitions;
import com.smoothlight.client.DynamicLightFadeAccess;
import com.smoothlight.client.SmoothLightClient;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * LambDynamicLights writes entity luminance every tick, including while a light
 * stays in hand at a constant level. SmoothLight must not treat those repeats as
 * new transitions, or the held light strobes.
 *
 * {@code setLuminance} is the edge: a new target is recorded only when the value
 * actually changes. Displayed luminance then steps toward that target once per
 * client tick, matching placed-block fades.
 */
@Mixin(value = Entity.class, priority = 1200)
public abstract class EntityDynamicLightFadeMixin implements DynamicLightFadeAccess {

    @Unique
    private boolean smoothlight$dynTracked;

    @Unique
    private volatile int smoothlight$dynCur;

    @Unique
    private int smoothlight$dynTarget;

    @Unique
    private int smoothlight$dynStepTick = Integer.MIN_VALUE;

    @Dynamic
    @Inject(method = "setLuminance(I)V", at = @At("HEAD"), remap = false)
    private void smoothlight$onSetLuminance(int value, CallbackInfo ci) {
        this.smoothlight$dynTracked = true;
        if (value != this.smoothlight$dynTarget) {
            this.smoothlight$dynTarget = value;
        }
        this.smoothlight$tickDynamicFade();
    }

    @Dynamic
    @Inject(method = "getLuminance()I", at = @At("HEAD"), cancellable = true, remap = false)
    private void smoothlight$onGetLuminance(CallbackInfoReturnable<Integer> cir) {
        if (this.smoothlight$dynTracked) {
            cir.setReturnValue(this.smoothlight$dynCur);
        }
    }

    @Override
    public void smoothlight$tickDynamicFade() {
        if (!this.smoothlight$dynTracked) {
            return;
        }
        int tick = SmoothLightClient.fadeTick();
        if (tick == this.smoothlight$dynStepTick) {
            return;
        }
        this.smoothlight$dynStepTick = tick;
        int cur = this.smoothlight$dynCur;
        int target = this.smoothlight$dynTarget;
        if (cur != target) {
            this.smoothlight$dynCur = LightTransitions.step(cur, target);
        }
    }
}
