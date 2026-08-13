package com.blockdustry.client;

import com.blockdustry.Blockdustry;
import com.blockdustry.possession.TurretControlPayload;
import com.blockdustry.possession.TurretPossessExitPayload;
import com.blockdustry.possession.TurretPossessStatePayload;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// 炮台附身客户端处理器：附身后视角锁定炮台（身体被传送进炮塔、相机俯仰锁水平）、
// 玩家鼠标驱动炮塔转向、左键开火、潜行退出；绘制穿透视野准星/瞄准线/射程圈喵
@EventBusSubscriber(modid = Blockdustry.MODID, value = Dist.CLIENT)
public class TurretPossessHandler {
    // 本地附身状态（由服务端 TurretPossessStatePayload 确认）喵
    private static boolean active;
    private static BlockPos turretPos;
    private static float range = 20f; // duo 默认射程，进入时以服务端为准喵

    // 控制包节流与潜行退出计时喵
    private static long lastControlSend = Long.MIN_VALUE;
    private static float lastSentAimYaw = Float.NaN;
    private static float lastSentAimPitch = Float.NaN;
    private static int shiftPressTicks;

    // 穿透视野专用线渲染（关深度测试，能看穿地形）喵
    private static final RenderType AIM_LINES = RenderType.create(
            "bd_possess_aim_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            1536, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(RenderStateShard.DEFAULT_LINE)
                    .setLayeringState(RenderStateShard.NO_LAYERING)
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOutputState(RenderStateShard.MAIN_TARGET)
                    .createCompositeState(false));

    private TurretPossessHandler() {}

    public static boolean isPossessing() {
        return active;
    }

    public static BlockPos getPossessedTurret() {
        return turretPos;
    }

    // 服务端进入/退出确认（主会话在 BlockdustryNetwork 里接线）喵
    public static void onState(TurretPossessStatePayload payload) {
        active = payload.entering();
        turretPos = payload.pos();
        if (payload.entering()) range = payload.range();
        shiftPressTicks = 0;
        lastControlSend = Long.MIN_VALUE;
        lastSentAimYaw = Float.NaN;
        lastSentAimPitch = Float.NaN;
    }

    // —— 附身后每 tick：发转向/开火控制包 + 潜行退出 ——
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!active || mc.player == null || mc.level == null || turretPos == null) return;
        // 潜行退出：需连续按住 5 tick（0.25s）防误触喵
        // 用实体潜行状态（isShiftKeyDown）而非裸按键判断，兼容「切换潜行」设置下
        // keyShift.isDown() 只按一下即假、导致 0.25s 累计不上而无法退出的问题喵
        if (mc.player.isShiftKeyDown() || mc.options.keyShift.isDown()) {
            if (++shiftPressTicks >= 5) {
                PacketDistributor.sendToServer(new TurretPossessExitPayload(turretPos));
                active = false; // 本地先退出，服务端随后还原玩家喵
                shiftPressTicks = 0;
                return;
            }
        } else {
            shiftPressTicks = 0;
        }
        // 玩家 yRot → 炮塔 aimYaw（推导：aimYaw = 180 - yRot，见 docs/子agent/T5）；xRot → aimPitch（向下看为正）喵
        float aimYaw = 180f - mc.player.getYRot();
        float aimPitch = mc.player.getXRot();
        boolean fire = mc.options.keyAttack.isDown();
        long gt = mc.level.getGameTime();
        if (fire || Float.isNaN(lastSentAimYaw) || Float.isNaN(lastSentAimPitch)
                || gt - lastControlSend >= 2
                || Math.abs(aimYaw - lastSentAimYaw) > 1f
                || Math.abs(aimPitch - lastSentAimPitch) > 1f) {
            lastControlSend = gt;
            lastSentAimYaw = aimYaw;
            lastSentAimPitch = aimPitch;
            PacketDistributor.sendToServer(new TurretControlPayload(aimYaw, aimPitch, fire));
        }
    }

    // —— 相机俯仰放开：允许鼠标上下看（pitch 跟随玩家 xRot 自由），只锁 roll 保持无侧倾喵 ——
    @SubscribeEvent
    public static void onComputeCamera(ViewportEvent.ComputeCameraAngles event) {
        if (!active) return;
        event.setRoll(0);
    }

    // —— 隐藏本地玩家模型（身体在炮塔里不渲染）喵 ——
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (active && event.getEntity() == Minecraft.getInstance().player) {
            event.setCanceled(true);
        }
    }

    // —— 隐藏第一人称手臂喵 ——
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (active) event.setCanceled(true);
    }

    // —— 冻结移动输入：附身期间身体钉在炮塔，不走动/不跳喵 ——
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!active || event.getEntity() != Minecraft.getInstance().player) return;
        Input input = event.getInput();
        input.forwardImpulse = 0;
        input.leftImpulse = 0;
        input.up = input.down = input.left = input.right = false;
        input.jumping = false;
    }

    // —— 取消攻击/使用键：左键开火由控制包驱动，不再敲方块/交互喵 ——
    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (active && (event.isAttack() || event.isUseItem())) {
            event.setCanceled(true);
        }
    }

    // —— HUD 准星：显示炮台锁定方向 + 操作提示喵 ——
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics g = event.getGuiGraphics();
        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        // 绿色准星十字喵
        g.fill(cx - 6, cy - 1, cx + 7, cy + 1, 0xFF55FF55);
        g.fill(cx - 1, cy - 6, cx + 1, cy + 7, 0xFF55FF55);
        // 操作提示喵
        g.drawCenteredString(mc.font, Component.literal("炮台附身 · 左键开火 · 潜行退出"), cx, cy + 14, 0xFFFFFF);
    }

    // —— 穿透视野：世界空间画射程圈（关深度测试，可看穿地形）；
    // 红线瞄准线已按用户反馈移除（弹道终点已对准玩家准星，HUD 准星即瞄线）喵 ——
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (!active || turretPos == null) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        Vec3 cam = event.getCamera().getPosition();
        Vec3 center = Vec3.atCenterOf(turretPos);
        PoseStack pose = event.getPoseStack();
        // RenderLevelStageEvent 未暴露 buffer source，用全局 entity buffer source 画线后按类型刷新喵
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vc = buffer.getBuffer(AIM_LINES);
        // 射程圈（炮塔底所在 y 平面，半径=射程）喵
        double y = turretPos.getY() + 0.02 - cam.y;
        int segs = 48;
        for (int i = 0; i < segs; i++) {
            double a0 = Math.PI * 2 * i / segs;
            double a1 = Math.PI * 2 * (i + 1) / segs;
            line(pose, vc,
                    center.x + Math.cos(a0) * range - cam.x, y, center.z + Math.sin(a0) * range - cam.z,
                    center.x + Math.cos(a1) * range - cam.x, y, center.z + Math.sin(a1) * range - cam.z,
                    100, 220, 255);
        }
        // 仅刷新本渲染类型，避免影响其他渲染器的 pending buffer 喵
        buffer.endBatch(AIM_LINES);
    }

    // 线段（相机相对坐标，世界坐标已由事件 PoseStack 平移过相机）喵
    private static void line(PoseStack pose, VertexConsumer vc,
                             double x0, double y0, double z0, double x1, double y1, double z1,
                             int r, int g, int b) {
        var matrix = pose.last().pose();
        vc.addVertex(matrix, (float) x0, (float) y0, (float) z0).setColor(r, g, b, 255).setNormal(0f, 1f, 0f);
        vc.addVertex(matrix, (float) x1, (float) y1, (float) z1).setColor(r, g, b, 255).setNormal(0f, 1f, 0f);
    }
}
