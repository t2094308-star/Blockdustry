package com.blockdustry.mixin;

import com.blockdustry.client.freecam.FreecamHandler;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// freecam 时隐藏第一人称手臂（避免相机悬空时屏幕下方飘着手）喵
@Mixin(GameRenderer.class)
public abstract class FreecamMixinGameRenderer {
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void blockdustry$hideHand(Camera camera, float partialTick, Matrix4f matrix, CallbackInfo ci) {
        if (FreecamHandler.isActive()) {
            ci.cancel();
        }
    }
}
