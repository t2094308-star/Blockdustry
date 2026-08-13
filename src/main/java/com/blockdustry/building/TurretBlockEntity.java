package com.blockdustry.building;

import java.util.Comparator;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.entities.BlockdustryBulletEntity;
import com.blockdustry.entities.TargetType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// 演示级 duo 双管炮塔方块实体：双管交替开火，预判拦截，索敌活体与敌方建筑，忠于 Mindustry 参数喵
// 也是所有陆/空炮台的骨架基类：参数为可覆盖实例字段，索敌按 targetFilter 过滤目标标签喵
public class TurretBlockEntity extends BlockdustryBuildingEntity {
    // —— 可配置炮塔参数（子类可覆盖；Scatter 等对空炮复用骨架）——
    protected int reload = 20;            // Mindustry duo reload=20f，换算 MC 装填 20tick 喵
    protected float range = 20f;          // 射程 160/8=20 格（Mindustry 1格=8单位）喵
    protected float shootY = 0.375f;      // 炮口前伸 3/8 格喵
    protected float spread = 0.4375f;     // 双管横偏 3.5/8 格喵
    protected float bulletSpeed = 0.8f;
    protected float bulletDamage = 9f;    // Mindustry duo 铜弹 damage=9 喵
    protected int rotateSpeedDeg = 10;    // 转向速度 10°/tick（Mindustry rotateSpeed=10）喵
    protected TargetType targetFilter = TargetType.GROUND; // 炮台标签：duo=对地，不主动锁空喵

    // 装填计时与当前管（0=右管，1=左管，交替）喵
    protected int cooldown;
    protected int barrel;

    // 炮塔动画（Mindustry DrawTurret）：朝向 + 双管/整体后坐力喵
    protected float aimYaw;             // 渲染角（度），贴图"前"方向校准见 docs/研究-炮塔动画.md 喵
    protected float recoilL, recoilR;   // 两管后坐力 0..1，开火置 1、reload tick 线性回零（curRecoils）喵
    protected float recoilTop;          // 整体后坐力（curRecoil），转盘本体位移喵
    protected float lastSyncedYaw = Float.MAX_VALUE; // 上次同步朝向，>2° 才发包喵
    protected long lastSyncTick;        // 客户端收到同步的时刻，供渲染器本地衰减后坐力喵

    // —— 炮台附身手动模式（由 TurretPossessManager 驱动）——
    private boolean manualMode;
    private float manualAimYaw;
    private float manualAimPitch; // 玩家俯仰角（度），向下看为正，开火方向带 y 分量喵
    private boolean manualFire;

    // 方块实体注册用的 (BlockPos, BlockState) 构造器，委托给带类型的完整构造器喵
    public TurretBlockEntity(BlockPos pos, BlockState state) {
        this(BlockdustryBlocks.TURRET_ENTITY.get(), pos, state);
    }

