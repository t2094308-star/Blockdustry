package com.blockdustry.research;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端→服务端：请求刷新研究全量状态（开屏/入服兜底，防 join 同步时序问题）喵
public record QueryResearchPayload() implements CustomPacketPayload {
    public static final Type<QueryResearchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "query_research"));
    public static final StreamCodec<ByteBuf, QueryResearchPayload> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> {
            }, buf -> new QueryResearchPayload());

    @Override
    public Type<QueryResearchPayload> type() {
        return TYPE;
    }
}
