package com.blockdustry.mixin;

import com.blockdustry.client.freecam.FreecamHandler;

import net.minecraft.client.Camera;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// freecam 时相机不进水雾（飞过水/岩浆时不糊屏）喵
@Mixin(Camera.class)
public abstract class FreecamMixinCamera {
    @Inject(method = "getFluidInCamera", at = @At("HEAD"), cancellable = true)
    private void blockdustry$disableFluidFog(CallbackInfoReturnable<FogType> cir) {
        if (FreecamHandler.isActive()) {
            cir.setReturnValue(FogType.NONE);
        }
    }
}
