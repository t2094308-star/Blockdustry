package com.blockdustry.network;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端→客户端：返回目标电力信息（产/耗/容量/存量/满足率）喵
public record PowerDataPayload(BlockPos pos, float produced, float needed, float capacity, float stored, float status)
        implements CustomPacketPayload {
    public static final Type<PowerDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "power_data"));
    public static final StreamCodec<ByteBuf, PowerDataPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, PowerDataPayload::pos,
                    ByteBufCodecs.FLOAT, PowerDataPayload::produced,
                    ByteBufCodecs.FLOAT, PowerDataPayload::needed,
                    ByteBufCodecs.FLOAT, PowerDataPayload::capacity,
                    ByteBufCodecs.FLOAT, PowerDataPayload::stored,
                    ByteBufCodecs.FLOAT, PowerDataPayload::status,
                    PowerDataPayload::new);

    @Override
    public Type<PowerDataPayload> type() {
        return TYPE;
    }
}
