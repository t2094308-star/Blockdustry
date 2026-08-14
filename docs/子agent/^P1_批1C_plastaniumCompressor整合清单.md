# ^P1 批1C plastaniumCompressor 整合清单喵

塑钢压缩机（Mindustry plastanium-compressor，GenericCrafter size 2）迁移完成，自包含新文件已就位。
以下为主会话（本仓库）需要合并的挂载点，全部是共享文件，勿由子 agent 改动喵。

## 一、新建文件清单（已完成，勿重复创建）
| 文件 | 说明 |
|---|---|
| `src/main/java/com/blockdustry/building/PlastaniumCompressorRegistrar.java` | 注册类：方块 + 方块物品 + BE + 石油占位物品 |
| `src/main/java/com/blockdustry/building/PlastaniumCompressorBlockEntity.java` | BE：生产逻辑 + 耗电 + plasticburn/formsmoke 粒子 |
| `src/main/java/com/blockdustry/client/PlastaniumCompressorBlockEntityRenderer.java` | DrawFade 顶面叠层渲染 |
| `src/main/resources/assets/blockdustry/blockstates/plastanium_compressor.json` | 全 corner 变体 |
| `src/main/resources/assets/blockdustry/models/block/plastanium_compressor_{nw,ne,sw,se}.json` | 2×2 四象限模型（顶面裁 quadrant UV，侧面用自生成 side） |
| `src/main/resources/assets/blockdustry/models/item/plastanium_compressor.json` | 物品模型（继承 nw） |
| `src/main/resources/assets/blockdustry/models/item/oil.json` | 石油占位物品模型 |
| `src/main/resources/assets/blockdustry/textures/block/plastanium_compressor.png` | 拷原版（64×64） |
| `src/main/resources/assets/blockdustry/textures/block/plastanium_compressor_top.png` | 拷原版 -top（64×64 白线稿） |
| `src/main/resources/assets/blockdustry/textures/block/plastanium_compressor_side.png` | 自生成金属侧面（原版无侧面美术，取样机身边缘色，见坑/机器侧面贴图.md） |
| `src/main/resources/assets/blockdustry/textures/item/oil.png` | 拷原版 liquid-oil.png（32×32） |
| `src/main/resources/assets/blockdustry/textures/research/blocks/plastanium_compressor.png` | 科技树方块图标（拷原版 base） |
| `src/main/resources/assets/blockdustry/textures/research/items/titanium.png` | 科技树钛图标（拷原版 item-titanium） |
| `src/main/resources/assets/blockdustry/textures/research/items/plastanium.png` | 科技树塑钢图标（拷原版 item-plastanium） |
| `src/main/resources/assets/blockdustry/textures/research/items/oil.png` | 科技树石油图标（拷原版 liquid-oil） |

## 二、Blockdustry.java（构造器挂载注册）
- 新增 import `com.blockdustry.building.PlastaniumCompressorRegistrar;`
- 在构造器 `com.blockdustry.distribution.BridgeRegistrar.register(modEventBus);` 之后加一行：
  `PlastaniumCompressorRegistrar.register(modEventBus);`

## 三、BlockdustryBlocks.java（创造栏 tab）
- 新增 import `com.blockdustry.building.PlastaniumCompressorRegistrar;`
- `CRAFTING_TAB`（锻造/生产）`displayItems` 内 `output.accept(GRAPHITE_PRESS_ITEM);` 后加：
  `output.accept(PlastaniumCompressorRegistrar.PLASTANIUM_COMPRESSOR_ITEM);`
- `ITEMS_TAB`（物品/材料）内加石油占位物品：
  `output.accept(PlastaniumCompressorRegistrar.OIL);`

## 四、ResearchNodes.java（科技树节点）
- 新增 import：`com.blockdustry.building.PlastaniumCompressorRegistrar;`
- 原版 parent = sporePress（未迁移）。建议挂 `graphite_press`（生产分支），在 `graphite_press` 节点后追加：
```java
ResearchNode.builder("plastanium_compressor")
        .parent("graphite_press")
        .unlockBlock(PlastaniumCompressorRegistrar.PLASTANIUM_COMPRESSOR.get())
        .buildRequirement(silicon, 80)     // Mindustry plastanium-compressor = 硅×80 铅×115 石墨×60 钛×80 喵
        .buildRequirement(lead, 115)
        .buildRequirement(graphite, 60)
        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 80)
        .build(),
```
（研究成本由 ResearchTree 按 Mindustry 公式自动计算，无需 researchCost 覆盖；钛映射见下方 BlockdustryItems 条目）

