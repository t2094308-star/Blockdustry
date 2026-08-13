package com.blockdustry.client.freecam;

import com.blockdustry.Blockdustry;
import com.blockdustry.client.TurretPossessHandler;
import com.blockdustry.possession.TurretPossessExitPayload;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// 灵魂出窍（freecam）客户端处理器：快捷键切换 + 每 tick 驱动相机移动 + 屏蔽交互喵
// 与炮台附身互斥：freecam 激活时退出附身；附身激活时退出 freecam 喵
@EventBusSubscriber(modid = Blockdustry.MODID, value = Dist.CLIENT)
public class FreecamHandler {
    // 快捷键：F4（在 BlockdustryClient 的 RegisterKeyMappingsEvent 里注册）喵
    public static final KeyMapping KEY_FREECAM = new KeyMapping(
            "key.blockdustry.freecam",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F4,
            "key.categories.blockdustry");

    private static boolean active;
    // 刚发送附身退出请求、等待服务端确认期间，不因「附身仍 active」误关 freecam 喵
    private static boolean pendingPossessionExit;
    // 检测到 freecam 被（误）绑定到非键盘键（鼠标/扫描码）时，只提示一次喵
    private static boolean invalidBindingWarned;
    // 本 tick 内攻击/使用键是否被触发过（onInteraction 在 handleKeybinds 里先于本事件置位）喵
    private static boolean attackOrUseTriggeredThisTick;
    // 结构性闸门：本 tick 内 freecam 绑定键是否被「真实键盘按下」。
    // 只由 InputEvent.Key（键盘回调）置位，鼠标事件/任何误路由点击永远无法置位，
    // 从结构上保证「双击左键」等鼠标操作绝不可能启动 freecam 喵
    private static boolean freecamKeyPressedThisTick;
    // 客户端 tick 计数器与最近一次攻击/使用触发的 tick，用于「双击左键」窗口防御喵
    private static long clientTickCounter;
    private static long lastAttackOrUseTick = Long.MIN_VALUE;

    private FreecamHandler() {}

    // 只允许「键盘键(KEYSYM)」绑定触发 freecam：
    // 任何鼠标键/扫描码绑定一律吞掉点击、绝不切换，从结构上保证
    // 「双击左键(攻击)/右键(使用)」等鼠标操作永远不可能启动 freecam 喵
    private static boolean isKeyboardBinding() {
        return KEY_FREECAM.getKey().getType() == InputConstants.Type.KEYSYM;
    }

    public static boolean isActive() {
        return active;
    }

