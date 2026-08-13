# T15 Mindustry 光效（Light）系统研究阶段产出喵

## 目标喵
深度研究 Mindustry 光效系统（`core/src/` 的 LightRenderer / Drawf.light / Lightning 电弧），为 Blockdustry（MC/NeoForge）的电弧闪电链光效改进提供设计依据喵。

## 一、总体结论喵
- 本版本 Mindustry **没有独立的 `Light` 实体组件或 `Lights` 类**，光效统一由客户端 `LightRenderer`（`mindustry/graphics/LightRenderer.java`）+ 静态入口 `Drawf.light(...)` 实现喵。
- 光效本质是**屏幕空间叠加层**：本帧所有光源画进一个低分辨率 FrameBuffer，再经 `Shaders.light` 光照着色器混入场景——「亮处被光色提亮、暗处保持 ambient 色」喵。
- 光源**没有内置 lifetime/衰减字段**：寿命与透明度完全由调用方控制（方块每帧重发、子弹用 `b.fout()` 随时间淡出）喵。
- 电弧（Lightning）**本身不带点光**，只播放 `Fx.lightning` 普通绘制效果（亮折线 + 白色渐变 + 端点圆点 + 快速淡出）；激光类（Laser/ContinuousLaser/Sap）才用 `Drawf.light(x,y,x2,y2,...)` 打一路**线形光**喵。
- MC 迁移建议：视觉光效用「渲染层叠加」（发光 quad / billboard 光晕 + additive），不要写真动态光源（数量少、瞬时高速会闪烁、联机同步复杂）喵。

## 二、Light 系统架构喵

### 1. LightRenderer（`mindustry/graphics/LightRenderer.java`）喵
- 客户端单例 `Vars.renderer.lights`，注释「Renders overlay lights. Client only.」喵。
- 收集：`Seq<Runnable> lights`（贴图光/线光的延迟绘制）+ `Seq<CircleLight> circles`（池化点光，防 lambda GC）。`circleIndex` 复用池化对象喵。
- 三个 add 入口喵：
  - `add(x, y, radius, Color color, float opacity)`：点光，存 `CircleLight{x, y, color(打包float bits含alpha), radius}`，用 `circle-shadow` 贴图画 `radius*2` 见方的 rect 喵。
  - `add(x, y, region, rotation, color, opacity)`：贴图形光，登记一个 `Draw.rect(region, x, y, rotation)` 的 Runnable 喵。
  - `line(x, y, x2, y2, stroke, tint, alpha)`：线形光，用 `circle-end`/`circle-mid` 贴图拼一个加宽（stroke 半宽）线段，rot 按角度算，纯手写 24 顶点喵。
- `enabled()`：`state.rules.lighting && state.rules.ambientLight.a > 0.0001f && renderer.drawLight` 喵。
- `draw()`：`buffer.resize(屏幕/scaling=4)` → additive blend（`Gl.funcAdd, Gl.max`）→ 跑所有 lights Runnable + 画 circles → 结束 → `Shaders.light.ambient.set(state.rules.ambientLight)` → `buffer.blit(Shaders.light)` → 清空收集喵。
- 关键：**低分辨率（1/4）+ additive + 屏幕空间混合**，这就是 Mindustry 光效便宜又「像素感」的原因喵。

### 2. LightShader 与 light.frag（`Shaders.java` L174 / `assets/shaders/light.frag`）喵
```glsl
gl_FragColor = clamp(vec4(mix(u_ambient.rgb, color.rgb, color.a), u_ambient.a - color.a), 0.0, 1.0);
```
- `color` = 光 buffer 里的光色（含 alpha）；`u_ambient` 默认 `(0.01,0.01,0.04,0.99)` 近黑蓝喵。
- 有光（color.a 高）→ 输出光色；无光 → 输出 ambient 暗色，即「把场景暗部压暗、亮部提成光色」喵。

