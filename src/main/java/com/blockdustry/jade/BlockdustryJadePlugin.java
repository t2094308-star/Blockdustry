package com.blockdustry.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

// Jade（玉）联动插件：Jade 扫描本 mod jar 发现 @WailaPlugin 自动加载，显示方块血量（数据来自前置库 BlockHealth）喵
@WailaPlugin
public class BlockdustryJadePlugin implements IWailaPlugin {
    // provider 唯一 id（也是 config 开关 key）喵
    public static final ResourceLocation UID_BLOCK_HP =
            ResourceLocation.fromNamespaceAndPath("blockdustry", "block_hp");
    public static final ResourceLocation UID_BUILDING_INFO =
            ResourceLocation.fromNamespaceAndPath("blockdustry", "building_info");
    public static final ResourceLocation UID_PROGRESS =
            ResourceLocation.fromNamespaceAndPath("blockdustry", "progress");

    @Override
    public void register(IWailaCommonRegistration registration) {
        // 对任意方块同步血量（免疫方块在 provider 内过滤）；只覆盖方块工业建筑可改 BlockdustryBuildingBlock.class 喵
        registration.registerBlockDataProvider(BlockHpServerDataProvider.INSTANCE, Block.class);
        registration.registerBlockDataProvider(BuildingInfoServerDataProvider.INSTANCE, Block.class);
        registration.registerProgress(ProgressServerProvider.INSTANCE, Block.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BlockHpComponentProvider.INSTANCE, Block.class);
        registration.registerBlockComponent(BuildingInfoComponentProvider.INSTANCE, Block.class);
        registration.registerProgressClient(ProgressClientProvider.INSTANCE);
    }
}
