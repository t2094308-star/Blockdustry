package com.blockdustry.network;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端→客户端：返回玩家队伍核心共享池中白名单物品（煤/石墨）数量，供右上角资源栏显示喵
public record CoreStorageDataPayload(int coal, int graphite) implements CustomPacketPayload {
    public static final Type<CoreStorageDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "core_storage_data"));
    public static final StreamCodec<ByteBuf, CoreStorageDataPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, CoreStorageDataPayload::coal,
                    ByteBufCodecs.VAR_INT, CoreStorageDataPayload::graphite,
                    CoreStorageDataPayload::new);

    @Override
    public Type<CoreStorageDataPayload> type() {
        return TYPE;
    }
}
