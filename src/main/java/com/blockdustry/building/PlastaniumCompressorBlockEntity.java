package com.blockdustry.building;

import java.util.List;

import com.blockdustry.item.BlockdustryItems;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.power.BlockdustryPowerNode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Vector3f;

// 塑钢压缩机（Mindustry plastanium-compressor，GenericCrafter size 2）喵。
// 数据忠于 Blocks.java L1118-1134：craftTime=60、产塑钢 1、耗钛 2/次、耗油 0.25/s、耗电 3/s、
// liquidCapacity 60、itemCapacity 默认 10（GenericCrafter 未覆写 → Block 默认）、health 320喵。
// 动画/特效：drawer = DrawMulti(DrawDefault, DrawFade)（顶面 -top 白色线稿随 warmup 渐隐呼吸）、
// craftEffect = Fx.formsmoke（浅黄烟）、updateEffect = Fx.plasticburn（浅白燃烧尘点，0.04/次概率）喵。
// 油：Blockdustry 暂无液体系统，先以占位物品「石油」供料（耗油速率 0.25/s 忠实原版），
// 待液体系统接入后可无缝替换喵。
public class PlastaniumCompressorBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final int CRAFT_TIME = 60;              // Mindustry craftTime=60tick（沿用本 mod 1:1→60 MC tick=3s）喵
    private static final int TITANIUM_PER_CRAFT = 2;       // consumeItem(Items.titanium, 2) 喵
    private static final int OUTPUT_AMOUNT = 1;            // outputItem = plastanium 1 喵
    private static final int CAPACITY = 10;                // itemCapacity 默认 10（存钛 + 存塑钢共用）喵
    private static final float WARMUP_SPEED = 0.019f;      // GenericCrafter warmupSpeed 喵
    private static final float POWER_NEEDED = 3f;          // consumePower(3) 喵
    private static final float OIL_CAPACITY = 60f;         // liquidCapacity 60 喵
    private static final float OIL_PER_TICK = 0.0125f;     // 0.25/s ÷ 20tick/s（忠实原版 consumeLiquid oil 0.25/s）喵
    private static final float UPDATE_EFFECT_CHANCE = 0.04f; // GenericCrafter updateEffectChance 喵

    private int titaniumCount;     // 钛库存（每 craft 扣 2）喵
    private int plastaniumCount;   // 塑钢输出缓冲（卸给传送带失败时暂存）喵
    private float oilBuffer;       // 石油缓冲（占位液体，容量 = liquidCapacity 60）喵
    private float craftProgress;   // 制作进度 0..1 喵
    private float warmup;          // 0..1 预热，驱动 DrawFade 顶面叠层 alpha 喵
    private float lastSyncedWarmup = -1f; // warmup 同步游标喵
    private float powerStatus;     // 电网满足率 0..1（PowerGridManager 注入）喵

    public PlastaniumCompressorBlockEntity(BlockPos pos, BlockState state) {
        super(PlastaniumCompressorRegistrar.PLASTANIUM_COMPRESSOR_ENTITY.get(), pos, state);
    }

    public PlastaniumCompressorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // —— 渲染/Jade/调试读取喵 ——
    public float getWarmup() {
        return warmup;
    }

    public float getCraftProgress() {
        return craftProgress;
    }

    public int getTitaniumCount() {
        return titaniumCount;
    }

    public int getPlastaniumCount() {
        return plastaniumCount;
    }

    public float getOilBuffer() {
        return oilBuffer;
    }

    // 接收判定：钛入钛库存，石油入油缓冲；各自未满才收喵
    @Override
    public boolean acceptsItem(Item item) {
        if (item == BlockdustryItems.TITANIUM.get()) return titaniumCount < CAPACITY;
        if (item == PlastaniumCompressorRegistrar.OIL.get()) return oilBuffer < OIL_CAPACITY - 0.001f;
        return false;
    }

    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (source == null || item == null) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        // 非锚点格转发给锚点格（统一库存，craft 只在锚点格跑）喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof PlastaniumCompressorBlockEntity anchor && anchor.acceptItem(source, item);
        }
        return acceptsItem(item);
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        // 非锚点格转发给锚点格喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof PlastaniumCompressorBlockEntity anchor && anchor.handleItem(source, item);
        }
        if (!acceptItem(source, item)) return false;
        if (item == BlockdustryItems.TITANIUM.get()) {
            titaniumCount++;
        } else if (item == PlastaniumCompressorRegistrar.OIL.get()) {
            oilBuffer = Math.min(OIL_CAPACITY, oilBuffer + 1f);
        }
        setChanged();
        return true;
    }

    // 每模组 tick（仅锚点格）：卸塑钢 → 判可生产 → warmup 渐热 → 推进度/耗油/产喵
    @Override
    protected void tickAnchor() {
        // 先把产出的塑钢卸给相邻传送带喵
        if (plastaniumCount > 0 && dumpItem(BlockdustryItems.PLASTANIUM.get())) {
            plastaniumCount--;
        }
        boolean hasPower = getPowerStatus() > 0.01f;
        boolean outputFull = plastaniumCount >= CAPACITY;
        // GenericCrafter.shouldConsume：有电 + 输出未满 + 钛够 2 + 有油才生产喵
        boolean producing = hasPower && !outputFull && titaniumCount >= TITANIUM_PER_CRAFT && oilBuffer > 0f;
        // warmup 预热：可生产爬升、否则衰减（approachDelta 0.019/tick，忠实 GenericCrafter.warmupSpeed）喵
        warmup = producing ? Math.min(1f, warmup + WARMUP_SPEED) : Math.max(0f, warmup - WARMUP_SPEED);
        if (producing) {
            craftProgress += 1f / CRAFT_TIME;
            // 持续耗油（0.25/s；GenericCrafter 运行时连续消耗液体）喵
            oilBuffer = Math.max(0f, oilBuffer - OIL_PER_TICK);
            // updateEffect = plasticburn：运行中有概率播浅白燃烧尘点喵
            if (level != null && !level.isClientSide && level.random.nextFloat() < UPDATE_EFFECT_CHANCE) {
                spawnUpdateParticles();
            }
            if (craftProgress >= 1f) {
                craftProgress = 0f;
                titaniumCount -= TITANIUM_PER_CRAFT;
                // 先直接卸货，失败则入缓冲（GenericCrafter.craft offload → dumpOutputs）喵
                if (!dumpItem(BlockdustryItems.PLASTANIUM.get())) {
                    plastaniumCount += OUTPUT_AMOUNT;
                }
                // craftEffect = formsmoke：成型浅黄烟喵
                spawnCraftParticles();
                setChanged();
            }
        }
        // warmup 变化超过阈值时同步客户端（渲染 DrawFade 需要读到 warmup）喵
        if (Math.abs(warmup - lastSyncedWarmup) > 0.02f) {
            lastSyncedWarmup = warmup;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // updateEffect Fx.plasticburn：40tick 特效，5 个圆点向外飞散，色 Pal.plasticBurn #e9ead3 → 灰，
    // 映射为 DustParticleOptions 浅白尘点（MC 无逐粒子变色，取起始色近似）喵
    private void spawnUpdateParticles() {
        if (level instanceof ServerLevel serverLevel) {
            double x = worldPosition.getX() + 1.0; // 2×2 中心喵
            double y = worldPosition.getY() + 0.6;
            double z = worldPosition.getZ() + 1.0;
            for (int i = 0; i < 5; i++) {
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.913f, 0.918f, 0.827f), 2.0f),
                        x, y, z, 1, 0.35, 0.25, 0.35, 0.3);
            }
        }
    }

    // craftEffect Fx.formsmoke：40tick 特效，6 个方块烟向外散，色 Pal.plasticSmoke #f1e479 → 浅灰，
    // 映射为 DustParticleOptions 浅黄尘点喵
    private void spawnCraftParticles() {
        if (level instanceof ServerLevel serverLevel) {
            double x = worldPosition.getX() + 1.0;
            double y = worldPosition.getY() + 0.7;
            double z = worldPosition.getZ() + 1.0;
            for (int i = 0; i < 6; i++) {
                serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.945f, 0.894f, 0.475f), 2.5f),
                        x, y, z, 1, 0.4, 0.35, 0.4, 0.25);
            }
        }
    }

    // —— BlockdustryPowerNode（耗电 3/s，仅锚点格计费）喵 ——
    @Override
    public float powerProduction() {
        return 0f;
    }

    @Override
    public float powerNeeded() {
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
    public List<BlockPos> getPowerLinks() {
        // 非锚点格把自己并入锚点所在电网喵
        if (isAnchor()) return List.of();
        BlockPos anchor = getAnchor();
        return anchor != null ? List.of(anchor) : List.of();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("bd_titanium", titaniumCount);
        tag.putInt("bd_plastanium", plastaniumCount);
        tag.putFloat("bd_oil", oilBuffer);
        tag.putFloat("bd_progress", craftProgress);
        tag.putFloat("bd_warmup", warmup);
        tag.putFloat("bd_power_status", powerStatus);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        titaniumCount = tag.getInt("bd_titanium");
        plastaniumCount = tag.getInt("bd_plastanium");
        oilBuffer = tag.getFloat("bd_oil");
        craftProgress = tag.getFloat("bd_progress");
        warmup = tag.getFloat("bd_warmup");
        powerStatus = tag.getFloat("bd_power_status");
    }
}
