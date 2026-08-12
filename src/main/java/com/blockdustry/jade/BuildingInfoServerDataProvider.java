package com.blockdustry.jade;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.building.GraphitePressBlockEntity;
import com.blockdustry.power.BlockdustryPowerNode;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

// 方块工业建筑信息服务端数据：队伍/内容物/制作进度/电量写入 Jade 同步 NBT 喵
public class BuildingInfoServerDataProvider implements IServerDataProvider<BlockAccessor> {
    public static final BuildingInfoServerDataProvider INSTANCE = new BuildingInfoServerDataProvider();
    public static final String KEY_TEAM = "bd_team";
    public static final String KEY_ITEM = "bd_item";
    public static final String KEY_ITEM_COUNT = "bd_item_count";
    public static final String KEY_CAPACITY = "bd_capacity";
    public static final String KEY_COAL = "bd_coal";
    public static final String KEY_GRAPHITE = "bd_graphite";
    public static final String KEY_PROGRESS = "bd_progress";
    public static final String KEY_PRODUCED = "bd_produced";
    public static final String KEY_NEEDED = "bd_needed";
    public static final String KEY_STORED = "bd_stored";
    public static final String KEY_POWER_CAP = "bd_power_cap";
    public static final String KEY_STATUS = "bd_status";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (be instanceof BlockdustryBuildingEntity building) {
            // 多格建筑统一读锚点格数据，避免钻头 4 格各自显示不同步喵
            if (!building.isAnchor() && building.hasAnchor()) {
                BlockEntity anchorBe = accessor.getLevel().getBlockEntity(building.getAnchor());
                if (anchorBe instanceof BlockdustryBuildingEntity anchorBuilding) {
                    building = anchorBuilding;
                }
            }
            data.putString(KEY_TEAM, building.getTeam().name());
            if (be instanceof GraphitePressBlockEntity g) {
                // 石墨压机：煤/石墨库存（进度条由 ProgressServerProvider 独立提供）喵
                data.putInt(KEY_COAL, g.getCoalCount());
                data.putInt(KEY_GRAPHITE, g.getGraphiteCount());
            } else {
                // 普通建筑内置库存（钻机存矿、发电机存煤等）喵
                Item item = building.getStoredItem();
                if (item != null) {
                    data.putString(KEY_ITEM, item.getDescription().getString());
                    data.putInt(KEY_ITEM_COUNT, building.getStoredCount());
                    data.putInt(KEY_CAPACITY, building.getCapacity());
                }
            }
            // 电力建筑电量喵
            if (be instanceof BlockdustryPowerNode pn) {
                data.putFloat(KEY_PRODUCED, pn.powerProduction());
                data.putFloat(KEY_NEEDED, pn.powerNeeded());
                data.putFloat(KEY_STORED, pn.powerStored());
                data.putFloat(KEY_POWER_CAP, pn.powerCapacity());
                data.putFloat(KEY_STATUS, pn.getPowerStatus());
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return BlockdustryJadePlugin.UID_BUILDING_INFO;
    }
}