### 3. 光源字段与生命周期喵
- **点光 CircleLight 字段**：`x, y, color, radius`，无 lifetime；透明度在打包 color 时由调用方给喵。
- **寿命控制范例**：子弹类在 `draw()`/`drawLight()` 里用 `b.fout()`（=1 减到 0）乘到 alpha 和 radius 上，`b.time >= b.lifetime` 就消失 → 光随子弹淡出喵。
- **建筑**：`Block.emitLight / lightRadius / lightColor / lightOpacity`，`BlockRenderer`（L835-858）在 lights pass 逐 tile 调 `entity.drawLight()`；`LightBlock` 里 `Drawf.light(x, y, lightRadius * min(smoothTime,2f), color, brightness * efficiency)`（radius 200 → lightRadius 500）喵。
- **环境光**：`Block.drawEnvironmentLight(tile)` = `Drawf.light(worldx, worldy, lightRadius, lightColor, lightColor.a)`，地板/植物用喵。
- **单位**：`UnitType.drawLight(unit)`（L1798）：`lightRadius > 0` 时 `Drawf.light(unit.x, unit.y, lightRadius, lightColor, lightOpacity)`；默认 `lightRadius=-1`（init 时 `max(60, hitSize*2.3f)`）、`lightColor=Pal.powerLight`、`lightOpacity=0.6` 喵。
- **子弹**：`BulletType.drawLight(b)`（L695）：`lightOpacity>0 && lightRadius>0` 时 `Drawf.light(b, lightRadius, lightColor, lightOpacity)`，被 `BulletComp.drawLight()` 调；`lightRadius` 默认 -1，init 时 `max(18, hitSize*5f)` 喵。

## 三、Drawf.light 用法（`mindustry/graphics/Drawf.java`）喵
| 签名 | 语义 | 底层 |
|---|---|---|
| `light(x, y, radius, color, opacity)` | 点光源 | `renderer.lights.add(...)` 圆光喵 |
| `light(Position pos, radius, color, opacity)` | 点光源（实体/子弹重载） | 同上喵 |
| `light(x, y, region, [rotation], color, opacity)` | 贴图形光 | 画染色半透明贴图喵 |
| `light(x, y, x2, y2)` | 线形光（默认橙 stroke30 alpha0.3） | `renderer.lights.line(...)` 喵 |
| `light(x, y, x2, y2, stroke, tint, alpha)` | 线形光（自定义） | 同上喵 |
| `Drawf.laser(line,start,end,x,y,x2,y2,scale,light)` | 激光绘制，`light=true` 时自动补默认线光 | 本体 + `light(x,y,x2,y2)` 喵 |

- 所有 `Drawf.light` 都是「登记本帧要画的光」，`renderer == null` 直接返回（服务端安全）喵。
- 线形光典型用法（LaserBulletType.draw L121）：
  ```java
  Tmp.v1.trns(b.rotation(), baseLen * 1.1f);
  Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.4f * b.fout(), colors[0], 0.6f);
  ```
  `ContinuousLaserBulletType`（L70，`lightStroke=40`）与 `SapBulletType`（L58，stroke `15f*b.fout`）同模式喵。
- 光效与 Effect（粒子/闪电）结合方式：`Fx.lightning.at(...)` 是普通绘制 pass 的 Effect；想加光就再单独 `Drawf.light(...)` 或 `Drawf.laser(..., light=true)`——**两套 pass 独立，Effect 不影响光 buffer**喵。

## 四、电弧（Lightning）视觉喵

### 1. 逻辑生成（`mindustry/entities/Lightning.java`）喵
- `Lightning.create(...)` 一条闪电链：`hitRange=30f`、`maxChain=8`、每步在 `rect.setSize(hitRange).setCenter(x,y)` 内 `Units.nearbyEnemies` 找最远敌人 `Geometry.findFurthest`；有目标就跳过去，无目标 `rotation += random.range(20f)`、`x += Angles.trnsx(rot, hitRange/2f)` 随机转向喵。
- 每步把当前点 `(x + Mathf.range(3f), y + Mathf.range(3f))` 加进 `Seq<Vec2> lines`，中间用 `World.raycastEach` 检测绝缘方块（命中则 snap 并 break）喵。
- 最终 `Fx.lightning.at(x, y, rotation, color, lines)` 播放效果喵。
- 结论：锯齿感来自「命中点/随机转向折线 + 每点 3 格抖动」，不是预先细分再抖动喵。

