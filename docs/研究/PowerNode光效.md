# 研究：Mindustry PowerNode 激光/光效渲染

> 目标：为 Minecraft 模组「方块工业」移植 PowerNode 连接光效喵。
> 源码：`D:\Blockdustry\Mindustry`，本文基于 v7/v8 主分支代码喵。

## 1. 激光画什么

连接时在两节点之间画一条**发光的线状光束**，由三段拼成喵：

- **中段**：`laser` 贴图（`sprites/effects/laser.png`，4x48 像素的细长条），被拉伸铺满连接线段喵。
- **两端帽**：`laser-end` 贴图（`sprites/effects/laser-end.png`，72x72 菱形），各画一个，旋转朝向连接方向喵。
- **辉光**：另叠加一层动态光照 `renderer.lights.line(...)`（见第 6 节），是半透明橙色亮带，不是粒子喵。

所以屏幕上看到的是：一根细亮线 + 两端菱形发光点喵。

关键代码（`PowerNodeBuild.draw` → `drawLaser` → `Drawf.laser`）：

```java
// PowerNode.java:191  drawLaser()
float angle1 = Angles.angle(x1, y1, x2, y2),
    vx = Mathf.cosDeg(angle1), vy = Mathf.sinDeg(angle1),
    len1 = size1 * tilesize / 2f - 1.5f, len2 = size2 * tilesize / 2f - 1.5f;
Drawf.laser(laser, laserEnd, laserEnd, x1 + vx*len1, y1 + vy*len1, x2 - vx*len2, y2 - vy*len2, laserScale, light, useLod);
```

```java
// Drawf.java:521  laser()
float scl = 8f * scale * Draw.scl, rot = Mathf.angle(x2 - x, y2 - y);
float vx = Mathf.cosDeg(rot) * scl, vy = Mathf.sinDeg(rot) * scl;
...
Draw.rect(start, x, y, start.width * scale * start.scl(), start.height * scale * start.scl(), rot + 180); // 起始端帽
Draw.rect(end,   x2, y2, end.width   * scale * end.scl(),   end.height * scale * end.scl(),   rot);        // 结束端帽
Lines.stroke(12f * scale);
Lines.line(line, x + vx, y + vy, x2 - vx, y2 - vy, false); // 中段光束
```

## 2. 激光颜色

两色字段（`PowerNode.java:39-40`）：

- `laserColor1 = Color.white`（`#ffffff` 纯白）喵。
- `laserColor2 = Pal.powerLight = Color.valueOf("fbd367")`（`#fbd367` 暖黄/橙黄）喵。

混合公式在 `setupColor`（`PowerNode.java:183-185`）：

```java
Draw.color(Tmp.c1.set(laserColor1).lerp(laserColor2, (1f - satisfaction) * 0.86f + Mathf.absin(3f, 0.1f))
    .a(Renderer.laserOpacity * (useLod ? Lod.alpha2 : 1f)));
```

- `satisfaction = power.graph.getSatisfaction()`（`PowerGraph.java:86-93`）= `clamp(produced / needed)`：无产电→0，无需求→1，否则产量/需求喵。
- lerp 因子 `t = (1 - satisfaction) * 0.86 + absin(3f, 0.1)` 喵。
- **高电**（satisfaction≈1）：`t≈0.0~0.1`，颜色几乎纯白（带极轻微黄 shimmer）喵。
- **低电/断电**（satisfaction≈0）：`t≈0.86~0.96`，颜色几乎就是 `#fbd367` 暖黄喵。
- **整条光束用一个统一颜色**（一次 `Draw.color`），不是沿光束渐变，两端帽与中段同色喵。

## 3. 激光几何

- 起点 = 源节点中心 + 单位方向向量 ×（源块半宽 − 1.5）；终点 = 目标中心 − 方向 ×（目标块半宽 − 1.5）喵。
  - Mindustry 格 `tilesize=8`，1x1 方块半宽 = 4，所以 1x1 节点光束从中心外 `4−1.5=2.5` 世界单位处开始喵。
  - 换算成 MC 格：除以 8，即缩进 `size/2 − 1.5/8 = size/2 − 0.1875` 格喵。
