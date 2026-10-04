# 研究：Mindustry 资源栏样式与 HUD 数字不更新修复

> 目标：排查「右上角核心物资 HUD 煤数字不变」的根因并给出修复代码；研究 Mindustry 资源栏的真实样式，给出可落地的 MC 复刻建议喵。
> 源码根：MC 侧 `D:\Blockdustry\仓库\src\main\java\com\blockdustry\`，Mindustry 侧 `D:\Blockdustry\Mindustry\core\src\mindustry\` 喵。

---

## 一、HUD 数字不更新的确切根因

### 1.1 链路梳理

```
CoreHudHandler.onRenderGui（RenderGuiEvent.Post，每 20 tick）
  → PacketDistributor.sendToServer(new QueryCoreStoragePayload())   // 空载荷包喵
  → 服务端 BlockdustryNetwork.handleQueryCoreStorage
      → team = BlockdustryTeams.getTeam(player)                     // 读玩家实体 attachment 喵
      → if (team != DERELICT) { coal = TeamStorage.get(level, team).getCount(COAL) ... }
      → sendToPlayer(new CoreStorageDataPayload(coal, graphite))    // 返回喵
  → 客户端 handleCoreStorageData → CoreHudHandler.setCoreStorage(coal, graphite) 喵
  → 下次渲染 drawResource 显示 coalCount/graphiteCount 喵
```

同时物品入池链路：

```
Drill.dumpItem(COAL) → Conveyor.handleItem → 皮带前进 → Conveyor.tickAnchor 头槽
  → CoreBlockEntity.acceptItem/handleItem
      → BlockdustryTeamStorage.get(level, core.getTeam()).add(COAL, 1)   // 按核心方块队伍入池喵
```

### 1.2 逐环节排查结论（对应题面 5 个怀疑点）

1. **QueryCoreStoragePayload 无参 StreamCodec —— 正确，不是根因喵**。
   空 record 用 `StreamCodec.of((buf, payload) -> {}, buf -> new QueryCoreStoragePayload())` 是标准写法，编码写 0 字节、解码读 0 字节，`playToServer` 能正常收发喵。
2. **handleQueryCoreStorage 确实在服务端被调用，但存在「DERELICT 队伍直接返回 0」的逻辑**。
   服务端读 `BlockdustryTeams.getTeam(player)`（玩家实体 `ENTITY_TEAM` attachment），默认值是 `DERELICT`（`BlockdustryAttachments` 的 builder 默认值）喵。当玩家队伍为 DERELICT 时，`if (team != BlockdustryTeam.DERELICT)` 不成立，直接发 `(0,0)`，**即使 DERELICT 队伍共享池里已经有煤也照发 0** 喵。
3. **客户端 setCoreStorage 有被调用，但收到的是 (0,0)**，所以 `coalCount` 恒为 0，`drawResource` 显示的就是 0 喵。
4. **存入链路本身是成功的**：传送带把煤交到核心时，核心 `acceptItem` 通过白名单（煤/石墨）+ 同队可交互检查（DERELICT 与 DERELICT `canInteract` 成立，`this == other`）+ 共享池 `canAccept`，随后 `handleItem` 把煤加进「核心方块当前队伍」的共享池喵。也就是说煤确实进了池子，只是进的不是 HUD 查询的那个队伍喵。
5. **队伍不匹配正是根因**：`BlockdustryTeamHandler.onBlockPlaced` 用 `BlockdustryTeams.getTeam(placer)`（放置者玩家队伍）写入方块队伍，玩家默认 DERELICT，所以核心/传送带/钻机全是 DERELICT，煤进了 DERELICT 池；而 HUD 查询按「玩家队伍 DERELICT」走，恰好被 `team != DERELICT` 的守卫挡掉，恒返回 0 喵。

### 1.3 确切根因表述

> **根因**：新玩家实体队伍默认 `DERELICT`（没有任何登录/重生逻辑赋初队），其放置的所有建筑（含核心）都继承 DERELICT，煤炭正确存入 DERELICT 队伍共享池；但 `handleQueryCoreStorage` 里 `if (team != BlockdustryTeam.DERELICT)` 明确对 DERELICT 队伍返回 `(0,0)`，导致右上角 HUD 恒显示 0，数字永远不变喵。

次要场景：玩家先以 DERELICT 建造核心、再用 `/blockdustry team player SHARDED` 改队后，核心方块仍是 DERELICT，煤进 DERELICT 池，而 HUD 按 SHARDED 查，同样恒 0 喵（这是同根因的变体：池按「放置瞬间的方块队伍」记，查询按「玩家当前队伍」读喵）。

### 1.4 修复代码

修复分两层：**根治**（给玩家赋初始队伍，忠于 Mindustry「玩家必有队伍」语义）与**兜底**（查询侧不再把 DERELICT 当透明，保证有数据可显示）喵。

#### 修复一（根治）：登录时给玩家赋默认队伍 SHARDED

新增一个事件订阅类（挂 NeoForge 事件总线）：

```java
package com.blockdustry;

