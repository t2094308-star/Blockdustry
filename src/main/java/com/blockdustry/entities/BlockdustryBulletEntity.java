package com.blockdustry.entities;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.lib.BlockHealthApi;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Mindustry 炮弹实体：move 平滑飞行，命中活体造成伤害，命中敌对建筑用 BlockHealth 扣血，撞方块/寿命耗尽消失喵
public class BlockdustryBulletEntity extends Projectile {
    // 伤害（Mindustry duo 铜弹 damage=9）喵
    private float damage = 9f;
    // 剩余寿命（tick），耗尽消失喵
    private int life = 60;
    // 发射者队伍（判断命中敌对的归属）喵
    private BlockdustryTeam ownerTeam = BlockdustryTeam.DERELICT;

    // 客户端重建构造：必须也设 noPhysics，否则客户端 move() 会撞方块与服务端分歧喵
    public BlockdustryBulletEntity(EntityType<BlockdustryBulletEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // 业务构造：给定出生点、方向速度与伤害；无视方块碰撞（穿透飞行，避免卡顿）喵
    public BlockdustryBulletEntity(Level level, double x, double y, double z, Vec3 dir, float damage) {
        this(BlockdustryEntities.BULLET.get(), level);
        this.setPos(x, y, z);
        this.setDeltaMovement(dir);
        this.damage = damage;
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    // 设置剩余寿命（由射程/速度决定）喵
    public void setLife(int life) {
        this.life = life;
    }

    // 设置发射者队伍喵
    public void setOwnerTeam(BlockdustryTeam team) {
        this.ownerTeam = team;
    }

    @Override
    public void tick() {
        super.tick();
        // 双端都移动：客户端自模拟后 xOld!=getX()，LevelRenderer 的 partialTick 插值立即恢复平滑喵
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.level().isClientSide) {
            return; // 客户端不处理寿命/命中，等服务端 discard 移除包即可喵
        }
        if (--life <= 0) {
            this.discard();
            return;
        }
        // 命中活体目标造成伤害喵
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(0.2), this::canHitEntity)) {
            target.hurt(this.damageSources().generic(), this.damage);
            this.discard();
            return;
        }
        // 命中敌对建筑用 BlockHealth 扣血；普通方块穿透（忠于 Mindustry 子弹不撞地形）喵
        BlockPos pos = this.blockPosition();
        BlockEntity be = this.level().getBlockEntity(pos);
        if (be instanceof BlockdustryBuildingEntity building
                && BlockdustryTeams.isHostile(ownerTeam, building.getTeam())
                && this.level() instanceof ServerLevel serverLevel) {
            BlockHealthApi.damage(serverLevel, pos, this.damage, null, BlockHealthApi.DamageType.PROJECTILE);
            this.discard();
            return;
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target instanceof LivingEntity;
    }
}
