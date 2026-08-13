package com.blockdustry.building;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.lib.BlockHealthApi;
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
    // 客户端队伍缓存：从 NBT 同步（服务端队伍存在 ServerLevel attachment，客户端拿不到，靠 BE 数据带过来喵）
    private BlockdustryTeam clientTeam = BlockdustryTeam.DERELICT;
    // 建筑装甲（Mindustry Building.armor，固定减伤：受伤 = max(1, 伤害 - 装甲)）喵
    private float armor;
    // 整组共享血量：是否已注册进 BlockHealth 组（仅锚点格，避免重复注册）喵
    private boolean healthGroupRegistered;

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

    // 建筑队伍：服务端读 Level attachment（放置时已继承放置者队伍）；客户端读 NBT 缓存喵
    public BlockdustryTeam getTeam() {
        if (level == null) return BlockdustryTeam.DERELICT;
        if (level.isClientSide) return clientTeam;
        return BlockdustryTeams.getTeam((ServerLevel) level, worldPosition);
    }

    // 建筑装甲：默认 0，子类构造函数可调 setArmor 设置喵
    public float getArmor() {
        return armor;
    }

    public void setArmor(float armor) {
        this.armor = Math.max(0f, armor);
        setChanged();
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

    // 服务端加载时注册进建筑管理器 + 整组共享血量组注册喵
    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && !registered) {
            BlockdustryBuildings.register(this);
            registered = true;
            registerHealthGroup();
        }
    }

    // 移除时注销，避免漏 tick；同时注销整组共享血量组喵
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (registered) {
            BlockdustryBuildings.unregister(this);
            registered = false;
            unregisterHealthGroup();
        }
    }

    // 实体可能被重新激活（如区块数据重载），恢复注册喵
    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if (level != null && !level.isClientSide && !registered) {
            BlockdustryBuildings.register(this);
            registered = true;
            registerHealthGroup();
        }
    }

    // 多格建筑整组共享血量（T10 Level 3）：锚点格把「锚点 + 全部格」注册进 BlockHealth 组喵。
    // 必须 hasAnchor && isAnchor：fresh 放置时 onLoad 早于 setAnchor 触发（各格 anchor 皆 null 会误判锚点），
    // 故 onLoad 只处理「已从 NBT 载入 anchor 的 chunk 重载」；fresh 放置由 place 设完锚点后调 registerHealthGroupExplicit 喵
    private void registerHealthGroup() {
        if (level == null || level.isClientSide || healthGroupRegistered) return;
        if (!hasAnchor() || !isAnchor() || getSize() <= 1) return; // 仅多格建筑且确系锚点格喵
        BlockPos anchorPos = getAnchor();
        Set<BlockPos> cells = new HashSet<>();
        int size = getSize();
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                cells.add(anchorPos.offset(dx, 0, dz));
            }
        }
        BlockHealthApi.registerGroup((ServerLevel) level, anchorPos, cells);
        healthGroupRegistered = true;
    }

    // 供放置逻辑在设完锚点后显式注册整组血量（fresh 放置专用）喵
    public void registerHealthGroupExplicit() {
        registerHealthGroup();
    }

    // 注销整组共享血量组（幂等）喵
    private void unregisterHealthGroup() {
        if (!healthGroupRegistered) return;
        if (level == null || level.isClientSide) return;
        BlockPos anchorPos = hasAnchor() ? getAnchor() : worldPosition;
        BlockHealthApi.unregisterGroup((ServerLevel) level, anchorPos);
        healthGroupRegistered = false;
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
        tag.putString("bd_team", getTeam().name());
        tag.putInt("bd_count", storedCount);
        tag.putInt("bd_capacity", itemCapacity);
        tag.putFloat("bd_armor", armor);
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
        if (tag.contains("bd_team")) {
            clientTeam = BlockdustryTeam.byName(tag.getString("bd_team"));
        }
        storedCount = tag.getInt("bd_count");
        itemCapacity = Math.max(0, tag.getInt("bd_capacity"));
        armor = Math.max(0f, tag.getFloat("bd_armor"));
    }
}
