package com.blockdustry.research;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端→服务端：请求研究指定节点（服务端从玩家背包扣料、记进度、满则解锁）喵
public record ResearchSpendPayload(String nodeId) implements CustomPacketPayload {
    public static final Type<ResearchSpendPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "research_spend"));
    public static final StreamCodec<ByteBuf, ResearchSpendPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ResearchSpendPayload::nodeId, ResearchSpendPayload::new);

    @Override
    public Type<ResearchSpendPayload> type() {
        return TYPE;
    }
}
