package com.blockdustry.power;

import java.util.ArrayList;
import java.util.List;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 电力节点（Mindustry PowerNode）：0 产 0 耗，纯连通工具，激光连接范围内同队有电建筑喵
public class PowerNodeBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    public static final float LASER_RANGE = 6f;
    public static final int MAX_NODES = 10;

    private final List<BlockPos> links = new ArrayList<>();
    private float powerStatus;
    private boolean autolinked;

    // 放置构造：super 用注册表实体类型（drill 同款，无循环）喵
    public PowerNodeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.POWER_NODE_ENTITY.get(), pos, state);
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
        return links;
    }

    @Override
    protected void tickAnchor() {
        // 节点 0 产 0 耗，连通由 PowerGridManager 处理喵
    }

    // 放置后自动连接范围内合法有电建筑（Mindustry autolink）喵
    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && !autolinked) {
            autolinked = true;
            autolink();
        }
    }

    public void autolink() {
        if (level == null || level.isClientSide) return;
        int r = (int) LASER_RANGE;
        for (int dx = -r; dx <= r && links.size() < MAX_NODES; dx++) {
            for (int dz = -r; dz <= r && links.size() < MAX_NODES; dz++) {
                BlockPos target = worldPosition.offset(dx, 0, dz);
                if (target.equals(worldPosition)) continue;
                if (linkValid(target)) {
                    connect(target);
                }
            }
        }
    }

    // 合法连接判定：同队、目标有电、距离内、对方未满喵
    public boolean linkValid(BlockPos target) {
        if (level == null || target == null || target.equals(worldPosition)) return false;
        BlockEntity be = level.getBlockEntity(target);
        if (!(be instanceof BlockdustryPowerNode other)) return false;
        if (!getTeam().canInteract(other.getTeam())) return false;
        double dist = worldPosition.distSqr(target);
        if (dist > LASER_RANGE * LASER_RANGE) return false;
        if (be instanceof PowerNodeBlockEntity pn && pn.links.size() >= MAX_NODES) return false;
        return true;
    }

    // 连接/断开目标（双向记录）喵
    public void toggleLink(BlockPos target) {
        if (links.contains(target)) {
            links.remove(target);
            BlockEntity be = level.getBlockEntity(target);
            if (be instanceof PowerNodeBlockEntity pn) pn.links.remove(worldPosition);
        } else if (linkValid(target) && links.size() < MAX_NODES) {
            connect(target);
        }
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private void connect(BlockPos target) {
        links.add(target);
        BlockEntity be = level.getBlockEntity(target);
        if (be instanceof PowerNodeBlockEntity pn && !pn.links.contains(worldPosition)) {
            pn.links.add(worldPosition);
            pn.setChanged();
        }
        setChanged();
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
