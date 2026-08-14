package com.blockdustry.production;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.logistics.BlockdustryItemSource;
import com.blockdustry.power.BlockdustryPowerNode;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 硫化物混合器（Mindustry pyratite-mixer，GenericCrafter，size 2）：吃 1 煤 + 2 铅 + 2 沙产 1 硫化物，
// craftTime=80tick（原版默认），各物品独立容量 10（GenericCrafter itemCapacity 默认 10），耗电 0.20/s（Mindustry consumePower(0.20f)）喵。
// 原版无 craftEffect/updateEffect（Fx.none）、无 emitLight、drawer=DrawDefault（静态贴图）→ 本 BE 不生成粒子、
// 不驱动任何渲染动画；warmup 按 GenericCrafter 语义跟踪（approachDelta 0.019）但无视觉消费者，仅存档保真喵。
// 参考模板：KilnBlockEntity（GenericCrafter + BlockdustryPowerNode 先例）喵
public class PyratiteMixerBlockEntity extends BlockdustryBuildingEntity implements BlockdustryPowerNode {
    private static final int CRAFT_TIME = 80;           // Mindustry pyratite-mixer craftTime 默认 80f tick 喵
    private static final int CAPACITY = 10;             // GenericCrafter 默认 itemCapacity = 10（各类型独立上限）喵
    private static final int COAL_INPUT = 1;            // consumeItems(Items.coal, 1, ...) 喵
    private static final int LEAD_INPUT = 2;            // consumeItems(..., Items.lead, 2, ...) 喵
    private static final int SAND_INPUT = 2;            // consumeItems(..., Items.sand, 2) 喵
    private static final float WARMUP_SPEED = 0.019f;   // Mindustry GenericCrafter warmupSpeed 喵
    private static final float POWER_NEEDED = 0.20f;    // Mindustry pyratite-mixer consumePower(0.20f) 喵

    private int coalCount;
    private int leadCount;
    private int sandCount;
    private int pyratiteCount;
    private float craftProgress;
    private float warmup;           // 0..1 预热（原版仅驱动动画/音效，本 mod 无视觉消费者，仅存档）喵
    private float powerStatus;      // 电网满足率 0..1（由 PowerGridManager 结算注入）喵

    public PyratiteMixerBlockEntity(BlockPos pos, BlockState state) {
        super(PyratiteMixerRegistrar.PYRATITE_MIXER_ENTITY.get(), pos, state);
    }

    public PyratiteMixerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Jade/调试读取用 getter 喵
    public float getWarmup() {
        return warmup;
    }

    public float getCraftProgress() {
        return craftProgress;
    }

    public int getCoalCount() {
        return coalCount;
    }

    public int getLeadCount() {
        return leadCount;
    }

    public int getSandCount() {
        return sandCount;
    }

    public int getPyratiteCount() {
        return pyratiteCount;
    }

    // 接收判定：仅收煤/铅/沙，且各自库存未满（各类型独立上限 CAPACITY）喵
    @Override
    public boolean acceptsItem(Item item) {
        if (item == Items.COAL) return coalCount < CAPACITY;
        if (item == com.blockdustry.building.BlockdustryBlocks.LEAD.get()) return leadCount < CAPACITY;
        if (item == Items.SAND) return sandCount < CAPACITY;
        return false;
    }

    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        if (source == null || item == null) return false;
        if (!getTeam().canInteract(source.getTeam())) return false;
        // 非锚点格转发给锚点格（统一库存，craft 只在锚点格跑）喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof PyratiteMixerBlockEntity anchor && anchor.acceptItem(source, item);
        }
        return acceptsItem(item);
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        // 非锚点格转发给锚点格喵
        if (!isAnchor()) {
            BlockEntity anchorBe = level.getBlockEntity(getAnchor());
            return anchorBe instanceof PyratiteMixerBlockEntity anchor && anchor.handleItem(source, item);
        }
        if (!acceptItem(source, item)) return false;
        if (item == Items.COAL) {
            coalCount++;
        } else if (item == com.blockdustry.building.BlockdustryBlocks.LEAD.get()) {
            leadCount++;
        } else if (item == Items.SAND) {
            sandCount++;
        }
        setChanged();
        return true;
    }

    @Override
    protected void tickAnchor() {
        // 先把产出的硫化物卸给相邻传送带喵
        if (pyratiteCount > 0 && dumpItem(com.blockdustry.item.BlockdustryItems.PYRATITE.get())) {
            pyratiteCount--;
            setChanged();
        }
        // 无电即停摆：powerStatus<=0.01 视为断电（Kiln 同款）喵
        boolean hasPower = getPowerStatus() > 0.01f;
        boolean producing = hasPower && coalCount >= COAL_INPUT && leadCount >= LEAD_INPUT
                && sandCount >= SAND_INPUT && pyratiteCount < CAPACITY;
        // warmup 预热：可生产爬升、否则衰减（Mindustry approachDelta 0.019/tick）喵
        warmup = producing ? Math.min(1f, warmup + WARMUP_SPEED) : Math.max(0f, warmup - WARMUP_SPEED);
        // 缺料或硫化物满则停摆喵
        if (!producing) return;
        craftProgress += 1f / CRAFT_TIME;
        if (craftProgress >= 1f) {
            craftProgress = 0;
            coalCount -= COAL_INPUT;
            leadCount -= LEAD_INPUT;
            sandCount -= SAND_INPUT;
            pyratiteCount++;
            // 原版 Fx.none → 无 craft 粒子；craftEffect/updateEffect 均为 Fx.none，无光效喵
            setChanged();
        }
    }

    // —— BlockdustryPowerNode ——
    @Override
    public BlockPos getPos() {
        return worldPosition;
    }

    @Override
    public float powerProduction() {
        return 0f;
    }

    @Override
    public float powerNeeded() {
        // 仅锚点格计入耗电：2×2 共 4 格 BE 都会进电网结算，非锚点格返回 0，避免耗电被计 4 次喵
        return isAnchor() ? POWER_NEEDED : 0f;
    }

    @Override
    public float powerCapacity() {
        return 0f;
    }

    @Override
    public float powerStored() {
        return 0f;
    }

    @Override
    public float getPowerStatus() {
        return powerStatus;
    }

    @Override
    public void setPowerStatus(float status) {
        this.powerStatus = status;
    }

    @Override
    public List<BlockPos> getPowerLinks() {
        // 非锚点格把自己并入锚点所在电网：无论 PowerNode/电力源连到混合器哪一格，整座混合器都在同一网喵
        if (isAnchor()) return List.of();
        BlockPos anchor = getAnchor();
        return anchor != null ? List.of(anchor) : List.of();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("bd_pyratite_coal", coalCount);
        tag.putInt("bd_pyratite_lead", leadCount);
        tag.putInt("bd_pyratite_sand", sandCount);
        tag.putInt("bd_pyratite_out", pyratiteCount);
        tag.putFloat("bd_pyratite_progress", craftProgress);
        tag.putFloat("bd_pyratite_warmup", warmup);
        tag.putFloat("bd_pyratite_power", powerStatus);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        coalCount = tag.getInt("bd_pyratite_coal");
        leadCount = tag.getInt("bd_pyratite_lead");
        sandCount = tag.getInt("bd_pyratite_sand");
        pyratiteCount = tag.getInt("bd_pyratite_out");
        craftProgress = tag.getFloat("bd_pyratite_progress");
        warmup = tag.getFloat("bd_pyratite_warmup");
        powerStatus = tag.getFloat("bd_pyratite_power");
    }
}
