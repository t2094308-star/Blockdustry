# 研究:PowerNode 激光颜色逻辑(Mindustry v8 / Build 159.7)喵

本文基于 `D:\Blockdustry\Mindustry` 仓库当前代码(commit `20da6a38ab`, tag `v159.7-49`)研究喵。
只读源码,未改动任何游戏文件喵。

## 1. LaserColor 类现状:本版本已不存在喵

搜索 `LaserColor` 类(区分大小写、含 git 全历史)均找不到喵。
v8 里激光颜色逻辑已内联到 `PowerNode.setupColor()` 和 `BeamNode.draw()` 中喵。
你之前记得的 `core/src/mindustry/graphics/LaserColor.java` 是 v7 的旧结构,已重构喵。
颜色数据现在挂在方块字段上,由每个方块自己声明喵。

关键字段(在 `core/src/mindustry/world/blocks/power/PowerNode.java` 第 31-40 行):

```java
public @Load(value = "@-laser", fallback = "laser") TextureRegion laser;
public @Load(value = "@-laser-end", fallback = "laser-end") TextureRegion laserEnd;
public float laserRange = 6;
public int maxNodes = 3;
public boolean autolink = true, drawRange = true, sameBlockConnection = false;
public float laserScale = 0.25f;
public boolean useLod = true;
public float powerLayer = Layer.power;
public Color laserColor1 = Color.white;
public Color laserColor2 = Pal.powerLight;
```

