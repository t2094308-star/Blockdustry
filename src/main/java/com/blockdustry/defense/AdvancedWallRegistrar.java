package com.blockdustry.defense;

import com.blockdustry.Blockdustry;
import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.building.BlockdustryBuildingItem;
import com.blockdustry.entities.BlockdustryBulletEntity;
import com.blockdustry.entities.WallLightningEntity;
import com.blockdustry.lib.BlockHealthApi;
import com.blockdustry.lib.BlockHealthDamageEvent;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// 高级墙体独立注册类（批2A）：plastanium-wall(+large)、thorium-wall(+large)、surge-wall(+large) 喵。
// 数据忠于 Mindustry Blocks.java L1729-1783（wallHealthMultiplier=4 已计入）：
//   plastaniumWall   health=125×4=500  需求塑钢×5+钢化玻璃×2  insulated/absorbLasers 喵
//   thoriumWall      health=200×4=800  需求钍×6 喵
//   surgeWall        health=230×4=920  需求巨浪合金×6  lightningChance=0.05（受击放电弧）喵
//   large 版 = 单格 health × 4（2×2 组血 ×4），size 2 喵
// 装甲：三墙 armor 均 0（原版 Building 默认 armor=0，Wall 未重设，仅 health 递增）喵。
// 血量映射：BlockHealthApi perCell = 10 + 10×strength → plastanium 500(strength49) / thorium 800(79) / surge 920(91) 喵。
// 方块类复用批1F T43 的 defense.WallBlock（通用静态墙基类）；BE 独立 AdvancedWallBlockEntity + id「advanced_wall」，
// 避免与批1F 已占用的「wall」BE id 重复（主会话整合时可统一合并所有墙的 BE 类型）喵。
// surge 受击放电：SurgeHandler 监听 BlockHealthDamageEvent（PROJECTILE 类型）按 5% 概率放一道 Pal.surge 闪电喵。
public final class AdvancedWallRegistrar {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Blockdustry.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Blockdustry.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Blockdustry.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Blockdustry.MODID);

    // surge-wall 原版受击放电数据（Wall.java）喵
    public static final float SURGE_LIGHTNING_CHANCE = 0.05f;
    public static final float SURGE_LIGHTNING_DAMAGE = 20f;
    public static final int SURGE_LIGHTNING_LENGTH = 17; // 原版 lightningLength=17 段，MC 放电近似 3 格（见 SurgeHandler）喵

    // —— 方块（普通 1×1 / 大型 2×2，strength 决定单格血；方块类复用 defense.WallBlock）——
    public static final DeferredBlock<Block> PLASTANIUM_WALL =
            BLOCKS.register("plastanium_wall", () -> wallBlock(1, 49f));
    public static final DeferredBlock<Block> PLASTANIUM_WALL_LARGE =
            BLOCKS.register("plastanium_wall_large", () -> wallBlock(2, 49f));
    public static final DeferredBlock<Block> THORIUM_WALL =
            BLOCKS.register("thorium_wall", () -> wallBlock(1, 79f));
    public static final DeferredBlock<Block> THORIUM_WALL_LARGE =
            BLOCKS.register("thorium_wall_large", () -> wallBlock(2, 79f));
    public static final DeferredBlock<Block> SURGE_WALL =
            BLOCKS.register("surge_wall", () -> wallBlock(1, 91f));
    public static final DeferredBlock<Block> SURGE_WALL_LARGE =
            BLOCKS.register("surge_wall_large", () -> wallBlock(2, 91f));

    // —— 方块物品（1×1 墙 size1，2×2 墙 size2）——
    public static final DeferredItem<BlockdustryBuildingItem> PLASTANIUM_WALL_ITEM =
            ITEMS.register("plastanium_wall", () -> new BlockdustryBuildingItem(PLASTANIUM_WALL.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> PLASTANIUM_WALL_LARGE_ITEM =
            ITEMS.register("plastanium_wall_large", () -> new BlockdustryBuildingItem(PLASTANIUM_WALL_LARGE.get(), new Item.Properties(), 2));
    public static final DeferredItem<BlockdustryBuildingItem> THORIUM_WALL_ITEM =
            ITEMS.register("thorium_wall", () -> new BlockdustryBuildingItem(THORIUM_WALL.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> THORIUM_WALL_LARGE_ITEM =
            ITEMS.register("thorium_wall_large", () -> new BlockdustryBuildingItem(THORIUM_WALL_LARGE.get(), new Item.Properties(), 2));
    public static final DeferredItem<BlockdustryBuildingItem> SURGE_WALL_ITEM =
            ITEMS.register("surge_wall", () -> new BlockdustryBuildingItem(SURGE_WALL.get(), new Item.Properties(), 1));
    public static final DeferredItem<BlockdustryBuildingItem> SURGE_WALL_LARGE_ITEM =
            ITEMS.register("surge_wall_large", () -> new BlockdustryBuildingItem(SURGE_WALL_LARGE.get(), new Item.Properties(), 2));

    // —— 方块实体类型（6 墙共用，type 由 blockstate 区分；id「advanced_wall」避免与批1F「wall」冲突）——
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AdvancedWallBlockEntity>> WALL_ENTITY =
            BLOCK_ENTITY_TYPES.register("advanced_wall",
                    () -> BlockEntityType.Builder.of(AdvancedWallBlockEntity::new,
                            PLASTANIUM_WALL.get(), PLASTANIUM_WALL_LARGE.get(),
                            THORIUM_WALL.get(), THORIUM_WALL_LARGE.get(),
                            SURGE_WALL.get(), SURGE_WALL_LARGE.get()).build(null));

    // —— surge 受击放电闪电实体（渲染注册见整合清单 BlockdustryClient）——
    public static final DeferredHolder<EntityType<?>, EntityType<WallLightningEntity>> WALL_LIGHTNING =
            ENTITY_TYPES.register("wall_lightning",
                    () -> EntityType.Builder.<WallLightningEntity>of(WallLightningEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .updateInterval(1)
                            .build("wall_lightning"));

    // 工厂方法：延迟取实体类型，避免注册期 unbound 喵
    private static WallBlock wallBlock(int size, float strength) {
        return new WallBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(strength),
                () -> WALL_ENTITY.get(),
                size);
    }

    // 注册到 mod 事件总线（主会话在 Blockdustry 构造器调用）+ 受击放电事件监听喵
    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        ENTITY_TYPES.register(bus);
        NeoForge.EVENT_BUS.register(SurgeHandler.class);
    }

    // surge-wall 受击放电（原版 Wall.WallBuild.collision）喵：
    // 仅当方块被 PROJECTILE 伤害命中且目标是 surge 墙时，按 5% 概率从墙中心向子弹来向放一道黄白闪电喵。
    public static class SurgeHandler {
        @SubscribeEvent
        public static void onBlockDamage(BlockHealthDamageEvent event) {
            if (event.getType() != BlockHealthApi.DamageType.PROJECTILE) return;
            ServerLevel level = event.getLevel();
            BlockPos pos = event.getPos();
            BlockState state = level.getBlockState(pos);
            boolean large = state.is(SURGE_WALL_LARGE.get());
            if (!state.is(SURGE_WALL.get()) && !large) return;
            if (level.random.nextFloat() >= SURGE_LIGHTNING_CHANCE) return;

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof BlockdustryBuildingEntity building)) return;
            BlockdustryTeam team = building.getTeam();
            BlockPos anchor = building.hasAnchor() ? building.getAnchor() : pos;
            int size = building.getSize();
            // 墙整体中心（大型墙取整组中心）喵
            Vec3 center = new Vec3(
                    anchor.getX() + (size - 1) / 2.0 + 0.5,
                    pos.getY() + 0.5,
                    anchor.getZ() + (size - 1) / 2.0 + 0.5);
            // 方向：子弹来向（速度反向），无子弹信息则随机水平方向喵
            Vec3 dir;
            if (event.getSource() instanceof BlockdustryBulletEntity bullet) {
                Vec3 vel = bullet.getDeltaMovement();
                if (vel.horizontalDistanceSqr() > 1e-6) {
                    dir = new Vec3(-vel.x, 0, -vel.z).normalize();
                } else {
                    dir = randomHorizontal(level);
                }
            } else {
                dir = randomHorizontal(level);
            }
            // 原版 lightningLength=17 段较长，MC 近似 3 格短闪电链（忠于「受击反向放电」语义）喵
            Vec3 to = center.add(dir.scale(3.0));
            WallLightningEntity bolt = new WallLightningEntity(level, center, to, SURGE_LIGHTNING_DAMAGE);
            bolt.setOwnerTeam(team);
            level.addFreshEntity(bolt);
        }

        private static Vec3 randomHorizontal(ServerLevel level) {
            float a = level.random.nextFloat() * (float) Math.PI * 2f;
            return new Vec3(Math.cos(a), 0, Math.sin(a));
        }
    }

    private AdvancedWallRegistrar() {}
}
