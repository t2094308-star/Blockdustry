package com.blockdustry.research;

import com.blockdustry.Blockdustry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// 科技树网络：注册 5 个 payload（独立于 BlockdustryNetwork，同 namespace 的 registrar 共享底层注册表）喵
@EventBusSubscriber(modid = Blockdustry.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ResearchNetwork {
    private ResearchNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Blockdustry.MODID);
        registrar.playToClient(ResearchStatePayload.TYPE, ResearchStatePayload.STREAM_CODEC, ResearchNetwork::handleState);
        registrar.playToClient(ResearchUnlockPayload.TYPE, ResearchUnlockPayload.STREAM_CODEC, ResearchNetwork::handleUnlock);
        registrar.playToClient(ResearchStoragePayload.TYPE, ResearchStoragePayload.STREAM_CODEC, ResearchNetwork::handleStorage);
        registrar.playToServer(ResearchSpendPayload.TYPE, ResearchSpendPayload.STREAM_CODEC, ResearchNetwork::handleSpend);
        registrar.playToServer(QueryResearchPayload.TYPE, QueryResearchPayload.STREAM_CODEC, ResearchNetwork::handleQuery);
    }

    // 客户端：收到全量状态 → 更新客户端缓存（FQN 引用客户端类避免专用服 ClassNotFound）喵
    private static void handleState(ResearchStatePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.blockdustry.client.ResearchClientCache.update(payload.unlocked(), payload.progress()));
    }

    // 客户端：单节点解锁事件 → 缓存 + 提示喵
    private static void handleUnlock(ResearchUnlockPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.blockdustry.client.ResearchClientCache.onUnlock(payload.nodeId()));
    }

    // 客户端：队伍共享池物品数 → 客户端缓存（研究面板「剩余/需求」用）喵
    private static void handleStorage(ResearchStoragePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.blockdustry.client.ResearchClientCache.updateStorage(payload.items()));
    }

    // 服务端：研究请求 → 从队伍共享池扣料/解锁 → 广播新状态 + 各玩家队伍存量喵
    private static void handleSpend(ResearchSpendPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            ResourceLocation id = ResourceLocation.tryParse(payload.nodeId());
            if (id == null) return;
            boolean wasUnlocked = ResearchManager.isUnlocked(player.serverLevel(), id);
            int spent = ResearchManager.spend(player, id);
            boolean nowUnlocked = ResearchManager.isUnlocked(player.serverLevel(), id);
            // 有投入，或空需求节点刚自动解锁 → 都要广播喵
            if (spent > 0 || (!wasUnlocked && nowUnlocked)) {
                if (!wasUnlocked && nowUnlocked) {
                    PacketDistributor.sendToAllPlayers(new ResearchUnlockPayload(id.toString()));
                }
                PacketDistributor.sendToAllPlayers(ResearchManager.buildState(player.serverLevel()));
                broadcastStorage(player.serverLevel());
            }
        });
    }

    // 服务端：状态刷新请求 → 回全量 + 本玩家队伍存量（开屏/入服兜底）喵
    private static void handleQuery(QueryResearchPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            PacketDistributor.sendToPlayer(player, ResearchManager.buildState(player.serverLevel()));
            PacketDistributor.sendToPlayer(player, new ResearchStoragePayload(ResearchManager.buildStorage(player)));
        });
    }

    // 按玩家广播各自队伍共享池存量（队伍不同则池不同，必须逐人发）喵
    private static void broadcastStorage(ServerLevel level) {
        for (ServerPlayer p : level.players()) {
            PacketDistributor.sendToPlayer(p, new ResearchStoragePayload(ResearchManager.buildStorage(p)));
        }
    }
}
