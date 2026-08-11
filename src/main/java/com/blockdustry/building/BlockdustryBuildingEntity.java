package com.blockdustry.building;

import java.util.ArrayList;
import java.util.List;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.logistics.BlockdustryItemSink;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

// 建筑方块实体基类：队伍归属 + 多格锚点 + 挂模组新 tick + 物品传递接口（忠实 Mindustry）喵
public abstract class BlockdustryBuildingEntity extends BlockEntity
        implements BlockdustryItemSource, BlockdustryItemSink {
    // 所属建筑锚点坐标（多格建筑共享同一锚点；单格=自身）喵
    private BlockPos anchor;
    // 内置储物空间（忠于 Mindustry Building.items + itemCapacity，mechanical-drill=10）喵
    private Item storedItem;
    private int storedCount;
    private int itemCapacity = 10;
    // 是否已注册到建筑管理器，避免重复注册喵
    private boolean registered;
    // 卸货轮询指针（Mindustry cdump，公平分配）喵
    protected int dumpPointer;

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

    // 入自己库存（原 acceptItem(Item)，改名避免与接口重载歧义）喵
    public void storeItem(Item item) {
        if (!acceptsItem(item)) return;
        if (storedItem == null) storedItem = item;
        storedCount++;
        setChanged();
    }

    // 取出一个已存储物品并扣减，库存空则清空物品种类；无货返回 null 喵
    public Item removeOne() {
        if (storedCount <= 0 || storedItem == null) return null;
        storedCount--;
        setChanged();
        if (storedCount == 0) storedItem = null;
        return storedItem;
    }

    // —— 物品传递接口（BlockdustryItemSource / Sink）——
    @Override
    public BlockPos getPos() {
        return worldPosition;
    }

    // 预检：同队 + 自己库存可收喵
    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (source == null || item == null) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        return acceptsItem(item);
    }

    // 真正移交：再校验一次后入库存喵
    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        if (!acceptItem(source, item)) return false;
        storeItem(item);
        return true;
    }

    // 多格占地边长（默认 1）喵
    public int getSize() {
        if (getBlockState().getBlock() instanceof BlockdustryBuildingBlock b) return b.getSize();
        return 1;
    }

    // 把 item 卸给「全占地四邻域」第一个可接收的 sink（轮询起点，成功前移指针）。
    // 关键：用「靠近该 sink 的建筑格」作为 source，保证传送带方向判断正确（否则多格建筑从格旁传送带判错）喵
    protected boolean dumpItem(Item item) {
        if (level == null || item == null) return false;
        BlockPos base = hasAnchor() ? anchor : worldPosition;
        List<BlockPos> cells = new ArrayList<>();
        for (int dx = 0; dx < getSize(); dx++) {
            for (int dz = 0; dz < getSize(); dz++) {
                cells.add(base.offset(dx, 0, dz));
            }
        }
        List<BlockPos> candidates = new ArrayList<>();
        java.util.Map<BlockPos, BlockPos> candidateToCell = new java.util.HashMap<>();
        for (BlockPos cell : cells) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos p = cell.relative(dir);
                if (!candidates.contains(p)) {
                    candidates.add(p);
                    candidateToCell.put(p, cell);
                }
            }
        }
        for (int i = 0; i < candidates.size(); i++) {
            int idx = (dumpPointer + i) % candidates.size();
            BlockPos p = candidates.get(idx);
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof BlockdustryItemSink sink) {
                BlockdustryItemSource source = sourceAt(candidateToCell.get(p));
                if (sink.acceptItem(source, item) && sink.handleItem(source, item)) {
                    dumpPointer = (idx + 1) % candidates.size();
                    setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    // 取指定格对应的物品源（该格 BE 或本格兜底）喵
    private BlockdustryItemSource sourceAt(BlockPos cell) {
        BlockEntity be = level.getBlockEntity(cell);
        return be instanceof BlockdustryItemSource s ? s : this;
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
