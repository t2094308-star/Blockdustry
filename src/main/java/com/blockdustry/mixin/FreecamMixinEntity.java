package com.blockdustry.mixin;

import com.blockdustry.client.freecam.FreecamEntity;
import com.blockdustry.client.freecam.FreecamHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// freecam 时拦截 Entity.turn：把鼠标转向重定向到相机实体，取消玩家本体转向喵
@Mixin(Entity.class)
public abstract class FreecamMixinEntity {
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void blockdustry$redirectTurn(double yawDelta, double pitchDelta, CallbackInfo ci) {
        if (!FreecamHandler.isActive()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && (Object) this instanceof LocalPlayer && (Object) this == player) {
            FreecamEntity.rotateCamera((float) yawDelta, (float) pitchDelta);
            ci.cancel(); // 玩家本体不转向喵
        }
    }
}
