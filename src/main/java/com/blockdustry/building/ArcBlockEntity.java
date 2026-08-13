package com.blockdustry.building;

import com.blockdustry.entities.ArcBeamEntity;
import com.blockdustry.entities.TargetType;
import com.blockdustry.power.BlockdustryPowerNode;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

// Mindustry「电弧」电系炮塔（arc）最小移植：1×1、射电击闪电链、只对地，需耗电喵
// 忠于原作参数：reload=35、range=90/8=11.25 格、LightningBulletType damage=20、rotateSpeed=8、targetAir=false、
// buildingDamageMultiplier=0.25（建筑 25% 伤害，在 ArcBeamEntity 内折算）喵
public class ArcBlockEntity extends TurretBlockEntity implements BlockdustryPowerNode {
    private float powerStatus;

    // 方块实体注册用的 (BlockPos, BlockState) 构造器，委托给带类型的完整构造器喵
    public ArcBlockEntity(BlockPos pos, BlockState state) {
        this(FuseArcRegistrar.ARC_ENTITY.get(), pos, state);
    }

    // 完整构造器：设置 arc 专属参数喵
    public ArcBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.reload = 35;            // Mindustry arc reload=35f 喵
        this.range = 11.25f;         // 射程 90/8=11.25 格喵
        this.shootY = 0.375f;        // 炮口前伸 3/8 格喵
        this.bulletDamage = 20f;     // Mindustry arc lightning damage=20 喵
        this.rotateSpeedDeg = 8;     // Mindustry rotateSpeed=8 喵
        this.targetFilter = TargetType.GROUND; // targetAir=false：只锁地面单位（GROUND 过滤不打空）喵
    }

    // 无电停摆：无供电不索敌不开火喵
    @Override
    protected void tickAnchor() {
        if (getPowerStatus() <= 0.01f) return;
        super.tickAnchor();
    }

    // —— BlockdustryPowerNode：arc 耗电约 2 功率（Mindustry arc powerUse≈2），产/存 0 喵
    @Override public float powerProduction() { return 0f; }
    @Override public float powerNeeded() { return 2f; }
    @Override public float powerCapacity() { return 0f; }
    @Override public float powerStored() { return 0f; }
    @Override public float getPowerStatus() { return powerStatus; }
    @Override public void setPowerStatus(float status) { this.powerStatus = status; }

    // 电弧瞬时伤害：忽略预判与弹速，直接对目标中心放电；建筑 0.25 系数在 ArcBeamEntity 内处理喵
    @Override
    protected void fireAt(Vec3 targetCenter, Vec3 targetVel, int barrel) {
        Vec3 center = worldPosition.getCenter().add(0, 0.3, 0);
        Vec3 dir = targetCenter.subtract(center).normalize();
        Vec3 spawn = center.add(dir.scale(shootY));
        ArcBeamEntity beam = new ArcBeamEntity(level, spawn, targetCenter, bulletDamage);
        beam.setOwnerTeam(getTeam());
        level.addFreshEntity(beam);
    }
}
