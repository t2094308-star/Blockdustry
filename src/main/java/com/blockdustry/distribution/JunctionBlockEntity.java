package com.blockdustry.distribution;

import java.util.ArrayDeque;
import java.util.Deque;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.logistics.BlockdustryItemSink;
import com.blockdustry.logistics.BlockdustryItemSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Mindustry junction（1×1 双向直通并道）：4 个方向各一条 FIFO 缓冲（capacity=6/侧），
// 物品从任意一侧进、从对侧直通输出（非旋转、不 round-robin）。speed=26 帧/件喵
// 忠于原版 Junction.java：acceptItem 要求对侧存在同队建筑；updateTile 按方向把缓冲头件推出喵
public class JunctionBlockEntity extends BlockdustryBuildingEntity {
    // Mindustry Junction：capacity=6（每侧），speed=26（帧/件，60fps 基准）喵
    public static final int CAPACITY = 6;
    public static final int SPEED_TICKS = 26;

    // 每方向 FIFO 队列（索引 0=NORTH,1=EAST,2=SOUTH,3=WEST）喵
    private final Deque<Item>[] queues = new ArrayDeque[4];
    // 每方向传输冷却（0=可送，>0 递减）；近似 Mindustry 每件 26 帧节拍喵
    private final int[] cooldown = new int[4];

    public JunctionBlockEntity(BlockPos pos, BlockState state) {
        super(JunctionRegistrar.JUNCTION_ENTITY.get(), pos, state);
        initQueues();
    }

    public JunctionBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        initQueues();
    }

    @SuppressWarnings("unchecked")
    private void initQueues() {
        for (int i = 0; i < 4; i++) {
            if (queues[i] == null) queues[i] = new ArrayDeque<>();
        }
    }

    @Override
    protected void tickAnchor() {
        if (level == null) return;
        for (int i = 0; i < 4; i++) {
            if (queues[i].isEmpty()) continue;
            if (cooldown[i] > 0) {
                cooldown[i]--;
                continue;
            }
            Direction dir = dirOf(i);
            Item head = queues[i].peek();
            BlockEntity be = level.getBlockEntity(worldPosition.relative(dir));
            // 直通输出：推给「输送方向」对侧的可接收建筑喵
            if (be instanceof BlockdustryItemSink sink
                    && sink.acceptItem(this, head)
                    && sink.handleItem(this, head)) {
                queues[i].poll();
                cooldown[i] = SPEED_TICKS;
                setChanged();
            }
        }
    }

    // 物品源 → 本格的输送方向（= 直通输出方向）：源在格西 → 向东直通输出喵
    private Direction travelDirection(BlockdustryItemSource source) {
        if (source == null || source.getPos() == null) return null;
        BlockPos sp = source.getPos();
        int dx = worldPosition.getX() - sp.getX();
        int dz = worldPosition.getZ() - sp.getZ();
        if (dx == 0 && dz == 0) return null;
        if (dx != 0 && dz != 0) return null; // 对角源不认（junction 只收四邻）喵
        if (dx == 1) return Direction.EAST;
        if (dx == -1) return Direction.WEST;
        if (dz == 1) return Direction.SOUTH;
        return Direction.NORTH;
    }

    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (level == null || source == null || item == null) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        Direction travel = travelDirection(source);
        if (travel == null) return false;
        int idx = dirIndex(travel);
        if (queues[idx].size() >= CAPACITY) return false;
        // 直通预检（Mindustry Junction.acceptItem）：对侧必须存在同队建筑喵
        BlockEntity be = level.getBlockEntity(worldPosition.relative(travel));
        if (!(be instanceof BlockdustryItemSink)) return false;
        if (be instanceof BlockdustryBuildingEntity b && !b.getTeam().canInteract(getTeam())) return false;
        return true;
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        if (!acceptItem(source, item)) return false;
        Direction travel = travelDirection(source);
        int idx = dirIndex(travel);
        if (queues[idx].isEmpty()) cooldown[idx] = Math.max(cooldown[idx], SPEED_TICKS);
        queues[idx].addLast(item);
        setChanged();
        return true;
    }

    // —— 供 Jade/显示用的库存汇总（忠于 4×6=24 总容量）——
    @Override
    public int getStoredCount() {
        int total = 0;
        for (int i = 0; i < 4; i++) total += queues[i].size();
        return total;
    }

    @Override
    public int getCapacity() {
        return CAPACITY * 4;
    }

    @Override
    public boolean isFull() {
        for (int i = 0; i < 4; i++) {
            if (queues[i].size() < CAPACITY) return false;
        }
        return true;
    }

    @Override
    public boolean acceptsItem(Item item) {
        return !isFull();
    }

    // 方向 ↔ 缓冲索引喵
    private static int dirIndex(Direction dir) {
        return switch (dir) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0; // NORTH
        };
    }

    private static Direction dirOf(int idx) {
        return switch (idx) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for (int i = 0; i < 4; i++) {
            ListTag list = new ListTag();
            for (Item it : queues[i]) {
                list.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(it).toString()));
            }
            tag.put("bd_junc_q" + i, list);
            tag.putInt("bd_junc_cd" + i, cooldown[i]);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        initQueues();
        for (int i = 0; i < 4; i++) {
            queues[i].clear();
            ListTag list = tag.getList("bd_junc_q" + i, net.minecraft.nbt.Tag.TAG_STRING);
            for (int j = 0; j < list.size(); j++) {
                Item it = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(list.getString(j)));
                if (it != null && it != Items.AIR) queues[i].addLast(it);
            }
            cooldown[i] = tag.getInt("bd_junc_cd" + i);
        }
    }
}
