package com.blockdustry.jade;

import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

// 方块工业建筑信息客户端 tooltip：队伍（按队伍色着色）/内容物/电量；制作进度由 Jade 原生进度条显示喵
public class BuildingInfoComponentProvider implements IBlockComponentProvider {
    public static final BuildingInfoComponentProvider INSTANCE = new BuildingInfoComponentProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data == null || !data.contains(BuildingInfoServerDataProvider.KEY_TEAM)) {
            return; // 非方块工业建筑，跳过喵
        }
        String teamName = data.getString(BuildingInfoServerDataProvider.KEY_TEAM);
        int teamColor = BlockdustryTeam.byName(teamName).getColor();
        tooltip.add(Component.literal("队伍: " + teamName).withColor(teamColor));
        if (data.contains(BuildingInfoServerDataProvider.KEY_COAL)) {
            int coal = data.getInt(BuildingInfoServerDataProvider.KEY_COAL);
            int graphite = data.getInt(BuildingInfoServerDataProvider.KEY_GRAPHITE);
            tooltip.add(Component.literal("煤: " + coal + " | 石墨: " + graphite));
        } else if (data.contains(BuildingInfoServerDataProvider.KEY_ITEM)) {
            String item = data.getString(BuildingInfoServerDataProvider.KEY_ITEM);
            int count = data.getInt(BuildingInfoServerDataProvider.KEY_ITEM_COUNT);
            int cap = data.getInt(BuildingInfoServerDataProvider.KEY_CAPACITY);
            tooltip.add(Component.literal("内容: " + item + " x" + count + " / " + cap));
        }
        if (data.contains(BuildingInfoServerDataProvider.KEY_STATUS)) {
            float produced = data.getFloat(BuildingInfoServerDataProvider.KEY_PRODUCED);
            float needed = data.getFloat(BuildingInfoServerDataProvider.KEY_NEEDED);
            int stored = Math.round(data.getFloat(BuildingInfoServerDataProvider.KEY_STORED));
            int cap = Math.round(data.getFloat(BuildingInfoServerDataProvider.KEY_POWER_CAP));
            int status = Math.round(data.getFloat(BuildingInfoServerDataProvider.KEY_STATUS) * 100f);
            tooltip.add(Component.literal("电量: 产 " + fmt(produced) + "/t | 耗 " + fmt(needed) + "/t | 存 "
                    + stored + "/" + cap + " | 满足率 " + status + "%"));
        }
    }

    // 数字格式化：整数省去小数点喵
    private static String fmt(float v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format("%.1f", v);
    }

    @Override
    public ResourceLocation getUid() {
        return BlockdustryJadePlugin.UID_BUILDING_INFO;
    }
}
