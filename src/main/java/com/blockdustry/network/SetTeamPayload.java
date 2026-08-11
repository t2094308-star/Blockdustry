package com.blockdustry.network;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端→服务端：设置目标（方块或实体）队伍喵
public record SetTeamPayload(BlockPos pos, int entityId, String teamName) implements CustomPacketPayload {
    public static final Type<SetTeamPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "set_team"));
    public static final StreamCodec<ByteBuf, SetTeamPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, SetTeamPayload::pos,
                    ByteBufCodecs.VAR_INT, SetTeamPayload::entityId,
                    ByteBufCodecs.STRING_UTF8, SetTeamPayload::teamName,
                    SetTeamPayload::new);

    @Override
    public Type<SetTeamPayload> type() {
        return TYPE;
    }
}
