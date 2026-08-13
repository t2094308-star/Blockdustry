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

// 物品源方块：1×1 建筑，空手右键在客户端打开菜单（Mindustry 配置菜单风格），点选产物喵。
// 覆写 useWithoutItem（1.21.1 空手交互入口），客户端弹菜单、服务端仅消费交互喵
public class ItemSourceBlock extends BlockdustryBuildingBlock {
    public ItemSourceBlock(Properties properties, Supplier<BlockEntityType<?>> entityType, int size) {
        super(properties, entityType, size);
    }

    // 空手右键：客户端打开物品源菜单；服务端不处理（产物由菜单选择设置）喵
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            Item current = null;
            if (level.getBlockEntity(pos) instanceof ItemSourceBlockEntity src) {
                current = src.getProduct();
            }
            mc.setScreen(new com.blockdustry.client.ItemSourceScreen(pos, current));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }
}