- 方向角：`angle = Angles.angle(x1, y1, x2, y2)`（从源到目标的方位角，度）；单位向量 `(cosDeg, sinDeg)` 喵。
- 中段再缩短：两端各缩 `8 × laserScale`（laserScale=0.25 → 2 世界单位，即 MC 0.25 格）喵。
- 线宽：普通线条 stroke 为 `12 × scale = 3` 世界单位；但中段用贴图拉伸绘制，实际厚度 ≈ laser 贴图宽度 `4 × Draw.scl`（约 4 世界单位，很细）喵。
- 端帽尺寸：`72 × scale = 18` 世界单位（MC 2.25 格）的菱形，比中段粗得多，视觉上是"亮点"喵。
- 长度：两节点中心距减去两端的块内缩进与中段缩短喵。

## 4. 激光动画

**核心结论：激光本身不做脉动/闪烁**喵。`Drawf.laser` 内部完全没有 `Time.time` 参与，是静止的亮线喵。

唯一随时间变化的地方是 `setupColor` 的颜色微晃喵：

- `Mathf.absin(3f, 0.1f) = |sin(Time.time / 3)| × 0.1`，范围 [0, 0.1]，周期 ≈ `3 × 2π ≈ 18.85` 秒喵。
- 它只让颜色在 lerp 因子上多 0~0.1 的缓慢摆动，非常轻微、几乎不可察觉喵。

透明度：

- `Renderer.laserOpacity` 默认 `0.5`（设置项「激光透明度」0~100%，`Renderer.java:29,166`），所以默认半透明喵。
- 再乘 `Lod.alpha2`（缩放 LOD 淡出：远处 zoom 拉远时透明度下降）喵。
- 小激光（端帽缩放后 <10px）用 `Lod.alpha1`，否则 `alpha2`；两者在 `Lod.java` 里由相机 zoom 计算喵。

## 5. 激光层次

- 画前 `Draw.z(powerLayer)`，`powerLayer = Layer.power = 70`（`Layer.java:57`）喵。
- 对比各层：`block=30`、`blockAdditive=31`、`blockOver=35`、`turret=50`、`groundUnit=60`、`power=70`、`darkness=80`、`bullet=100`、`effect=110` 喵。
- **所以激光画在方块之上**（在地面单位、炮塔之上），但低于黑暗遮罩、子弹、特效喵。
- PowerNode 设 `drawCached=true`（基础贴图走缓存），激光在动态 pass 的 `draw()` 里画（`BlockRenderer.java:583` 判断 `block.drawDynamic`），因此能盖在建筑上喵。

## 6. 粒子

- 连接/传输本身**不发射任何粒子**（没有 Fx/effect 调用）喵。
- 唯一的附加效果是 `Drawf.laser` 末尾的 `light(x, y, x2, y2)`：

```java
// Drawf.java:320-328
renderer.lights.line(x, y, x2, y2, 30, Color.orange, 0.3f);
```

- 这是一条橙色、stroke=30、alpha=0.3 的**柔光带**（动态光照系统，属于 glow 而非粒子），让光束有辉光感喵。

## 7. autolink

放置时 `placed()`（`PowerNode.java:399-410`）：非客户端且当前无 links 才自动连接，用 `getPotentialLinks` 找候选并 `configureAny` 喵。

候选判定 `valid` Boolf（`PowerNode.java:224-233`）：

- `other != null`、不是自己、`other.block.connectedPower`、`other.power != null` 喵。
- 目标必须是**输出电 / 耗电 / 本身是 PowerNode** 的方块喵。
- **圆形范围**：`overlaps(src, other, laserRange*tilesize)`，即以源中心为圆心、`laserRange × 8` 为半径的圆，与目标 hitbox 相交喵（`laserRange=6` → 48 世界单位 = 6 格）喵。
- **同队**：`other.team == team` 喵。
- **去重**：`!graphs.contains(other.power.graph)`，防止连进同一电网（相邻格的电网也预先加入 graphs）喵。
- **绝缘**：`PowerNode.insulated` 用 `world.raycast` 检查连线上有 `isInsulated` 的建筑则拒绝喵。
- **maxNodes**：目标若是节点，其 `power.links.size < maxNodes` 且源自身也受 `maxNodes=3` 限制（`returnInt++ < maxNodes`）喵。
- **排除紧邻**：`Edges.getEdges(size)` 相邻格上的建筑不连喵。

排序：优先连非节点的 PowerNode？实际是「本身是 PowerNode 的优先、同距离则近者优先」（`tempBuilds.sort`，PowerNode 类排前，再按 `dst2`）喵。

