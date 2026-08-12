package com.blockdustry.building;

import com.blockdustry.logistics.BlockdustryItemSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Vector3f;

// 石墨压缩机（Mindustry graphite-press）：吃 2 煤产 1 石墨，craftTime=90tick，库存 10，缺料/满停。
// 带 warmup 预热（动画开关）与 craft 灰色尘点粒子喵
public class GraphitePressBlockEntity extends BlockdustryBuildingEntity {
    private static final int CRAFT_TIME = 90;
    private static final int INPUT_AMOUNT = 2;
    private static final int CAPACITY = 10;
    private static final float WARMUP_SPEED = 0.019f; // Mindustry GenericCrafter warmupSpeed 喵

    private int coalCount;
    private int graphiteCount;
    private float craftProgress;
    private float warmup; // 0..1 预热，驱动染色/旋转动画喵
    private float lastSyncedWarmup = -1f; // 同步游标，warmup 变化超过阈值才发包喵

    public GraphitePressBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.GRAPHITE_PRESS_ENTITY.get(), pos, state);
    }

    public GraphitePressBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float getWarmup() {
        return warmup;
    }

    // Jade/调试读取用 getter 喵
    public int getCoalCount() {
        return coalCount;
    }

    public int getGraphiteCount() {
        return graphiteCount;
    }

    public float getCraftProgress() {
        return craftProgress;
    }

    // 接收煤（库存 < 容量）喵
    @Override
    public boolean acceptsItem(Item item) {
        return item == Items.COAL && coalCount < CAPACITY;
    }

    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (source == null || item == null) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        // 非锚点格转发给锚点格（统一库存，craft 只在锚点格跑）喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof GraphitePressBlockEntity anchor && anchor.acceptItem(source, item);
        }
        return acceptsItem(item);
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        // 非锚点格转发给锚点格喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof GraphitePressBlockEntity anchor && anchor.handleItem(source, item);
        }
        if (!acceptItem(source, item)) return false;
        coalCount++;
        setChanged();
        return true;
    }

    @Override
    protected void tickAnchor() {
        // 先把产出的石墨卸给相邻传送带喵
        if (graphiteCount > 0 && dumpItem(BlockdustryBlocks.GRAPHITE.get())) {
            graphiteCount--;
        }
        boolean producing = coalCount >= INPUT_AMOUNT && graphiteCount < CAPACITY;
        // warmup 预热：可生产爬升、否则衰减（Mindustry approachDelta 0.019/tick）喵
        warmup = producing ? Math.min(1f, warmup + WARMUP_SPEED) : Math.max(0f, warmup - WARMUP_SPEED);
        // 缺煤或石墨满则停摆喵
        if (!producing) return;
        craftProgress += 1f / CRAFT_TIME;
        if (craftProgress >= 1f) {
            craftProgress = 0;
            coalCount -= INPUT_AMOUNT;
            if (!dumpItem(BlockdustryBlocks.GRAPHITE.get())) {
                graphiteCount++;
            }
            spawnCraftParticles();
            setChanged();
        }
        // warmup 变化超过阈值时同步客户端（渲染动画需要读到 warmup）喵
        if (Math.abs(warmup - lastSyncedWarmup) > 0.02f) {
            lastSyncedWarmup = warmup;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // craft 完成：灰色尘点向外散（Mindustry pulverizeMedium 映射为 DustParticleOptions 灰）喵
    private void spawnCraftParticles() {
        if (level instanceof ServerLevel serverLevel) {
            double x = worldPosition.getX() + 1.0; // 2×2 中心喵
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 1.0;
            serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.56f, 0.56f, 0.56f), 2.5f),
                    x, y, z, 6, 0.4, 0.3, 0.4, 0.15);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("bd_coal", coalCount);
        tag.putInt("bd_graphite", graphiteCount);
        tag.putFloat("bd_progress", craftProgress);
        tag.putFloat("bd_warmup", warmup);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        coalCount = tag.getInt("bd_coal");
        graphiteCount = tag.getInt("bd_graphite");
        craftProgress = tag.getFloat("bd_progress");
        warmup = tag.getFloat("bd_warmup");
    }
}
