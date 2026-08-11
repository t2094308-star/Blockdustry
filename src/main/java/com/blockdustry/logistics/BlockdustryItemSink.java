package com.blockdustry.logistics;

import net.minecraft.world.item.Item;

// 物品接收方：拉模型预检 + 移交（Mindustry acceptItem/handleItem）喵
public interface BlockdustryItemSink {
    // 预检：能否接收该来源的这个物品（含同队/方向/容量/间距判定）喵
    boolean acceptItem(BlockdustryItemSource source, Item item);

    // 真正移交，成功返回 true；调用方须先 acceptItem 喵
    boolean handleItem(BlockdustryItemSource source, Item item);
}
