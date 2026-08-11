package com.blockdustry.building;

import java.util.Comparator;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.entities.BlockdustryBulletEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// 演示级 duo 双管炮塔方块实体：双管交替开火，预判拦截，索敌活体与敌方建筑，忠于 Mindustry 参数喵
public class TurretBlockEntity extends BlockdustryBuildingEntity {
    // Mindustry duo 参数（Mindustry 1格=8单位，换算到 MC 1格=1单位）喵
    private static final int RELOAD = 20;          // reload=20tick，3发/秒喵
    private static final float RANGE = 20f;        // 射程 160/8=20 格喵
    private static final float SHOOT_Y = 0.375f;   // 炮口前伸 3/8 格喵
    private static final float SPREAD = 0.4375f;   // 双管横偏 3.5/8 格喵
    private static final float BULLET_SPEED = 0.8f;
    private static final float BULLET_DAMAGE = 9f;   // Mindustry duo 铜弹 damage=9 喵

    // 装填计时与当前管（0=右管，1=左管，交替）喵
    private int cooldown;
    private int barrel;

    // 方块实体注册用的 (BlockPos, BlockState) 构造器，委托给带类型的完整构造器喵
    public TurretBlockEntity(BlockPos pos, BlockState state) {
        this(BlockdustryBlocks.TURRET_ENTITY.get(), pos, state);
    }

    // 完整构造器：把方块实体类型传给基类喵
    public TurretBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 每模组 tick（仅锚点格）：装填计时，归零后索敌（活体+建筑）用当前管开火并换管喵
    @Override
    protected void tickAnchor() {
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        AABB range = new AABB(worldPosition).inflate(RANGE);
        LivingEntity livingTarget = nearestLiving(range);
        BlockdustryBuildingEntity buildingTarget = nearestBuilding(range);
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
            fireAt(targetCenter, targetVel, barrel);
            barrel = 1 - barrel;
            cooldown = RELOAD;
        }
    }

    // 最近的敌对活体目标喵
    private LivingEntity nearestLiving(AABB range) {
        return level.getEntitiesOfClass(LivingEntity.class, range, this::isEnemyOf).stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(worldPosition.getCenter())))
                .orElse(null);
    }

    // 最近的敌对方块（建筑）目标，遍历建筑管理器；DERELICT 炮塔也攻击所有非 DERELICT 建筑喵
    private BlockdustryBuildingEntity nearestBuilding(AABB range) {
        BlockdustryBuildingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockdustryBuildingEntity b : BlockdustryBuildings.all()) {
            if (b.isRemoved() || b.getBlockPos().distSqr(worldPosition) > RANGE * RANGE) continue;
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
    private boolean isEnemyOf(LivingEntity entity) {
        return BlockdustryTeams.isHostile(getTeam(), BlockdustryTeams.getTeam(entity));
    }

    // 从指定管（±SPREAD 横偏）的炮口朝预判点发射炮弹，下一轮换管喵
    private void fireAt(Vec3 targetCenter, Vec3 targetVel, int barrel) {
        Vec3 center = worldPosition.getCenter();
        Vec3 aim = predict(center, targetCenter, targetVel, BULLET_SPEED);
        Vec3 dir = aim.subtract(center).normalize();
        Vec3 perp = new Vec3(-dir.z, 0, dir.x);
        if (perp.lengthSqr() < 1e-6) perp = new Vec3(1, 0, 0);
        perp = perp.normalize();
        float sign = barrel == 0 ? 1f : -1f;
        Vec3 spawn = center.add(dir.scale(SHOOT_Y)).add(perp.scale(sign * SPREAD)).add(0, 0.3, 0);
        Vec3 fireDir = aim.subtract(spawn).normalize();
        fireDir = fireDir.add(new Vec3(
                (level.random.nextDouble() - 0.5) * 0.1,
                (level.random.nextDouble() - 0.5) * 0.05,
                (level.random.nextDouble() - 0.5) * 0.1));
        BlockdustryBulletEntity bullet = new BlockdustryBulletEntity(level,
                spawn.x, spawn.y, spawn.z, fireDir.scale(BULLET_SPEED), BULLET_DAMAGE);
        bullet.setLife((int) (RANGE / BULLET_SPEED));
        bullet.setOwnerTeam(getTeam());
        level.addFreshEntity(bullet);
    }

    // Mindustry Predict.intercept：解 |P+V·t-S|=v·t 二次方程取最小正根外推目标，无正解回退当前位置喵
    private Vec3 predict(Vec3 src, Vec3 dst, Vec3 vel, double speed) {
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
