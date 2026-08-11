package com.blockdustry.network;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端→服务端：查询目标（方块或实体）当前队伍喵
public record QueryTeamPayload(BlockPos pos, int entityId) implements CustomPacketPayload {
    public static final Type<QueryTeamPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "query_team"));
    public static final StreamCodec<ByteBuf, QueryTeamPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, QueryTeamPayload::pos,
                    ByteBufCodecs.VAR_INT, QueryTeamPayload::entityId,
                    QueryTeamPayload::new);

    @Override
    public Type<QueryTeamPayload> type() {
        return TYPE;
    }
}
