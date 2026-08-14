package com.blockdustry.distribution;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.SorterBlockEntity;
import com.blockdustry.item.BlockdustryItems;
import com.blockdustry.network.SorterSelectPayload;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// 分拣器配置网络（自包含 @EventBusSubscriber，NeoForge 自动发现，无需改共享 BlockdustryNetwork）喵。
// 服务端处理 SorterSelectPayload：校验目标确为分拣器后设置设定物品；空串=清空喵
@EventBusSubscriber(modid = Blockdustry.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class SorterNetwork {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Blockdustry.MODID);
        registrar.playToServer(SorterSelectPayload.TYPE, SorterSelectPayload.STREAM_CODEC, SorterNetwork::handleSelect);
    }

    // 服务端：菜单选中设定物品，校验在迁移材料内（与物品源同纪律）；itemId 空串=清空喵
    private static void handleSelect(SorterSelectPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            BlockEntity be = player.serverLevel().getBlockEntity(payload.pos());
            if (be instanceof SorterBlockEntity sorter) {
                if (payload.itemId().isEmpty()) {
                    sorter.setSortItem(null);
                } else {
                    Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(payload.itemId()));
                    if (item != null && item != Items.AIR && BlockdustryItems.allMaterials().contains(item)) {
                        sorter.setSortItem(item);
                    }
                }
            }
        });
    }

    private SorterNetwork() {}
}
