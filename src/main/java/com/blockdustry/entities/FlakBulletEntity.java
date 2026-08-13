package com.blockdustry.entities;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Mindustry Scatter 的 flak 霰弹：近炸引信 + 溅射，只命中「空」标签单位（collidesGround=false 语义）喵
// 行为仿 FlakBulletType：接近空中敌机进入 explodeRange → 延时 5tick 引爆，对 splashRadius 内空中敌机造成溅射伤害；
// 直接碰撞或寿命耗尽也引爆；对地面单位与建筑无伤害（忠于 Mindustry 对空炮）喵
public class FlakBulletEntity extends Projectile {
    // 近炸引爆延迟（Mindustry FlakBulletType.explodeDelay=5f）喵
    private static final int PRIME_DELAY = 5;

    private float damage = 40f;           // 直接命中伤害（Mindustry lead flak 基础 3 折算，此处以溅射为主）喵
    private int life = 60;                // 剩余寿命（tick），耗尽引爆喵
    private float splashDamage = 40f;     // 溅射伤害（lead ammo 27*1.5≈40）喵
    private float splashRadius = 2f;      // 溅射半径（Mindustry 15/8≈1.9 格）喵
    private float explodeRange = 2f;      // 近炸引信触发半径（Mindustry explodeRange=30/8≈3.75，取 2 更近炸）喵
    private BlockdustryTeam ownerTeam = BlockdustryTeam.DERELICT;
    private int primeTimer = -1;          // >=0 表示已近炸待爆，倒计时中喵

    // 客户端重建构造：必须也设 noPhysics，否则客户端 move() 会撞方块与服务端分歧喵
    public FlakBulletEntity(EntityType<FlakBulletEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // 业务构造：给定出生点、方向速度与伤害；无视方块碰撞（穿透飞行，避免卡顿）喵
    public FlakBulletEntity(Level level, double x, double y, double z, Vec3 dir, float damage) {
        this(BlockdustryEntities.FLAK.get(), level);
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

    // 设置溅射参数（由炮台开火时传入）喵
    public void setSplash(float splashDamage, float splashRadius, float explodeRange) {
        this.splashDamage = splashDamage;
        this.splashRadius = splashRadius;
        this.explodeRange = explodeRange;
    }

    @Override
    public void tick() {
        super.tick();
        // 双端都移动：客户端自模拟后 xOld!=getX()，LevelRenderer 的 partialTick 插值立即恢复平滑喵
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.level().isClientSide) {
            return; // 客户端不处理寿命/命中/引爆，等服务端 discard 移除包即可喵
        }
        if (--life <= 0) {
            explode();
            return;
        }
        // 近炸引信：有敌对「空」单位进入 explodeRange 则进入引爆倒计时喵
        if (primeTimer < 0) {
            boolean nearEnemyAir = this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(explodeRange), this::isEnemyAir).stream()
                    .anyMatch(e -> e.distanceToSqr(this.position()) <= explodeRange * explodeRange);
            if (nearEnemyAir) {
                primeTimer = PRIME_DELAY;
            }
        } else if (--primeTimer <= 0) {
            explode();
            return;
        }
        // 直接碰撞空中敌机也引爆（flak 弹体命中）喵
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(0.2), this::canHitEntity)) {
            if (isEnemyAir(target)) {
                explode();
                return;
            }
        }
    }

    // 敌对「空」标签单位判定（flak 只打空中单位）喵
    private boolean isEnemyAir(LivingEntity entity) {
        return TargetType.of(entity) == TargetType.AIR
                && BlockdustryTeams.isHostile(ownerTeam, BlockdustryTeams.getTeam(entity));
    }

    // 引爆：对 splashRadius 内所有敌对空中单位造成溅射伤害 + 播爆炸粒子，然后消失喵
    private void explode() {
        if (this.level().isClientSide || this.isRemoved()) return;
        this.discard();
        AABB box = this.getBoundingBox().inflate(splashRadius);
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, box, this::canHitEntity)) {
            if (isEnemyAir(target) && target.distanceToSqr(this.position()) <= splashRadius * splashRadius) {
                target.hurt(this.damageSources().generic(), splashDamage);
            }
        }
        // 爆炸特效：小爆团 + 烟尘（Mindustry Fx.flakExplosion 近似）喵
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(),
                    1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(),
                    10, 0.4, 0.4, 0.4, 0.05);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target instanceof LivingEntity;
    }
}
