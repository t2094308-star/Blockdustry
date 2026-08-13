package com.blockdustry.jade;

import com.blockdustry.building.BlockdustryBuildingEntity;
import com.blockdustry.building.GraphitePressBlockEntity;
import com.blockdustry.building.UnitFactoryBlockEntity;
import com.blockdustry.lib.BlockHealthApi;
import com.blockdustry.power.BlockdustryPowerNode;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 各类条服务端数据（Mindustry Bar 迁移）：血量条（纯红）/电量条（Pal.powerBar 橙）/进度条（Pal.ammo 橙），
// 每个 ViewGroup 带 id 区分类型，客户端按 id 配色（研究-Mindustry各类条.md）喵
public class ProgressServerProvider implements IServerExtensionProvider<CompoundTag> {
    public static final ProgressServerProvider INSTANCE = new ProgressServerProvider();
    public static final String ID_HP = "hp";
    public static final String ID_POWER = "power";
    public static final String ID_CRAFT = "craft";

    @Override
    public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
        if (!(accessor instanceof BlockAccessor ba)) return null;
        BlockEntity be = ba.getBlockEntity();
        // 多格建筑统一读锚点格数据（进度/库存只在锚点格跑，避免从格显示 0）喵
        BlockEntity info = be;
        // 血量统一读锚点格（整组共享血量，T10 Level 3）喵
        BlockPos hpPos = ba.getPosition();
        if (be instanceof BlockdustryBuildingEntity b && !b.isAnchor() && b.hasAnchor()) {
            BlockEntity anchor = ba.getLevel().getBlockEntity(b.getAnchor());
            if (anchor instanceof BlockdustryBuildingEntity) {
                info = anchor;
                hpPos = b.getAnchor();
            }
        }
        List<ViewGroup<CompoundTag>> groups = new ArrayList<>();
        // 血量条（BlockHealth 前置库，免疫方块 maxHp<=0 跳过）；整组共享血量读锚点格喵
        float maxHp = BlockHealthApi.getMaxHp(ba.getLevel(), hpPos);
        if (maxHp > 0f) {
            float hp = BlockHealthApi.getHp(ba.getLevel(), hpPos);
            groups.add(new ViewGroup<>(List.of(ProgressView.create(hp / maxHp)), Optional.of(ID_HP), Optional.empty()));
        }
        // 电量条（电力建筑满足率）喵
        if (be instanceof BlockdustryPowerNode pn) {
            groups.add(new ViewGroup<>(List.of(ProgressView.create(pn.getPowerStatus())), Optional.of(ID_POWER), Optional.empty()));
        }
        // 制作进度条（石墨压机 / 单位工厂）喵
        if (info instanceof GraphitePressBlockEntity g) {
            groups.add(new ViewGroup<>(List.of(ProgressView.create(g.getCraftProgress())), Optional.of(ID_CRAFT), Optional.empty()));
        }
        if (info instanceof UnitFactoryBlockEntity uf) {
            groups.add(new ViewGroup<>(List.of(ProgressView.create(uf.getCraftProgress())), Optional.of(ID_CRAFT), Optional.empty()));
        }
        return groups.isEmpty() ? null : groups;
    }

    @Override
    public ResourceLocation getUid() {
        return BlockdustryJadePlugin.UID_PROGRESS;
    }
}