### 2. LightningBulletType（`entities/bullet/LightningBulletType.java`）喵
- `damage=1`、`speed=0`、`lifetime=1`、`hittable=false`、`status=shocked`、`lightningLength=25`、`lightningColor=Pal.lancerLaser`、`hitEffect=Fx.hitLancer` 喵。
- `init(b)`：`Lightning.create(b, lightningColor, damage, b.x, b.y, b.rotation(), lightningLength + Mathf.random(lightningLengthRand))` —— 碰撞瞬间触发电弧，之后 1 tick 消失喵。
- `LaserBulletType` 还可 `lightningSpacing` 沿途延迟放闪电（`Time.run`）喵。

### 3. Fx.lightning 视觉效果（`content/Fx.java` L188）喵
```java
lightning = new Effect(10f, 500f, e -> {
    if(!(e.data instanceof Seq)) return;
    Seq<Vec2> lines = e.data();
    stroke(3f * e.fout());                       // 线宽随 fout 从 3 减到 0
    color(e.color, Color.white, e.fin());         // 颜色从闪电色向白色渐变
    for(int i = 0; i < lines.size - 1; i++){ Lines.line(cur, next, false); }
    for(Vec2 p : lines){ Fill.circle(p.x, p.y, Lines.getStroke() / 2f); } // 折点画圆
});
```
- lifetime 10（约 1/6 秒），`fout` 快速衰减 = **短时辉光**喵。
- 视觉三层：粗折线 + 白→主题色渐变 + 折点圆点（端点更亮更粗）喵。

### 4. Fx.chainLightning（L2871）喵
- 单向 start→target（`data` 是 Position），`range=6f` 每 6 格一个 `setToRandomDirection().scl(range/2)` 抖动点，`Links.beginLine/linePoint` 画连续折线，stroke `2.5f*fout`，颜色 `Color.white → e.color`，lifetime 20 喵。
- 是「固定两点」的廉价闪电（无需中间目标），最贴近 MC 现有 ArcBeam 的两点场景喵。

### 5. 相关效果喵
- `Fx.hitLancer`（L992，lifetime 12）：白闪 + `randLenVectors` 8 条 `fout*4+1` 短辐射线，命中爆闪喵。
- `Fx.lancerLaserShoot / lancerLaserCharge / lancerLaserChargeBegin`：激光炮蓄力/发射特效，颜色 `Pal.lancerLaser` 喵。
- `Pal.lancerLaser = Color.valueOf("a9d8ff")`（浅蓝）喵。

### 6. 关键洞察喵
- **电弧不带点光**：`Lightning` 只播 Fx（普通绘制 pass），不像激光有 `Drawf.light` 线光 → 电弧「亮但不照亮周围」喵。MC 移植补光晕/点光是合理增强，不是照抄偏差喵。

## 五、光照与渲染：Mindustry 2D vs MC 3D 迁移喵

### Mindustry 2D 光效模型喵
- 屏幕空间叠加：低分辨率光 buffer + additive 混合 + light 着色器，暗部 ambient、亮部光色喵。
- 光 = 额外一层半透明叠加（圆/贴图/线），无阴影、无体积光、无真实衰减物理喵。
- 衰减靠调用方每帧传 alpha（`fout` 等），所以便宜、可大量叠加、像素风格喵。

### MC 3D 可行迁移路径喵
1. **发光 quad / billboard 光晕（推荐主力）**：在命中点/起点/每段中点画半透明 billboard 圆片（发光纹理 + 全亮 light），模拟 Drawf.light 点光与 Fx.lightning 端点圆点喵。
2. **线形光 → 沿折线铺光晕片**：每段中点放一张小光晕 billboard（alpha 低），模拟 `Drawf.light(x,y,x2,y2,stroke,tint,alpha)` 线光，成本 = 段数×1 quad，很便宜喵。
3. **光束本体**：继续用 `RenderType.entityTranslucent` 白色纹理染色 + `setLight(0xF000F0)` 全亮（现有 ArcBeamRenderer 已做）喵。
4. **真动态光源（不推荐）**：MC 动态光源数量有限、瞬时高速移动会闪烁、需手动管理 light engine 脏标记与联机同步；电弧每 tick 换位基本不可行喵。如坚持要「照亮环境」，可用短暂放置发光实体（3-5 tick）或 `level.getLightEngine()` 临时标块，但复杂度/性能不划算喵。

## 六、MC 电弧（ArcBeam）光效改进方案喵

