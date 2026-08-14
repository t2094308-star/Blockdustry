# T35 silicon-smelter 冒烟特效与火焰研究喵

> 任务：迁移 Mindustry 硅冶炼厂 silicon-smelter（GenericCrafter, size 2）。本文件逐条记录原版冒烟特效（Fx.smeltsmoke）与尾焰（DrawFlame）的实现，供渲染器/BE 实现对照喵。
> 数据全部取自原版源码，未实现/存疑处显式标注喵。

## 1. 方块定义（Blocks.java L1069-1083）喵

| 参数 | 原版值 | Blockdustry 映射 |
|---|---|---|
| class | GenericCrafter | SiliconSmelterBlockEntity extends BlockdustryBuildingEntity（GenericCrafter 先例 GraphitePress） |
| size | 2 | 2×2 corner |
| craftTime | 40f tick | 40 tick |
| craftEffect | **Fx.smeltsmoke** | 渲染器 billboard 白方烟团（见 §3） |
| outputItem | silicon ×1 | BlockdustryBlocks.SILICON ×1 |
| consumeItems | coal ×1, sand ×2 | Items.COAL ×1, Items.SAND ×2 |
| consumePower | 0.50f | powerNeeded = 0.50f（锚点格） |
| requirements | copper 30, lead 25 | 科技树 buildRequirement |
| hasPower | true | 实现 BlockdustryPowerNode |
| hasLiquids | false | 无液体 |
| drawer | DrawMulti(DrawDefault, DrawFlame(#ffef99)) | 方块模型顶面 + BER 火焰（§4） |
| ambientSound | loopSmelter 0.07f | 本 mod 无环境音系统，**不实现**（已知省略） |
| itemCapacity | 默认 10 | 煤/沙/硅 各容量 10（沿用 GraphitePress 按类型容量惯例） |
| warmupSpeed | 0.019f（GenericCrafter 默认） | warmup 每 tick ±0.019 |
| updateEffect | none | 无 |
| updateEffectChance | 0.04f（未触发，updateEffect=none） | 无 |

## 2. 科技树（SerpuloTechTree.java L129）喵

- parent：`graphitePress`（L104 node(graphitePress) 内嵌 siliconSmelter）
- 成本：默认 = 方块建造配方 = **copper 30 + lead 25**
- 目标：`SectorComplete(frozenForest)`（区块解锁目标，Blockdustry 科技树不建模区块目标，照抄成本与 parent 即可）

## 3. 冒烟特效 Fx.smeltsmoke（Fx.java L2557-2562）喵

```java
smeltsmoke = new Effect(15, e -> {
    randLenVectors(e.id, 6, 4f + e.fin() * 5f, (x, y) -> {
        color(Color.white, e.color, e.fin());
        Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 2f, 45);
    });
}),
```

逐条拆解（Arc 单位：1 格 = 8 单位）：
1. **寿命**：15 tick（=0.75 秒）
2. **粒子数**：6 个
3. **径向散布**：`randLenVectors`（Angles.randLenVectors）——每个粒子随机角度 + 随机长度，长度上限 `4 + fin*5` 单位。fin=0 时长 4 单位（0.5 格），fin=1 时长 9 单位（1.125 格）。**即：烟团从中心向四周随机方向飞散，随时间半径扩大**
4. **颜色**：`color(Color.white, e.color, e.fin())`——白色与 effect 色 lerp。craftEffect.at(x,y) 未传色 → e.color=白 → **纯白**
5. **形状**：`Fill.square(px, py, halfSize, 45)`——旋转 45° 的实心方块（方形烟团）
6. **尺寸**：halfSize = `0.5 + fout*2` 单位。fout=1（起始）→ 2.5 单位（0.3125 格）；fout=0（结束）→ 0.5 单位（0.0625 格）。**即：烟团随时间缩小**
7. **透明度**：原版未显式设 alpha（color() 带全 alpha）→ 15 tick 全程不透明、骤灭

### 冒烟特效贴图结论
原版 smeltsmoke 是**程序化 Fill.square**，无独立冒烟贴图可拷。MC 实现用 `blockdustry:textures/misc/white.png`（1×1 纯白）染色作方形烟团喵。

### MC 渲染器实现（3D 适配，非自创）
- 保持：6 个、寿命 15 tick、白、旋转 45°、径向飞散半径 4→9 单位、halfSize 2.5→0.5 单位
- 3D 适配：原版方形画在**水平面**（Mindustry 俯视 2D），MC 玩家从侧面看会成线 → 改为**相机朝向 billboard**（FireBulletRenderer.drawBillboard 同款），保证任意视角可见
- 3D 适配：加轻微上升（0.3→0.8 格）更符合烟柱形态
- 3D 适配：alpha 用 fout 渐变淡出（原版骤灭在 3D 突兀，淡化更接近「烟消散」）；文档化此偏差
- 位置：2×2 建筑中心（锚点 +1,+1）上方 y+1.3 起（烟囱口高度）

## 4. 尾焰 DrawFlame（DrawFlame.java）喵

silicon-smelter `drawer = DrawMulti(DrawDefault, DrawFlame(Color.valueOf("ffef99")))`，flameColor = #ffef99（亮暖黄）。

### 加载（load）喵
- `block.emitLight = true`，lightRadius=60、lightAlpha=0.65、lightSinScl=10、lightSinMag=5（单位）
- 找 `block.name + "-top"` 贴图 = **silicon-smelter-top.png**（64×64，中心 16×16 径向火焰光晕，白→#ffef99）

### 绘制（draw，warmup>0 时）喵
1. `Draw.z(block+0.01)` 层
2. `Draw.alpha(build.warmup())`；`Draw.rect(top, build.x, build.y)` —— 顶贴图全 footprint（2×2）铺画，alpha=warmup
3. 火焰外圈：`alpha=(0.7 + absin(time,8,0.3) + rand(±0.06)) * warmup`；`tint(#ffef99)`；`Fill.circle(radius = 3 + absin(time,5,2) + rand(0~0.1))`（单位）→ 半径 3~5 单位 = 0.375~0.625 格
4. 火焰内芯：`color(白, alpha=warmup)`；`Fill.circle(radius = 1.9 + absin(time,5,1) + rand)` → 半径 1.9~2.9 单位 = 0.2375~0.3625 格

### MC 渲染器实现
- **顶贴图 quad**：silicon_smelter_top 纹理，2×2 footprint（半宽 1.0，建筑中心），y+0.001，alpha=warmup
- **外圈光晕**：`misc/glow.png`（径向白晕）染 #ffef99，halfSize = (3 + 2·absin)/8 = 0.375+0.25·absin(time,5)，alpha = warmup·(0.7+0.3·absin(time,8))，y+0.002
- **内芯**：`misc/white.png` 染白，halfSize = (1.9 + 1·absin)/8 = 0.2375+0.125·absin(time,5)，alpha=warmup，y+0.003
- 三层 y 微差防共面渗色（坑文档 BER渲染 §3）
- 动态光（emitLight）：MC 原版无动态光，**不实现**（已知省略）

## 5. 同步与实现要点喵

- BE 服务端 tickAnchor：warmup 变化 > 0.02 发 `sendBlockUpdated`（GraphitePress 同款）；craft 完成时记录 `smokeStartGameTime` 并发包
- 客户端 loadAdditional 记 `lastSyncTick`（TurretBlockEntity 同款），供渲染器本地衰减
- 渲染器 smoke：`elapsed = level.getGameTime()+partialTick - be.getSmokeStartGameTime()`；0≤elapsed≤15 才画
- 随机种子：`blockPos.hashCode()*31 + smokeStartGameTime` 每爆不同、跨帧稳定（FuseBlockEntityRenderer 同款思路）

## 6. 已知省略/偏差清单喵

| 项 | 原因 |
|---|---|
| ambientSound loopSmelter | 本 mod 无环境音系统 |
| emitLight 动态光 | MC 原版无动态光 |
| 烟团 alpha 由「骤灭」改为 fout 淡出 | 3D 视觉更自然 |
| 烟团由「水平面」改为「相机 billboard」+ 轻微上升 | 3D 可见性 |
| itemCapacity 总容量 10 → 按类型各 10 | 沿用 GraphitePress 既有惯例 |
