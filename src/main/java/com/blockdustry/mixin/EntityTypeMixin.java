package com.blockdustry.mixin;

import com.blockdustry.BlockdustryTeams;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 刷怪蛋生成（EntityType.spawn）时继承使用者的队伍，让炮塔能按敌我攻击喵
@Mixin(EntityType.class)
public abstract class EntityTypeMixin {
    @Inject(method = "spawn(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/MobSpawnType;ZZ)Lnet/minecraft/world/entity/Entity;",
            at = @At("RETURN"))
    private void blockdustry$inheritTeam(ServerLevel serverLevel, ItemStack stack, Player player, BlockPos pos,
                                         MobSpawnType spawnType, boolean shouldOffsetY, boolean shouldOffsetYMore,
                                         CallbackInfoReturnable<Entity> cir) {
        Entity entity = cir.getReturnValue();
        if (entity instanceof Mob mob && player != null && spawnType == MobSpawnType.SPAWN_EGG) {
            BlockdustryTeams.setTeam(mob, BlockdustryTeams.getTeam(player));
        }
    }
}
