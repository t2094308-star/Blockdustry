package com.blockdustry.building;

import com.blockdustry.logistics.BlockdustryItemSource;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

// 物品源（Mindustry sandbox item-source）：每模组 tick 凭空产出一个物品，卸给相邻可接收的传送带/建筑喵。
// 产物默认煤，空手右键循环切换：煤 → 石墨 → 硅 → 铅 喵
public class ItemSourceBlockEntity extends BlockdustryBuildingEntity {
    private Item product = Items.COAL;

    public ItemSourceBlockEntity(BlockPos pos, BlockState state) {
        super(BlockdustryBlocks.ITEM_SOURCE_ENTITY.get(), pos, state);
    }

    public ItemSourceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // 当前产物（供右键提示/Jade 读取）喵
    public Item getProduct() {
        return product;
    }

    // 循环切换产物：煤→石墨→硅→铅→煤喵
    public void cycleProduct() {
        if (product == Items.COAL) {
            product = BlockdustryBlocks.GRAPHITE.get();
        } else if (product == BlockdustryBlocks.GRAPHITE.get()) {
            product = BlockdustryBlocks.SILICON.get();
        } else if (product == BlockdustryBlocks.SILICON.get()) {
            product = BlockdustryBlocks.LEAD.get();
        } else {
            product = Items.COAL;
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // 纯产器：不接收任何外部物品（Mindustry source 不吃料）喵
    @Override
    public boolean acceptItem(BlockdustryItemSource source, Item item) {
        return false;
    }

    @Override
    public boolean handleItem(BlockdustryItemSource source, Item item) {
        return false;
    }

    // 每模组 tick：向相邻传送带/建筑卸出一个产物；无接收方则丢弃（沙盒语义）喵
    @Override
    protected void tickAnchor() {
        dumpItem(product);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("bd_product", BuiltInRegistries.ITEM.getKey(product).toString());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("bd_product")) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(tag.getString("bd_product")));
            if (item != null && item != Items.AIR) product = item;
        }
    }
}
