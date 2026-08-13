package com.blockdustry.building;

import com.blockdustry.logistics.BlockdustryItemSink;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 垂直提升机方块实体（T9 方案 B1）：单槽直上带，一格存 1 件 + progress∈[0,1]，
// 从下方/侧面收件，沿 +y 逐格上移，顶格交给上方 sink（另一节提升机或 Router/机器）喵
public class ElevatorBlockEntity extends BlockdustryBuildingEntity {
    public static final int SLOTS = 1;
    public static final float SPEED = 0.046f;   // 格/tick（与传送带一致）喵
    private static final float NEXT_MAX_BLOCKED = 0.95f;
    private static final Direction[] HORIZONTALS =
            { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

    private final Item[] items = new Item[SLOTS];
    private final float[] progress = new float[SLOTS];

    public ElevatorBlockEntity(BlockPos pos, BlockState state) {
        super(ElevatorBlocks.ELEVATOR_ENTITY.get(), pos, state);
    }

    public ElevatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 输出方向固定朝上（y 轴），不像传送带水平朝向喵
    public Direction getFacing() {
        return Direction.UP;
    }

    // 供渲染器读取带槽喵
    public Item[] beltItems() {
        return items;
    }

    public float[] beltProgress() {
        return progress;
    }

    // 来源格相对本格的方位：上方=UP / 水平=东/南/西/北 / 下方=DOWN 喵
    private Direction directionOf(BlockdustryItemSource source) {
        if (source == null || source.getPos() == null) return null;
        if (worldPosition.above().equals(source.getPos())) return Direction.UP;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (worldPosition.relative(dir).equals(source.getPos())) return dir;
        }
        if (worldPosition.below().equals(source.getPos())) return Direction.DOWN;
        return null;
    }

    // 每模组 tick：物品沿 +y 推进，顶格交给上方 sink 喵
    @Override
    protected void tickAnchor() {
        Item item = items[0];
        if (item == null) return;

        BlockEntity frontBe = level.getBlockEntity(worldPosition.above());
        BlockdustryItemSink frontSink = frontBe instanceof BlockdustryItemSink s ? s : null;
        boolean headCanExit = frontSink != null && frontSink.acceptItem(this, item);

        // 出口被堵时顶到 0.95 停住（保持带内，不穿模进上方格）喵
        float limit = headCanExit ? 1.0f : NEXT_MAX_BLOCKED;
        float p = progress[0] + SPEED;
        if (p > limit) p = limit;
        if (p < 0.001f) p = 0.001f;
        progress[0] = p;

        // 顶格交接喵
        if (headCanExit && progress[0] >= 1.0f
                && frontSink.acceptItem(this, item)
                && frontSink.handleItem(this, item)) {
            items[0] = null;
            progress[0] = 0f;
        } else if (!headCanExit && progress[0] >= NEXT_MAX_BLOCKED && dumpItemTop(item)) {
            // 上方不可收时：物品已顶到本格上沿（Y+1 层），水平吐给顶面四邻（Y+1 层）的传送带/建筑，
            // 使输出保持在提升后的一层高，而不是回落到输入端同 Y 层喵
            items[0] = null;
            progress[0] = 0f;
        }
        syncBelt();
    }

    // 顶面四邻卸货（T18 跟进）：顶格物品在 Y+1 层水平吐给四邻传送带/建筑喵。
    // 关键：源位置取「候选格正下方一格」，使传送带 directionOf 按「正下方源」判定收件（与垂直交接同分支）喵
    private boolean dumpItemTop(Item item) {
        if (level == null || item == null) return false;
        for (int i = 0; i < 4; i++) {
            int idx = (dumpPointer + i) % 4;
            Direction dir = HORIZONTALS[idx];
            BlockPos p = worldPosition.above().relative(dir); // Y+1 层邻格喵
            BlockEntity be = level.getBlockEntity(p);
            if (be instanceof BlockdustryItemSink sink) {
                BlockdustryItemSource source = new TopSource(p.below()); // 源定位在候选格正下方喵
                if (sink.acceptItem(source, item) && sink.handleItem(source, item)) {
                    dumpPointer = (idx + 1) % 4;
                    setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    // 顶面卸货用的源：getPos 返回候选格正下方一格，使接收方（传送带）按「正下方源」判定方向喵
    private final class TopSource implements BlockdustryItemSource {
        private final BlockPos pos;

        TopSource(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public BlockdustryTeam getTeam() {
            return ElevatorBlockEntity.this.getTeam();
        }

        @Override
        public BlockPos getPos() {
            return pos;
        }
    }

    // 非空时同步客户端喵
    private void syncBelt() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 预检：同队 + 来源非上方（上方是输出端）+ 槽空喵
    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (level == null || item == null || source == null) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        Direction entry = directionOf(source);
        if (entry == null || entry == Direction.UP) return false; // 不从输出端收喵
        return items[0] == null;
    }

    // 真正移交：从格底开始上移喵
    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        if (!acceptItem(source, item)) return false;
        items[0] = item;
        progress[0] = 0.001f;
        setChanged();
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("bd_elevator_item", items[0] != null
                ? BuiltInRegistries.ITEM.getKey(items[0]).toString() : "");
        tag.putFloat("bd_elevator_progress", progress[0]);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("bd_elevator_item")) {
            String key = tag.getString("bd_elevator_item");
            items[0] = key.isEmpty() ? null : BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(key));
        }
        progress[0] = tag.getFloat("bd_elevator_progress");
    }
}
