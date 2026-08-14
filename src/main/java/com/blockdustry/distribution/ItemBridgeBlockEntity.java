package com.blockdustry.distribution;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.logistics.BlockdustryItemSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 传送带桥方块实体（Mindustry BufferedItemBridge / ItemBridge 忠实移植）喵。
// 关键语义（原版 Blocks.java L2105-2113 + ItemBridge.java + BufferedItemBridge.java）：
//   - range = 4（桥接距离，直线同轴）喵
//   - speed = 74（物品在 buffer 中停留的 Mindustry 逻辑帧数，60Hz → MC 20Hz = 74*20/60 ≈ 24.667 tick）喵
//   - bufferCapacity = 14（缓冲队列容量）喵
//   - itemCapacity = 10（建筑自身库存，ItemBridge 基类）喵
//   - 交付节流 timerAccept = 4 逻辑帧 → MC 20Hz = 4*20/60 ≈ 1.333 tick 喵
// 配对：放置时自动连接「最近放置的未配对桥」（原版 findLink 只认 lastBuild），
//       要求同队、同 block、直线、距离 2..range（相邻格不自动配对，原版 !proximity.contains）喵。
// 传输：自身库存 → 缓冲队列 →（到期 + 节流）→ 对端库存；未配对时 dump 库存给邻居喵。
public class ItemBridgeBlockEntity extends BlockdustryBuildingEntity {
    // —— 数据（忠于原版）喵 ——
    public static final int RANGE = 4;
    public static final int BUFFER_CAPACITY = 14;
    public static final float FLIGHT_TICK = 74f * 20f / 60f;      // speed=74 @60Hz → MC tick 喵
    public static final float DELIVERY_INTERVAL = 4f * 20f / 60f; // timerAccept=4 @60Hz → MC tick 喵

    // 最近放置的未配对桥（原版 ItemBridge.lastBuild 静态字段，服务端逻辑线程维护）喵
    private static ItemBridgeBlockEntity lastPlaced;

    // 配对端坐标（null=未配对；原版 link=-1）喵
    private BlockPos link;
    // 待配对标志：onPlace 时队伍可能未就绪，tick 兜底再试一次喵
    private boolean placedPending;

    // 缓冲队列（原版 ItemBuffer，环形前移数组）喵
    private final Item[] bufferItems = new Item[BUFFER_CAPACITY];
    private final long[] bufferArrival = new long[BUFFER_CAPACITY];
    private int bufferCount;
    // 交付节流累加器（浮点，处理 1.333 非整 tick）喵
    private float deliveryTimer;

    public ItemBridgeBlockEntity(BlockPos pos, BlockState state) {
        super(BridgeRegistrar.BRIDGE_ENTITY.get(), pos, state);
    }

    public ItemBridgeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // —— 渲染/交互读取接口 ——
    public BlockPos getLink() {
        return link;
    }

    public boolean hasLink() {
        return link != null;
    }

    // —— 放置钩子：服务端 onPlace 调用（原版 playerPlaced）喵 ——
    public void onPlaced() {
        placedPending = true;
        tryAutoLink();
        lastPlaced = this; // 原版 playerPlaced 末尾 lastBuild = this 喵
    }

    // 自动配对：连接最近放置的未配对桥（原版 findLink 规则）喵
    public void tryAutoLink() {
        ItemBridgeBlockEntity other = lastPlaced;
        if (other == null || other == this || other.link != null) return;
        if (isAdjacent(other)) return; // 原版 !proximity.contains：相邻格不自动配对喵
        if (!other.linkValid(this)) return;
        this.link = other.worldPosition.immutable();
        other.link = this.worldPosition.immutable();
        other.setChanged();
        other.sync();
        setChanged();
        sync();
    }

    // —— 主 tick（仅锚点格，由基类调度）喵 ——
    @Override
    protected void tickAnchor() {
        // 放置兜底配对（onPlace 时队伍可能未就绪）喵
        if (placedPending) {
            placedPending = false;
            tryAutoLink();
        }

        ItemBridgeBlockEntity other = linked();
        if (other == null || !linkValid(other)) {
            // 未配对/配对失效：把库存 dump 给邻居（原版 doDump → dumpAccumulate）喵
            Item it = getStoredItem();
            if (it != null && getStoredCount() > 0) {
                dumpItem(it);
            }
            return;
        }

        // 1. 自身库存 → 缓冲（原版 updateTransport 第一步）喵
        if (bufferCount < BUFFER_CAPACITY && getStoredCount() > 0) {
            Item it = removeOne();
            bufferItems[bufferCount] = it;
            bufferArrival[bufferCount] = level.getGameTime();
            bufferCount++;
        }

        // 2. 到期物品 → 对端（原版 buffer.poll(speed) + timer(timerAccept)）喵
        if (bufferCount > 0 && expired(bufferArrival[0])) {
            deliveryTimer += 1f;
            if (deliveryTimer >= DELIVERY_INTERVAL) {
                deliveryTimer -= DELIVERY_INTERVAL;
                Item it = bufferItems[0];
                if (other.acceptItem(this, it) && other.handleItem(this, it)) {
                    removeBufferFront();
                    setChanged();
                }
            }
        }
    }