### 现状（`ArcBeamEntity.java` / `ArcBeamRenderer.java`）喵
- 一条 `SEGS=8` 的锯齿线，`entityTranslucent` + 白色纹理染 lancerLaser（a9d8ff），半宽 0.05 画两层，端点两个白色小条；无光晕、无淡出、无命中爆闪，`VISUAL_LIFE=10` tick 硬消失喵。

### 改进设计（对照 Mindustry 三层：亮线 + 外圈光晕 + 短时辉光/爆闪）喵
1. **分层闪电主体**（对应 `Fx.lightning` 颜色渐变 + Laser 多色 stroked）喵：
   - 内层亮线：半宽 `0.08`，色近白 `(255,255,255)`，alpha 高（≈230），主视觉喵。
   - 外层光晕线：同一折线再画一次，半宽 `0.20~0.26`，色 `a9d8ff`，alpha 低（≈60~80），外圈辉光喵。
   - 可选第三层极淡宽线：半宽 `0.40`，alpha ≈25，廉价提氛围喵。
2. **渐显渐隐**（对应 `fout` 衰减）喵：
   - 客户端渲染时 `alpha *= min(1f, life/3f)` 前段淡入、`alpha *= life/3f` 尾段淡出（剩 3 tick 开始），线宽也随 `life` 线性缩窄，避免硬消失闪烁喵。
3. **命中/端点爆闪**（对应 `Fx.hitLancer` + Fx.lightning 折点圆点）喵：
   - 在 start 与 end 各画一个 billboard 光晕圆片（发光圆纹理 + additive + 全亮），半径 `finpow()*1.2` 扩散、alpha `fout` 衰减，持续 4-5 tick 喵。
   - 折线每段中点可选放小光点（alpha 极低），增强「锯齿节点」感喵。
4. **线形光迁移**（对应 `Drawf.light(x,y,x2,y2,stroke,tint,alpha)`）喵：
   - 沿闪电折线每段中点铺一张低 alpha 光晕 billboard，模拟一路照亮喵。
   - 不写真动态光源（见第五节）喵。
5. **形状改进**（对应 Mindustry 折线随机转向）喵：
   - 现有抖动只在一个固定平面 `perp`；改为用实体 id 播种的 RNG 给**每段生成独立随机法向**，使闪电更立体、不在同一平面喵。
   - 幅度 `amt` 可保持 `len*0.09`，SEGS 提到 10-12 更接近 Mindustry 折线感（代价小）喵。
6. **渲染层与性能**喵：
   - 光晕层建议用 additive 混合的 RenderType（原版 `RenderType.lightning()` 是 alpha 混合，可自定义 translucent + blendState 做 additive）喵。
   - 同屏电弧多时只给最靠近相机的少量电弧加光晕层（如 16 条），避免 fillrate 爆炸喵。
   - 保持 `setLight(0xF000F0)` 全亮防环境变暗、`setOverlay(NO_OVERLAY)` 防采样越界染黑（现有已对）喵。
7. **附加**：可加轻微相机震动（Mindustry arc 带 shake）+ 命中白闪粒子（`Fx.hitLancer` 的辐射短线），强化爆闪感喵。

## 七、占用与交接喵
- 占用文件（只读，Mindustry）：`graphics\LightRenderer.java`、`graphics\Drawf.java`、`graphics\Shaders.java`、`graphics\Pal.java`、`assets\shaders\light.frag`、`entities\Lightning.java`、`entities\bullet\{LightningBulletType,LaserBulletType,ContinuousLaserBulletType,SapBulletType,BulletType}.java`、`entities\comp\{BuildingComp,BulletComp}.java`、`world\Block.java`、`world\blocks\power\LightBlock.java`、`graphics\BlockRenderer.java`、`type\UnitType.java`、`content\Fx.java` 喵。
- 占用文件（只读，Blockdustry）：`src\main\java\com\blockdustry\entities\ArcBeamEntity.java`、`src\main\java\com\blockdustry\client\ArcBeamRenderer.java` 喵。
- 交接给：主会话（据此设计 T 系列电弧光效实现子任务；建议实现时改 `ArcBeamRenderer` + 可能给 `ArcBeamEntity` 加淡出字段）喵。
- 风险/待人工排查：MC 真动态光源的性能与闪烁问题需确认不做；additive RenderType 需自定义 blendState（NeoForge/原版均可）；billboard 光晕纹理需在资源包加发光圆贴图喵。

## 异常喵
无喵
