package com.smoothlight.mixin;

import com.smoothlight.LightEngineLevelAccess;
import com.smoothlight.LightEnginePosAccess;
import com.smoothlight.LightTransitions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightEngine.class)
public abstract class LightEngineMixin implements LightEngineLevelAccess {

    @Shadow
    @Final
    protected LightChunkGetter chunkSource;

    @Override
    public Object smoothlight$level() {
        return this.chunkSource.getLevel();
    }

    @Inject(method = "getOpacity", at = @At("RETURN"), cancellable = true)
    private void smoothlight$opacity(BlockState state, CallbackInfoReturnable<Integer> cir) {
        if (!(this instanceof LightEnginePosAccess posAccess)) {
            return;
        }
        int actual = cir.getReturnValueI();
        int value = LightTransitions.opacity(smoothlight$level(), posAccess.smoothlight$lightingPos(), actual);
        if (value != actual) {
            cir.setReturnValue(value);
        }
    }
}
