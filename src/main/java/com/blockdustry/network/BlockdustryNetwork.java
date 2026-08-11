package com.blockdustry.network;

import com.blockdustry.Blockdustry;
import com.blockdustry.BlockdustryTeams;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// 队伍调试网络：查询/设置方块与实体队伍喵
@EventBusSubscriber(modid = Blockdustry.MODID, bus = EventBusSubscriber.Bus.MOD)
public class BlockdustryNetwork {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Blockdustry.MODID);
        registrar.playToServer(QueryTeamPayload.TYPE, QueryTeamPayload.STREAM_CODEC, BlockdustryNetwork::handleQuery);
        registrar.playToServer(SetTeamPayload.TYPE, SetTeamPayload.STREAM_CODEC, BlockdustryNetwork::handleSet);
        registrar.playToClient(TeamDataPayload.TYPE, TeamDataPayload.STREAM_CODEC, BlockdustryNetwork::handleTeamData);
    }

    private static void handleQuery(QueryTeamPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            BlockdustryTeam team;
            if (payload.entityId() != -1) {
                Entity entity = player.serverLevel().getEntity(payload.entityId());
                team = entity != null ? BlockdustryTeams.getTeam(entity) : BlockdustryTeam.DERELICT;
            } else {
                team = BlockdustryTeams.getTeam(player.serverLevel(), payload.pos());
            }
            PacketDistributor.sendToPlayer(player, new TeamDataPayload(payload.pos(), payload.entityId(), team.name()));
        });
    }

    private static void handleSet(SetTeamPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            BlockdustryTeam team = BlockdustryTeam.byName(payload.teamName());
            if (payload.entityId() != -1) {
                Entity entity = player.serverLevel().getEntity(payload.entityId());
                if (entity != null) BlockdustryTeams.setTeam(entity, team);
            } else {
                BlockdustryTeams.setTeam(player.serverLevel(), payload.pos(), team);
            }
            PacketDistributor.sendToPlayer(player, new TeamDataPayload(payload.pos(), payload.entityId(), team.name()));
        });
    }

    // 客户端：把返回的队伍更新到打开的队伍 UI 喵
    private static void handleTeamData(TeamDataPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof com.blockdustry.client.BlockdustryTeamScreen screen) {
                screen.updateTeam(payload.teamName());
            }
        });
    }
}
