package com.blockdustry.possession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.blockdustry.Blockdustry;
import com.blockdustry.BlockdustryTeams;
import com.blockdustry.building.TurretBlockEntity;
import com.blockdustry.team.BlockdustryTeam;
import com.blockdustry.tick.BlockdustryTicks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// 炮台附身服务端管理器：维护 玩家↔炮塔 映射，附身时传送/冻结玩家、把炮塔切为手动模式，
// 每模组 tick 校验有效性并防漂移；玩家视角（yRot）经网络包驱动炮塔转向/开火喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public final class TurretPossessManager {
    // 附身快照：直接持有 ServerPlayer 引用（登出由事件兜底清理）+ 炮塔坐标 + 原位/原朝向喵
    private record Possession(ServerPlayer player, BlockPos turretPos, Vec3 originPos,
                              float originYRot, float originXRot) {}

    private static final Map<UUID, Possession> POSSESSIONS = new ConcurrentHashMap<>();
    private static boolean hooked;

    private TurretPossessManager() {}

    // 幂等挂到模组新 tick（主会话在 Blockdustry 构造器调用）喵
    public static void hook() {
        if (!hooked) {
            hooked = true;
            BlockdustryTicks.register(TurretPossessManager::tickAll);
        }
    }

    // —— 空手右键炮塔进入 / 再次右键退出（服务端，单机不重复）——
    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.getMainHandItem().isEmpty()) return; // 空手喵
        // 让位电力节点连接模式：玩家正选中节点时，右键交给 PowerNode 处理喵
        if (player.getPersistentData().getLong("bd_selected_node") != 0L) return;
        BlockPos pos = event.getPos();
        BlockEntity be = player.serverLevel().getBlockEntity(pos);
        if (!(be instanceof TurretBlockEntity turret)) return;
        // 队伍校验：只能附身同队或无主（DERELICT）炮塔喵
        BlockdustryTeam pteam = BlockdustryTeams.getTeam(player);
        BlockdustryTeam tteam = turret.getTeam();
        if (pteam != tteam && tteam != BlockdustryTeam.DERELICT) {
            player.sendSystemMessage(Component.literal("不能附身异队炮塔"));
            event.setCanceled(true);
            return;
        }
        toggle(player, pos);
        event.setCanceled(true);
    }

    // —— 登出自动退出，防止残留与炮塔卡手动模式 ——
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Possession p = POSSESSIONS.remove(player.getUUID());
            if (p != null && player.serverLevel() != null
                    && player.serverLevel().getBlockEntity(p.turretPos()) instanceof TurretBlockEntity turret) {
                turret.setManualMode(false);
            }
            // 玩家已登出，无需传送/发包喵
        }
    }

    public static void toggle(ServerPlayer player, BlockPos pos) {
        UUID id = player.getUUID();
        Possession p = POSSESSIONS.get(id);
        if (p != null && p.turretPos().equals(pos)) {
            unpossess(player, p, true);
        } else {
            if (p != null) unpossess(player, p, true);
            possess(player, pos);
        }
    }

    // 客户端请求退出（潜行）：校验确实附身了该炮塔喵
    public static void clientExit(ServerPlayer player, BlockPos pos) {
        Possession p = POSSESSIONS.get(player.getUUID());
        if (p != null && p.turretPos().equals(pos)) {
            unpossess(player, p, true);
        }
    }

    // 附身相机站位：炮塔正上方往上找「脚底与头顶都在空气」的干净位置，
    // 保证相机不被实体方块遮挡（否则会黑屏）；找不到就回退塔顶上方 3 格喵
    private static Vec3 cameraSpot(ServerLevel level, BlockPos pos) {
        for (int i = 1; i <= 6; i++) {
            BlockPos feet = pos.above(i);
            if (level.isEmptyBlock(feet) && level.isEmptyBlock(feet.above())) {
                return Vec3.atBottomCenterOf(feet);
            }
        }
        return Vec3.atBottomCenterOf(pos.above(3));
    }

    private static void possess(ServerPlayer player, BlockPos pos) {
        BlockEntity be = player.serverLevel().getBlockEntity(pos);
        if (!(be instanceof TurretBlockEntity turret)) return;
        UUID id = player.getUUID();
        Possession p = new Possession(player, pos, player.position(), player.getYRot(), player.getXRot());
        POSSESSIONS.put(id, p);
        turret.setManualMode(true);
        // 传送玩家到炮塔正上方的空气位（脚底在塔顶之上、眼睛避开实体方块），
        // noPhysics 防窒息、noGravity 防坠落、invisible 隐藏身体喵
        Vec3 cam = cameraSpot(player.serverLevel(), pos);
        player.teleportTo(cam.x, cam.y, cam.z);
        player.setNoGravity(true);
        player.noPhysics = true;
        player.setInvisible(true);
        player.getPersistentData().putBoolean("bd_possessing", true);
        // 通知客户端进入附身（含射程，供穿透视野绘制）喵
        PacketDistributor.sendToPlayer(player, new TurretPossessStatePayload(true, pos, turret.getRange()));
    }

    private static void unpossess(ServerPlayer player, Possession p, boolean notify) {
        if (player == null) return;
        POSSESSIONS.remove(player.getUUID());
        if (player.serverLevel().getBlockEntity(p.turretPos()) instanceof TurretBlockEntity turret) {
            turret.setManualMode(false);
        }
        player.teleportTo(player.serverLevel(), p.originPos().x, p.originPos().y, p.originPos().z,
                p.originYRot(), p.originXRot());
        player.setNoGravity(false);
        player.noPhysics = false;
        player.setInvisible(false);
        player.getPersistentData().remove("bd_possessing");
        if (notify) {
            PacketDistributor.sendToPlayer(player, new TurretPossessStatePayload(false, p.turretPos(), 0f));
        }
    }

    // —— 网络包处理（主会话在 BlockdustryNetwork 里接线）——
    public static void handleControl(TurretControlPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                Possession p = POSSESSIONS.get(player.getUUID());
                if (p == null) return;
                BlockEntity be = player.serverLevel().getBlockEntity(p.turretPos());
                if (!(be instanceof TurretBlockEntity turret)) return;
                turret.setManualAim(payload.aimYaw(), payload.aimPitch());
                if (payload.fire()) turret.requestManualFire();
            }
        });
    }

    public static void handleExit(TurretPossessExitPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                clientExit(player, payload.pos());
            }
        });
    }

    // 每模组 tick：失效清理 + 保持玩家钉在炮塔（防推挤漂移）喵
    private static void tickAll() {
        for (var it = POSSESSIONS.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            Possession p = entry.getValue();
            ServerPlayer player = p.player();
            // 玩家死亡：退出并通知客户端（死亡后引用仍有效，可安全还原）喵
            if (player == null || player.isRemoved() || !player.isAlive()) {
                it.remove();
                if (player != null) unpossess(player, p, true);
                continue;
            }
            // 玩家已彻底断开（登出事件应已兜底，这里防御）喵
            if (player.getServer() == null) {
                it.remove();
                continue;
            }
            // 炮塔被破坏：退出并还原玩家喵
            if (!(player.serverLevel().getBlockEntity(p.turretPos()) instanceof TurretBlockEntity)) {
                it.remove();
                unpossess(player, p, true);
                continue;
            }
            // 保持玩家钉在炮塔正上方的相机位，防推挤漂移（与 possess 同一站位）喵
            Vec3 target = cameraSpot(player.serverLevel(), p.turretPos());
            if (player.distanceToSqr(target) > 0.25) {
                player.teleportTo(target.x, target.y, target.z);
            }
            player.setNoGravity(true);
        }
    }
}
