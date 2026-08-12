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

// 各类条客户端：按 ViewGroup.id 给进度条填 Mindustry 颜色（血 #ff341c / 电 #ec7b4c / 进度 #ff8947），
// color2 同色避免横纹，纯色左→右（研究-Mindustry各类条.md）喵
public class ProgressClientProvider implements IClientExtensionProvider<CompoundTag, ProgressView> {
    public static final ProgressClientProvider INSTANCE = new ProgressClientProvider();

    @Override
    public List<ClientViewGroup<ProgressView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> serverData) {
        if (serverData == null || serverData.isEmpty()) return null;
        List<ClientViewGroup<ProgressView>> result = new ArrayList<>();
        for (ViewGroup<CompoundTag> group : serverData) {
            if (group.views == null || group.views.isEmpty()) continue;
            int color = colorFor(group.id);
            List<ProgressView> views = new ArrayList<>();
            for (CompoundTag nbt : group.views) {
                ProgressView pv = ProgressView.read(nbt);
                // ProgressView.read() 内部固定 new SlimProgressStyle()（color 默认 0=透明黑，条呈黑灰），
                // style 永不为 null，之前的 if (pv.style==null) 永不执行导致配色失效；必须无条件覆盖喵
                pv.style = new SimpleProgressStyle().color(color, color);
                views.add(pv);
            }
            result.add(new ClientViewGroup<>(views));
        }
        return result.isEmpty() ? null : result;
    }

    // Mindustry 各条颜色喵
    private static int colorFor(String id) {
        if (ProgressServerProvider.ID_HP.equals(id)) return 0xFFff341c;      // 血量纯红喵
        if (ProgressServerProvider.ID_POWER.equals(id)) return 0xFFec7b4c;   // 电量 Pal.powerBar 橙喵
        return 0xFFff8947;                                                   // 进度 Pal.ammo 橙喵
    }

    @Override
    public ResourceLocation getUid() {
        return BlockdustryJadePlugin.UID_PROGRESS;
    }
}
