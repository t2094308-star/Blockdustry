package com.blockdustry.building;

import com.blockdustry.logistics.BlockdustryItemSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 石墨压缩机（Mindustry graphite-press）：吃 2 煤产 1 石墨，craftTime=90tick，库存 10，缺料/满停喵
public class GraphitePressBlockEntity extends BlockdustryBuildingEntity {
    private static final int CRAFT_TIME = 90;
    private static final int INPUT_AMOUNT = 2;
    private static final int CAPACITY = 10;

    private int coalCount;
    private int graphiteCount;
    private float craftProgress;

    public GraphitePressBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.GRAPHITE_PRESS_ENTITY.get(), pos, state);
    }

    public GraphitePressBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
        // 缺煤或石墨满则停摆喵
        if (coalCount < INPUT_AMOUNT || graphiteCount >= CAPACITY) return;
        craftProgress += 1f / CRAFT_TIME;
        if (craftProgress >= 1f) {
            craftProgress = 0;
            coalCount -= INPUT_AMOUNT;
            if (!dumpItem(BlockdustryBlocks.GRAPHITE.get())) {
                graphiteCount++;
            }
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("bd_coal", coalCount);
        tag.putInt("bd_graphite", graphiteCount);
        tag.putFloat("bd_progress", craftProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        coalCount = tag.getInt("bd_coal");
        graphiteCount = tag.getInt("bd_graphite");
        craftProgress = tag.getFloat("bd_progress");
    }
}
