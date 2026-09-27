package com.smoothlight.mixin;

import com.smoothlight.LightTransitions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void smoothlight$onBlockChange(BlockPos pos, BlockState newState, int flags,
                                           CallbackInfoReturnable<BlockState> cir) {
        LevelChunk chunk = (LevelChunk) (Object) this;
        BlockState oldState = chunk.getBlockState(pos);
        LightTransitions.onBlockChanged(chunk.getLevel(), pos.immutable(), oldState, newState);
    }
}