## 五、BlockdustryClient.java（BER 渲染器）
- 新增 import：`com.blockdustry.building.PlastaniumCompressorRegistrar;`、`com.blockdustry.client.PlastaniumCompressorBlockEntityRenderer;`
- `registerRenderers` 内加：
  `event.registerBlockEntityRenderer(PlastaniumCompressorRegistrar.PLASTANIUM_COMPRESSOR_ENTITY.get(), PlastaniumCompressorBlockEntityRenderer::new);`

## 六、ResearchIcons.java（科技树图标）
- `nodeTexture` switch 加：`case "plastanium_compressor" -> ResourceLocation.tryParse(base + "plastanium_compressor.png");`
- `itemTexture` switch 加：
  - `case "titanium" -> ResourceLocation.tryParse("blockdustry:textures/research/items/titanium.png");`
  - `case "plastanium" -> ResourceLocation.tryParse("blockdustry:textures/research/items/plastanium.png");`

## 七、BlockdustryBlocks.java registerBlockHealthDefaults（组血量）
- 在 `registerGroupMaxHp(ContainerRegistrar.CONTAINER.get(), 2);` 后加：
  `registerGroupMaxHp(PlastaniumCompressorRegistrar.PLASTANIUM_COMPRESSOR.get(), 2);`
- 说明：strength 7.0 → 单格血 10+10×7=80，组血 80×4=320 = Mindustry health 320 喵

## 八、BlockdustryItems.java allMaterials（石油供料，供 ItemSource 物品源菜单）
- `allMaterials()` 的 list 末尾加：`PlastaniumCompressorRegistrar.OIL.get();`
- 若嫌石油入物品源菜单太杂，可跳过此条 → 压缩机将无法测试（无油源），见下方「平台缺口」

## 九、lang（zh_cn.json / en_us.json）
zh_cn.json 追加：
```json
"block.blockdustry.plastanium_compressor": "塑钢压缩机",
"item.blockdustry.oil": "石油"
```
en_us.json 追加：
```json
"block.blockdustry.plastanium_compressor": "Plastanium Compressor",
"item.blockdustry.oil": "Oil"
```
（中文名取自原版 bundle_zh_CN：block.plastanium-compressor.name=塑钢压缩机、liquid.oil.name=石油；item.plastanium/钛已在 lang 中存在，勿重复）

## 十、数据与特效核对（忠于原版 Blocks.java L1118-1134）
- requirements: 硅 80 / 铅 115 / 石墨 60 / 钛 80，Category.crafting
- craftTime 60、outputItem plastanium 1、耗钛 2、耗油 0.25/s、耗电 3/s、liquidCapacity 60、itemCapacity 10、health 320、size 2
- drawer DrawMulti(DrawDefault, DrawFade)：-top 白线稿叠层 alpha=absin(totalProgress,3,0.6)*warmup（BER 实现，见渲染器注释的时序映射）
- craftEffect Fx.formsmoke（浅黄烟，Pal.plasticSmoke #f1e479）、updateEffect Fx.plasticburn（浅白尘点，Pal.plasticBurn #e9ead3，0.04/次）

## 十一、平台缺口（需协调者知悉）
1. **液体系统缺失**：原版 consumeLiquid oil 0.25/s 无法忠实实现（Blockdustry 无液体/石油）。已用占位物品「石油」(Registrar.OIL) 供料，速率 0.25/s 忠实原版；待液体系统接入后可将油缓冲替换为真液体。若第八条不合并，压缩机无油源会停摆。
2. **科技树 parent 原版为 sporePress**（未迁移），建议挂 graphite_press，可在研究 UI 中再调整挂点。

## 十二、待办
- [x] 独立注册类 + Block/BE/Renderer
- [x] 资源（blockstate/模型/贴图拷原版/特效贴图/科技树图标）
- [x] 整合清单
- [ ] 主会话合并以上挂载点
- [ ] 主会话 `./gradlew compileJava` 验证
