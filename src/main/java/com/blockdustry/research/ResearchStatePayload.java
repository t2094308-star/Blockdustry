package com.blockdustry.research;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

import com.blockdustry.Blockdustry;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// 服务端→客户端：研究全量状态（已解锁节点集合 + 各节点研究进度），id 均用 RL 字符串传输喵
public record ResearchStatePayload(Set<String> unlocked, Map<String, Map<String, Integer>> progress)
        implements CustomPacketPayload {
    public static final Type<ResearchStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Blockdustry.MODID, "research_state"));

    private static final IntFunction<Set<String>> SET_FACTORY = HashSet::new;
    private static final IntFunction<Map<String, Integer>> ITEM_MAP_FACTORY = HashMap::new;
    private static final IntFunction<Map<String, Map<String, Integer>>> PROGRESS_FACTORY = HashMap::new;

    private static final StreamCodec<ByteBuf, Set<String>> UNLOCKED_CODEC =
            ByteBufCodecs.collection(SET_FACTORY, ByteBufCodecs.STRING_UTF8);
    private static final StreamCodec<ByteBuf, Map<String, Integer>> ITEM_MAP_CODEC =
            ByteBufCodecs.map(ITEM_MAP_FACTORY, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT);
    private static final StreamCodec<ByteBuf, Map<String, Map<String, Integer>>> PROGRESS_CODEC =
            ByteBufCodecs.map(PROGRESS_FACTORY, ByteBufCodecs.STRING_UTF8, ITEM_MAP_CODEC);

    public static final StreamCodec<ByteBuf, ResearchStatePayload> STREAM_CODEC = StreamCodec.composite(
            UNLOCKED_CODEC, ResearchStatePayload::unlocked,
            PROGRESS_CODEC, ResearchStatePayload::progress,
            ResearchStatePayload::new);

    @Override
    public Type<ResearchStatePayload> type() {
        return TYPE;
    }
}
