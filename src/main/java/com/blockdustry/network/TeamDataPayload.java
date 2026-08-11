package com.blockdustry.network;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端→客户端：返回目标当前队伍喵
public record TeamDataPayload(BlockPos pos, int entityId, String teamName) implements CustomPacketPayload {
    public static final Type<TeamDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "team_data"));
    public static final StreamCodec<ByteBuf, TeamDataPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, TeamDataPayload::pos,
                    ByteBufCodecs.VAR_INT, TeamDataPayload::entityId,
                    ByteBufCodecs.STRING_UTF8, TeamDataPayload::teamName,
                    TeamDataPayload::new);

    @Override
    public Type<TeamDataPayload> type() {
        return TYPE;
    }
}