激光颜色 = `laserColor1` 与 `laserColor2` 两个端色按满意度 lerp 喵。
默认 `laserColor1 = Color.white`(#ffffff),`laserColor2 = Pal.powerLight`(#fbd367)喵。
`BeamNode` 变体:`laserColor1 = Color.white`, `laserColor2 = Color.valueOf("ffd9c2")`(暖肤橙色)喵。
`Blocks.java` 里 `beamLink`(Erekir 长距离节点)也覆盖了 `laserColor2 = Color.valueOf("ffd9c2")` 喵。

## 2. 确切的渐变公式喵

核心在 `PowerNode.setupColor()`(PowerNode.java 第 183-185 行):

```java
protected void setupColor(float satisfaction){
    Draw.color(Tmp.c1.set(laserColor1).lerp(laserColor2, (1f - satisfaction) * 0.86f + Mathf.absin(3f, 0.1f)).a(Renderer.laserOpacity * (useLod ? Lod.alpha2 : 1f)));
}
```

等价展开:

```
t     = (1 - satisfaction) * 0.86 + absin(3f, 0.1f)
color = lerp(laserColor1, laserColor2, t)         // 线性 lerp,arc 不 clamp t
alpha = laserOpacity * (useLod ? Lod.alpha2 : 1f)
```

其中:
- `satisfaction` 是整张电网的满意度 `power.graph.getSatisfaction()`,范围 [0,1] 喵。
  它反映"产出 / 需求"比,不是节点自身电量喵。
- `Mathf.absin(3f, 0.1f)` 是 arc 的脉冲函数喵。
  经仓库大量调用反推其语义:`absin(in, s)` = `abs(sin(Time.time * 2π / in)) * s`,
  即第一参数是周期(单位 tick,60 tick/s),第二参数是振幅喵。
  所以 `absin(3f, 0.1f)` = `|sin(Time.time * 2π / 3)| * 0.1`,取值 [0, 0.1],
  周期 3 tick = 0.05 秒,是一个缓慢的小幅脉冲喵。
  佐证:同代码库 `PlayerComp.hover = Mathf.absin(5f, 1f)`(周期 5tick、振幅 1)、
  `Fx` 里 `50f + Mathf.absin(5f, 5f)`(周期 5tick、振幅 5),都是恒定首参却能动画,说明 2 参版内部自动接 `Time.time` 喵。
- 因此 lerp 系数 `t` 的实际范围:
  - 满电(satisfaction = 1):t ∈ [0, 0.1],颜色≈近白色喵。
  - 缺电(satisfaction = 0):t ∈ [0.86, 0.96],颜色≈ powerLight 琥珀色喵。
  - 中间状态按满意度线性插值喵。

注意 `0.86` 这个系数:即使满意度为 0,lerp 也到不了 1.0,
laser 永远保留一点点白色成分,不会完全变成纯琥珀喵。

`Drawf.laser`(Drawf.java 第 521-541 行)对端帽还额外乘了一层 LOD 透明度,主体线条保持上述 alpha 喵:

```java
float lod = useLod ? (start.width * scale * start.scl() < 10f ? Lod.alpha1 : Lod.alpha2) : 1f;
float a = Draw.getColorAlpha();
if(a >= 1f/255f){
    if(lod > 0.0001f){
        Draw.alpha(lod * a);          // 端帽 alpha 再乘 LOD
        Draw.rect(start, x, y, ...);
        Draw.rect(end, x2, y2, ...);
        Draw.alpha(a);
    }
    Lines.stroke(12f * scale);        // 主体用 12 * laserScale 的粗线
    Lines.line(line, x + vx, y + vy, x2 - vx, y2 - vy, false);
    Lines.stroke(1f);
}
if(light) light(x, y, x2, y2);        // 附加橙色 0.3 强度的光晕
```

`light()` 是 Drawf.java 第 320-323 行,用 `Color.orange, 0.3f` 画一道光晕线喵。

## 3. 颜色常量来源喵

`Pal.powerLight` 定义于 `core/src/mindustry/graphics/Pal.java` 第 94 行:

```java
powerLight = Color.valueOf("fbd367"),
```

`Color.white` 即 #ffffff 喵。

BeamNode 用到的 `"ffd9c2"` 是暖珊瑚色(255, 217, 194)喵。

## 4. 各状态下的具体 RGB/hex 值喵

线性 lerp:`color = white + (powerLight - white) * t`,逐通道计算:
- R = 255 + (251 - 255) * t = 255 - 4t
- G = 255 + (211 - 255) * t = 255 - 44t
- B = 255 + (103 - 255) * t = 255 - 152t

| 状态 | satisfaction | lerp 系数 t | RGB | hex |
| --- | --- | --- | --- | --- |
| 满电 | 1.0 | 0 | (255, 255, 255) | #ffffff |
| 满电(脉冲峰值) | 1.0 | 0.1 | (255, 251, 240) | #fffbf0 |
| 半电 | 0.5 | 0.43 | (253, 236, 190) | #fdecbe |
| 缺电 | 0.0 | 0.86 | (252, 217, 124) | #fcd97c |
| 缺电(脉冲峰值) | 0.0 | 0.96 | (251, 213, 109) | #fbd56d |
| powerLight 基准色 | — | 1.0 | (251, 211, 103) | #fbd367 |

结论:满电是近白暖白,缺电是琥珀色(#fbd367 附近),这是 Mindustry 激光的经典观感喵。

## 5. satisfaction 的定义喵

`core/src/mindustry/world/blocks/power/PowerGraph.java` 第 86-93 行:

```java
public float getSatisfaction(){
    if(Mathf.zero(lastPowerProduced)){
        return 0f;
    }else if(Mathf.zero(lastPowerNeeded)){
        return 1f;
    }
    return Mathf.clamp(lastPowerProduced / lastPowerNeeded);
}
```

即:
- 没有任何产出(比如孤立的节点):满意度 = 0,激光呈琥珀色喵。
- 没有任何负载需求:满意度 = 1,激光呈白色喵。
- 正常情况:`clamp(产出/需求)`,产能不足时满意度 < 1,激光逐渐变黄喵。

## 6. MC 1.21.1 RenderType.lines 复刻建议喵

Mindustry 激光其实是**整条线统一一个颜色**(按满意度 lerp 后的单一 tint),不是两端不同色渐变喵。
贴图上看到的"中间亮两端暗"是 laser.png 纹理自带的纵向渐变 + 端帽图(laserEnd)叠加出来的,不是顶点色渐变喵。
在 MC 的 `RenderType.lines` 里建议:

1. 端点色:两个端点都用同一个满意度颜色喵。
   若想模拟"中间亮两端暗"的纹理观感,可把两端的 alpha 或亮度调低(比如末端乘 0.7),中间保持满值,做顶点色渐变喵。
   最简单也最忠实:统一色即可喵。
2. 插值方式:用 arc 相同的线性 lerp 公式喵。
   `t = (1 - satisfaction) * 0.86 + 0.1 * abs(sin(worldTime * 2π / 周期))`
   周期按真实秒换算:Mindustry 3 tick / 60 tps = 0.05 秒;MC 20 tps,所以 MC 里周期用 1 个游戏 tick(0.05s)即可,即 `abs(sin(ticks * 2π)) * 0.1` 喵。
3. 满意度:按你电网的 `min(产能,1) / 需求` 之类映射,产出 0 时取 0、无需求时取 1,与 Mindustry 一致喵。
4. alpha:Mindustry 默认 `Renderer.laserOpacity = 0.5f`(设置项 "lasersopacity" 默认 50)喵。
   建议 MC 里主线条 alpha 取 0.5;若想要发光感,可叠加一条更粗的 additive(加色混合)渲染通道,或用第二层 alpha ≈ 0.25 的粗线做辉光喵。
   端帽的 LOD 淡出在 MC 里不必模拟喵。
5. 线宽:Mindustry 主体用 `Lines.stroke(12 * laserScale = 12 * 0.25 = 3)` 个像素的粗线喵。
   RenderType.lines 是 1px 细线,视觉会明显偏细;建议用多层细线并排,或改用 quad 拉伸一条细矩形来近似 3px 宽喵。

## 7. autolink 扫描范围:是 xz 平面圆,不是球喵

`PowerNode.getPotentialLinks()`(PowerNode.java 第 221-274 行)里对候选建筑做重叠判断喵:

```java
Boolf<Building> valid = other -> other != null && other.tile != tile && other.block.connectedPower && other.power != null &&
    (other.block.outputsPower || other.block.consumesPower || other.block instanceof PowerNode) &&
    overlaps(tile.x * tilesize + offset, tile.y * tilesize + offset, other.tile, laserRange * tilesize) && other.team == team &&
    ...（图去重、绝缘、maxNodes、排除相邻格子等）;
```

`overlaps()` 最终走到(PowerNode.java 第 199-218 行):

```java
protected boolean overlaps(float srcx, float srcy, Tile other, float range){
    return Intersector.overlaps(Tmp.cr1.set(srcx, srcy, range), other.getHitbox(Tmp.r1));
}
```

`Tmp.cr1.set(x, y, range)` 是 arc.geom.Circle,只存 (x, y, 半径),是**二维圆**喵。
Mindustry 世界是 2D(地图平面 x/y),没有 z 轴喵。
所以在 MC 里映射:把 Mindustry x/y 对应到 MC 的 x/z,autolink 范围是**水平面(xz)上的圆,完全忽略 MC 的 y 坐标**,不是球喵。

范围数值:默认 `laserRange = 6`,判断半径 = `laserRange * tilesize = 6 * 8 = 48` 像素 = 6 格喵。
按 1 Mindustry 格 = 1 MC 格换算,autolink 是半径 6 格的水平圆,并且是"圆 vs 对方方块 AABB"的相交判定喵。
另外注意边界条件:
- 用 buildingTree 的方形包围盒做粗筛,再用 `valid.get()` 里的圆判定精筛,最终是圆喵。
- 排除相邻格(`Edges.getEdges(size)` 的 4 邻)喵。
- 排除不同队伍、绝缘路径(`insulated()` 用 `World.raycast` 沿线扫绝缘块,仅 Erekir 玩法)、同一电网图、以及已满 maxNodes=3 的节点喵。
- 排序先 Node 后普通建筑、再按距离近优先喵。

## 附:v7 旧 LaserColor 的说明喵

本仓库 git 全历史中没有任何 `LaserColor.java` 文件记录(搜 `--all` 也无结果),因此无法贴出旧版确切代码喵。
若你在旧教程里见过 `LaserColor.get(satisfaction)` 的 HSV 实现,那是 v7 时代产物,当前游戏(你手里这份 v8 源码)已不用,不必复刻喵。
