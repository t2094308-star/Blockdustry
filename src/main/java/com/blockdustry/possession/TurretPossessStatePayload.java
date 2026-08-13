package com.blockdustry.possession;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端→客户端：附身状态变更（进入/退出 + 炮塔坐标 + 射程，射程供穿透视野绘制用）喵
public record TurretPossessStatePayload(boolean entering, BlockPos pos, float range) implements CustomPacketPayload {
    public static final Type<TurretPossessStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "turret_possess_state"));
    public static final StreamCodec<ByteBuf, TurretPossessStatePayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, TurretPossessStatePayload::entering,
                    BlockPos.STREAM_CODEC, TurretPossessStatePayload::pos,
                    ByteBufCodecs.FLOAT, TurretPossessStatePayload::range,
                    TurretPossessStatePayload::new);

    @Override
    public Type<TurretPossessStatePayload> type() {
        return TYPE;
    }
}
