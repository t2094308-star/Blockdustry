package com.blockdustry.possession;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 客户端→服务端：玩家操控炮塔的转向（aimYaw，度）、俯仰（aimPitch，度）与开火请求，每 1~2 tick 节流发送喵
public record TurretControlPayload(float aimYaw, float aimPitch, boolean fire) implements CustomPacketPayload {
    public static final Type<TurretControlPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "turret_control"));
    public static final StreamCodec<ByteBuf, TurretControlPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.FLOAT, TurretControlPayload::aimYaw,
                    ByteBufCodecs.FLOAT, TurretControlPayload::aimPitch,
                    ByteBufCodecs.BOOL, TurretControlPayload::fire,
                    TurretControlPayload::new);

    @Override
    public Type<TurretControlPayload> type() {
        return TYPE;
    }
}
