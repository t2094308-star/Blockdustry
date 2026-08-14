package com.blockdustry.defense;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import com.blockdustry.building.BlockdustryBuildingEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

// 门方块实体（Mindustry DoorBuild）：open 状态 + 连锁门（相邻同 block 同队门同开同关）喵。
// 核心动画（用户最高要求）：
//   - 状态切换（Mindustry config(Boolean)）：切换时服务端播 Sounds.door 音效 + 记录切换时刻/方向，供渲染器画方块轮廓特效
//   （Fx.dooropen 外扩 / Fx.doorclose 内缩，Effect(10) tick）喵
//   - 有实体不能关（Mindustry Units.anyEntities(tile)）、右键冷却（timerToggle 60f）喵
public class DoorBlockEntity extends BlockdustryBuildingEntity {
    /** Mindustry tapped timerToggle = 60f tick 喵 */
    public static final int TOGGLE_COOLDOWN = 60;
    /** Mindustry Fx.dooropen/doorclose = Effect(10) tick 喵 */
    public static final int EFFECT_DURATION = 10;

    private boolean open;
    private long lastToggleGameTime = Long.MIN_VALUE; // 上次切换时刻（MC 游戏刻），渲染器画特效用喵
    private boolean lastToggleOpening = true;          // 上次切换方向：true=开（dooropen 外扩）、false=关（doorclose 内缩）喵

    public DoorBlockEntity(BlockPos pos, BlockState state) {
        super(DefenseRegistrar.DOOR_ENTITY.get(), pos, state);
    }

    public DoorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 非锚点格转发给锚点格（2×2 四格统一状态）喵
    public boolean isOpen() {
        if (hasAnchor() && !isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            if (anchorBe instanceof DoorBlockEntity anchor) return anchor.isOpen();
        }
        return open;
    }

    public boolean isOpening() {
        return lastToggleOpening;
    }

    /** 距上次切换经过的游戏刻；未切换过返回 Long.MAX_VALUE（渲染器不画特效）喵 */
    public long getEffectAge(long gameTime) {
        return lastToggleGameTime == Long.MIN_VALUE ? Long.MAX_VALUE : gameTime - lastToggleGameTime;
    }

    // 右键切换（Mindustry DoorBuild.tapped → configure(!open)）：
    // 开门中且门内有实体不关（Units.anyEntities）、右键冷却 60 tick 喵
    public void toggle() {
        if (level == null || level.isClientSide) return;
        if (open && anyEntityInside()) return;
        if (lastToggleGameTime != Long.MIN_VALUE && level.getGameTime() - lastToggleGameTime < TOGGLE_COOLDOWN) return;
        setOpen(!open);
    }

    // 设置开/关：状态 + 连锁 + 音效 + 特效标记 + 同步客户端（Mindustry config 处理器核心逻辑）喵
    public void setOpen(boolean newOpen) {
        if (level == null || level.isClientSide) return;
        if (open == newOpen) return;
        open = newOpen;
        lastToggleGameTime = level.getGameTime();
        lastToggleOpening = newOpen;
        playDoorSound();
        // 连锁：BFS 相邻同 block 同队门同开同关（Mindustry updateChained + chained 遍历）喵
        for (DoorBlockEntity chained : findChained()) {
            if (chained == this) continue;
            // 关闭时该扇门内有实体则跳过（Mindustry 循环内 anyEntities 检查，chainEffect=false 只 base 出特效）喵
            if (!newOpen && chained.anyEntityInside()) continue;
            if (chained.open == newOpen) continue;
            chained.open = newOpen;
            chained.lastToggleGameTime = lastToggleGameTime;
            chained.lastToggleOpening = newOpen;
            chained.syncToClient();
        }
        syncToClient();
    }

    private void playDoorSound() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, worldPosition, DefenseRegistrar.DOOR_SOUND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    // 同步 BE 数据（open/特效时刻）到客户端：sendBlockUpdated 触发 ChunkHolder 广播块更新 + BE 数据包喵
    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 该格是否有实体（Mindustry Units.anyEntities：玩家/单位在门格内阻挡关门；忽略掉落物）喵
    private boolean anyEntityInside() {
        if (level == null) return false;
        AABB box = new AABB(worldPosition);
        return !level.getEntities((Entity) null, box, e -> !(e instanceof ItemEntity)).isEmpty();
    }

    // BFS 找相邻连锁门（Mindustry DoorBuild.updateChained 的 BFS）：同 block + 同队 喵
    private Set<DoorBlockEntity> findChained() {
        Set<DoorBlockEntity> result = new HashSet<>();
        if (level == null) return result;
        ArrayDeque<DoorBlockEntity> queue = new ArrayDeque<>();
        result.add(this);
        queue.add(this);
        while (!queue.isEmpty()) {
            DoorBlockEntity cur = queue.poll();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos p = cur.worldPosition.relative(dir);
                BlockEntity be = level.getBlockEntity(p);
                if (be instanceof DoorBlockEntity d
                        && d.getBlockState().getBlock() == getBlockState().getBlock()
                        && d.getTeam() == getTeam()
                        && result.add(d)) {
                    queue.add(d);
                }
            }
        }
        return result;
    }

    @Override
    protected void tickAnchor() {
        // 门是纯被动方块，无每 tick 逻辑（Mindustry Door 无 updateTile）喵
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("bd_open", open);
        tag.putLong("bd_toggle_time", lastToggleGameTime);
        tag.putBoolean("bd_toggle_opening", lastToggleOpening);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        open = tag.getBoolean("bd_open");
        if (tag.contains("bd_toggle_time")) lastToggleGameTime = tag.getLong("bd_toggle_time");
        if (tag.contains("bd_toggle_opening")) lastToggleOpening = tag.getBoolean("bd_toggle_opening");
    }
}
