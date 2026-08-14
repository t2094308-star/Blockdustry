package com.blockdustry.building;

import java.util.ArrayList;
import java.util.List;

import com.blockdustry.power.BlockdustryPowerNode;
import com.blockdustry.power.PowerNodeBlockEntity;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// Mindustry PowerNode 涌电塔（surge-tower）实体喵。
// 忠实原版：size=2、maxNodes=2、laserRange=40f（Blocks.java L2494-2500）喵。
// 0 产 0 耗，纯连通工具；激光连接范围内同队有电建筑（40 格远距）喵。
// 自包含：不与共享 PowerNodeBlockEntity 混用（避免改共享文件），
// 但为互连兼容，链接 PowerNodeBlockEntity 时调用其 connect() 写反向链接喵。
// 已知差距：PowerNodeBlockEntity.linkValid 硬编码 MAX_NODES=10，外部普通节点连本塔时可能略超 maxNodes=2，
// 本塔 tick 内自清理多余链接维持 maxNodes（见整合清单「已知差距」）喵
public class SurgeTowerBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    public static final float LASER_RANGE = 40f;   // 原版 laserRange=40f 喵
    public static final int MAX_NODES = 2;          // 原版 maxNodes=2 喵
    public static final int SIZE = 2;               // 原版 size=2 喵

    private final List<BlockPos> links = new ArrayList<>();
    private float powerStatus;
    private float lastSyncedStatus = -1f;
    private boolean autolinked;
    private boolean needsAutolink;

    // 方块实体注册用 (BlockPos, BlockState) 构造器，委托给带类型的完整构造器（引用自注册器，延迟解析避免循环）喵
    public SurgeTowerBlockEntity(BlockPos pos, BlockState state) {
        this(DiodeSurgeTowerRegistrar.SURGE_TOWER_ENTITY.get(), pos, state);
    }

    public SurgeTowerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 涌电塔不吃任何物品喵
    @Override
    public boolean acceptsItem(Item item) {
        return false;
    }

    // 多格建筑中心格（锚点为 NW，size2 中心偏移 +1,+1）喵
    private BlockPos center() {
        return worldPosition.offset(SIZE / 2, 0, SIZE / 2);
    }

    @Override
    protected void tickAnchor() {
        if (level == null || level.isClientSide) return;
        // 首 tick 自动连接（此时相邻建筑已就位）喵
        if (needsAutolink) {
            needsAutolink = false;
            autolink();
        }
        // 清理失效链接：目标建筑被移除后激光仍残留；区块未加载时保留喵
        if (links.removeIf(p -> level.isLoaded(p) && !(level.getBlockEntity(p) instanceof BlockdustryPowerNode))) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        // 自清理：维持 maxNodes=2（外部节点连入超限时兜底）喵
        if (links.size() > MAX_NODES) {
            links.subList(MAX_NODES, links.size()).clear();
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        // 电网满足率变化时同步客户端（激光颜色随 status 渐变）喵
        if (Math.abs(powerStatus - lastSyncedStatus) > 0.02f) {
            lastSyncedStatus = powerStatus;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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

    // 自动连接：以塔中心为球心扫 40 格球范围，链接同队有电建筑（最多 maxNodes）喵
    public void autolink() {
        if (level == null || level.isClientSide) return;
        BlockPos c = center();
        int r = (int) LASER_RANGE;
        for (int dx = -r; dx <= r && links.size() < MAX_NODES; dx++) {
            for (int dy = -r; dy <= r && links.size() < MAX_NODES; dy++) {
                for (int dz = -r; dz <= r && links.size() < MAX_NODES; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r * r) continue;
                    BlockPos target = c.offset(dx, dy, dz);
                    if (target.equals(c)) continue;
                    // 只扫已加载区块：40 格范围若不守卫 isLoaded 会强制加载约百个区块喵
                    if (!level.isLoaded(target)) continue;
                    if (linkValid(target)) connect(target);
                }
            }
        }
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 合法连接判定：同队、目标有电、以塔中心为球心 40 格内、对方节点未满喵
    public boolean linkValid(BlockPos target) {
        if (level == null || target == null || target.equals(worldPosition)) return false;
        BlockEntity be = level.getBlockEntity(target);
        if (!(be instanceof BlockdustryPowerNode other)) return false;
        if (!getTeam().canInteract(other.getTeam())) return false;
        if (center().distSqr(target) > LASER_RANGE * LASER_RANGE) return false;
        // 对方为节点时检查对方容量（Mindustry linkValid 检查目标节点 maxNodes）喵
        if (be instanceof PowerNodeBlockEntity pn && pn.getPowerLinks().size() >= PowerNodeBlockEntity.MAX_NODES) return false;
        if (be instanceof SurgeTowerBlockEntity st && st.links.size() >= MAX_NODES) return false;
        return true;
    }

    // 连接/断开目标（双向记录）喵
    public void toggleLink(BlockPos target) {
        if (links.contains(target)) {
            links.remove(target);
            BlockEntity be = level.getBlockEntity(target);
            if (be instanceof PowerNodeBlockEntity pn) pn.getPowerLinks().remove(worldPosition);
            if (be instanceof SurgeTowerBlockEntity st) st.links.remove(worldPosition);
        } else if (linkValid(target) && links.size() < MAX_NODES) {
            connect(target);
        }
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void connect(BlockPos target) {
        if (links.contains(target)) return; // 防重复喵
        links.add(target);
        BlockEntity be = level.getBlockEntity(target);
        // 反向链接：普通节点走其 connect()（内部再判断类型）；涌电塔互连直接互加喵
        if (be instanceof PowerNodeBlockEntity pn) {
            if (!pn.getPowerLinks().contains(worldPosition)) {
                pn.connect(worldPosition);
            }
        } else if (be instanceof SurgeTowerBlockEntity st && !st.links.contains(worldPosition)) {
            st.links.add(worldPosition);
            st.setChanged();
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
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

    @Override
    public List<BlockPos> getPowerLinks() {
        // 非锚点格委托给锚点格的 links：避免外部节点连到非锚点格导致整塔电网分裂（多格建筑统一连接）喵
        if (!isAnchor() && getAnchor() != null && level != null
                && level.getBlockEntity(getAnchor()) instanceof SurgeTowerBlockEntity st) {
            return st.links;
        }
        return links;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (BlockPos p : links) {
            list.add(NbtUtils.writeBlockPos(p));
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
