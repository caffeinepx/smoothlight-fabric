package com.smoothlight.mixin;

import com.smoothlight.LightEngineLevelAccess;
import com.smoothlight.LightTransitions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin {

    @Inject(method = "getEmission", at = @At("RETURN"), cancellable = true)
    private void smoothlight$emission(long pos, BlockState state, CallbackInfoReturnable<Integer> cir) {
        int actual = cir.getReturnValueI();
        int value = LightTransitions.emission(((LightEngineLevelAccess) this).smoothlight$level(), pos, actual);
        if (value != actual) {
            cir.setReturnValue(value);
        }
    }
}
