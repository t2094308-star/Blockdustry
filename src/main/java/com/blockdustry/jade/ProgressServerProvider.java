package com.blockdustry.jade;

import com.blockdustry.building.GraphitePressBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

// 制作进度条服务端数据（Mindustry 式进度条）：石墨压机 craft 进度打包成 ProgressView NBT 喵
public class ProgressServerProvider implements IServerExtensionProvider<CompoundTag> {
    public static final ProgressServerProvider INSTANCE = new ProgressServerProvider();

    @Override
    public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
        if (!(accessor instanceof BlockAccessor blockAccessor)) return null;
        BlockEntity be = blockAccessor.getBlockEntity();
        if (be instanceof GraphitePressBlockEntity g) {
            return List.of(new ViewGroup<>(List.of(ProgressView.create(g.getCraftProgress()))));
        }
        return null;
    }

    @Override
    public ResourceLocation getUid() {
        return BlockdustryJadePlugin.UID_PROGRESS;
    }
}
