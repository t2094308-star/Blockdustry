package com.blockdustry.jade;

import com.blockdustry.lib.BlockHealthApi;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

// 服务端数据提供者：把方块血量 hp/maxHp 写入 Jade 同步 NBT，供客户端 tooltip 显示喵
public class BlockHpServerDataProvider implements IServerDataProvider<BlockAccessor> {
    public static final BlockHpServerDataProvider INSTANCE = new BlockHpServerDataProvider();
    public static final String KEY_HP = "blockdustry_hp";
    public static final String KEY_MAX_HP = "blockdustry_max_hp";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        // 仅逻辑服务端调用，accessor.getLevel() 是 ServerLevel，能读到 blockhealth 附件喵
        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        float max = BlockHealthApi.getMaxHp(level, pos);
        if (max <= 0f) {
            return; // 免疫方块（基岩等）不显示血量喵
        }
        float hp = BlockHealthApi.getHp(level, pos);
        data.putFloat(KEY_HP, hp);
        data.putFloat(KEY_MAX_HP, max);
    }

    @Override
    public ResourceLocation getUid() {
        return BlockdustryJadePlugin.UID_BLOCK_HP;
    }
}
