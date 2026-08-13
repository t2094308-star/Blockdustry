package com.blockdustry.network;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端→服务端：物品源菜单选中某产物（pos + 物品注册名）喵
public record ItemSourceSelectPayload(BlockPos pos, String itemId) implements CustomPacketPayload {
    public static final Type<ItemSourceSelectPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "item_source_select"));
    public static final StreamCodec<ByteBuf, ItemSourceSelectPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, ItemSourceSelectPayload::pos,
                    ByteBufCodecs.STRING_UTF8, ItemSourceSelectPayload::itemId,
                    ItemSourceSelectPayload::new);

    @Override
    public Type<ItemSourceSelectPayload> type() {
        return TYPE;
    }
}
