# ^T36 kiln（窖炉）火焰特效研究喵

> 依据原版源码逐条记录，数据不串、不瞎编。源：`mindustry/world/draw/DrawFlame.java`、`mindustry/content/Fx.java` L2557、`mindustry/world/blocks/production/GenericCrafter.java`、`Blocks.java` L1103-1116 喵。

## 0. 结论摘要喵

- kiln 的火焰 = `DrawMulti(new DrawDefault(), new DrawFlame(#ffc099))`：
  1. `DrawDefault` 画本体贴图 `kiln.png`（64×64）喵
  2. `DrawFlame` 画**炉口烧红叠层**（`kiln-top` 贴图，alpha=warmup）+ **双圈火苗**（外圈 #ffc099、内圈白，半径脉动）+ **光**（Drawf.light）喵
- 制作特效 = `Fx.smeltsmoke`：6 片白色方块 15 tick 内由中心向外飞散（白色，无火焰色）喵
- 通用动画 = GenericCrafter `warmup`（0.019/tick 渐热，驱动火焰 alpha）、无 updateEffect（kiln 未设，默认 Fx.none）喵
- 无任何独立「火焰粒子贴图」——原版火苗是 `Fill.circle` 程序化圆，非贴图。故本实现不拷 fireN.png（那是火焰方块贴图，非 kiln 火苗），只拷 `kiln-top.png`（炉口叠层）喵

## 1. DrawFlame 逐参记录喵

原版 `DrawFlame.java`（kiln 用 `new DrawFlame(Color.valueOf("ffc099"))`）喵：

| 参数 | 值 | 含义 |
|---|---|---|
| flameColor | #ffc099 | 外圈火焰色（255,192,153）喵 |
| top | block.name + "-top" = `kiln-top` | 炉口烧红叠层贴图喵 |
| lightRadius / lightAlpha | 60 / 0.65 | 光半径/透明度喵 |
| lightSinScl / lightSinMag | 10 / 5 | 光半径正弦脉动周期 10s、幅度 5 单位喵 |
| flameRadius / flameRadiusIn | 3 / 1.9 | 外/内圈基半径（世界单位，8 单位=1 格）喵 |
| flameRadiusScl / flameRadiusMag | 5 / 2 | 外圈半径脉动周期 5s、幅度 2 单位喵 |
| flameRadiusInMag | 1 | 内圈半径脉动幅度 1 单位喵 |
| flameX / flameY | 0 / 0 | 火焰偏移（中心）喵 |

`draw()` 流程（warmup>0 才画）喵：
1. `Draw.alpha(warmup)` + `Draw.rect(top, x, y)` → 炉口叠层整块 alpha=warmup（无呼吸）喵
2. 火苗 alpha = `((1-0.3) + absin(time,8,0.3) + rand(0.06) - 0.06) * warmup`（呼吸周期 8s，幅度 0.3，逐帧随机抖动）喵
3. 外圈 `Draw.tint(#ffc099)` + `Fill.circle(半径 = 3 + absin(time,5,2) + rand(0.1))` 喵
4. 内圈 `Draw.color(1,1,1,warmup)` + `Fill.circle(半径 = 1.9 + absin(time,5,1) + rand(0.1))` 喵

`drawLight()` 流程喵：
- `Drawf.light(x, y, (60 + absin(time,10,5)) * warmup * size(=2), #ffc099, 0.65)` → 光半径约 120-130 世界单位 = 15-16 格（MC 侧裁剪到 4 格防超大 quad）喵

`Mathf.absin(time, scl, mag) = |sin(time * 2π / scl)| * mag` 喵。

## 2. Fx.smeltsmoke（craftEffect）逐参记录喵

`Fx.java` L2557 喵：
```java
smeltsmoke = new Effect(15, e -> {
    randLenVectors(e.id, 6, 4f + e.fin() * 5f, (x, y) -> {
        color(Color.white, e.color, e.fin());
        Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 2f, 45);
    });
});
```
- 寿命 15 tick；6 片粒子；距中心 4→9 世界单位（fin 0→1）向外散；色=白色（e.color 默认白）；边长 0.5→2.5 世界单位（fout），旋转 45° 方块喵
- MC 映射：craft 完成时在 2×2 中心发 6 个白色 DustParticleOptions（向外飞散）喵

## 3. GenericCrafter 通用动画喵

| 项 | 值 | 说明 |
|---|---|---|
| warmupSpeed | 0.019 | approachDelta 渐热/衰减速率（驱动火焰 alpha）喵 |
| updateEffect | Fx.none | kiln 未设 → 无持续粒子喵 |
| craftTime | 30f | 30 tick/窑喵 |
| outputItem | metaglass×1 | 产钢化玻璃 1 喵 |
| consumeItems | lead×1 + sand×1 | 每窑 1 铅 1 沙喵 |
| consumePower | 0.60f | 耗电 0.6/s，无电停摆喵 |
| itemCapacity | 10 | 各类型独立上限 10（Building.items 每类型 capacity）喵 |
| dumpTime | 8 | 每 8 tick 轮询卸货 1 件喵 |

## 4. 本实现（KilnBlockEntityRenderer）对照喵

- 顶面叠层：`kiln_top.png` quad 覆盖 2×2 顶面，alpha=warmup（对应 Draw.rect(top)）喵
- 火焰双圈：外圈染 #ffc099、内圈白，半径 = 原版参数/8 折算成格，absin(5s) 脉动 + 逐帧随机抖动（对应 Fill.circle ×2）喵
- 呼吸 alpha = `(0.7 + 0.3*absin(time,8) + rand(0.06) - 0.06) * warmup` 喵
- 环境光晕：`(60 + absin(time,10,5))*warmup*2/8` 折算，裁剪到 4 格低 alpha（对应 Drawf.light）喵
- 光晕/火苗全部用 `textures/misc/glow.png` 径向渐变 billboard（原版火苗是程序化圆无贴图，MC 用软边光斑等效，不新画贴图）喵
- craft 白烟：6 个白色 DustParticleOptions 向外散（对应 Fx.smeltsmoke）喵
