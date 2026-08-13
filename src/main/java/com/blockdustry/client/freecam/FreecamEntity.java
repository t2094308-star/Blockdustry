package com.blockdustry.client.freecam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

// 灵魂出窍的相机实体：extends LocalPlayer，noPhysics=true + isSpectator() 恒真，
// 塞进 Minecraft.setCameraEntity() 成为渲染相机（原版渲染管线天然以相机实体为中心）喵
public class FreecamEntity extends LocalPlayer {
    // 相机实体单例喵
    private static FreecamEntity camera;
    // 原相机实体（真实玩家），退出时恢复喵
    private static Entity originalCamera;
    private static boolean originalCameraWasPlayer;
    // 原 smartCull 值，退出时恢复喵
    private static boolean smartCullOriginal;
    // 相机运动速度向量（衰减算法）喵
    private static Vec3 motion = Vec3.ZERO;
    private static boolean sprinting;
    // 相机上次所在区块（跨区块时标脏重建）喵
    private static int lastChunkX = Integer.MIN_VALUE;
    private static int lastChunkZ = Integer.MIN_VALUE;

    // 1.21.1 LocalPlayer 构造器签名：
    // (Minecraft, ClientLevel, ClientPacketListener, StatsCounter, ClientRecipeBook, boolean wasShiftKeyDown, boolean wasSprinting)喵
    private FreecamEntity(Minecraft mc) {
        super(mc, mc.level, mc.player.connection, mc.player.getStats(), mc.player.getRecipeBook(), false, false);
        this.noPhysics = true; // 无碰撞喵
        this.input = new FreecamInput(mc.options); // 相机自身不用输入，防 NPE 喵
    }

    @Override
    public boolean isSpectator() {
        return true; // 相机按观战视角处理（自动跳过第三人称距离缩放等）喵
    }

    // 复用原相机实体（玩家）的 id，兼容部分 mod 的实体 id 校验喵
    @Override
    public int getId() {
        return originalCamera != null ? originalCamera.getId() : super.getId();
    }

    public static boolean isActive() {
        return camera != null;
    }

    // 玩家实例是否仍是开启 freecam 时的那个（死亡重生会换实例）喵
    public static boolean isOriginalPlayerStillValid() {
        return !originalCameraWasPlayer || Minecraft.getInstance().player == originalCamera;
    }

    // 设置相机朝向（yaw/pitch）喵
    public void setCameraRotations(float yaw, float pitch) {
        this.setYRot(yaw);
        this.setXRot(pitch);
    }

    // 鼠标转向：由 Entity.turn mixin 调用，把增量叠加到相机朝向喵
    public static void rotateCamera(float yawDelta, float pitchDelta) {
        if (camera == null) return;
        float yaw = camera.getYRot() + yawDelta * 0.15F;
        float pitch = Mth.clamp(camera.getXRot() + pitchDelta * 0.15F, -90F, 90F);
        camera.setCameraRotations(yaw, pitch);
    }

    // —— 开启/关闭 freecam ——
    public static void setEnabled(boolean enabled, Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        if (enabled && camera == null) {
            createCamera(mc);
        } else if (!enabled && camera != null) {
            removeCamera(mc);
        }
    }

    private static void createCamera(Minecraft mc) {
        LocalPlayer player = mc.player;
        FreecamEntity cam = new FreecamEntity(mc);
        // 站在玩家脚底 + 0.125 格，避免与身体模型 z-fighting 喵
        Vec3 pos = player.position();
        cam.setPosRaw(pos.x(), pos.y() + 0.125, pos.z());
        cam.setYRot(player.getYRot());
        cam.setXRot(player.getXRot());
        cam.setDeltaMovement(Vec3.ZERO);
        cam.updateLastTickPosition();

        camera = cam;
        originalCamera = mc.getCameraEntity();
        originalCameraWasPlayer = originalCamera == player;
        smartCullOriginal = mc.smartCull;

        mc.setCameraEntity(camera); // 关键：换成相机实体喵
        mc.smartCull = false; // 关掉实体智能剔除，避免相机远处区块被裁喵

        lastChunkX = Mth.floor(camera.getX() / 16.0);
        lastChunkZ = Mth.floor(camera.getZ() / 16.0);
        motion = Vec3.ZERO;
        sprinting = false;
    }

    private static void removeCamera(Minecraft mc) {
        if (mc.level == null || camera == null) return;
        // 玩家死亡重生后实例会变，用当前 mc.player 兜底恢复相机喵
        mc.setCameraEntity(originalCameraWasPlayer ? mc.player : originalCamera);
        mc.smartCull = smartCullOriginal;
        // 相机可能已飞离玩家，退出时重绘玩家附近的区块喵
        markChunksForRebuildOnDeactivation(Mth.floor(camera.getX() / 16.0), Mth.floor(camera.getZ() / 16.0));
        originalCamera = null;
        camera = null;
        motion = Vec3.ZERO;
    }

    // —— 每 tick 驱动相机移动（由 FreecamHandler 在 ClientTickEvent.Post 调用）——
    public static void movementTick() {
        if (camera == null) return;
        Minecraft mc = Minecraft.getInstance();
        Options options = mc.options;

        camera.updateLastTickPosition();

        // 疾跑状态：按下疾跑进入，松开前进/后退才退出喵
        if (options.keySprint.isDown()) {
            sprinting = true;
        } else if (!options.keyUp.isDown() && !options.keyDown.isDown()) {
            sprinting = false;
        }

        motion = calculateMotion(motion, 0.15, 0.4);
        double forward = sprinting ? motion.x * 3 : motion.x;
        camera.handleMotion(forward, motion.y, motion.z);

        // 跨区块时标脏重建，否则相机看到的地形不刷新（服务端不会为相机发新区块）喵
        int cx = Mth.floor(camera.getX() / 16.0);
        int cz = Mth.floor(camera.getZ() / 16.0);
        if (cx != lastChunkX || cz != lastChunkZ) {
            markChunksForRebuild(cx, cz, lastChunkX, lastChunkZ);
            lastChunkX = cx;
            lastChunkZ = cz;
        }
    }

