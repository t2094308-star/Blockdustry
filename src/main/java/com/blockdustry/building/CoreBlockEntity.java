package com.blockdustry.building;

import com.blockdustry.logistics.BlockdustryItemSink;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.team.BlockdustryTeamStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 核心（Mindustry CoreBlock 迁移）：队伍共享存储入口，实现 BlockdustryItemSink 委托入队池；
// 只收白名单物品（煤炭/石墨）；玩家队伍绑定后死亡在此重生（CoreRespawnHandler）喵
public class CoreBlockEntity extends BlockdustryBuildingEntity implements BlockdustryItemSink {
    public CoreBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.CORE_ENTITY.get(), pos, state);
    }

    @Override
    protected void tickAnchor() {
        // 核心无 tick 逻辑，存储由共享池维护喵
    }

    // 白名单：目前只收煤炭与石墨（Mindustry 核心存储任意物，这里先限定）喵
    private static boolean isWhitelisted(Item item) {
        return item == Items.COAL || item == BlockdustryBlocks.GRAPHITE.get();
    }

    // 白名单 + 队伍共享池可收则接受喵
    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (source == null || item == null) return false;
        if (!isWhitelisted(item)) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        if (level == null || level.isClientSide) return false;
        return BlockdustryTeamStorage.get((ServerLevel) level, getTeam()).canAccept(item);
    }

    // 真正入队伍共享池喵
    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        if (!acceptItem(source, item)) return false;
        BlockdustryTeamStorage.get((ServerLevel) level, getTeam()).add(item, 1);
        return true;
    }
}
