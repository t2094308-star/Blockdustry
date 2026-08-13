package com.blockdustry.building;

import com.blockdustry.item.BlockdustryItems;
import com.blockdustry.logistics.BlockdustryItemSink;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.team.BlockdustryTeamStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 核心（Mindustry CoreBlock 迁移）：队伍共享存储入口，实现 BlockdustryItemSink 委托入队池；
// 只收迁移的全部材料（煤/沙/石墨/硅/铅/铜/玻璃/钛/钍/塑料钢等）；玩家队伍绑定后死亡在此重生（CoreRespawnHandler）喵
public class CoreBlockEntity extends BlockdustryBuildingEntity implements BlockdustryItemSink {
    // 白名单：全部迁移材料（含 MC 煤/沙），惰性缓存避免每次 acceptItem 重组装列表喵
    private static final java.util.Set<Item> WHITELIST = buildWhitelist();

    private static java.util.Set<Item> buildWhitelist() {
        return new java.util.HashSet<>(BlockdustryItems.allMaterials());
    }

    public CoreBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.CORE_ENTITY.get(), pos, state);
    }

    @Override
    protected void tickAnchor() {
        // 核心无 tick 逻辑，存储由共享池维护喵
    }

    // 白名单：所有迁移材料喵
    private static boolean isWhitelisted(Item item) {
        return WHITELIST.contains(item);
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
