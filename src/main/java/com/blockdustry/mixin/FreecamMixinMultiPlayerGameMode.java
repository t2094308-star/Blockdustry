package com.blockdustry.mixin;

import com.blockdustry.client.freecam.FreecamHandler;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// freecam 时屏蔽一切方块/实体交互（挖、放、打、点），并隐藏经验条数字喵
@Mixin(MultiPlayerGameMode.class)
public abstract class FreecamMixinMultiPlayerGameMode {
    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void blockdustry$noDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (FreecamHandler.isActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void blockdustry$noContinueDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (FreecamHandler.isActive()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void blockdustry$noUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result,
                                         CallbackInfoReturnable<InteractionResult> cir) {
        if (FreecamHandler.isActive()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void blockdustry$noUseItem(Player player, InteractionHand hand,
                                       CallbackInfoReturnable<InteractionResult> cir) {
        if (FreecamHandler.isActive()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void blockdustry$noInteract(Player player, Entity target, InteractionHand hand,
                                        CallbackInfoReturnable<InteractionResult> cir) {
        if (FreecamHandler.isActive()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void blockdustry$noInteractAt(Player player, Entity target, EntityHitResult hitResult, InteractionHand hand,
                                          CallbackInfoReturnable<InteractionResult> cir) {
        if (FreecamHandler.isActive()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void blockdustry$noAttack(Player player, Entity target, CallbackInfo ci) {
        if (FreecamHandler.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "hasExperience", at = @At("RETURN"), cancellable = true)
    private void blockdustry$hideExp(CallbackInfoReturnable<Boolean> cir) {
        if (FreecamHandler.isActive()) {
            cir.setReturnValue(false);
        }
    }
}
