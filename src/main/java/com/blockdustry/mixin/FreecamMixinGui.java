package com.blockdustry.mixin;

import com.blockdustry.client.freecam.FreecamHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// freecam 时 HUD 用真实玩家渲染（修复快捷栏/物品栏显示为空）喵
@Mixin(Gui.class)
public abstract class FreecamMixinGui {
    @Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
    private void blockdustry$useRealPlayerForHud(CallbackInfoReturnable<Player> cir) {
        if (FreecamHandler.isActive() && Minecraft.getInstance().player != null) {
            cir.setReturnValue(Minecraft.getInstance().player);
        }
    }
}
