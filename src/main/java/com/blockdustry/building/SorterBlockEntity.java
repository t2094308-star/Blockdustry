package com.blockdustry.building;

import com.blockdustry.distribution.SorterRegistrar;
import com.blockdustry.logistics.BlockdustryItemSink;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.logistics.LogisticsUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// 分拣器方块实体（Mindustry SorterBuild 忠实移植）喵。
// 关键：instantTransfer=true——不设缓冲，acceptItem 按「设定物品 + invert + 来向」算出目标邻居，
// handleItem 立即把物品移交过去；sorter 直通同向来向，inverted-sorter 设定物品反向、其他直通喵。
// invert 由方块类型决定（sorter=false / inverted-sorter=true），同一 BE 类覆盖两种方块喵
public class SorterBlockEntity extends BlockdustryBuildingEntity {
    // 设定物品（null=未设定；Mindustry configClear，未设定时 sorter 全侧出、invertedSorter 全直通）喵
    private Item sortItem;
    // 双向都收时的轮换 bitmask（Mindustry Building.rotation & (1<<dir)，防同配置 sorter 堵住一侧）喵
    private int sideRotation;

    public SorterBlockEntity(BlockPos pos, BlockState state) {
        super(SorterRegistrar.SORTER_ENTITY.get(), pos, state);
    }

    // 当前设定物品（供菜单高亮 / 渲染 / Jade 读取）喵
    public Item getSortItem() {
        return sortItem;
    }

    // 反转标记：方块决定（sorter=false，inverted-sorter=true）喵
    public boolean isInvert() {
        return getBlockState().getBlock() instanceof SorterBlock sb && sb.isInvert();
    }

    // 菜单选中/清空：服务端设置设定物品（null=清空），并同步客户端喵
    public void setSortItem(Item item) {
        if (item == Items.AIR) item = null;
        sortItem = item;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 瞬时传递：无缓冲，本格不实际存放；接受判定 = 目标邻居能接收。
    // 深度兜底：瞬时链递归超限（超 16）即拒收，防 sorter↔gate 等对脸直连同步递归 StackOverflow 喵
    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (!LogisticsUtil.enterTransfer()) return false;
        try {
            if (source == null || item == null) return false;
            if (!getTeam().canInteract(source.getTeam())) return false;
            BlockPos target = resolveTarget(item, source, false);
            if (target == null) return false;
            if (level != null && level.getBlockEntity(target) instanceof BlockdustryItemSink sink) {
                return sink.acceptItem(this, item);
            }
            return false;
        } finally {
            LogisticsUtil.exitTransfer();
        }
    }

    // 真正移交：目标邻居接收该物品（Mindustry handleItem → getTileTarget(flip=true).handleItem）。
    // 深度兜底同 acceptItem 喵
    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        if (!LogisticsUtil.enterTransfer()) return false;
        try {
            if (!acceptItem(source, item)) return false;
            BlockPos target = resolveTarget(item, source, true);
            if (target == null) return false;
            if (level != null && level.getBlockEntity(target) instanceof BlockdustryItemSink sink) {
                return sink.handleItem(this, item);
            }
            return false;
        } finally {
            LogisticsUtil.exitTransfer();
        }
    }

    // Mindustry getTileTarget：按「(item==sortItem)!=invert」判直通/侧出，返回目标格喵
    // flip=true 时（handleItem）双向都收才翻转轮换 bit，与 acceptItem(flip=false) 判定一致喵
    private BlockPos resolveTarget(Item item, BlockdustryItemSource source, boolean flip) {
        if (level == null) return null;
        Direction dir = directionFrom(source);
        if (dir == null) return null;
        // Mindustry enabled 恒 true（分拣器无电力）；直通 = (item==sortItem) 异或 invert 喵
        boolean straight = ((item == sortItem) != isInvert());
        if (straight) {
            // 防三连环（Mindustry isSame 双 instantTransfer）：源与正前方都是瞬时块（Sorter|Gate）→ 不直通，避免瞬时无限传递喵
            if (LogisticsUtil.isInstant(source)
                    && LogisticsUtil.isInstant(level.getBlockEntity(worldPosition.relative(dir)))) {
                return null;
            }
            return worldPosition.relative(dir);
        }
        Direction da = dir.getClockWise();            // Mindustry mod(dir-1,4) 喵
        Direction db = dir.getCounterClockWise();     // Mindustry mod(dir+1,4) 喵
        BlockPos pa = worldPosition.relative(da);
        BlockPos pb = worldPosition.relative(db);
        boolean ac = canSideReceive(pa, item, source);
        boolean bc = canSideReceive(pb, item, source);
        if (ac && !bc) return pa;
        if (bc && !ac) return pb;
        if (!bc) return null; // 两侧都不收 → 拒收喵
        int bit = 1 << dir.get2DDataValue();
        BlockPos to = (sideRotation & bit) == 0 ? pa : pb;
        if (flip) sideRotation ^= bit;
        return to;
    }

    // 侧向接收判定（Mindustry 侧分支 ac/bc）：目标存在、非「源+侧目标双瞬时」死循环、且目标可接收喵
    private boolean canSideReceive(BlockPos p, Item item, BlockdustryItemSource source) {
        BlockEntity be = level.getBlockEntity(p);
        if (!(be instanceof BlockdustryItemSink sink)) return false;
        if (LogisticsUtil.isInstant(be) && LogisticsUtil.isInstant(source)) return false;
        return sink.acceptItem(this, item);
    }

    // 来源相对本格的方位（Mindustry source.relativeTo：从源指向本格的方向）喵
    private Direction directionFrom(BlockdustryItemSource source) {
        if (source == null || source.getPos() == null) return null;
        BlockPos sp = source.getPos();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (sp.relative(d).equals(worldPosition)) return d;
        }
        return null;
    }

    // 瞬时传递：无需逐 tick 行为（Mindustry update=false）喵
    @Override
    protected void tickAnchor() {
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (sortItem != null) {
            tag.putString("bd_sort_item", BuiltInRegistries.ITEM.getKey(sortItem).toString());
        }
        tag.putInt("bd_side_rot", sideRotation);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("bd_sort_item")) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("bd_sort_item")));
            if (item != null && item != Items.AIR) sortItem = item;
        }
        sideRotation = tag.getInt("bd_side_rot");
    }
}