import com.blockdustry.team.BlockdustryTeam;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

// 玩家默认队伍：登录时若仍是 DERELICT，赋为初始阵营 SHARDED（忠于 Mindustry 玩家必有队伍）喵
@EventBusSubscriber(modid = Blockdustry.MODID)
public class BlockdustryPlayerTeamHandler {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (BlockdustryTeams.getTeam(player) == BlockdustryTeam.DERELICT) {
            BlockdustryTeams.setTeam(player, BlockdustryTeam.SHARDED);
        }
    }
}
```

这样新玩家放置的核心/传送带/钻机都继承 SHARDED，煤进 SHARDED 池，HUD 按 SHARDED 查询即可实时更新喵。已在世界里的旧档玩家需重进（登录事件触发）后再重建建筑，或在单机命令台执行 `/blockdustry team player SHARDED` 后重新放置核心喵。

#### 修复二（兜底）：查询侧对 DERELICT 玩家回退初始阵营

在 `BlockdustryNetwork.handleQueryCoreStorage` 中，把 DERELICT 玩家当作初始阵营查询：

```java
private static void handleQueryCoreStorage(QueryCoreStoragePayload payload, IPayloadContext ctx) {
    ctx.enqueueWork(() -> {
        ServerPlayer player = (ServerPlayer) ctx.player();
        BlockdustryTeam team = BlockdustryTeams.getTeam(player);
        // 兜底：无队玩家按初始阵营查，避免 HUD 恒 0 喵
        if (team == BlockdustryTeam.DERELICT) team = BlockdustryTeam.SHARDED;
        BlockdustryTeamStorage.Storage s = BlockdustryTeamStorage.get(player.serverLevel(), team);
        PacketDistributor.sendToPlayer(player, new CoreStorageDataPayload(
                s.getCount(Items.COAL), s.getCount(BlockdustryBlocks.GRAPHITE.get())));
    });
}
```

说明：修复二单独用只能覆盖「玩家改队但核心仍 DERELICT」之外的一半场景，最佳做法是修复一 + 修复二一起上（修复一保证全链队伍一致，修复二保证极端情况不空白）喵。

---

## 二、Mindustry 资源栏的具体样式

Mindustry 显示队伍核心物品的栏是 `HudFragment` 里的 `CoreItemsDisplay`（`ui/CoreItemsDisplay.java`），由 `HudFragment.build` 的 `coreinfo` 表格承载喵。

### 2.1 显示位置：屏幕顶中

`HudFragment.java` 行 578-595 附近：

```java
parent.fill(t -> {
    t.top();                       // 内容贴屏幕顶部喵
    ...
    t.name = "coreinfo";
    ...
    t.table(c -> {
        c.top().collapser(coreItems, () -> Core.settings.getBool("coreitems") && !mobile && shown).fillX().row();
        ...
    }).row();
    ...
});
```

- `parent.fill(t -> ...)` 让 `coreinfo` 表格铺满全屏，`t.top()` 把内容顶到屏幕**上缘**，水平方向默认**居中**，所以资源栏出现在**屏幕顶部正中**（不是右上角）喵。
- 面板名称就叫 `coreinfo`，`UI.showInfoFade` 等提示会出现在它正下方（`table.marginTop(cinfo.getPrefHeight() / Scl.scl() / 2)`），佐证它在顶部居中喵。
- 只在非移动端、且设置项 `coreitems` 开启时显示；玩家队伍没有核心时 `core == null`，计数显示 0 喵。

### 2.2 面板与布局

`CoreItemsDisplay.java` 全文核心逻辑：

```java
public class CoreItemsDisplay extends Table{
    private final ObjectSet<Item> usedItems = new ObjectSet<>();
    private CoreBuild core;

