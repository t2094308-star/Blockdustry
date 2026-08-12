package com.blockdustry.network;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端→服务端：查询目标方块的电力信息（调试用）喵
public record QueryPowerPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<QueryPowerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "query_power"));
    public static final StreamCodec<ByteBuf, QueryPowerPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, QueryPowerPayload::pos, QueryPowerPayload::new);

    @Override
    public Type<QueryPowerPayload> type() {
        return TYPE;
    }
}
