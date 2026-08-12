package com.blockdustry.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

// 客户端 tooltip 提供者：从服务端同步的 NBT 读出 hp/maxHp 追加到 Jade 面板喵
public class BlockHpComponentProvider implements IBlockComponentProvider {
    public static final BlockHpComponentProvider INSTANCE = new BlockHpComponentProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data == null || !data.contains(BlockHpServerDataProvider.KEY_MAX_HP)) {
            return; // 服务端没同步（未装 Jade 或免疫方块）喵
        }
        float hp = data.getFloat(BlockHpServerDataProvider.KEY_HP);
        float max = data.getFloat(BlockHpServerDataProvider.KEY_MAX_HP);
        tooltip.add(Component.translatable("blockdustry.jade.block_hp",
                String.format("%.0f", hp), String.format("%.0f", max)));
    }

    @Override
    public ResourceLocation getUid() {
        return BlockdustryJadePlugin.UID_BLOCK_HP;
    }
}