    void rebuild(){
        clear();
        if(usedItems.size > 0){
            background(Styles.black6);   // 半透明黑底，仅在有条目时出现喵
            margin(4);                   // 内边距 4 喵
        }
        update(() -> {
            core = Vars.player.team().core();
            if(content.items().contains(item -> core != null && core.items.get(item) > 0 && usedItems.add(item))){
                rebuild();               // 有新物品出现时重建，条目动态增删喵
            }
        });
        int i = 0;
        for(Item item : content.items()){
            if(usedItems.contains(item)){
                image(item.uiIcon).size(iconSmall).padRight(3);   // 图标，右侧留 3 喵
                label(() -> core == null ? "0" : UI.formatAmount(core.items.get(item)))
                     .padRight(3).minWidth(52f).left();           // 数量文字在图标右侧喵
                if(++i % 4 == 0){
                    row();              // 每行 4 组，超过 4 个自动换行喵
                }
            }
        }
    }
}
```

逐项拆解：

- **每项布局**：`图标 + 数量文字并排（同行）`，图标在左、数量在右；数量**不是**叠在图标右上角，而是图标右侧、左对齐、占最小宽度 `52f` 的单元格喵。
- **图标大小**：`iconSmall = 8*3f = 24`（`Vars.java` 行 139 定义），单位是 arc UI 虚拟像素，实际渲染还会乘 `Scl` 缩放系数；图标右侧 `padRight(3)` 喵。
- **数量文字**：默认字体（`Styles.defaultLabel`，白色、无描边），`minWidth(52f)` + `.left()` 左对齐；每格右侧再 `padRight(3)` 喵。
- **数值格式化**：用 `UI.formatAmount`（`core/UI.java` 行 791-809）——
  - 数量 < 1000：直接显示数字喵；
  - ≥ 1000：缩写成 `K`（千）/ `M`（百万）/ `B`（十亿），后缀用 `[gray]` 灰色，例如 `1.2K`、`3.5M`（中文 bundle 用 K/M/B，英文 bundle 用 k/mil/b）喵。
- **背景**：`Styles.black6 = whiteui.tint(0f, 0f, 0f, 0.6f)`（`ui/Styles.java` 行 109），即**纯黑、不透明度 60%** 的矩形贴图，面板 `margin(4)`；只有当栏里有条目（usedItems 非空）才显示背景，空时不画背景喵。
- **分组/颜色**：**不分组、不按物品上色**；物品按 `content.items()` 的顺序排列（大致是「获取顺序/原版物品表顺序」），图标本身用物品自己的 `uiIcon`（自带彩色），数量统一白色喵。
- **动态行为**：只显示「核心里数量 > 0」的物品；玩家队伍没有核心时 `core == null`，计数显示 `0` 喵。

### 2.3 与原作其它元素的关系

- 它挂在「波次信息 / 状态」之下方，属于 `coreinfo` 这一顶层竖向堆叠的第一个元素（coreItems 在上，下面依次是「核心被攻击」横幅、boss 血条、任务 hudText）喵。
- 每个条目带 tooltip：图标悬停显示物品名，数量悬停显示精确数字（不带缩写）喵。

---

## 三、现有 MC 实现对照与复刻建议

### 3.1 对照差异

| 维度 | Mindustry（原作） | 现有 MC `CoreHudHandler` | 差异点 |
|------|-------------------|--------------------------|--------|
| 位置 | 屏幕顶中，水平居中 | 右上角（`guiScaledWidth - panelW - 6, y=6`） | 位置不一致喵 |
| 每项布局 | 图标 + 数量并排，数量左对齐、`minWidth(52)` | 图标 + 数量并排，数量在 `x+20`（固定偏移） | 数量没有最小宽度与左对齐喵 |
| 每行数量 | 每行 4 项，超 4 换行 | 每行 1 项（两行共 2 项） | 行列数不同喵 |
| 图标大小 | `iconSmall = 24` 虚拟像素（乘 Scl） | MC `renderItem` 原版 16px | 尺寸与缩放语义不同喵 |
| 背景 | 黑 60% alpha（`0.6f`），仅有条目时显示，`margin(4)` | `0xAA000000`（≈66.7% alpha），固定面板 92×40 | 透明度与「仅有条目时显示」不同喵 |
| 数量格式 | ≥1000 缩写 K/M/B，后缀灰色 | `String.valueOf(count)` 纯数字 | 无缩写喵 |
| 分组/颜色 | 不分组，图标自带色，数字白色 | 图标自带色，数字白色 | 一致喵 |

### 3.2 可落地复刻建议

1. **位置改为屏幕顶中**：`x = (guiScaledWidth - panelW) / 2`，`y = 6`（保留右上也可，但贴 Mindustry 就顶中）喵。
2. **每行放 4 项，超 4 换行**：把 `coal/graphite` 用 `List<ItemStack>` + 循环绘制，`i % 4 == 0` 时换行；面板高度随行数 `rows * 22 + 8` 动态算，不再写死 92×40 喵。
3. **数量与图标并排、左对齐、最小宽**：图标 16px + 间距 3px，数量文本占最小宽 `52px`、左对齐（MC `drawString` 本身左对齐，只需固定 x 步进 = 16+3+52）喵。
4. **背景黑 60% alpha、仅当有条目时显示**：把 `0xAA000000` 换成 `0x99000000`（0x99 ≈ 60%）；若两项都 0 则整块不画背景，避免空面板喵。
5. **数量缩写**：仿 `UI.formatAmount` 写一个 `formatAmount(int)`：≥10_000 用 `n/1000 + "K"`（后缀灰色可省略），<10_000 原样输出喵。
6. **动态条目**：仅当该物品 `count > 0` 才绘制该项（原作只显示核心中存在的物品），这样煤炭为 0 时不显示煤行，贴近原作喵。

示例核心改法（示意，供 `CoreHudHandler` 参考，不直接提交）：

```java
// 顶中 + 每行 4 项 + 数量缩写 + 60% 黑底，仅当有数才画该项喵
int panelW = 300, panelH = 8 + (rows * 22), x = (mc.getWindow().getGuiScaledWidth() - panelW) / 2, y = 6;
gui.fill(x, y, x + panelW, y + panelH, 0x99000000);   // 黑 60% alpha 喵
int row = 0, col = 0;
for (ResourceEntry e : entries) {                     // entries 为 [item,count] 列表喵
    if (e.count <= 0) continue;                       // 只在有条目时绘制喵
    int ix = x + 6 + col * 74, iy = y + 6 + row * 22;
    gui.renderItem(new ItemStack(e.item), ix, iy);
    gui.drawString(mc.font, formatAmount(e.count), ix + 20, iy + 4, 0xFFFFFF);
    if (++col >= 4) { col = 0; row++; }
}
```

### 3.3 复刻优先级

- 先修任务一（队伍/数值不更新），再做样式对齐喵。
- 样式优先级建议：**位置顶中** > **每行 4 项换行** > **数量最小宽左对齐** > **60% 黑底** > **数量 K/M 缩写** > **仅绘非零项**；前两者观感差异最大，后两者属锦上添花喵。

---

## 四、参考文件索引

- MC 侧：`client/CoreHudHandler.java`、`network/BlockdustryNetwork.java`、`network/QueryCoreStoragePayload.java`、`network/CoreStorageDataPayload.java`、`team/BlockdustryTeamStorage.java`、`building/CoreBlockEntity.java`、`BlockdustryTeamHandler.java`、`BlockdustryTeams.java`、`BlockdustryAttachments.java` 喵。
- Mindustry 侧：`ui/fragments/HudFragment.java`、`ui/CoreItemsDisplay.java`、`ui/Styles.java`（black6）、`core/UI.java`（formatAmount）、`Vars.java`（iconSmall）喵。
