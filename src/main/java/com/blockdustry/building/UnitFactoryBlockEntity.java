package com.blockdustry.building;

import com.blockdustry.entities.BlockdustryEntities;
import com.blockdustry.entities.DaggerUnitEntity;
import com.blockdustry.logistics.BlockdustryItemSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

// 地面单位工厂（Mindustry groundFactory）最小移植：吃 10 硅 + 10 铅产 1 架 dagger，craftTime=900tick=15s 喵
public class UnitFactoryBlockEntity extends BlockdustryBuildingEntity {
    private static final int CRAFT_TIME = 900;       // 生产耗时 900tick=15s（忠于原作）喵
    private static final int CRAFT_SILICON = 10;     // 每架消耗硅 10 喵
    private static final int CRAFT_LEAD = 10;        // 每架消耗铅 10 喵
    private static final int SILICON_CAPACITY = 20;  // 硅库存容量 = 需求*2 喵
    private static final int LEAD_CAPACITY = 20;     // 铅库存容量 = 需求*2 喵
    private static final float WARMUP_SPEED = 0.02f; // 预热爬升/衰减速率（驱动顶面染色动画）喵

    private int siliconCount;
    private int leadCount;
    private float craftProgress;
    private float warmup;           // 0..1 预热，驱动顶面染色动画喵
    private float lastSyncedWarmup = -1f; // 同步游标，warmup 变化超过阈值才发包喵

    public UnitFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.UNIT_FACTORY_ENTITY.get(), pos, state);
    }

    // Jade 进度条用：0..1 喵
    public float getCraftProgress() {
        return craftProgress;
    }

    // 渲染/调试读 warmup 喵
    public float getWarmup() {
        return warmup;
    }

    public int getSiliconCount() {
        return siliconCount;
    }

    public int getLeadCount() {
        return leadCount;
    }

    // 接收判定：仅收硅/铅，且各自库存未满喵
    @Override
    public boolean acceptsItem(Item item) {
        if (item == BlockdustryBlocks.SILICON.get()) return siliconCount < SILICON_CAPACITY;
        if (item == BlockdustryBlocks.LEAD.get()) return leadCount < LEAD_CAPACITY;
        return false;
    }

    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (source == null || item == null) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        // 非锚点格转发给锚点格（统一库存，craft 只在锚点格跑）喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof UnitFactoryBlockEntity anchor && anchor.acceptItem(source, item);
        }
        return acceptsItem(item);
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        // 非锚点格转发给锚点格喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof UnitFactoryBlockEntity anchor && anchor.handleItem(source, item);
        }
        if (!acceptItem(source, item)) return false;
        if (item == BlockdustryBlocks.SILICON.get()) {
            siliconCount++;
        } else if (item == BlockdustryBlocks.LEAD.get()) {
            leadCount++;
        }
        setChanged();
        return true;
    }

    // 生产：材料够则推进进度；攒满 1 扣料并在门口生成 dagger；缺料停工且进度清零喵
    @Override
    protected void tickAnchor() {
        boolean producing = siliconCount >= CRAFT_SILICON && leadCount >= CRAFT_LEAD;
        // warmup 预热：可生产爬升、否则衰减（Mindustry Factory 预热近似 0.02/tick）喵
        warmup = producing ? Math.min(1f, warmup + WARMUP_SPEED) : Math.max(0f, warmup - WARMUP_SPEED);
        if (producing) {
            craftProgress += 1f / CRAFT_TIME;
            if (craftProgress >= 1f) {
                craftProgress = 0f;
                siliconCount -= CRAFT_SILICON;
                leadCount -= CRAFT_LEAD;
                spawnUnit();
                setChanged();
            }
        } else {
            // 缺料停工，进度清零（忠于原作 Factory 缺料复位）喵
            craftProgress = 0f;
        }
        // warmup 变化超过阈值时同步客户端（渲染染色需要读到 warmup）喵
        if (Math.abs(warmup - lastSyncedWarmup) > 0.02f) {
            lastSyncedWarmup = warmup;
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    // 在建筑门口（facing 前方一格）生成 dagger，并继承本建筑队伍；门口被占则就近找空闲点，避免卡墙/卡建筑喵
    private void spawnUnit() {
        if (level == null || level.isClientSide) return;
        BlockPos spawnPos = findSpawnPos();
        DaggerUnitEntity unit = new DaggerUnitEntity(BlockdustryEntities.DAGGER.get(), level);
        unit.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        unit.setBlockdustryTeam(getTeam());
        level.addFreshEntity(unit);
        // 完成生产：门口播白色烟团粒子（Mindustry UnitFactory create 特效近似）喵
        if (level instanceof ServerLevel sl) {
            BlockPos door = worldPosition.relative(getFacing());
            sl.sendParticles(ParticleTypes.CLOUD,
                    door.getX() + 0.5, door.getY() + 0.6, door.getZ() + 0.5,
                    10, 0.4, 0.3, 0.4, 0.05);
        }
    }

    // 出生点查找：门口 > 门口上方 > 门口四邻 > 工厂顶面，保证单位能落地不被卡死喵
    private BlockPos findSpawnPos() {
        BlockPos door = worldPosition.relative(getFacing());
        if (isFreeSpawn(door)) return door;
        if (isFreeSpawn(door.above())) return door.above();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos p = door.relative(dir);
            if (isFreeSpawn(p)) return p;
        }
        return worldPosition.above();
    }

    private boolean isFreeSpawn(BlockPos p) {
        return level.getBlockState(p).isAir();
    }

    // 朝向：方块带 HORIZONTAL_FACING 属性则读取，否则默认朝北喵
    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("bd_silicon", siliconCount);
        tag.putInt("bd_lead", leadCount);
        tag.putFloat("bd_progress", craftProgress);
        tag.putFloat("bd_warmup", warmup);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        siliconCount = tag.getInt("bd_silicon");
        leadCount = tag.getInt("bd_lead");
        craftProgress = tag.getFloat("bd_progress");
        warmup = tag.getFloat("bd_warmup");
    }
}
