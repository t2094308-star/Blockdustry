package com.blockdustry.building;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.blockdustry.entities.BlockdustryBulletEntity;
import com.blockdustry.entities.FireBulletEntity;
import com.blockdustry.entities.FlakBulletEntity;
import com.blockdustry.item.BlockdustryItems;
import com.blockdustry.power.BlockdustryPowerNode;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

// 力墙投影（Mindustry force-projector，ForceProjector size 3）：六边形力场护盾，拦截敌对子弹并蓄力。
// 数据忠于原版 Blocks.java L1964 + ForceProjector.java：radius=101.7（=12.7125 格）、sides=6、shieldRotation=0、
// shieldHealth=750、phaseShieldBoost=400、phaseRadiusBoost=80、cooldownNormal=1.5、cooldownBrokenBase=0.35、
// phaseUseTime=350、耗电 4、吃相织布 boost、broken 初始 true。
// 机制：拦截进入六边形的敌对子弹（反射读伤害折算 buildup），buildup 超过阈值 → broken 破碎动画（Fx.shieldBreak 40 tick），
// broken 时护盾消失并快速回冷却。冷却液（ConsumeCoolant）因本 mod 无液体系统省略，cooldownNormal 保留喵。
// 特效：护盾绘制（Fill.poly/Lines.poly 六边形，受击 hit 闪白）+ 蓄力 top 加色 + 破碎扩散 + 拦截点光点（Fx.absorb 12 tick）喵
public class ForceProjectorBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final float RADIUS = 101.7f;          // Mindustry radius=101.7 单位 → 12.7125 格喵
    private static final float PHASE_RADIUS_BOOST = 80f; // Mindustry phaseRadiusBoost=80 单位 → 10 格喵
    private static final float SHIELD_HEALTH = 750f;     // Mindustry shieldHealth=750f 喵
    private static final float PHASE_SHIELD_BOOST = 400f; // Mindustry phaseShieldBoost=400f（默认）喵
    private static final float COOLDOWN_NORMAL = 1.5f;   // Mindustry cooldownNormal=1.5f 喵
    private static final float COOLDOWN_BROKEN_BASE = 0.35f; // Mindustry cooldownBrokenBase=0.35f 喵
    private static final float PHASE_USE_TIME = 350f;    // Mindustry phaseUseTime=350f（吃相织布周期）喵
    private static final int SIDES = 6;                  // Mindustry sides=6 喵
    private static final float POWER_NEEDED = 4f;        // Mindustry consumePower(4f) 喵
    private static final int CAPACITY = 10;              // Mindustry Building 默认 itemCapacity 10 喵
    private static final int BREAK_EFFECT_LIFE = 40;     // Fx.shieldBreak 寿命 40 tick 喵
    private static final int ABSORB_EFFECT_LIFE = 12;    // Fx.absorb 寿命 12 tick 喵
    private static final int MAX_ABSORB_POINTS = 5;      // 同步拦截点上限（特效缓存）喵
    private static final float SHIELD_HEIGHT = 5f;       // 3D 护盾判定高度（基座上方格数）喵

    // 拦截点特效（Fx.absorb 光点）：时刻 + 世界坐标喵
    public static final class AbsorbPoint {
        public final long time;
        public final float x, y, z;

        public AbsorbPoint(long time, float x, float y, float z) {
            this.time = time;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private boolean broken = true;   // 原版 broken 初始 true 喵
    private float buildup;           // 蓄力（当前护盾承受伤害量）喵
    private float radscl;            // 半径比例（broken→0，否则→warmup）喵
    private float warmup;            // 启动预热（→efficiency）喵
    private float phaseHeat;         // 有相织布 0..1（范围/强度加成）喵
    private float hit;               // 受击白闪 1→0（1/5 每 tick 衰减）喵
    private float powerStatus;       // 电网满足率 0..1 喵
    private long breakStartGameTime = -1;  // 破碎动画起点（Fx.shieldBreak 起点）喵
    private float breakRadius;             // 破碎时护盾半径（扩散起始）喵
    private final List<AbsorbPoint> absorbPoints = new ArrayList<>(); // 拦截点特效（渲染用）喵
    private float lastSyncedWarmup = -1f;  // 同步游标，warmup 变化超过阈值才发包喵

    public ForceProjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ForceProjectorRegistrar.FORCE_PROJECTOR_ENTITY.get(), pos, state);
    }

    public ForceProjectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // —— 渲染 getter ——
    public boolean isBroken() {
        return broken;
    }

    public float getBuildup() {
        return buildup;
    }

    public float getPhaseHeat() {
        return phaseHeat;
    }

    public float getHit() {
        return hit;
    }

    public float getWarmup() {
        return warmup;
    }

    public long getBreakStartGameTime() {
        return breakStartGameTime;
    }

    public float getBreakRadius() {
        return breakRadius;
    }

    public List<AbsorbPoint> getAbsorbPoints() {
        return absorbPoints;
    }

    public int getBreakEffectLife() {
        return BREAK_EFFECT_LIFE;
    }

    public int getAbsorbEffectLife() {
        return ABSORB_EFFECT_LIFE;
    }

    // 护盾中心（size 3 建筑中心 = 锚点 +1.5,+1.5）喵
    public double centerX() {
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        return base.getX() + 1.5;
    }

    public double centerZ() {
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        return base.getZ() + 1.5;
    }

    // 实时护盾半径（格）：(radius + phaseHeat×phaseRadiusBoost) × radscl，单位从 Mindustry 换算到格喵
    public float realRadius() {
        return (RADIUS + phaseHeat * PHASE_RADIUS_BOOST) * radscl / 8f;
    }

    // 接收判定：只收相织布（Mindustry consumeItem(phaseFabric).boost()）喵
    @Override
    public boolean acceptsItem(Item item) {
        return item == BlockdustryItems.PHASE_FABRIC.get() && !isFull();
    }

    // —— 主逻辑（仅锚点格 tick）喵
    @Override
    protected void tickAnchor() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        // phaseValid：有相织布喵
        boolean phaseValid = getStoredCount() > 0;
        phaseHeat = approach(phaseHeat, phaseValid ? 1f : 0f, 0.1f);
        // 每 phaseUseTime=350 tick 吃一块相织布（原版 timer(timerUse, phaseUseTime)）喵
        if (phaseValid && !broken && getPowerStatus() > 0.01f && serverLevel.getGameTime() % (long) PHASE_USE_TIME == 0) {
            removeOne();
        }
        // 半径缩放：broken→0，否则→warmup（原版 lerpDelta 0.05）喵
        radscl = approach(radscl, broken ? 0f : warmup, 0.05f);
        warmup = approach(warmup, getPowerStatus(), 0.1f);
        // 蓄力冷却：原版 buildup -= delta × (broken?cooldownBrokenBase:cooldownNormal)；冷却液省略喵
        if (buildup > 0) {
            float scale = !broken ? COOLDOWN_NORMAL : COOLDOWN_BROKEN_BASE;
            buildup -= scale;
            if (buildup < 0f) buildup = 0f;
        }
        // broken 且冷却完 → 恢复喵
        if (broken && buildup <= 0f) {
            broken = false;
            syncClient();
        }
        // 受击闪白衰减（原版 hit -= 1/5 × delta）喵
        if (hit > 0f) {
            hit -= 1f / 5f;
            if (hit < 0f) hit = 0f;
        }
        deflectBullets();
        // warmup 变化超阈值同步（护盾半径动画）喵
        if (Math.abs(warmup - lastSyncedWarmup) > 0.02f) {
            lastSyncedWarmup = warmup;
            syncClient();
        }
    }

    // —— 拦截子弹（原版 deflectBullets：Groups.bullet.intersect 六边形区域）喵
    private void deflectBullets() {
        float r = realRadius();
        if (r <= 0.001f || broken) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        double cx = centerX(), cz = centerZ();
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        // 包围盒覆盖护盾水平范围 + 垂直判定高度喵
        AABB box = new AABB(cx - r, base.getY() - 0.5, cz - r, cx + r, base.getY() + SHIELD_HEIGHT, cz + r);
        for (Projectile p : serverLevel.getEntitiesOfClass(Projectile.class, box, this::isBlockdustryBullet)) {
            if (p.isRemoved()) continue;
            BlockdustryTeam owner = readTeam(p);
            if (owner == null || !getTeam().isEnemy(owner)) continue; // 只拦敌对喵
            if (!insideHex(cx, cz, r, p.getX(), p.getZ())) continue;
            absorbBullet(serverLevel, p);
        }
    }

    private boolean isBlockdustryBullet(Projectile p) {
        return p instanceof BlockdustryBulletEntity || p instanceof FireBulletEntity || p instanceof FlakBulletEntity;
    }

    // 吸收子弹：伤害折算 buildup + 移除子弹 + 记录拦截点特效（原版 absorbEffect.at(bullet)）喵
    private void absorbBullet(ServerLevel serverLevel, Projectile p) {
        float dmg = readDamage(p);
        buildup += dmg;
        hit = 1f;
        addAbsorbPoint(p.getX(), p.getY(), p.getZ());
        p.discard();
        if (buildup >= SHIELD_HEALTH + PHASE_SHIELD_BOOST * phaseHeat) {
            triggerBreak();
        } else {
            syncClient();
        }
    }

    // 破碎（原版 buildup ≥ shieldHealth + phaseShieldBoost×phaseHeat → broken + Fx.shieldBreak）喵
    private void triggerBreak() {
        broken = true;
        buildup = SHIELD_HEALTH;
        breakStartGameTime = level.getGameTime();
        breakRadius = realRadius();
        syncClient();
    }

    private void addAbsorbPoint(double x, double y, double z) {
        absorbPoints.add(new AbsorbPoint(level.getGameTime(), (float) x, (float) y, (float) z));
        while (absorbPoints.size() > MAX_ABSORB_POINTS) {
            absorbPoints.remove(0);
        }
    }

    private void syncClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 水平正六边形点包含（顶点从角度 0 起，逆时针；与原版 Intersector.isInRegularPolygon 同构）喵
    static boolean insideHex(double cx, double cz, double r, double px, double pz) {
        double dx = px - cx, dz = pz - cz;
        if (dx * dx + dz * dz > r * r + 1e-6) return false;
        for (int i = 0; i < SIDES; i++) {
            double a1 = Math.toRadians(i * 60.0);
            double a2 = Math.toRadians(((i + 1) % SIDES) * 60.0);
            double v1x = cx + r * Math.cos(a1), v1z = cz + r * Math.sin(a1);
            double v2x = cx + r * Math.cos(a2), v2z = cz + r * Math.sin(a2);
            double ex = v2x - v1x, ez = v2z - v1z;
            double wx = px - v1x, wz = pz - v1z;
            if (ex * wz - ez * wx < 0) return false;
        }
        return true;
    }

    // 反射读取子弹伤害（BlockdustryBulletEntity/FireBulletEntity/FlakBulletEntity 均声明 private float damage，未提供 getter，只读不改）喵
    private static float readDamage(Projectile p) {
        Object val = readField(p, "damage");
        return val instanceof Float f ? f : 0f;
    }

    // 反射读取子弹队伍（同理 private BlockdustryTeam ownerTeam）喵
    private static BlockdustryTeam readTeam(Projectile p) {
        Object val = readField(p, "ownerTeam");
        return val instanceof BlockdustryTeam t ? t : null;
    }

    private static Object readField(Object obj, String name) {
        for (Class<?> c = obj.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(obj);
            } catch (NoSuchFieldException ignored) {
                // 沿继承链向上找喵
            } catch (ReflectiveOperationException e) {
                return null;
            }
        }
        return null;
    }

    // 趋近：current += (target-current)×rate（Mindustry Mathf.lerpDelta, delta=1）喵
    private static float approach(float current, float target, float rate) {
        return current + (target - current) * Math.min(1f, rate);
    }

    // —— BlockdustryPowerNode ——
    @Override
    public BlockPos getPos() {
        return worldPosition;
    }

    @Override
    public float powerProduction() {
        return 0f;
    }

    @Override
    public float powerNeeded() {
        // 仅锚点格计入耗电：3×3 共 9 格 BE 都会进电网结算，非锚点格返回 0，避免耗电被计 9 次喵
        return isAnchor() ? POWER_NEEDED : 0f;
    }

    @Override
    public float powerCapacity() {
        return 0f;
    }

    @Override
    public float powerStored() {
        return 0f;
    }

    @Override
    public float getPowerStatus() {
        return powerStatus;
    }

    @Override
    public void setPowerStatus(float status) {
        this.powerStatus = status;
    }

    @Override
    public java.util.List<BlockPos> getPowerLinks() {
        // 非锚点格把自己并入锚点所在电网喵
        if (isAnchor()) return List.of();
        BlockPos anchor = getAnchor();
        return anchor != null ? List.of(anchor) : List.of();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("bd_fp_broken", broken);
        tag.putFloat("bd_fp_buildup", buildup);
        tag.putFloat("bd_fp_radscl", radscl);
        tag.putFloat("bd_fp_warmup", warmup);
        tag.putFloat("bd_fp_phase", phaseHeat);
        tag.putFloat("bd_fp_hit", hit);
        tag.putFloat("bd_fp_power", powerStatus);
        tag.putLong("bd_fp_break_start", breakStartGameTime);
        tag.putFloat("bd_fp_break_radius", breakRadius);
        ListTag list = new ListTag();
        for (AbsorbPoint ap : absorbPoints) {
            CompoundTag c = new CompoundTag();
            c.putLong("t", ap.time);
            c.putFloat("x", ap.x);
            c.putFloat("y", ap.y);
            c.putFloat("z", ap.z);
            list.add(c);
        }
        tag.put("bd_fp_absorb", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        broken = tag.getBoolean("bd_fp_broken");
        buildup = tag.getFloat("bd_fp_buildup");
        radscl = tag.getFloat("bd_fp_radscl");
        warmup = tag.getFloat("bd_fp_warmup");
        phaseHeat = tag.getFloat("bd_fp_phase");
        hit = tag.getFloat("bd_fp_hit");
        powerStatus = tag.getFloat("bd_fp_power");
        breakStartGameTime = tag.getLong("bd_fp_break_start");
        breakRadius = tag.getFloat("bd_fp_break_radius");
        absorbPoints.clear();
        ListTag list = tag.getList("bd_fp_absorb", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            absorbPoints.add(new AbsorbPoint(c.getLong("t"), c.getFloat("x"), c.getFloat("y"), c.getFloat("z")));
        }
    }
}