    // 记录上一 tick 位置，供 Camera.setup 的 partialTick 插值喵
    private void updateLastTickPosition() {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    // 按键 → 运动向量（带加速斜坡/减速因子）喵
    private static Vec3 calculateMotion(Vec3 last, double ramp, double decel) {
        Minecraft mc = Minecraft.getInstance();
        Options options = mc.options;
        int forward = 0;
        int vertical = 0;
        int strafe = 0;
        if (options.keyUp.isDown()) forward += 1;
        if (options.keyDown.isDown()) forward -= 1;
        if (options.keyLeft.isDown()) strafe += 1;
        if (options.keyRight.isDown()) strafe -= 1;
        if (options.keyJump.isDown()) vertical += 1;
        if (options.keyShift.isDown()) vertical -= 1;

        double speed = (forward != 0 && strafe != 0) ? 1.2 : 1.0;
        double fwd = getRampedMotion(last.x, forward, ramp, decel) / speed;
        double v = getRampedMotion(last.y, vertical, ramp, decel);
        double strafeV = getRampedMotion(last.z, strafe, ramp, decel) / speed;
        return new Vec3(fwd, v, strafeV);
    }

    private static double getRampedMotion(double current, int input, double ramp, double decel) {
        if (input != 0) {
            if (input < 0) ramp *= -1.0;
            // 反向时立刻清零，避免急停后反向滑动喵
            if ((input < 0) != (current < 0.0)) current = 0.0;
            current = Mth.clamp(current + ramp, -1.0, 1.0);
        } else {
            current *= decel;
        }
        return current;
    }

    // 沿朝向位移相机喵
    private void handleMotion(double forward, double up, double strafe) {
        float yaw = this.getYRot();
        double scale = getMoveSpeed(); // 0.07 * 10 = 0.7 格/tick 喵
        double xFactor = Math.sin(Math.toRadians(yaw));
        double zFactor = Math.cos(Math.toRadians(yaw));
        double x = (strafe * zFactor - forward * xFactor) * scale;
        double y = up * scale;
        double z = (forward * zFactor + strafe * xFactor) * scale;
        this.setDeltaMovement(new Vec3(x, y, z));
        this.move(MoverType.SELF, this.getDeltaMovement()); // noPhysics 时不发生碰撞喵
    }

    private static double getMoveSpeed() {
        return 0.07 * 10;
    }

    // —— 区块标脏重建（服务端不会为相机发新区块，需本地重建可见区块）——
    private static boolean isClientChunkLoaded(ClientLevel level, int chunkX, int chunkZ) {
        return level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) != null;
    }

    private static void markChunkForReRender(Minecraft mc, int chunkX, int chunkZ) {
        for (int cy = 0; cy < 16; cy++) {
            mc.levelRenderer.setSectionDirty(chunkX, cy, chunkZ); // 1.21.1 公开的三参重载喵
        }
    }

    private static void markChunksForRebuild(int chunkX, int chunkZ, int lastChunkX, int lastChunkZ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.levelRenderer == null) return;
        ClientLevel level = mc.level;
        int vd = mc.options.renderDistance().get();
        if (chunkX != lastChunkX) {
            int minCX = chunkX > lastChunkX ? lastChunkX + vd : chunkX - vd;
            int maxCX = chunkX > lastChunkX ? chunkX + vd : lastChunkX - vd;
            for (int cx = minCX; cx <= maxCX; cx++) {
                for (int cz = chunkZ - vd; cz <= chunkZ + vd; cz++) {
                    if (isClientChunkLoaded(level, cx, cz)) markChunkForReRender(mc, cx, cz);
                }
            }
        }
        if (chunkZ != lastChunkZ) {
            int minCZ = chunkZ > lastChunkZ ? lastChunkZ + vd : chunkZ - vd;
            int maxCZ = chunkZ > lastChunkZ ? chunkZ + vd : lastChunkZ - vd;
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                for (int cx = chunkX - vd; cx <= chunkX + vd; cx++) {
                    if (isClientChunkLoaded(level, cx, cz)) markChunkForReRender(mc, cx, cz);
                }
            }
        }
    }

    private static void markChunksForRebuildOnDeactivation(int lastCameraChunkX, int lastCameraChunkZ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.levelRenderer == null || mc.player == null) return;
        ClientLevel level = mc.level;
        int vd = mc.options.renderDistance().get();
        int chunkX = mc.player.chunkPosition().x;
        int chunkZ = mc.player.chunkPosition().z;
        int minCameraCX = lastCameraChunkX - vd;
        int maxCameraCX = lastCameraChunkX + vd;
        int minCameraCZ = lastCameraChunkZ - vd;
        int maxCameraCZ = lastCameraChunkZ + vd;
        int minCX = chunkX - vd;
        int maxCX = chunkX + vd;
        int minCZ = chunkZ - vd;
        int maxCZ = chunkZ + vd;
        for (int cz = minCZ; cz <= maxCZ; cz++) {
            for (int cx = minCX; cx <= maxCX; cx++) {
                // 只重绘 freecam 相机范围之外的玩家附近区块喵
                if ((cx < minCameraCX || cx > maxCameraCX || cz < minCameraCZ || cz > maxCameraCZ)
                        && isClientChunkLoaded(level, cx, cz)) {
                    markChunkForReRender(mc, cx, cz);
                }
            }
        }
    }
}
