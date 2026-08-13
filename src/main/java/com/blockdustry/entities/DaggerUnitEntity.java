package com.blockdustry.entities;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;

// Mindustry dagger 地面单位最小移植：队伍 attachment + 索敌敌对活体近战攻击（演示级 AI）喵
public class DaggerUnitEntity extends PathfinderMob {
    // Mindustry dagger 血量为 150，移动速度约 0.3 格/tick 喵
    private static final double DAGGER_HEALTH = 150.0D;
    private static final double DAGGER_SPEED = 0.3D;
    // 近战攻击力（Mindustry dagger 近战伤害约 26，这里取演示值 10）喵
    private static final double DAGGER_DAMAGE = 10.0D;
    private static final double DAGGER_FOLLOW_RANGE = 20.0D;

    public DaggerUnitEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    // 队伍：读取/写入 Entity attachment（与炮塔同源，DERELICT 为无主）喵
    public BlockdustryTeam getBlockdustryTeam() {
        return BlockdustryTeams.getTeam(this);
    }

    public void setBlockdustryTeam(BlockdustryTeam team) {
        BlockdustryTeams.setTeam(this, team);
    }

    // 目标标签：dagger 是地面单位（Mindustry 陆），对地炮可锁定、对空炮不锁定喵
    public TargetType getTargetType() {
        return TargetType.GROUND;
    }

    // 属性供应商：交给主会话在 EntityAttributeCreationEvent 里注册喵
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, DAGGER_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, DAGGER_SPEED)
                .add(Attributes.ATTACK_DAMAGE, DAGGER_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, DAGGER_FOLLOW_RANGE);
    }

    // 简单行为：靠近敌对活体近战攻击，同队/无主（DERELICT）目标不索敌喵
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class,
                10, true, false, this::isEnemyOf));
    }

    // 与炮塔同款索敌判定：DERELICT 攻击者打所有非 DERELICT，DERELICT 目标永不被攻击喵
    private boolean isEnemyOf(LivingEntity target) {
        return BlockdustryTeams.isHostile(getBlockdustryTeam(), BlockdustryTeams.getTeam(target));
    }
}
