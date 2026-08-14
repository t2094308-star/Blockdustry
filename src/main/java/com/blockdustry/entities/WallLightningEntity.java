package com.blockdustry.entities;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.building.BlockdustryBuildings;
import com.blockdustry.defense.AdvancedWallRegistrar;
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

// Mindustry surge-wall 受击放电弧（Blocks.java surgeWall lightningChance=0.05、Wall.lightningDamage=20）喵。
// 原版 Wall.WallBuild.collision：子弹命中时 5% 概率从墙向子弹来向放一道 Lightning（lightningColor=Pal.surge #f3e979）喵。
// 本实体：从墙中心沿子弹来向释放短闪电链，对路径敌对活体/建筑全额扣血（无 arc 的 0.25 建筑系数）喵。
// 视觉黄白 Pal.surge，由 WallLightningRenderer 渲染喵。
public class WallLightningEntity extends Projectile {
    // 终点三轴（随 spawn 包同步到客户端，供渲染器画闪电链）喵
    private static final EntityDataAccessor<Float> DATA_END_X =
            SynchedEntityData.defineId(WallLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Y =
            SynchedEntityData.defineId(WallLightningEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Z =
            SynchedEntityData.defineId(WallLightningEntity.class, EntityDataSerializers.FLOAT);
    // 电弧视觉停留时长（tick）喵
    private static final int VISUAL_LIFE = 8;

    private float damage = 20f;         // Mindustry Wall.lightningDamage=20 喵
    private BlockdustryTeam ownerTeam = BlockdustryTeam.DERELICT;
    private int life = VISUAL_LIFE;
    private boolean damageDealt = false;

    // 客户端重建构造：必须也设 noPhysics 喵
    public WallLightningEntity(EntityType<WallLightningEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // 业务构造：墙中心 from → 放电终点 to，瞬时伤害喵
    public WallLightningEntity(Level level, Vec3 from, Vec3 to, float damage) {
        this(AdvancedWallRegistrar.WALL_LIGHTNING.get(), level);
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

    // 设置施放队伍（判定伤害归属）喵
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

    // 沿墙中心→终点的线段，对路径附近敌对活体/建筑全额扣血（surge 闪电无建筑系数）喵
    private void damageAlongSegment() {
        Vec3 start = this.position();
        Vec3 end = this.getEnd();
        Vec3 seg = end.subtract(start);
        double len = seg.length();
        if (len < 1e-4) return;
        Vec3 dir = seg.normalize();
        double reachSq = 1.5 * 1.5; // 命中判定半径 1.5 格（闪电链范围）喵
        AABB box = new AABB(start, end).inflate(1.5);
        // 活体：只伤地面单位（Mindustry Lightning collidesAir=false 语义）喵
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
        // 建筑：全额伤害（Mindustry Lightning 对建筑无 buildingDamageMultiplier）喵
        if (this.level() instanceof ServerLevel serverLevel) {
            for (BlockdustryBuildingEntity b : BlockdustryBuildings.all()) {
                if (b.isRemoved()) continue;
                if (!BlockdustryTeams.isHostile(ownerTeam, b.getTeam())) continue;
                Vec3 c = b.getBlockPos().getCenter();
                double t = c.subtract(start).dot(dir);
                if (t < -1 || t > len + 1) continue;
                Vec3 closest = start.add(dir.scale(Math.max(0, Math.min(t, len))));
                if (closest.distanceToSqr(c) <= reachSq) {
                    BlockHealthApi.damage(serverLevel, b.getBlockPos(),
                            BlockdustryArmor.applyToBuilding(b, this.damage), null, BlockHealthApi.DamageType.PROJECTILE);
                }
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target instanceof LivingEntity;
    }
}
