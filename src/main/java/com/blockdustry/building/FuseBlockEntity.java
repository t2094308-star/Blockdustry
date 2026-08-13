package com.blockdustry.building;

import com.blockdustry.entities.FireBulletEntity;
import com.blockdustry.entities.TargetType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

// Mindustry「熔毁」重型霰弹炮塔（fuse）最小移植：3×3、3 发扇形火弹、对地+对空喵
// 忠于原作参数：reload=35、range=90/8=11.25 格、ShootSpread(3,20f)、rotateSpeed 默认 5、targetAir+targetGround=true、
// titanium 弹药 ShrapnelBulletType damage=66、length=(90+10)/8=12.5 格喵
public class FuseBlockEntity extends TurretBlockEntity {
    // 扇形霰弹参数（Mindustry ShootSpread(3, 20f)：3 发，弹间 20°）喵
    private static final int SHOTS = 3;
    private static final float SHOT_SPREAD_DEG = 20f;

    // 方块实体注册用的 (BlockPos, BlockState) 构造器，委托给带类型的完整构造器喵
    public FuseBlockEntity(BlockPos pos, BlockState state) {
        this(FuseArcRegistrar.FUSE_ENTITY.get(), pos, state);
    }

    // 完整构造器：设置 fuse 专属参数喵
    public FuseBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.reload = 35;              // Mindustry fuse reload=35f 喵
        this.range = 11.25f;           // 射程 90/8=11.25 格喵
        this.bulletSpeed = 1.8f;       // shrapnel 瞬击感：高速弹丸（原作 hitscan，MC 折中为高速）喵
        this.bulletDamage = 66f;       // titanium 弹药 damage=66（thorium 105 未启用）喵
        this.shootY = 0.5f;            // 3×3 大炮口前伸 4/8 格喵
        this.rotateSpeedDeg = 5;       // Mindustry BaseTurret 默认 rotateSpeed=5 喵
        this.targetFilter = TargetType.GROUND; // 占位；fuse 实际对地+对空，覆写 canTarget 全锁喵
    }

    // fuse 对空+对地都锁（Mindustry targetAir=true, targetGround=true）：覆写标签过滤为全锁喵
    @Override
    protected boolean canTarget(LivingEntity entity) {
        return true;
    }

    // 转向：以整座建筑中心（3×3 中心 = 锚点 NW +1,+1）为轴，避免从锚点角瞄准导致炮管偏角喵
    @Override
    protected void turnToward(Vec3 targetCenter) {
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        Vec3 center = base.getCenter().add(1, 0, 1);
        double dx = targetCenter.x - center.x;
        double dz = targetCenter.z - center.z;
        if (dx * dx + dz * dz > 1e-6) {
            // 贴图"前"朝反方向，加 180° 修正（与基类一致）喵
            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, -dz));
            aimYaw = moveToward(aimYaw, targetYaw, rotateSpeedDeg);
        }
    }

    // 开火：3 发扇形火弹（相对瞄准方向 ±20°），从建筑中心炮口发射喵
    @Override
    protected void fireAt(Vec3 targetCenter, Vec3 targetVel, int barrel) {
        // 开火原点取整座建筑中心（3×3 中心 = 锚点 NW +1,+1），与渲染转盘中心对齐喵
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        Vec3 center = base.getCenter().add(1, 0, 1);
        Vec3 aim = predict(center, targetCenter, targetVel, bulletSpeed);
        Vec3 dir = aim.subtract(center).normalize();
        Vec3 spawn = center.add(dir.scale(shootY)).add(0, 0.5, 0);
        for (int i = 0; i < SHOTS; i++) {
            // 相对瞄准方向水平展开：-20°, 0°, +20°（Mindustry ShootSpread 弹间 20°）喵
            float ang = (i - (SHOTS - 1) / 2f) * SHOT_SPREAD_DEG;
            Vec3 fireDir = rotateY(dir, ang).add(new Vec3(
                    (level.random.nextDouble() - 0.5) * 0.05,
                    (level.random.nextDouble() - 0.5) * 0.03,
                    (level.random.nextDouble() - 0.5) * 0.05));
            FireBulletEntity bullet = new FireBulletEntity(level,
                    spawn.x, spawn.y, spawn.z, fireDir.scale(bulletSpeed), bulletDamage);
            bullet.setLife((int) (range / bulletSpeed) + 1);
            bullet.setOwnerTeam(getTeam());
            level.addFreshEntity(bullet);
        }
    }

    // 绕 Y 轴旋转向量（度）：把瞄准方向水平展开成扇形喵
    private static Vec3 rotateY(Vec3 v, float degrees) {
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad), sin = Math.sin(rad);
        return new Vec3(v.x * cos + v.z * sin, v.y, -v.x * sin + v.z * cos);
    }
}
