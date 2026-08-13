package com.blockdustry.building;

import java.util.ArrayList;
import java.util.List;

import com.blockdustry.logistics.BlockdustryItemSink;
import com.blockdustry.logistics.BlockdustryItemSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 传送带方块实体：Mindustry 三槽平行数组，物品沿朝向移动、间距约束、出口交接喵
public class ConveyorBlockEntity extends BlockdustryBuildingEntity {
    public static final int SLOTS = 3;
    public static final float SPEED = 0.046f;   // 格/tick（Mindustry conveyor）喵
    public static final float ITEM_SPACE = 0.4f;
    private static final float NEXT_MAX_BLOCKED = 0.95f;

    private final Item[] items = new Item[SLOTS];
    private final float[] progress = new float[SLOTS];

    public ConveyorBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.CONVEYOR_ENTITY.get(), pos, state);
    }

    public ConveyorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction getFacing() {
        BlockState state = getBlockState();
        if (state.getBlock() instanceof ConveyorBlock b) return b.getFacing(state);
        return Direction.NORTH;
    }

    public Item[] beltItems() {
        return items;
    }

    public float[] beltProgress() {
        return progress;
    }

    @Override
    protected void tickAnchor() {
        boolean any = false;
        for (Item it : items) {
            if (it != null) {
                any = true;
                break;
            }
        }
        if (!any) return;

        Direction facing = getFacing();
        BlockEntity frontBe = level.getBlockEntity(worldPosition.relative(facing));
        BlockdustryItemSink frontSink = frontBe instanceof BlockdustryItemSink s ? s : null;

        // 头部槽：progress 最大的非空槽喵
        int headIdx = -1;
        float headP = -1;
        for (int i = 0; i < SLOTS; i++) {
            if (items[i] != null && progress[i] > headP) {
                headP = progress[i];
                headIdx = i;
            }
        }
        boolean headCanExit = headIdx >= 0 && frontSink != null
                && frontSink.acceptItem(this, items[headIdx]);

        float nextMax = headCanExit ? 1.0f : NEXT_MAX_BLOCKED;

        // 从前往后扫描逐件限位，保持间距喵
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) {
            if (items[i] != null) order.add(i);
        }
        order.sort((a, b) -> Float.compare(progress[b], progress[a]));
        float limit = nextMax;
        for (int idx : order) {
            float p = progress[idx] + SPEED;
            if (p > limit) p = limit;
            if (p < 0.001f) p = 0.001f;
            progress[idx] = p;
            limit = p - ITEM_SPACE;
        }

        // 出口交接喵
        if (headCanExit && progress[headIdx] >= 1.0f
                && frontSink.acceptItem(this, items[headIdx])
                && frontSink.handleItem(this, items[headIdx])) {
            items[headIdx] = null;
            progress[headIdx] = 0f;
        }
        syncBelt();
    }

    // 非空时同步客户端喵
    private void syncBelt() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 来源格相对本格的方位喵
    // 源直接在本格正下方（提升机顶格垂直交接 + 顶面四邻卸货的源都定位在候选格正下方）→
    // 视为尾部进料，沿本带朝向运走；正常平面/坡道行为不受影响喵
    private Direction directionOf(BlockdustryItemSource source) {
        if (source == null || source.getPos() == null) return null;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (worldPosition.relative(dir).equals(source.getPos())) return dir;
        }
        if (worldPosition.below().equals(source.getPos())) {
            return getFacing().getOpposite();
        }
        return null;
    }

    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (level == null || item == null) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        Direction entry = directionOf(source);
        if (entry == null || entry == getFacing()) return false; // 输出端不接收喵
        if (entry == getFacing().getOpposite()) {
            // 正后尾插：尾部 [0, ITEM_SPACE) 无物 + 有空槽喵
            for (int i = 0; i < SLOTS; i++) {
                if (items[i] != null && progress[i] < ITEM_SPACE) return false;
            }
            return hasEmptySlot();
        }
        // 侧插：中段 [0.1, 0.9] 无物喵
        for (int i = 0; i < SLOTS; i++) {
            if (items[i] != null && Math.abs(progress[i] - 0.5f) < ITEM_SPACE) return false;
        }
        return hasEmptySlot();
    }

    private boolean hasEmptySlot() {
        for (Item it : items) {
            if (it == null) return true;
        }
        return false;
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        if (!acceptItem(source, item)) return false;
        boolean fromBack = directionOf(source) == getFacing().getOpposite();
        if (fromBack) {
            int best = -1;
            float bestP = Float.MAX_VALUE;
            for (int i = 0; i < SLOTS; i++) {
                if (items[i] == null && progress[i] < bestP) {
                    bestP = progress[i];
                    best = i;
                }
            }
            if (best < 0) return false;
            items[best] = item;
            progress[best] = 0.001f;
        } else {
            for (int i = 0; i < SLOTS; i++) {
                if (items[i] == null) {
                    items[i] = item;
                    progress[i] = 0.5f;
                    break;
                }
            }
        }
        setChanged();
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag itemsTag = new ListTag();
        ListTag progTag = new ListTag();
        for (int i = 0; i < SLOTS; i++) {
            itemsTag.add(StringTag.valueOf(items[i] != null
                    ? BuiltInRegistries.ITEM.getKey(items[i]).toString() : ""));
            progTag.add(FloatTag.valueOf(progress[i]));
        }
        tag.put("bd_belt_items", itemsTag);
        tag.put("bd_belt_progress", progTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ListTag itemsTag = tag.getList("bd_belt_items", net.minecraft.nbt.Tag.TAG_STRING);
        ListTag progTag = tag.getList("bd_belt_progress", net.minecraft.nbt.Tag.TAG_FLOAT);
        for (int i = 0; i < SLOTS && i < itemsTag.size() && i < progTag.size(); i++) {
            String key = itemsTag.getString(i);
            items[i] = key.isEmpty() ? null : BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(key));
            progress[i] = progTag.getFloat(i);
        }
    }
}
