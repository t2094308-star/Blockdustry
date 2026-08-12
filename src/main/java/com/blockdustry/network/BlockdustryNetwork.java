package com.blockdustry.network;

import com.blockdustry.Blockdustry;
import com.blockdustry.BlockdustryTeams;
import com.blockdustry.team.BlockdustryTeam;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.team.BlockdustryTeam;
import com.blockdustry.team.BlockdustryTeamStorage;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        registrar.playToServer(QueryPowerPayload.TYPE, QueryPowerPayload.STREAM_CODEC, BlockdustryNetwork::handleQueryPower);
        registrar.playToClient(PowerDataPayload.TYPE, PowerDataPayload.STREAM_CODEC, BlockdustryNetwork::handlePowerData);
        registrar.playToServer(QueryCoreStoragePayload.TYPE, QueryCoreStoragePayload.STREAM_CODEC, BlockdustryNetwork::handleQueryCoreStorage);
        registrar.playToClient(CoreStorageDataPayload.TYPE, CoreStorageDataPayload.STREAM_CODEC, BlockdustryNetwork::handleCoreStorageData);
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

    // 查询目标电力信息（调试棒用）喵
    private static void handleQueryPower(QueryPowerPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            BlockEntity be = player.serverLevel().getBlockEntity(payload.pos());
            if (be instanceof com.blockdustry.power.BlockdustryPowerNode pn) {
                PacketDistributor.sendToPlayer(player, new PowerDataPayload(payload.pos(),
                        pn.powerProduction(), pn.powerNeeded(), pn.powerCapacity(), pn.powerStored(), pn.getPowerStatus()));
            } else {
                PacketDistributor.sendToPlayer(player, new PowerDataPayload(payload.pos(), 0f, 0f, 0f, 0f, 0f));
            }
        });
    }

    // 客户端：把返回的电力信息更新到打开的队伍 UI 喵
    private static void handlePowerData(PowerDataPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof com.blockdustry.client.BlockdustryTeamScreen screen) {
                screen.updatePower(payload);
            }
        });
    }

    // 查询玩家队伍共享核心物资（煤/石墨数量，HUD 用；DERELICT 兜底回退 SHARDED 喵）
    private static void handleQueryCoreStorage(QueryCoreStoragePayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            BlockdustryTeam team = BlockdustryTeams.getTeam(player);
            if (team == BlockdustryTeam.DERELICT) team = BlockdustryTeam.SHARDED;
            int coal = 0;
            int graphite = 0;
            BlockdustryTeamStorage.Storage s = BlockdustryTeamStorage.get(player.serverLevel(), team);
            coal = s.getCount(Items.COAL);
            graphite = s.getCount(BlockdustryBlocks.GRAPHITE.get());
            PacketDistributor.sendToPlayer(player, new CoreStorageDataPayload(coal, graphite));
        });
    }

    // 客户端：更新右上角核心物资资源栏（Mindustry 资源栏样式）喵
    private static void handleCoreStorageData(CoreStorageDataPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.blockdustry.client.CoreHudHandler.setCoreStorage(payload.coal(), payload.graphite()));
    }
}
