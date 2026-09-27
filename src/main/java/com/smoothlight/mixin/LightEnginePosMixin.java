package com.smoothlight.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.smoothlight.LightEngineLevelAccess;
import com.smoothlight.LightEnginePosAccess;
import com.smoothlight.LightTransitions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.lighting.SkyLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({BlockLightEngine.class, SkyLightEngine.class})
public abstract class LightEnginePosMixin implements LightEnginePosAccess {

    @Shadow
    @Final
    private BlockPos.MutableBlockPos mutablePos;

    @Override
    public long smoothlight$lightingPos() {
        return this.mutablePos.asLong();
    }

    @WrapOperation(
            method = "propagateIncrease",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/lighting/LightEngine;shapeOccludes(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"
            )
    )
    private boolean smoothlight$shapeOccludes(
            LightEngine instance,
            BlockState from,
            BlockState to,
            Direction dir,
            Operation<Boolean> original,
            long packedPos,
            long queueEntry,
            int receivedLevel) {
        Object level = ((LightEngineLevelAccess) this).smoothlight$level();
        long targetPos = BlockPos.offset(packedPos, dir);
        if (LightTransitions.overridesShape(level, packedPos)
                || LightTransitions.overridesShape(level, targetPos)) {
            return false;
        }
        return original.call(instance, from, to, dir);
    }
}