    // 完整构造器：把方块实体类型传给基类喵
    public TurretBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 每模组 tick（仅锚点格）：每 tick 衰减后坐力 + 索敌转向（冷却中也转） + 装填归零开火喵
    @Override
    protected void tickAnchor() {
        // 附身手动模式覆盖自动 AI 喵
        if (manualMode) {
            tickManual();
            return;
        }
        // 1) 后坐力线性衰减（recoilTime = reload tick 回零，Mindustry approachDelta）喵
        float decay = 1f / reload;
        recoilL = Math.max(0, recoilL - decay);
        recoilR = Math.max(0, recoilR - decay);
        recoilTop = Math.max(0, recoilTop - decay);

        // 2) 索敌并转向（即使冷却中也持续转向，Mindustry turnToTarget）喵
        AABB rangeBox = new AABB(worldPosition).inflate(range);
        LivingEntity livingTarget = nearestLiving(rangeBox);
        BlockdustryBuildingEntity buildingTarget = nearestBuilding(rangeBox);
        Vec3 targetCenter = null;
        Vec3 targetVel = Vec3.ZERO;
        if (livingTarget != null && (buildingTarget == null
                || livingTarget.distanceToSqr(worldPosition.getCenter()) <= buildingTarget.getBlockPos().distSqr(worldPosition))) {
            targetCenter = livingTarget.getBoundingBox().getCenter();
            targetVel = livingTarget.getDeltaMovement();
        } else if (buildingTarget != null) {
            targetCenter = buildingTarget.getBlockPos().getCenter();
        }
        if (targetCenter != null) {
            turnToward(targetCenter);
        }

        // 3) 装填归零且有目标则开火喵
        if (cooldown > 0) {
            cooldown--;
        } else if (targetCenter != null) {
            fireAt(targetCenter, targetVel, barrel);
            // 当前管后坐力置 1（barrel 0=右管→recoilR，1=左管→recoilL）+ 整体后坐力置 1 喵
            if (barrel == 0) recoilR = 1f; else recoilL = 1f;
            recoilTop = 1f;
            barrel = 1 - barrel;
            cooldown = reload;
            sync(); // 开火必发：客户端本地衰减后坐力需要同步快照喵
        }

        // 4) 转向超过 2° 才发包（静止不发，节省带宽）喵
        if (Math.abs(aimYaw - lastSyncedYaw) > 2f) {
            lastSyncedYaw = aimYaw;
            sync();
        }
    }

