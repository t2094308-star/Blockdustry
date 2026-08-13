package com.blockdustry.research;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端→客户端：玩家队伍共享池物品数表（item RL 字符串 → 数量），供研究面板显示「剩余/需求」喵
// 消耗走队伍共享资源（BlockdustryTeamStorage 核心池），客户端须知道池内各材料存量喵
public record ResearchStoragePayload(Map<String, Integer> items) implements CustomPacketPayload {
    public static final Type<ResearchStoragePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "research_storage"));

    private static final IntFunction<Map<String, Integer>> MAP_FACTORY = HashMap::new;

    public static final StreamCodec<ByteBuf, ResearchStoragePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(MAP_FACTORY, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
                    ResearchStoragePayload::items,
                    ResearchStoragePayload::new);

    @Override
    public Type<ResearchStoragePayload> type() {
        return TYPE;
    }
}