    public static void setActive(boolean enabled, Minecraft mc) {
        if (enabled == active) return;
        if (mc.player == null || mc.level == null) return;
        if (enabled) {
            // 附身中先退出附身（纯客户端，服务器随后确认）喵
            if (TurretPossessHandler.isPossessing()) {
                BlockPos turretPos = TurretPossessHandler.getPossessedTurret();
                if (turretPos != null) {
                    PacketDistributor.sendToServer(new TurretPossessExitPayload(turretPos));
                }
                pendingPossessionExit = true;
            }
            FreecamEntity.setEnabled(true, mc);
            active = true;
            message(mc, "灵魂出窍：开启（" + KEY_FREECAM.getTranslatedKeyMessage().getString() + " 关闭）");
        } else {
            FreecamEntity.setEnabled(false, mc);
            active = false;
            pendingPossessionExit = false;
            message(mc, "灵魂出窍：关闭");
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        clientTickCounter++;
        // 记录本 tick 攻击/使用键与 freecam 键的状态，随后立即清零，避免跨 tick 泄漏喵
        boolean attackOrUseTriggered = attackOrUseTriggeredThisTick;
        attackOrUseTriggeredThisTick = false;
        boolean freecamKeyPressed = freecamKeyPressedThisTick;
        freecamKeyPressedThisTick = false;

        if (mc.player == null || mc.level == null) {
            if (active) FreecamEntity.setEnabled(false, mc);
            active = false;
            return;
        }

        // 快捷键切换：仅键盘键绑定允许；鼠标/扫描码绑定一律吞掉点击、绝不切换喵
        if (!isKeyboardBinding()) {
            // 绑定被设为鼠标/扫描码（可能误绑到攻击键鼠标左/右键）：吞掉点击、绝不切换，
            // 从结构上保证「左键/双击左键」等鼠标操作永远不触发 freecam，并提示一次喵
            while (KEY_FREECAM.consumeClick()) {}
            if (!invalidBindingWarned && mc.player != null) {
                invalidBindingWarned = true;
                mc.player.displayClientMessage(
                        Component.literal("灵魂出窍：请绑定到键盘键（默认 F4）"), true);
            }
        } else {
            while (KEY_FREECAM.consumeClick()) {
                // 结构性闸门：本 tick 内绑定键必须被真实键盘按下（InputEvent.Key 置位）。
                // 鼠标左/右键点击永远无法置位该标志，因此「双击左键」等鼠标操作
                // 从结构上不可能切换 freecam（无论 clickCount 被什么污染）喵
                if (!freecamKeyPressed) {
                    continue;
                }
                // 双击窗口防御：关闭状态下，最近 6 tick（约 300ms）内攻击/使用刚触发过
                // （如双击左键的两击间隙）→ 不开启，直接吞掉喵
                if (!active && clientTickCounter - lastAttackOrUseTick <= 6) {
                    continue;
                }
                // 防御一：关闭状态下，本 tick 攻击/使用键刚被触发过（如双击左键），
                // 即使 freecam 键有残留点击也绝不开启——「双击攻击键」绝不影响 freecam 喵
                if (!active && attackOrUseTriggered) {
                    continue;
                }
                // 防御二：关闭状态下正按住攻击/使用键时，绝不开启（防「按着左键打怪时误开」）喵
                if (!active && (mc.options.keyAttack.isDown() || mc.options.keyUse.isDown())) {
                    continue;
                }
                setActive(!active, mc);
            }
        }
        if (!active) return;

        // 玩家死亡重生（实例被替换）→ 关闭 freecam，防止引用失效喵
        if (!FreecamEntity.isOriginalPlayerStillValid()) {
            setActive(false, mc);
            return;
        }

        // 附身激活（非自己刚发起的退出）→ 退出 freecam，保证互斥喵
        if (TurretPossessHandler.isPossessing()) {
            if (!pendingPossessionExit) {
                setActive(false, mc);
                return;
            }
        } else {
            pendingPossessionExit = false;
        }

        // 驱动相机移动喵
        FreecamEntity.movementTick();
    }

    // 屏蔽攻击/使用键：freecam 期间不敲方块、不交互实体；
    // 同时记录本 tick 攻击/使用被触发，供切换检查做防御喵
    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() || event.isUseItem()) {
            attackOrUseTriggeredThisTick = true;
            // onInteraction 在 handleKeybinds 中先于 Post 触发，此刻 clientTickCounter 还是上一 Post 的值，
            // +1 即本 tick Post 递增后的值，保证「本 tick 触发过」的差值为 0 喵
            lastAttackOrUseTick = clientTickCounter + 1;
            if (active) {
                event.setCanceled(true);
            }
        }
    }

    // 结构性闸门：只有「真实键盘按下」才允许 freecam 切换。
    // InputEvent.Key 仅由 KeyboardHandler（键盘回调）发出，鼠标事件走的是
    // InputEvent.MouseButton，永远无法走到这里——因此鼠标点击再也不可能启动 freecam 喵
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) return;
        // 自动重复（按住）不置位，避免「按住键被 clickCount 反复切换」喵
        InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());
        if (KEY_FREECAM.getKey().equals(key)) {
            freecamKeyPressedThisTick = true;
        }
    }

    private static void message(Minecraft mc, String text) {
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(text), true);
        }
    }
}
