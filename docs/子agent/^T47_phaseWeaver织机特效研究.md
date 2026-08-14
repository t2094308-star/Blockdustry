# ^T47 phaseWeaver 织机特效研究喵

> 相织布编织器（Mindustry phase-weaver，GenericCrafter size 2）动画/特效逐条核对与移植方案。原版全部源码已读，无一自编喵。

## 一、原版 drawer 逐条拆解（Blocks.java L1143）喵

```
drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawWeave(), new DrawDefault());
```

1. **DrawRegion("-bottom")**：贴 `phase-weaver-bottom.png`（64×64，全不透明），静态（rotateSpeed=0）喵。
   → MC：顶面窗口地板 quad，y=1.001 喵。

2. **DrawWeave**（DrawWeave.java 全文）：
   - `Draw.rect(weave, build.x, build.y, build.totalProgress())`：贴 `phase-weaver-weave.png`（64×64，含透明），绕建筑中心旋转，**角度 = totalProgress（弧度）**喵。
     → MC：织纹 quad，绕 2×2 中心旋转 `Axis.YP.rotation(tp)`，y=1.002 喵。
   - `Draw.color(Pal.accent)` + `Draw.alpha(build.warmup())`：梭线染 **Pal.accent = #ffd37f**，透明度 = warmup 喵。
   - `Lines.lineAngleCenter(build.x + Mathf.sin(totalProgress, 6f, tilesize/3f*size), build.y, 90, size*tilesize/2f)`：
     - x 偏移 = `Mathf.sin(totalProgress, 6, amp)`，amp = `tilesize/3 * size` = 8/3×2 = 16/3 单位 = **2/3 格**喵。
     - `Mathf.sin(p,s,m) = m·sin(p·2π/s)`，s=6 → 相位 = `sin(totalProgress·2π/6)` = `sin(totalProgress·π/3)`，周期 6 时间单位喵。
     - 角度 90 = Mindustry 屏幕 +y（南）→ MC **沿 z 轴**；线长 = `size·tilesize/2` = 8 单位 = **1 格**，半长 0.5 格喵。
     - 线宽 = Lines stroke ≈ 1.5 单位 ≈ **0.19 格**，半宽 0.095 格喵。
     → MC：accent 竖线 quad（沿 z），x 扫描、alpha=warmup，y=1.003 喵。
   - 注：DrawWeave 的 x/y 偏移默认 0，织纹/梭线都居中喵。

3. **DrawDefault**：贴 `phase-weaver.png`（64×64，**中央有透明斜切矩形窗口**：x 16..47 / y 16..47，四角斜切），frame 不透明、窗口透明，盖在最上层遮住窗口外的织纹/地板喵。
   → MC：mask quad（基贴），y=1.004，frame 盖住窗口外内容、窗口露出下方喵。

## 二、Fx.smeltsmoke（craftEffect）喵

```java
smeltsmoke = new Effect(15, e -> {
    randLenVectors(e.id, 6, 4f + e.fin() * 5f, (x, y) -> {
        color(Color.white, e.color, e.fin());
        Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 2f, 45);
    });
}),
```
- 寿命 15 tick、粒子 6 个、半径 4+fin·5 单位、halfSize 0.5+fout·2 单位、旋转 45°、色 白→e.color（Effect 默认 Color.white → 全程白）喵。
- craft 完成瞬间在 `(build.x, build.y)` 触发（GenericCrafter.craft() 内 `craftEffect.at(x, y)`）喵。
- 3D 适配：相机 billboard + 轻微上升 + fout 淡出（与 SiliconSmelter 冒烟同款，见 ^T35 研究）喵。

## 三、时序映射（本 mod 20tps vs Mindustry 60tps）喵

| 原版量 | 原版速率 | 本 mod 映射 |
|---|---|---|
| totalProgress | += warmup·delta/tick → 60/s | BE 每 MC tick `totalProgress += warmup×3`（= 60/s）；渲染器从同步时刻按 `warmup×3/MC tick` 外推，保证旋转连续喵 |
| 织纹旋转角 | totalProgress 弧度 | `Axis.YP.rotation(tp)` 喵 |
| 梭线扫描相位 | sin(totalProgress·π/3) | `sin(tp·π/3)`，tp 每 MC tick +3 → π·(tp)/3 每 MC tick 增 π → 周期 2 MC tick（= 原版 10 周期/s）喵 |

## 四、渲染层次（MC 3D 适配）喵

原版是 2D 叠绘（bottom → weave → shuttle → base-on-top）。MC 用锚点格 BER 在 2×2 顶面按 y 从低到高叠 4 层 quad：
1. y=1.001 底贴 `phase_weaver_bottom.png`（窗口地板，全不透明）
2. y=1.002 织纹 `phase_weaver_weave.png`（绕 2×2 中心旋转）
3. y=1.003 梭线（white 贴图染 #ffd37f，alpha=warmup）
4. y=1.004 mask 基贴 `phase_weaver.png`（frame 不透明遮边缘、窗口透明露内容）

模型 JSON 顶面即 `phase_weaver.png`（带窗口），与 mask 同一贴图、frame 双重绘制无视觉差异；窗口内由 BER 4 层补齐喵。
全部 entityTranslucent 单一系（坑/BER渲染.md §4 防交错崩）、顶点全亮 + NO_OVERLAY（§1/§2）喵。

## 五、数据核对（Blocks.java L1136-1152）喵

- requirements：硅×130 铅×120 钍×75，Category.crafting
- craftTime 120、outputItem phaseFabric 1、size 2、hasPower true、itemCapacity 30
- consumeItems：钍×4 + 沙×10；consumePower 5f
- envEnabled |= Env.space（本 mod 无空间维度概念，忽略）
- ambientSound loopTech 0.02（本 mod 无环境音系统，忽略）
- health = size²×40 = 160 → registrar strength 3（单格 40，组血 160）喵
- 科技树（SerpuloTechTree.java L140）：parent = **plastaniumCompressor**，需 SectorComplete(impact0078) 喵

## 六、已发现平台缺口喵

- **织纹旋转 60rad/s 视觉极快**：这是原版真实表现（totalProgress 每秒 +60），非 bug；织纹贴图周期对称，观感为快速旋转喵。
- **窗口透明区**：模型顶面用 solid 渲染类型时透明像素不 alpha-test，BER 的 4 层在 y>1.001 处画在模型之上，视觉正确；若模型 solid 写出深度，BER 层在前（更近）仍通过深度测试喵。
- 环境音/空间属性本 mod 无对应系统，忽略喵。