    // 缓冲头部物品是否已到期（停留达到 FLIGHT_TICK）喵
    private boolean expired(long arrival) {
        return level != null && (float) (level.getGameTime() - arrival) >= FLIGHT_TICK;
    }

    private void removeBufferFront() {
        for (int i = 1; i < bufferCount; i++) {
            bufferItems[i - 1] = bufferItems[i];
            bufferArrival[i - 1] = bufferArrival[i];
        }
        bufferCount--;
        bufferItems[bufferCount] = null;
    }

    // 配对端 BE（客户端也能用 link 解析 BE，但渲染只读 link 坐标）喵
    public ItemBridgeBlockEntity linked() {
        if (link == null || level == null) return null;
        if (level.getBlockEntity(link) instanceof ItemBridgeBlockEntity be) return be;
        return null;
    }

    // 原版 linkValid：同 block（同为桥）、同队、直线同轴且距离 2..range 喵
    public boolean linkValid(ItemBridgeBlockEntity other) {
        if (other == null || other == this) return false;
        if (level == null || other.level == null) return false;
        if (!(other.getBlockState().getBlock() instanceof ItemBridgeBlock)) return false;
        if (!getTeam().canInteract(other.getTeam())) return false;
        return positionsValid(worldPosition, other.worldPosition);
    }

    private static boolean positionsValid(BlockPos a, BlockPos b) {
        int dx = Math.abs(a.getX() - b.getX());
        int dz = Math.abs(a.getZ() - b.getZ());
        if (dx == 0 && dz >= 1 && dz <= RANGE) return true;
        if (dz == 0 && dx >= 1 && dx <= RANGE) return true;
        return false;
    }

    private boolean isAdjacent(ItemBridgeBlockEntity other) {
        return Math.abs(other.worldPosition.getX() - worldPosition.getX())
                + Math.abs(other.worldPosition.getZ() - worldPosition.getZ()) == 1;
    }

    // —— 物品接收（原版 acceptItem + checkAccept）喵 ——
    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (level == null || item == null || source == null) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        if (!acceptsItem(item)) return false; // 库存未满且同类型（itemCapacity=10）喵

        // 来源是我配对端 → 接收（对端传过来的物品）喵
        if (source instanceof ItemBridgeBlockEntity sb && link != null && link.equals(sb.worldPosition)) {
            return true;
        }
        // 配对端有效：来源不能从配对方向进入（原版 checkAccept 防回灌）喵
        ItemBridgeBlockEntity other = linked();
        if (other != null && linkValid(other)) {
            Direction rel = directionTo(other.worldPosition);
            Direction rel2 = directionFrom(source);
            if (rel2 != null && rel == rel2) return false;
        }
        return true;
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        if (!acceptItem(source, item)) return false;
        storeItem(item);
        return true;
    }

    // 本格到目标格的水平方向喵
    private Direction directionTo(BlockPos other) {
        int dx = other.getX() - worldPosition.getX();
        int dz = other.getZ() - worldPosition.getZ();
        if (dx > 0) return Direction.EAST;
        if (dx < 0) return Direction.WEST;
        if (dz > 0) return Direction.SOUTH;
        if (dz < 0) return Direction.NORTH;
        return null;
    }

    // 源格相对本格的方位（源在本格哪个方向）喵
    private Direction directionFrom(BlockdustryItemSource source) {
        if (source == null || source.getPos() == null) return null;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (worldPosition.relative(d).equals(source.getPos())) return d;
        }
        return null;
    }

    // 同步配对状态到客户端（渲染桥面需要 link）喵
    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // —— 持久化：link + 缓冲队列（物品 + 到达 tick）喵 ——
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (link != null) {
            tag.putInt("bd_link_x", link.getX());
            tag.putInt("bd_link_y", link.getY());
            tag.putInt("bd_link_z", link.getZ());
        }
        ListTag itemsTag = new ListTag();
        ListTag arrTag = new ListTag();
        for (int i = 0; i < bufferCount; i++) {
            itemsTag.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(bufferItems[i]).toString()));
            arrTag.add(LongTag.valueOf(bufferArrival[i]));
        }
        tag.put("bd_bridge_buffer", itemsTag);
        tag.put("bd_bridge_arrival", arrTag);
        tag.putInt("bd_bridge_count", bufferCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("bd_link_x")) {
            link = new BlockPos(tag.getInt("bd_link_x"), tag.getInt("bd_link_y"), tag.getInt("bd_link_z"));
        }
        bufferCount = Math.min(BUFFER_CAPACITY, tag.getInt("bd_bridge_count"));
        ListTag itemsTag = tag.getList("bd_bridge_buffer", net.minecraft.nbt.Tag.TAG_STRING);
        ListTag arrTag = tag.getList("bd_bridge_arrival", net.minecraft.nbt.Tag.TAG_LONG);
        for (int i = 0; i < bufferCount && i < itemsTag.size() && i < arrTag.size(); i++) {
            String key = itemsTag.getString(i);
            Item item = key.isEmpty() ? null : BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(key));
            bufferItems[i] = (item == null || item == Items.AIR) ? null : item;
            bufferArrival[i] = ((net.minecraft.nbt.LongTag) arrTag.get(i)).getAsLong();
        }
    }
}
