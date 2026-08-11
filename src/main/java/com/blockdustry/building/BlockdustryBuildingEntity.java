package com.blockdustry.building;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 建筑方块实体基类：队伍归属 + 多格锚点 + 挂模组新 tick（忠实 Mindustry，无等级概念）喵
public abstract class BlockdustryBuildingEntity extends BlockEntity {
    // 所属建筑锚点坐标（多格建筑共享同一锚点；单格=自身）喵
    private BlockPos anchor;
    // 内置储物空间（忠于 Mindustry Building.items + itemCapacity，mechanical-drill=10）喵
    private Item storedItem;
    private int storedCount;
    private int itemCapacity = 10;
    // 是否已注册到建筑管理器，避免重复注册喵
    private boolean registered;

    public BlockdustryBuildingEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 每模组 tick 驱动入口：仅服务端且仅锚点格执行实际逻辑喵
    public final void tick() {
        if (level == null || level.isClientSide) return;
        if (!isAnchor()) return;
        tickAnchor();
    }

    // 子类实现锚点格的具体行为喵
    protected abstract void tickAnchor();

    // 建筑队伍：读取 Level attachment（放置时已继承放置者队伍）喵
    public BlockdustryTeam getTeam() {
        if (level == null || level.isClientSide) return BlockdustryTeam.DERELICT;
        return BlockdustryTeams.getTeam((ServerLevel) level, worldPosition);
    }

    // 库存 API：单品种存储，总库存判满（忠于 Mindustry）喵
    public Item getStoredItem() {
        return storedItem;
    }

    public int getStoredCount() {
        return storedCount;
    }

    public int getCapacity() {
        return itemCapacity;
    }

    public boolean isFull() {
        return storedCount >= itemCapacity;
    }

    public boolean acceptsItem(Item item) {
        return !isFull() && (storedItem == null || storedItem == item);
    }

    public void acceptItem(Item item) {
        if (!acceptsItem(item)) return;
        if (storedItem == null) storedItem = item;
        storedCount++;
        setChanged();
    }

    // 锚点相关喵
    public BlockPos getAnchor() {
        return anchor;
    }

    public void setAnchor(BlockPos anchor) {
        this.anchor = anchor.immutable();
        setChanged();
        // 强制客户端重新同步 BE 数据，否则新放置时客户端 anchor 为 null 喵
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean hasAnchor() {
        return anchor != null;
    }

    // 无锚点（旧档/单格）时视为锚点格喵
    public boolean isAnchor() {
        return anchor == null || anchor.equals(worldPosition);
    }

    // 服务端加载时注册进建筑管理器喵
    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && !registered) {
            BlockdustryBuildings.register(this);
            registered = true;
        }
    }

    // 移除时注销，避免漏 tick 喵
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (registered) {
            BlockdustryBuildings.unregister(this);
            registered = false;
        }
    }

    // 实体可能被重新激活（如区块数据重载），恢复注册喵
    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level != null && !level.isClientSide && !registered) {
            BlockdustryBuildings.register(this);
            registered = true;
        }
    }

    // 客户端同步：sendBlockUpdated 时随 packet 携带完整数据（含 anchor），否则客户端 anchor 不同步喵
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (anchor != null) {
            tag.putInt("bd_anchor_x", anchor.getX());
            tag.putInt("bd_anchor_y", anchor.getY());
            tag.putInt("bd_anchor_z", anchor.getZ());
        }
        if (storedItem != null) {
            tag.putString("bd_item", BuiltInRegistries.ITEM.getKey(storedItem).toString());
        }
        tag.putInt("bd_count", storedCount);
        tag.putInt("bd_capacity", itemCapacity);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("bd_anchor_x")) {
            anchor = new BlockPos(tag.getInt("bd_anchor_x"), tag.getInt("bd_anchor_y"), tag.getInt("bd_anchor_z"));
        }
        if (tag.contains("bd_item")) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("bd_item")));
            if (item != null && item != Items.AIR) storedItem = item;
        }
        storedCount = tag.getInt("bd_count");
        itemCapacity = Math.max(0, tag.getInt("bd_capacity"));
    }
}