    // 转向：水平角匀速逼近目标角（含最短角差）喵
    protected void turnToward(Vec3 targetCenter) {
        double dx = targetCenter.x - worldPosition.getCenter().x;
        double dz = targetCenter.z - worldPosition.getCenter().z;
        if (dx * dx + dz * dz > 1e-6) {
            // duo 贴图"前"朝反方向，加 180° 修正（研究-炮塔动画.md 角度校准）喵
            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, -dz));
            aimYaw = moveToward(aimYaw, targetYaw, rotateSpeedDeg);
        }
    }

    // 最短角差逼近（Mindustry Angles.moveToward）喵
    protected static float moveToward(float from, float to, float step) {
        float diff = ((to - from) % 360f + 540f) % 360f - 180f;
        if (Math.abs(diff) <= step) return to;
        return from + Math.copySign(step, diff);
    }

    // 同步客户端（getUpdateTag 携带 aimYaw/recoil，客户端 loadAdditional 收到）喵
    protected void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // —— 动画 getter（渲染器用）——
    public float getAimYaw() { return aimYaw; }
    public float getRecoilL() { return recoilL; }
    public float getRecoilR() { return recoilR; }
    public float getRecoilTop() { return recoilTop; }
    public long lastSyncTick() { return lastSyncTick; }
    // 装填周期（渲染器用后坐力衰减时长）喵
    public int getReload() { return reload; }
    // 炮台标签（Jade/调试可读）喵
    public TargetType getTargetFilter() { return targetFilter; }

    // —— 附身控制入口（TurretPossessManager 调用）——
    public void setManualMode(boolean manual) {
        this.manualMode = manual;
        if (manual) {
            this.manualAimYaw = this.aimYaw;
            this.manualAimPitch = 0f;
        }
        if (!manual) this.manualFire = false;
        sync();
    }

    public boolean isManualMode() {
        return manualMode;
    }

    // 玩家视角 yaw/pitch 换算出的炮塔目标角（度），pitch 向下看为正喵
    public void setManualAim(float yaw, float pitch) {
        this.manualAimYaw = yaw;
        this.manualAimPitch = pitch;
    }

    // 请求开火（下一 tick 消费）喵
    public void requestManualFire() {
        this.manualFire = true;
    }

    // 射程（穿透视野/状态包用）喵
    public float getRange() {
        return range;
    }

    // 手动模式 tick：后坐力衰减 + 匀速转向 + 冷却就绪开火（沿 aimYaw 水平方向）喵
    private void tickManual() {
        float decay = 1f / reload;
        recoilL = Math.max(0, recoilL - decay);
        recoilR = Math.max(0, recoilR - decay);
        recoilTop = Math.max(0, recoilTop - decay);
        aimYaw = moveToward(aimYaw, manualAimYaw, rotateSpeedDeg);
        if (cooldown > 0) cooldown--;
        if (manualFire && cooldown <= 0) {
            // 开火方向含俯仰分量：pitch>0（向下看）→ dir.y<0 朝下，与玩家视线一致喵
            double yawRad = Math.toRadians(aimYaw);
            double pitchRad = Math.toRadians(manualAimPitch);
            Vec3 dir = new Vec3(
                    -Math.sin(yawRad) * Math.cos(pitchRad),
                    -Math.sin(pitchRad),
                    -Math.cos(yawRad) * Math.cos(pitchRad));
            // 瞄准点以附身玩家眼睛为基准（沿视线 range 格处）：弹道终点落在准星上，
            // 否则以炮塔中心高度为基准时，弹道恒定比玩家准星低约 2 格（眼睛钉在塔顶），瞄准对不上喵
            Vec3 target = possessorEye().add(dir.scale(range));
            fireAt(target, Vec3.ZERO, barrel);
            if (barrel == 0) recoilR = 1f; else recoilL = 1f;
            recoilTop = 1f;
            barrel = 1 - barrel;
            cooldown = reload;
            sync();
        }
        manualFire = false;
        if (Math.abs(aimYaw - lastSyncedYaw) > 2f) {
            lastSyncedYaw = aimYaw;
            sync();
        }
    }

    // 附身瞄准基准：找到被钉在炮塔正上方的附身玩家（NBT 标记 bd_possessing，由 TurretPossessManager 维护），
    // 以其眼睛位置作为弹道终点基准，让子弹落在玩家准星处；找不到时回退常见站位喵
    private Vec3 possessorEye() {
        if (level instanceof ServerLevel serverLevel) {
            for (ServerPlayer p : serverLevel.getEntitiesOfClass(ServerPlayer.class,
                    new AABB(worldPosition).inflate(7))) {
                if (p.getPersistentData().getBoolean("bd_possessing")) {
                    return p.getEyePosition();
                }
            }
        }
        // 回退：炮塔中心上 2.12 格（玩家脚底钉塔顶 y+1、眼睛 +1.62、减去塔心 0.5 = 2.12）喵
        return worldPosition.getCenter().add(0, 2.12, 0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putFloat("bd_aim_yaw", aimYaw);
        tag.putFloat("bd_recoil_l", recoilL);
        tag.putFloat("bd_recoil_r", recoilR);
        tag.putFloat("bd_recoil_top", recoilTop);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        aimYaw = tag.getFloat("bd_aim_yaw");
        recoilL = tag.getFloat("bd_recoil_l");
        recoilR = tag.getFloat("bd_recoil_r");
        recoilTop = tag.getFloat("bd_recoil_top");
        // 客户端收到同步时记录时刻，渲染器据此本地衰减后坐力喵
        if (level != null && level.isClientSide) lastSyncTick = level.getGameTime();
    }

    // 目标标签过滤：对地炮不锁定 AIR 单位；对空炮只锁定 AIR 单位喵
    protected boolean canTarget(LivingEntity entity) {
        return targetFilter.canTarget(TargetType.of(entity));
    }

    // 最近的敌对活体目标（按本炮 targetFilter 过滤标签）喵
    protected LivingEntity nearestLiving(AABB range) {
        return level.getEntitiesOfClass(LivingEntity.class, range, this::isTargetableLiving).stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(worldPosition.getCenter())))
                .orElse(null);
    }

    // 敌对活体判定：敌对 + 标签可锁定喵
    private boolean isTargetableLiving(LivingEntity entity) {
        return isEnemyOf(entity) && canTarget(entity);
    }

    // 最近的敌对方块（建筑）目标，遍历建筑管理器；DERELICT 炮塔也攻击所有非 DERELICT 建筑喵
    // 对空炮（AIR 过滤）不锁定地面建筑（Mindustry targetGround=false 语义）喵
    protected BlockdustryBuildingEntity nearestBuilding(AABB range) {
        if (targetFilter == TargetType.AIR) return null;
        BlockdustryBuildingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockdustryBuildingEntity b : BlockdustryBuildings.all()) {
            if (b.isRemoved() || b.getBlockPos().distSqr(worldPosition) > this.range * this.range) continue;
            if (!BlockdustryTeams.isHostile(getTeam(), b.getTeam())) continue;
            double d = b.getBlockPos().distSqr(worldPosition);
            if (d < bestDist) {
                bestDist = d;
                best = b;
            }
        }
        return best;
    }

    // 判断实体是否与本建筑敌对（DERELICT 炮塔也攻击所有非 DERELICT 生物）喵
    protected boolean isEnemyOf(LivingEntity entity) {
        return BlockdustryTeams.isHostile(getTeam(), BlockdustryTeams.getTeam(entity));
    }

    // 从指定管（±SPREAD 横偏）的炮口朝预判点发射炮弹，下一轮换管喵
    protected void fireAt(Vec3 targetCenter, Vec3 targetVel, int barrel) {
        Vec3 center = worldPosition.getCenter();
        Vec3 aim = predict(center, targetCenter, targetVel, bulletSpeed);
        Vec3 dir = aim.subtract(center).normalize();
        Vec3 perp = new Vec3(-dir.z, 0, dir.x);
        if (perp.lengthSqr() < 1e-6) perp = new Vec3(1, 0, 0);
        perp = perp.normalize();
        float sign = barrel == 0 ? 1f : -1f;
        Vec3 spawn = center.add(dir.scale(shootY)).add(perp.scale(sign * spread)).add(0, 0.3, 0);
        Vec3 fireDir = aim.subtract(spawn).normalize();
        fireDir = fireDir.add(new Vec3(
                (level.random.nextDouble() - 0.5) * 0.1,
                (level.random.nextDouble() - 0.5) * 0.05,
                (level.random.nextDouble() - 0.5) * 0.1));
        BlockdustryBulletEntity bullet = new BlockdustryBulletEntity(level,
                spawn.x, spawn.y, spawn.z, fireDir.scale(bulletSpeed), bulletDamage);
        bullet.setLife((int) (range / bulletSpeed));
        bullet.setOwnerTeam(getTeam());
        level.addFreshEntity(bullet);
    }

    // Mindustry Predict.intercept：解 |P+V·t-S|=v·t 二次方程取最小正根外推目标，无正解回退当前位置喵
    protected Vec3 predict(Vec3 src, Vec3 dst, Vec3 vel, double speed) {
        Vec3 offset = dst.subtract(src);
        double a = vel.lengthSqr() - speed * speed;
        double b = 2 * vel.dot(offset);
        double c = offset.lengthSqr();
        double t0 = 0, t1 = 0;
        boolean hasRoots;
        if (Math.abs(a) < 1e-6) {
            if (Math.abs(b) >= 1e-6) {
                t0 = t1 = -c / b;
                hasRoots = true;
            } else {
                hasRoots = false;
            }
        } else {
            double disc = b * b - 4 * a * c;
            if (disc >= 0) {
                double sqrt = Math.sqrt(disc);
                double twoA = 2 * a;
                t0 = (-b - sqrt) / twoA;
                t1 = (-b + sqrt) / twoA;
                hasRoots = true;
            } else {
                hasRoots = false;
            }
        }
        if (!hasRoots) return dst;
        double t = Math.min(t0, t1);
        if (t < 0) t = Math.max(t0, t1);
        if (t <= 0) return dst;
        return dst.add(vel.scale(t));
    }
}
