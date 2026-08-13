package com.blockdustry.research;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端→客户端：单节点解锁事件（研究完成时发，客户端弹提示/音效用）喵
public record ResearchUnlockPayload(String nodeId) implements CustomPacketPayload {
    public static final Type<ResearchUnlockPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "research_unlock"));
    public static final StreamCodec<ByteBuf, ResearchUnlockPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ResearchUnlockPayload::nodeId, ResearchUnlockPayload::new);

    @Override
    public Type<ResearchUnlockPayload> type() {
        return TYPE;
    }
}