连接后**立即画激光**：`draw()` 每帧遍历 `power.links` 渲染，配置生效下一帧即出现喵。双节点连接只画一次（`link.block instanceof PowerNode && link.id >= id` 时跳过，从 id 小的一端画）喵。

## 8. 给 MC（NeoForge 1.21.1 BER）的移植建议

- **中段用 quads，不要用 `RenderType.LINES`**：MC 的 `LINES` 渲染线宽固定 1px、不能调粗细，画不出 Mindustry 那种亮线喵。中段按"两个端点 ± 垂直偏移"造 4 个顶点、两个三角形，即可自定义宽度喵。
- **端帽用一个旋转 45° 的正方形 quad**（半透明、尺寸约中段宽度 3~5 倍），模拟 laser-end 菱形亮点喵。
- **辉光**：加第二层更宽、更低 alpha 的 quad（additive 混合），或直接省略（BER 里做多层 glow 较麻烦）喵。
- **RenderType 选择**：用 `RenderType.translucent()`（SRC_ALPHA / ONE_MINUS_SRC_ALPHA）即可；想要 Mindustry 的"亮感"可用 `RenderSystem.setShaderColor` 配合 additive（ONE / ONE）再画一遍喵。半透明物体要 `DEPTH_WRITE off` 且排在方块之后画，避免被自己挡住喵。
- **动画**：忠实移植就是**不脉动**，只有颜色微晃喵。呼吸感公式（Mindustry 的 absin 等价实现，`gameTime` 单位 tick，1s=20tick）：
  `t = (1 - satisfaction) * 0.86 + Math.abs(Math.sin(gameTimeTicks / (3.0 * 20))) * 0.1` 喵。
  若想要更明显的脉动可以自己加 `0.5 + 0.5*sin(...)`，但这偏离原版观感喵。
- **颜色随 powerStatus 渐变**：BER 每帧从 TE 取该电网的 satisfaction（`clamp(produced/needed)`，mod 侧在 tick 里维护），用 `white.lerp(#fbd367, t)` 得到最终色，写入顶点颜色，alpha 用 0.5（对应 Mindustry 默认 laserOpacity）喵。
- **几何换算**：把 Mindustry 世界单位 ÷ 8 得到 MC 格数喵。端点缩进 = `(本块边长/2 − 1.5/8)` 格；中段两端再缩 `0.25` 格；角度与中段缩短公式原样照搬喵。
- **z 层**：BER 默认画在方块之上；给 PoseStack `translate(0, 0.01, 0)` 微抬即可避免与方块的 Z-fighting 喵。
- **连接去重**：TE 存链接数组（对方的 blockPos），渲染时只画一次（按 id/顺序比较）喵。

## 附：关键常量速查

| 名称 | 值 | 来源 |
|---|---|---|
| `laserRange` | 6（格，×8=48 世界单位） | `PowerNode.java:33` |
| `maxNodes` | 3 | `PowerNode.java:34` |
| `laserScale` | 0.25 | `PowerNode.java:36` |
| `laserColor1` | `#ffffff` | `PowerNode.java:39` |
| `laserColor2` | `#fbd367`（Pal.powerLight） | `Pal.java:94` |
| `Renderer.laserOpacity` | 0.5（默认） | `Renderer.java:29` |
| `Layer.power` | 70 | `Layer.java:57` |
| `laser.png` | 4 x 48 px | 素材 |
| `laser-end.png` | 72 x 72 px | 素材 |

## 源文件

- `D:\Blockdustry\Mindustry\core\src\mindustry\world\blocks\power\PowerNode.java`
- `D:\Blockdustry\Mindustry\core\src\mindustry\world\blocks\power\PowerGraph.java`
- `D:\Blockdustry\Mindustry\core\src\mindustry\graphics\Drawf.java`
- `D:\Blockdustry\Mindustry\core\src\mindustry\graphics\Pal.java`
- `D:\Blockdustry\Mindustry\core\src\mindustry\core\Renderer.java`
- `D:\Blockdustry\Mindustry\core\src\mindustry\graphics\Layer.java`
- `D:\Blockdustry\Mindustry\core\src\mindustry\graphics\Lod.java`
- `D:\Blockdustry\Mindustry\core\src\mindustry\graphics\BlockRenderer.java`
