# T46 粉碎机+焚化炉 动画/特效逐条研究记录喵

> 来源：`D:\Blockdustry\Mindustry\core\src\mindustry\content\Blocks.java`（L1293-1329）、`world\blocks\production\GenericCrafter.java`、`world\blocks\production\Incinerator.java`、`world\draw\DrawRegion.java`、`graphics\Drawf.java`（spinSprite L606）、`content\Fx.java`（L2463/2499/2513）喵。

## 一、pulverizer（GenericCrafter，size 1）喵

### 1. drawer 三层（DrawMulti，按序叠画）喵
| 层 | 来源 | 原版行为 | Blockdustry 实现 |
|---|---|---|---|
| base | `pulverizer.png`（DrawDefault） | 本体整幅 | 方块模型顶面贴 pulverizer.png、侧面石料（mod 机器惯例）喵 |
| rotator | `pulverizer-rotator.png`（DrawRegion("-rotator")） | `spinSprite=true; rotateSpeed=2`；角=totalProgress×2（度），totalProgress += warmup/ tick → warmup=1 时 120°/s | BER 绕 Y 旋转 quad（1×1 全幅），`angle = (gameTime+partialTick) × warmup × 0.1047` rad/tick（=120°/s），停转时停喵 |
| top | `pulverizer-top.png`（DrawRegion("-top")） | 静态顶盖，叠 rotator 之上 | BER 静态 quad，y 略高 0.004 防共面渗色喵 |

> 注：spinSprite 原版会对 r mod 90° 并画双幅（90° 偏移低 alpha）模拟旋转光照，MC 无此机制，以整幅旋转近似（视觉等效，见整合清单风险节）喵。

### 2. craft 粒子 Fx.pulverize（L2499-2504）喵
- 寿命 40 tick；5 个方块粒子；色 `Pal.stoneGray #8f8f8f`；散布半径 `3 + fin×8` 单位（0→11 单位 ≈ 1.375 格）；尺寸 `fout×2 + 0.5` 单位；方块绕自身 45° 旋转喵。
- MC 映射：`DustParticleOptions(new Vector3f(0.56,0.56,0.56), 1.2f)`，count=5，中心偏上 `(x+0.5, y+0.5, z+0.5)`，偏移 0.5/0.25/0.5、速度 0.1 喵。

### 3. 持续粒子 Fx.pulverizeSmall（L2513-2518）喵
- 寿命 30 tick；3 个方块粒子；色 stoneGray；散布半径 `fin×5` 单位（0→0.625 格）；尺寸 `fout+0.5` 单位；45° 旋转；触发 `updateEffectChance=0.04/tick` 且 `efficiency>0` 喵。
- MC 映射：`DustParticleOptions(new Vector3f(0.56,0.56,0.56), 0.8f)`，count=3，随机散布在 1×1 内、y+0.6，偏移 0.25/0.1/0.25、速度 0.05，运行中 4%/tick 概率喵。

### 4. warmup 预热动画喵
- `warmup = approachDelta(warmup, warmupTarget=1, 0.019)`，仅 efficiency>0 时爬升；驱动转盘转速（totalProgress += warmup）喵。
- MC：服务端 tickAnchor 维护 warmup，NBT 同步，BER 读 warmup 乘转盘角速度喵。

### 5. 环境音喵
- `ambientSound = Sounds.loopGrind; ambientSoundVolume = 0.025` —— Blockdustry 无方块环境循环音基础设施，未实现（与 kiln/graphite_press 一致）喵。

## 二、incinerator（Incinerator，size 1）喵

### 1. 火焰 draw（IncineratorBuild.draw 全参数）喵
```
if(heat > 0):
  g=0.3, r=0.06
  alpha = ((1-g) + absin(Time.time, 8, g) + rand(r) - r) × heat
  Draw.tint(flameColor #ffad9d); Fill.circle(x, y, 2)     // 外圈 2 单位 = 0.25 格
  Draw.color(1,1,1,heat); Fill.circle(x, y, 1)            // 内圈 1 单位 = 0.125 格，纯白 alpha=heat
```
- MC 映射：相机 billboard 双圈——外圈 glow.png 染 #ffad9d、半径 0.25 格、alpha=呼吸×heat；内圈 white.png 纯白、半径 0.125 格、alpha=heat；呼吸 `absin(time,8,0.3)` + 随机抖动 `rand(0.06)` 喵。

### 2. heat 预热喵
- `heat = approachDelta(heat, efficiency, 0.04)`（efficiency = enabled×powerStatus）。MC：通电目标 1、断电目标 0，0.04/tick 逼近；heat>0.5 才接收物品喵。

### 3. 吞噬特效 Fx.fuelburn（L2463-2468）喵
- 寿命 23 tick；5 个圆点粒子；色 `Color.lightGray #c0c0c0 → Color.gray #808080` 渐变（按 fin 插值）；散布半径 `fin×9` 单位（0→1.125 格）；半径 `fout×2` 单位；触发：handleItem 30% 概率、handleLiquid 2% 概率喵。
- MC 映射：`DustParticleOptions(new Vector3f(0.60,0.60,0.60), 1.0f)`，count=5，中心偏上 `(x+0.5, y+0.4, z+0.5)`，偏移 0.45/0.25/0.45、速度 0.08，handleItem 30% 概率喵。

### 4. 接收判定喵
- `acceptItem: heat>0.5 && enabled`（enabled 本 mod 映射为通电 powerStatus>0.01）；`acceptLiquid: heat>0.5 && liquid.incinerable && enabled`（液体未迁移）喵。
