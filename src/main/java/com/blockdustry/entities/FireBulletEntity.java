package com.blockdustry.entities;

import java.util.HashSet;
import java.util.Set;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.building.FuseArcRegistrar;
import com.blockdustry.lib.BlockHealthApi;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

// Mindustry fuse 的 ShrapnelBulletType 金属碎片霰弹：3 发扇形弹丸、穿透活体、hitscan 折中为高速飞行。
// 忠于原作参数：titanium 弹药 damage=66、length=(90+10)/8=12.5 格、pierce=true（穿单位）、collides=false（不撞地形）。
// 视觉：白→a9d8ff 长三角金属片（FireBulletRenderer），命中播 Fx.hitLancer 白闪，原版无任何火焰/尾迹粒子喵
public class FireBulletEntity extends Projectile {
    // Fx.hitLancer 白闪寿命 12 tick 喵
    private static final int FLASH_LIFE = 12;

    // 同步数据：命中白闪剩余 tick（服务端递减，客户端渲染）、弹丸起点（炮口，点光 billboard 锚点）、总寿命（fin 渐变）喵
    private static final EntityDataAccessor<Integer> DATA_FLASH =
            SynchedEntityData.defineId(FireBulletEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_SPAWN_X =
            SynchedEntityData.defineId(FireBulletEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SPAWN_Y =
            SynchedEntityData.defineId(FireBulletEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SPAWN_Z =
            SynchedEntityData.defineId(FireBulletEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_MAX_LIFE =
            SynchedEntityData.defineId(FireBulletEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_STOPPED =
            SynchedEntityData.defineId(FireBulletEntity.class, EntityDataSerializers.BOOLEAN);

    private float damage = 66f;         // Mindustry fuse titanium 弹 damage=66 喵
    private int life = 60;              // 剩余寿命（tick），耗尽消失喵
    private BlockdustryTeam ownerTeam = BlockdustryTeam.DERELICT;
    // 已命中的活体（pierce：同一弹丸只伤同一目标一次，但穿过后继续飞行）喵
    private final Set<Integer> hitIds = new HashSet<>();
    // 已命中建筑：停在命中点只播白闪，不再结算伤害喵
    private boolean stopped;

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
        // 记录炮口起点（暖黄点光 / 渲染锚点），随 spawn 包发给客户端喵
        this.entityData.set(DATA_SPAWN_X, (float) x);
        this.entityData.set(DATA_SPAWN_Y, (float) y);
        this.entityData.set(DATA_SPAWN_Z, (float) z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FLASH, 0);
        builder.define(DATA_SPAWN_X, 0f);
        builder.define(DATA_SPAWN_Y, 0f);
        builder.define(DATA_SPAWN_Z, 0f);
        builder.define(DATA_MAX_LIFE, 10);
        builder.define(DATA_STOPPED, false);
    }

    // 设置剩余寿命（由射程/速度决定）；同步总寿命给客户端供渲染渐变喵
    public void setLife(int life) {
        this.life = life;
        this.entityData.set(DATA_MAX_LIFE, life);
    }

    // 客户端渲染用：总寿命（fin = age/maxLife 白→a9d8ff 渐变与 fout 淡出）喵
    public int getMaxLife() {
        return this.entityData.get(DATA_MAX_LIFE);
    }

    // 客户端渲染用：命中白闪剩余 tick（>0 时画 8 根白色辐射短线）喵
    public int getFlashTicks() {
        return this.entityData.get(DATA_FLASH);
    }

    // 客户端渲染用：炮口起点（暖黄点光 billboard 锚点）喵
    public Vec3 getSpawn() {
        return new Vec3(this.entityData.get(DATA_SPAWN_X),
                this.entityData.get(DATA_SPAWN_Y),
                this.entityData.get(DATA_SPAWN_Z));
    }

    // 设置发射者队伍喵
    public void setOwnerTeam(BlockdustryTeam team) {
        this.ownerTeam = team;
    }

    @Override
    public void tick() {
        super.tick();
        // 双端都移动：客户端自模拟后 xOld!=getX()，LevelRenderer 的 partialTick 插值立即恢复平滑喵
        // 命中建筑后服务端同步 DATA_STOPPED，客户端停 move 停在命中点播白闪（否则白闪滑离命中点）喵
        if (this.level().isClientSide) {
            if (!this.entityData.get(DATA_STOPPED)) {
                this.move(MoverType.SELF, this.getDeltaMovement());
            }
            return; // 客户端不处理寿命/命中，等服务端 discard 移除包即可喵
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (--life <= 0) {
            this.discard();
            return;
        }
        // 命中白闪倒计时（同步给客户端渲染）喵
        int flash = this.entityData.get(DATA_FLASH);
        if (flash > 0) {
            this.entityData.set(DATA_FLASH, flash - 1);
        }
        if (stopped) {
            return; // 已命中建筑：停在命中点只播白闪，不再结算伤害喵
        }
        // 穿透活体：命中敌对活体扣血但弹丸继续（Mindustry shrapnel pierce=true），命中点播白闪喵
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(0.2), this::canHitEntity)) {
            if (hitIds.contains(target.getId())) continue;
            if (!BlockdustryTeams.isHostile(ownerTeam, BlockdustryTeams.getTeam(target))) continue;
            hitIds.add(target.getId());
            this.entityData.set(DATA_FLASH, FLASH_LIFE); // Fx.hitLancer 白闪喵
            target.hurt(this.damageSources().generic(), BlockdustryArmor.applyToEntity(target, this.damage));
        }
        // 命中敌对建筑用 BlockHealth 扣血并停止（建筑终结弹道；普通方块穿透，忠于原版弹不撞地形）。
        // 停在命中点播白闪 12 tick 后消失，视觉上还原 hitscan 命中白闪喵
        BlockPos pos = this.blockPosition();
        BlockEntity be = this.level().getBlockEntity(pos);
        if (be instanceof BlockdustryBuildingEntity building
                && BlockdustryTeams.isHostile(ownerTeam, building.getTeam())
                && this.level() instanceof ServerLevel serverLevel) {
            BlockHealthApi.damage(serverLevel, pos, BlockdustryArmor.applyToBuilding(building, this.damage),
                    null, BlockHealthApi.DamageType.PROJECTILE);
            this.stopped = true;
            this.setDeltaMovement(Vec3.ZERO); // 停在命中点，白闪原地播完喵
            this.life = FLASH_LIFE;           // 12 tick 后消失喵
            this.entityData.set(DATA_FLASH, FLASH_LIFE);
            this.entityData.set(DATA_STOPPED, true); // 同步客户端：停 move，白闪停在命中点喵
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target instanceof LivingEntity;
    }
}
