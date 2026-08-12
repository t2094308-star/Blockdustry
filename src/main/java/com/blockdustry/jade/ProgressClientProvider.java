package com.blockdustry.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ProgressView;
import snownee.jade.api.view.ViewGroup;
import snownee.jade.impl.ui.SimpleProgressStyle;

import java.util.ArrayList;
import java.util.List;

// 制作进度条客户端：服务端同步的 ProgressView NBT 转成可渲染的进度条；
// style 为空时用 SimpleProgressStyle 填色（Mindustry accent 橙，详见 docs/研究-Mindustry进度条样式.md）喵
public class ProgressClientProvider implements IClientExtensionProvider<CompoundTag, ProgressView> {
    public static final ProgressClientProvider INSTANCE = new ProgressClientProvider();

    @Override
    public List<ClientViewGroup<ProgressView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> serverData) {
        if (serverData == null || serverData.isEmpty()) return null;
        List<ClientViewGroup<ProgressView>> result = new ArrayList<>();
        for (ViewGroup<CompoundTag> group : serverData) {
            if (group.views == null || group.views.isEmpty()) continue;
            List<ProgressView> views = new ArrayList<>();
            for (CompoundTag nbt : group.views) {
                ProgressView pv = ProgressView.read(nbt);
                if (pv.style == null) {
                    // Mindustry Pal.accent #ffd37f，进度条填充色喵
                    pv.style = new SimpleProgressStyle().color(0xFFffd37f);
                }
                views.add(pv);
            }
            result.add(new ClientViewGroup<>(views));
        }
        return result.isEmpty() ? null : result;
    }

    @Override
    public ResourceLocation getUid() {
        return BlockdustryJadePlugin.UID_PROGRESS;
    }
}
