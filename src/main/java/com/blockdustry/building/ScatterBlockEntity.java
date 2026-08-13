package com.blockdustry.building;

import com.blockdustry.entities.FlakBulletEntity;
import com.blockdustry.entities.TargetType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Mindustry「分裂」对空炮塔（Scatter）最小移植：对空标签、flak 霰弹、双连射、大散布、近炸溅射喵
// 忠于原作参数：reload=18、射程 220/8=27.5 格、targetGround=false（只锁空）、shoot.shots=2 + shotDelay=5、
// rotateSpeed=15、inaccuracy=17°；炮弹为 FlakBulletEntity（近炸+溅射，只伤空）喵
public class ScatterBlockEntity extends TurretBlockEntity {
    // 连射参数（Mindustry scatter shoot.shotDelay=5f、shoot.shots=2）喵
    private static final int SHOT_DELAY = 5;
    private static final int SHOTS = 2;
    // flak 溅射参数（lead 弹药：splashDamage=27*1.5≈40，溅射半径 15/8≈1.9 格，近炸范围取 2 格）喵
    private static final float SPLASH_DAMAGE = 40f;
    private static final float SPLASH_RADIUS = 2f;
    private static final float EXPLODE_RANGE = 2f;

    // 已连射计数（第 2 发后进入完整装填）喵
    private int shotsFired;

    // 方块实体注册用的 (BlockPos, BlockState) 构造器，委托给带类型的完整构造器喵
    public ScatterBlockEntity(BlockPos pos, BlockState state) {
        this(BlockdustryBlocks.SCATTER_ENTITY.get(), pos, state);
    }

    // 完整构造器：设置对空炮特有参数喵
    public ScatterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.reload = 18;              // Mindustry scatter reload=18f 喵
        this.range = 27.5f;            // 射程 220/8=27.5 格喵
        this.bulletSpeed = 1.3f;       // flak 弹速（lead 4.2 单位/tick，较 duo 快）喵
        this.bulletDamage = 27f;       // flak 弹体伤害（lead ammo）喵
        this.spread = 0.25f;           // 炮口横偏 2/8 格喵
        this.rotateSpeedDeg = 15;      // Mindustry rotateSpeed=15 喵
        this.targetFilter = TargetType.AIR; // 对空：只锁定「空」标签单位喵
    }

    // 转向：覆盖基类，以整座建筑中心（2×2 中心）为轴，避免从锚点 NW 角瞄准导致炮管偏角喵
    @Override
    protected void turnToward(Vec3 targetCenter) {
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        float half = (getSize() - 1) / 2f;
        Vec3 center = base.getCenter().add(half, 0, half);
        double dx = targetCenter.x - center.x;
        double dz = targetCenter.z - center.z;
        if (dx * dx + dz * dz > 1e-6) {
            // 贴图"前"朝反方向，加 180° 修正（与基类一致）喵
            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, -dz));
            aimYaw = moveToward(aimYaw, targetYaw, rotateSpeedDeg);
        }
    }

    // 每模组 tick：后坐力衰减 + 索敌（只锁空）+ 双连射喵
    @Override
    protected void tickAnchor() {
        // 1) 后坐力线性衰减（recoilTime = reload tick 回零）喵
        float decay = 1f / reload;
        recoilL = Math.max(0, recoilL - decay);
        recoilR = Math.max(0, recoilR - decay);
        recoilTop = Math.max(0, recoilTop - decay);

        // 2) 索敌并转向（对空过滤已由 nearestLiving 的 canTarget 完成；对空炮不锁建筑）喵
        AABB rangeBox = new AABB(worldPosition).inflate(range);
        LivingEntity livingTarget = nearestLiving(rangeBox);
        Vec3 targetCenter = null;
        Vec3 targetVel = Vec3.ZERO;
        if (livingTarget != null) {
            targetCenter = livingTarget.getBoundingBox().getCenter();
            targetVel = livingTarget.getDeltaMovement();
        }
        if (targetCenter != null) {
            turnToward(targetCenter);
        }

        // 3) 装填归零且有目标则开火：连发 2 弹（间隔 SHOT_DELAY），打满进入完整装填喵
        if (cooldown > 0) {
            cooldown--;
        } else if (targetCenter != null) {
            fireFlak(targetCenter, targetVel);
            // 单管后坐力（Mindustry DrawTurret "-mid" 单转盘整体后坐力，渲染用 recoilTop）喵
            recoilR = 1f;
            recoilTop = 1f;
            if (++shotsFired >= SHOTS) {
                shotsFired = 0;
                cooldown = reload;
            } else {
                cooldown = SHOT_DELAY;
            }
            sync(); // 开火必发：客户端本地衰减后坐力需要同步快照喵
        }

        // 4) 转向超过 2° 才发包（静止不发，节省带宽）喵
        if (Math.abs(aimYaw - lastSyncedYaw) > 2f) {
            lastSyncedYaw = aimYaw;
            sync();
        }
    }

    // 发射 flak 霰弹：预判点 + 大散布（Mindustry inaccuracy=17°），带近炸引信与溅射参数喵
    private void fireFlak(Vec3 targetCenter, Vec3 targetVel) {
        // 开火原点取整座建筑中心（2×2 中心 = 锚点 NW +0.5,+0.5），与渲染转盘中心对齐喵
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        float half = (getSize() - 1) / 2f;
        Vec3 center = base.getCenter().add(half, 0, half);
        Vec3 aim = predict(center, targetCenter, targetVel, bulletSpeed);
        Vec3 dir = aim.subtract(center).normalize();
        Vec3 perp = new Vec3(-dir.z, 0, dir.x);
        if (perp.lengthSqr() < 1e-6) perp = new Vec3(1, 0, 0);
        perp = perp.normalize();
        Vec3 spawn = center.add(dir.scale(shootY)).add(perp.scale(spread)).add(0, 0.3, 0);
        Vec3 fireDir = aim.subtract(spawn).normalize();
        // 大散布（较 duo 大 3 倍，忠实 inaccuracy=17° 霰弹风格）喵
        fireDir = fireDir.add(new Vec3(
                (level.random.nextDouble() - 0.5) * 0.3,
                (level.random.nextDouble() - 0.5) * 0.15,
                (level.random.nextDouble() - 0.5) * 0.3));
        FlakBulletEntity bullet = new FlakBulletEntity(level,
                spawn.x, spawn.y, spawn.z, fireDir.scale(bulletSpeed), bulletDamage);
        bullet.setLife((int) (range / bulletSpeed));
        bullet.setOwnerTeam(getTeam());
        bullet.setSplash(SPLASH_DAMAGE, SPLASH_RADIUS, EXPLODE_RANGE);
        level.addFreshEntity(bullet);
    }
}
