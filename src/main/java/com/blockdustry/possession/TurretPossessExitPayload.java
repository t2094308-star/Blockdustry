package com.blockdustry.possession;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端→服务端：玩家潜行退出炮台附身（携带当前附身炮塔坐标做校验）喵
public record TurretPossessExitPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<TurretPossessExitPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "turret_possess_exit"));
    public static final StreamCodec<ByteBuf, TurretPossessExitPayload> STREAM_CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, TurretPossessExitPayload::pos,
                    TurretPossessExitPayload::new);

    @Override
    public Type<TurretPossessExitPayload> type() {
        return TYPE;
    }
}
