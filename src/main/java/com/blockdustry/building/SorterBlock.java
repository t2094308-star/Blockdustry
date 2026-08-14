package com.blockdustry.building;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

// 分拣器方块（Mindustry Sorter）：1×1 建筑，空手右键打开配置菜单选择「设定物品」喵。
// 同一 SorterBlock 类用 invert 区分 sorter / inverted-sorter（Mindustry 也是同一 Sorter 类 + invert 字段）喵
public class SorterBlock extends BlockdustryBuildingBlock {
    // 反转标记：false=sorter（设定物品直通、其他侧出）；true=inverted-sorter（设定物品侧出、其他直通）喵
    private final boolean invert;

    public SorterBlock(Properties properties, Supplier<BlockEntityType<?>> entityType, int size, boolean invert) {
        super(properties, entityType, size);
        this.invert = invert;
    }

    public boolean isInvert() {
        return invert;
    }

    // 空手右键：客户端打开分拣器配置菜单（Mindustry 配置菜单风格）；服务端仅消费交互喵
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            Item current = null;
            if (level.getBlockEntity(pos) instanceof SorterBlockEntity sb) {
                current = sb.getSortItem();
            }
            mc.setScreen(new com.blockdustry.client.SorterScreen(pos, current));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
