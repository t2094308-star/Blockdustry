package com.blockdustry.power;

import java.util.ArrayList;
import java.util.List;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 大型电力节点（Mindustry power-node-large）：2×2、maxNodes 15、laserRange 15f，纯连通工具喵。
// 多格电网连通性：锚点格 getPowerLinks() 返回「实际激光链接 + 本组全部格」，保证节点连到任意格都能让整组入网
// （与 1×1 节点/电池互连时电网不分裂）；激光渲染端自行过滤本组格喵
public class PowerNodeLargeBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    public static final float LASER_RANGE = 15f; // 原版 Blocks.java power-node-large laserRange=15f 喵
    public static final int MAX_NODES = 15;      // 原版 maxNodes=15 喵

    // 实际激光链接（渲染 + NBT 持久化用）；本组格不存此列表，由 getPowerLinks() 动态补喵
    private final List<BlockPos> links = new ArrayList<>();
    private float powerStatus;
    private float lastSyncedStatus = -1f;
    private boolean autolinked;
    private boolean needsAutolink;

    public PowerNodeLargeBlockEntity(BlockPos pos, BlockState state) {
        super(PowerNodeLargeRegistrar.POWER_NODE_LARGE_ENTITY.get(), pos, state);
    }

    // —— BlockdustryPowerNode ——
    @Override
    public BlockdustryTeam getTeam() {
        return super.getTeam();
    }

    @Override
    public BlockPos getPos() {
        return worldPosition;
    }

    @Override
    public float powerProduction() {
        return 0f;
    }

    @Override
    public float powerNeeded() {
        return 0f;
    }

    @Override
    public float powerCapacity() {
        return 0f;
    }

    @Override
    public float powerStored() {
        return 0f;
    }

    @Override
    public float getPowerStatus() {
        return powerStatus;
    }

    @Override
    public void setPowerStatus(float status) {
        this.powerStatus = status;
    }

    // 电网用链接 = 实际激光链接 + 本组全部格（锚点格才补，非锚点格保持实际列表）。
    // 这样 Union-Find 把本组所有格与锚点连成同一分量，节点连到任意格整组入网喵
    @Override
    public List<BlockPos> getPowerLinks() {
        if (!isAnchor() || getSize() <= 1) return links;
        List<BlockPos> out = new ArrayList<>(links);
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        int size = getSize();
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                BlockPos p = base.offset(dx, 0, dz);
                if (!out.contains(p)) out.add(p);
            }
        }
        return out;
    }

    // 供渲染器：只取实际激光链接（排除本组格）喵
    public List<BlockPos> getRenderLinks() {
        return links;
    }

    @Override
    protected void tickAnchor() {
        // 首 tick 自动连接（此时相邻建筑已就位）喵
        if (needsAutolink) {
            needsAutolink = false;
            autolink();
        }
        // 清理失效链接：目标建筑被移除后激光仍残留；区块未加载时保留（避免误删后无法重连）喵
        if (links.removeIf(p -> level.isLoaded(p) && !(level.getBlockEntity(p) instanceof BlockdustryPowerNode))) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        // 电网满足率变化时同步客户端（激光颜色随 status 渐变）喵
        if (Math.abs(powerStatus - lastSyncedStatus) > 0.02f) {
            lastSyncedStatus = powerStatus;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && !autolinked) {
            autolinked = true;
            needsAutolink = true;
        }
    }

    public void autolink() {
        if (level == null || level.isClientSide) return;
        int r = (int) LASER_RANGE;
        for (int dx = -r; dx <= r && links.size() < MAX_NODES; dx++) {
            for (int dy = -r; dy <= r && links.size() < MAX_NODES; dy++) {
                for (int dz = -r; dz <= r && links.size() < MAX_NODES; dz++) {
                    // 三维球范围（Mindustry 2D 只有平面圆，MC 应含 y 轴）喵
                    if (dx * dx + dy * dy + dz * dz > r * r) continue;
                    BlockPos target = worldPosition.offset(dx, dy, dz);
                    if (target.equals(worldPosition)) continue;
                    if (linkValid(target)) {
                        connect(target);
                    }
                }
            }
        }
        // 同步客户端连接数据，激光才会显示喵
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 合法连接判定：同队、目标有电、距离内、对方未满（对方若是节点查其 maxNodes）喵
    public boolean linkValid(BlockPos target) {
        if (level == null || target == null || target.equals(worldPosition)) return false;
        // 本组格不视为外部连接目标喵
        if (isOwnCell(target)) return false;
        BlockEntity be = level.getBlockEntity(target);
        if (!(be instanceof BlockdustryPowerNode other)) return false;
        if (!getTeam().canInteract(other.getTeam())) return false;
        double dist = worldPosition.distSqr(target);
        if (dist > LASER_RANGE * LASER_RANGE) return false;
        if (be instanceof PowerNodeLargeBlockEntity pn && pn.links.size() >= MAX_NODES) return false;
        if (be instanceof PowerNodeBlockEntity pn && pn.getPowerLinks().size() >= PowerNodeBlockEntity.MAX_NODES) return false;
        return true;
    }

    // 目标是否属于本组占地（自连排除）喵
    public boolean isOwnCell(BlockPos target) {
        BlockPos base = hasAnchor() ? getAnchor() : worldPosition;
        int size = getSize();
        return target.getY() == base.getY()
                && target.getX() >= base.getX() && target.getX() < base.getX() + size
                && target.getZ() >= base.getZ() && target.getZ() < base.getZ() + size;
    }

    // 连接/断开目标（双向记录，兼容 1×1 节点）喵
    public void toggleLink(BlockPos target) {
        if (links.contains(target)) {
            links.remove(target);
            BlockEntity be = level.getBlockEntity(target);
            if (be instanceof PowerNodeLargeBlockEntity pn) pn.links.remove(worldPosition);
            else if (be instanceof PowerNodeBlockEntity pn) pn.getPowerLinks().remove(worldPosition);
        } else if (linkValid(target) && links.size() < MAX_NODES) {
            connect(target);
        }
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void connect(BlockPos target) {
        if (links.contains(target)) return;
        links.add(target);
        BlockEntity be = level.getBlockEntity(target);
        // 双向记录：对方若是任意电力节点也补上反向连接（大型节点与 1×1 节点互连）喵
        if (be instanceof PowerNodeLargeBlockEntity pn && !pn.links.contains(worldPosition)) {
            pn.links.add(worldPosition);
            pn.setChanged();
        } else if (be instanceof PowerNodeBlockEntity pn && !pn.getPowerLinks().contains(worldPosition)) {
            pn.getPowerLinks().add(worldPosition);
            pn.setChanged();
        }
        setChanged();
        // 同步客户端连接数据（LinkHandler 反向连接时也要刷新激光）喵
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (BlockPos p : links) {
            list.add(net.minecraft.nbt.NbtUtils.writeBlockPos(p));
        }
        tag.put("bd_power_links", list);
        tag.putFloat("bd_power_status", powerStatus);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        links.clear();
        ListTag list = tag.getList("bd_power_links", Tag.TAG_INT_ARRAY);
        for (int i = 0; i < list.size(); i++) {
            int[] arr = list.getIntArray(i);
            if (arr.length == 3) links.add(new BlockPos(arr[0], arr[1], arr[2]));
        }
        powerStatus = tag.getFloat("bd_power_status");
    }
}
