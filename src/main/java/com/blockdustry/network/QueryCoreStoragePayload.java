package com.blockdustry.network;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端→服务端：查询当前玩家队伍的共享核心物资总数（右上角 HUD 用）喵
public record QueryCoreStoragePayload() implements CustomPacketPayload {
    public static final Type<QueryCoreStoragePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "query_core_storage"));
    public static final StreamCodec<ByteBuf, QueryCoreStoragePayload> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> {
            }, buf -> new QueryCoreStoragePayload());

    @Override
    public Type<QueryCoreStoragePayload> type() {
        return TYPE;
    }
}
