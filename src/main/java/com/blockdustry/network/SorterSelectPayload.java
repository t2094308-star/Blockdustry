package com.blockdustry.network;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端→服务端：分拣器菜单选中设定物品（pos + 物品注册名；itemId 空串=清空配置）喵。
// 独立 payload 类型，不复用 item_source_select，避免两种建筑配置数据串用（数据不串最高要求）喵
public record SorterSelectPayload(BlockPos pos, String itemId) implements CustomPacketPayload {
    public static final Type<SorterSelectPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "sorter_select"));
    public static final StreamCodec<ByteBuf, SorterSelectPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, SorterSelectPayload::pos,
                    ByteBufCodecs.STRING_UTF8, SorterSelectPayload::itemId,
                    SorterSelectPayload::new);

    @Override
    public Type<SorterSelectPayload> type() {
        return TYPE;
    }
}
