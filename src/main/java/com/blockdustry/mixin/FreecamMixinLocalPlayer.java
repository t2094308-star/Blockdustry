package com.blockdustry.mixin;

import com.blockdustry.client.freecam.FreecamHandler;
import com.blockdustry.client.freecam.FreecamInput;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// freecam 期间冻结玩家移动输入、取消挥臂，并强制 isControlledCamera=true
// （让 serverAiStep 把空输入写入 xxa/zza，避免身体沿用上一帧旧输入继续走）喵
@Mixin(LocalPlayer.class)
public abstract class FreecamMixinLocalPlayer {
    @Shadow
    public Input input;

    // 空操作输入；options 传 null 即可（tick 覆写为空，永不解引用）喵
    @Unique
    private final Input dummyInput = new FreecamInput(null);
    @Unique
    private Input realInput;

    @Inject(method = "tick", at = @At("HEAD"))
    private void blockdustry$freezeInputPre(CallbackInfo ci) {
        if (FreecamHandler.isActive() && (Object) this == Minecraft.getInstance().player) {
            this.realInput = this.input;
            this.input = this.dummyInput;
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void blockdustry$freezeInputPost(CallbackInfo ci) {
        if (this.realInput != null) {
            this.input = this.realInput;
            this.realInput = null;
        }
    }

    // 强制返回 true：freecam 期间相机实体≠玩家，原版会返回 false，
    // 导致 serverAiStep 不再把（空）输入写入 xxa/zza，身体会沿用旧输入漂移喵
    @Inject(method = "isControlledCamera", at = @At("HEAD"), cancellable = true)
    private void blockdustry$forceControlledCamera(CallbackInfoReturnable<Boolean> cir) {
        if (FreecamHandler.isActive() && (Object) this == Minecraft.getInstance().player) {
            cir.setReturnValue(true);
        }
    }

    // 取消挥臂动画，身体保持站立姿势喵
    @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
    private void blockdustry$preventSwing(InteractionHand hand, CallbackInfo ci) {
        if (FreecamHandler.isActive() && (Object) this == Minecraft.getInstance().player) {
            ci.cancel();
        }
    }
}
