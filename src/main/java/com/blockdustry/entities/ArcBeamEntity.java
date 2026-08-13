package com.blockdustry.entities;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.building.BlockdustryBuildings;
import com.blockdustry.building.FuseArcRegistrar;
import com.blockdustry.lib.BlockHealthApi;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Mindustry arc 的 LightningBulletType 电弧：瞬时闪电链，对路径上敌对目标造成伤害喵
// 忠于原作参数：damage=20、collidesAir=false（只伤地面单位与建筑）、buildingDamageMultiplier=0.25 喵
public class ArcBeamEntity extends Projectile {
    // 终点三轴（随 spawn 包同步到客户端，供渲染器画闪电链；此 MC 版无 DOUBLE 序列化器，用 FLOAT 精度足够）喵
    private static final EntityDataAccessor<Float> DATA_END_X =
            SynchedEntityData.defineId(ArcBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Y =
            SynchedEntityData.defineId(ArcBeamEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Z =
            SynchedEntityData.defineId(ArcBeamEntity.class, EntityDataSerializers.FLOAT);
    // 电弧视觉停留时长（Mindustry lightning lifetime 近似）喵
    private static final int VISUAL_LIFE = 10;

    private float damage = 20f;         // Mindustry arc lightning damage=20 喵
    private BlockdustryTeam ownerTeam = BlockdustryTeam.DERELICT;
    private int life = VISUAL_LIFE;
    private boolean damageDealt = false;

    // 客户端重建构造：必须也设 noPhysics 喵
    public ArcBeamEntity(EntityType<ArcBeamEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // 业务构造：炮口起点 from → 命中点 to，瞬时伤害喵
    public ArcBeamEntity(Level level, Vec3 from, Vec3 to, float damage) {
        this(FuseArcRegistrar.ARC_BEAM.get(), level);
        this.setPos(from.x, from.y, from.z);
        this.noPhysics = true;
        this.damage = damage;
        this.setEnd(to);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_END_X, 0f);
        builder.define(DATA_END_Y, 0f);
        builder.define(DATA_END_Z, 0f);
    }

    // 服务端开火时设置终点（存进同步数据，随 spawn 包发给客户端）喵
    public void setEnd(Vec3 end) {
        this.entityData.set(DATA_END_X, (float) end.x);
        this.entityData.set(DATA_END_Y, (float) end.y);
        this.entityData.set(DATA_END_Z, (float) end.z);
    }

    // 客户端渲染用终点（闪电链从 position() 到该点）喵
    public Vec3 getEnd() {
        return new Vec3(this.entityData.get(DATA_END_X), this.entityData.get(DATA_END_Y), this.entityData.get(DATA_END_Z));
    }

    // 设置发射者队伍喵
    public void setOwnerTeam(BlockdustryTeam team) {
        this.ownerTeam = team;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return; // 客户端只等渲染/移除包喵
        }
        if (--life <= 0) {
            this.discard();
            return;
        }
        // 首次服务端 tick 结算瞬时伤害，之后纯视觉停留喵
        if (!damageDealt) {
            damageDealt = true;
            damageAlongSegment();
        }
    }

    // 沿炮口→终点的线段，对路径附近敌对活体扣血、敌对建筑按 0.25 系数扣血喵
    private void damageAlongSegment() {
        Vec3 start = this.position();
        Vec3 end = this.getEnd();
        Vec3 seg = end.subtract(start);
        double len = seg.length();
        if (len < 1e-4) return;
        Vec3 dir = seg.normalize();
        double reachSq = 1.5 * 1.5; // 命中判定半径 1.5 格（闪电链范围）喵
        AABB box = new AABB(start, end).inflate(1.5);
        // 活体：只伤地面（arc collidesAir=false；对空单位标签 AIR 过滤掉）喵
        for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, box, this::canHitEntity)) {
            if (TargetType.of(e) == TargetType.AIR) continue;
            if (!BlockdustryTeams.isHostile(ownerTeam, BlockdustryTeams.getTeam(e))) continue;
            Vec3 c = e.getBoundingBox().getCenter();
            double t = c.subtract(start).dot(dir);
            if (t < -1 || t > len + 1) continue;
            Vec3 closest = start.add(dir.scale(Math.max(0, Math.min(t, len))));
            if (closest.distanceToSqr(c) <= reachSq) {
                e.hurt(this.damageSources().generic(), BlockdustryArmor.applyToEntity(e, this.damage));
            }
        }
        // 建筑：Mindustry arc buildingDamageMultiplier=0.25，按 25% 伤害结算喵
        if (this.level() instanceof ServerLevel serverLevel) {
            float bdmg = this.damage * 0.25f;
            for (BlockdustryBuildingEntity b : BlockdustryBuildings.all()) {
                if (b.isRemoved()) continue;
                if (!BlockdustryTeams.isHostile(ownerTeam, b.getTeam())) continue;
                Vec3 c = b.getBlockPos().getCenter();
                double t = c.subtract(start).dot(dir);
                if (t < -1 || t > len + 1) continue;
                Vec3 closest = start.add(dir.scale(Math.max(0, Math.min(t, len))));
                if (closest.distanceToSqr(c) <= reachSq) {
                    BlockHealthApi.damage(serverLevel, b.getBlockPos(),
                            BlockdustryArmor.applyToBuilding(b, bdmg), null, BlockHealthApi.DamageType.PROJECTILE);
                }
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target instanceof LivingEntity;
    }
}
