package com.blockdustry.entities;

import java.util.HashSet;
import java.util.Set;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.building.FuseArcRegistrar;
import com.blockdustry.lib.BlockHealthApi;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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

// Mindustry fuse 的 ShrapnelBulletType 霰弹（火弹）：3 发扇形弹丸、穿透活体、火色尾迹喵
// 忠于原作参数：titanium 弹药 damage=66、length=(90+10)/8=12.5 格、pierce=true（穿单位）、collides=false（不撞地形）喵
public class FireBulletEntity extends Projectile {
    private float damage = 66f;         // Mindustry fuse titanium 弹 damage=66 喵
    private int life = 60;              // 剩余寿命（tick），耗尽消失喵
    private BlockdustryTeam ownerTeam = BlockdustryTeam.DERELICT;
    // 已命中的活体（pierce：同一弹丸只伤同一目标一次，但穿过后继续飞行）喵
    private final Set<Integer> hitIds = new HashSet<>();

    // 客户端重建构造：必须也设 noPhysics，否则客户端 move() 会撞方块与服务端分歧喵
    public FireBulletEntity(EntityType<FireBulletEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // 业务构造：给定出生点、方向速度与伤害；无视方块碰撞（穿透飞行）喵
    public FireBulletEntity(Level level, double x, double y, double z, Vec3 dir, float damage) {
        this(FuseArcRegistrar.FIRE_BULLET.get(), level);
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
            spawnFireTrail();
            return; // 客户端不处理寿命/命中，等服务端 discard 移除包即可喵
        }
        if (--life <= 0) {
            this.discard();
            return;
        }
        // 穿透活体：命中敌对活体扣血但弹丸继续（Mindustry shrapnel pierce=true）喵
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(0.2), this::canHitEntity)) {
            if (hitIds.contains(target.getId())) continue;
            if (!BlockdustryTeams.isHostile(ownerTeam, BlockdustryTeams.getTeam(target))) continue;
            hitIds.add(target.getId());
            target.hurt(this.damageSources().generic(), BlockdustryArmor.applyToEntity(target, this.damage));
        }
        // 命中敌对建筑用 BlockHealth 扣血并停止（建筑终结弹道）；普通方块穿透（忠于 Mindustry 弹不撞地形）喵
        BlockPos pos = this.blockPosition();
        BlockEntity be = this.level().getBlockEntity(pos);
        if (be instanceof BlockdustryBuildingEntity building
                && BlockdustryTeams.isHostile(ownerTeam, building.getTeam())
                && this.level() instanceof ServerLevel serverLevel) {
            BlockHealthApi.damage(serverLevel, pos, BlockdustryArmor.applyToBuilding(building, this.damage),
                    null, BlockHealthApi.DamageType.PROJECTILE);
            this.discard();
        }
    }

    // 客户端火焰尾迹粒子（Mindustry Fx.fireballsmoke 近似）：每几 tick 喷一颗火焰喵
    private void spawnFireTrail() {
        if (this.random.nextInt(3) != 0) return;
        this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(),
                (this.random.nextDouble() - 0.5) * 0.03, 0.03,
                (this.random.nextDouble() - 0.5) * 0.03);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target instanceof LivingEntity;
    }
}
