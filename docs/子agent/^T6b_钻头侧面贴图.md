# T6b 钻头侧面贴图 阶段产出喵

## 目标喵
为 Blockdustry 钻头（2×2 建筑）自绘一张 Mindustry 深色底座风格的**侧面贴图**，覆盖 `textures/block/drill_side.png`，不依赖原版图，不改 Java/模型 JSON 喵。

## 结论/产出喵
**新贴图：64×64、RGBA、全不透明（alpha 255）、主色 `#3a3d42`（58,61,66）深灰金属，10 色有限调色板**。自绘像素风「深灰金属底座 + 面板槽线 + 通风栅格 + 铜色点缀」，无铆钉、无磨损细节（按用户更新指示），左右边缘无缝以贴合 2×1 可见侧面喵。

### 关键几何决策：贴图保持 1:1 方形（64×64），不做 64×32 喵
用户要求「侧面比例 1:2 / 宽是高的 2 倍」。核对 `models/block/drill_nw.json`（ne/sw/se 同构）后判定如下喵：

| 项 | 值喵 |
|---|---|
| 侧面面片几何 | 元素为满立方体 `[0,0,0]~[16,16,16]`，north/south/east/west/down 面均 **16×16 方形**喵 |
| 侧面 UV | 全部 `uv:[0,0,16,16]` = **整幅贴图**映射到方形面片喵 |
| UV 缩放依据 | 顶面 `uv:[0,0,8,8]` 从 64×64 的 `drill.png` 精确选取左上 32×32 象限 → 证明 MC 按 `纹理尺寸/16` **逐轴缩放** UV 喵 |
| 若做 64×32 | 横向 4px/unit、纵向 2px/unit，内容被**纵向拉伸**、圆/方/文字皆变形喵 |
| 正确结论 | 面片方形 + 整幅映射 → 贴图必须 **1:1 方形（64×64）**才不变形喵 |

2×1 可见侧面 = 两块方块面片并排，**各自显示整幅贴图**。为让视觉上呈 2:1 连续侧面，新贴图**左右边缘无缝**、横向槽线贯穿全宽，两块拼起来即一条连续宽面板（每块中央一道竖筋，侧面呈现均匀竖筋节奏）喵。

若后续确实要物理 64×32 的 2:1 贴图，需把模型侧面 UV 改为每块取半幅（如 `[0,0,8,16]`/`[8,0,16,16]`），属模型改动，不在本任务约束内，已在「交接」中列作待人工决策喵。

### 绘制思路喵
- 基底 `#3a3d42` 满铺 + 轻微金属颗粒（3% 提亮 `#42454b`、3% 压暗 `#32353a`，先于细节绘制，避免纯色死板，非磨损）喵
- 上下 1px 接缝深色 `#1e2024`，顶部 2px 高光倒角、底部 2px 阴影倒角（模拟顶光）喵
- 两条贯穿全宽的横向槽线（y=15/47，暗线+下缘受光），一条中央竖筋（x=31/32），形成 2×3 分块喵
- 顶部左：凹槽铭牌 + 中央刻度线；顶部右：铜色小面板喵
- 中部左右：通风栅格（暗缝+下缘受光，各 2 条）喵
- 底部左：维护铭牌 + 2×2 指示灯；底部右：铜色警示条喵
- 铜色 `#c6a38d`/`#8c6459` 呼应钻头顶面的 Mindustry 铜色钻头，保持全模组风格一致喵

### 绘制代码片段（PIL 12.x，完整脚本另存 `D:\Blockdustry\像素图片绘制尝试\draw_drill_side.py`）喵
```python
# -*- coding: utf-8 -*-
"""自绘钻头侧面贴图 drill_side.png（64×64 深灰金属底座风格）喵"""
from PIL import Image
import random

SIZE = 64
im = Image.new("RGBA", (SIZE, SIZE))
px = im.load()

# 调色板喵
P_SEAM=(30,32,36); P_SHADOW=(43,45,50); P_DARK=(50,53,58)   # 深色系喵
P_BASE=(58,61,66); P_MID=(66,69,75);   P_INSET=(52,55,60)   # 主金属喵
P_LIGHT=(80,84,91); P_LIGHTER=(96,101,109)                  # 受光喵
P_COPPER=(198,163,141); P_COPPERD=(140,100,89)              # 铜色喵

def hline(x0,x1,y,c):
    for x in range(x0,x1+1): px[x,y]=c
def vline(y0,y1,x,c):
    for y in range(y0,y1+1): px[x,y]=c
def rect(x0,y0,x1,y1,c):
    for y in range(y0,y1+1):
        for x in range(x0,x1+1): px[x,y]=c

# 1. 基底填充 + 轻微金属颗粒喵
rect(0,0,63,63,P_BASE)
random.seed(7)
for y in range(1,63):
    for x in range(0,64):
        r=random.random()
        if r<0.03: px[x,y]=P_MID
        elif r<0.06: px[x,y]=P_DARK

# 2. 上下接缝线（左右不设缝，水平无缝拼接）喵
hline(0,63,0,P_SEAM); hline(0,63,63,P_SEAM)
# 3. 顶部/底部倒角（上亮下暗）喵
hline(0,63,1,P_LIGHTER); hline(0,63,2,P_LIGHT)
hline(0,63,61,P_DARK);   hline(0,63,62,P_SHADOW)
# 4. 横向面板槽线（贯穿全宽）喵
hline(0,63,15,P_DARK); hline(0,63,16,P_MID)
hline(0,63,47,P_DARK); hline(0,63,48,P_MID)
# 5. 中央竖筋喵
vline(3,60,31,P_DARK); vline(4,59,32,P_MID)
# 6. 顶部左：指示铭牌喵
rect(10,5,24,12,P_INSET)
hline(10,24,5,P_DARK); vline(5,12,10,P_DARK)
hline(10,24,12,P_MID); vline(5,12,24,P_MID)
hline(12,22,8,P_LIGHT)
# 7. 顶部右：铜色小面板喵
rect(38,6,52,11,P_COPPER); hline(38,52,6,P_COPPERD); hline(38,52,11,P_LIGHT)
# 8. 中部左右：通风栅格喵
for y in (24,34):
    hline(8,26,y,P_DARK); hline(8,26,y+1,P_MID)
    hline(36,54,y,P_DARK); hline(36,54,y+1,P_MID)
# 9. 底部左：维护铭牌 + 指示灯喵
rect(10,52,24,58,P_INSET)
hline(10,24,52,P_DARK); vline(52,58,10,P_DARK)
hline(10,24,58,P_MID);  vline(52,58,24,P_MID)
rect(16,55,19,56,P_LIGHT)
# 10. 底部右：铜色警示条喵
rect(36,54,52,57,P_COPPER); hline(36,52,57,P_COPPERD)

im.save(r"D:\Blockdustry\仓库\src\main\resources\assets\blockdustry\textures\block\drill_side.png")
```

### 结果图预览说明喵
调色板映射预览（S=接缝、L=高光、l=受光、b=基底、m=稍亮、d=暗、i=凹槽、C=铜、c=铜暗），自顶向下结构清晰喵：
```
SSSSSS...(行0 接缝) LLLLL...(行1 高光) llll...(行2 受光)
行3~14 顶带：左=铭牌(i+l 刻度线)，右=铜面板(C)，中央竖筋 dm 贯穿
行15/16 与行47/48：贯穿全宽的横向槽线（d+m）
行17~46 中带：左右各 2 条通风栅格（d+m），余为带颗粒的基底
行49~60 底带：左=维护铭牌+指示灯(l)，右=铜警示条(C)
行61~62 底倒角(d+s)，行63 接缝(S)
```
整幅贴图 4096 像素全不透明，纵向由 2 条槽线分三段、横向由中央竖筋分两半，两块拼合后侧面呈「连续宽面板 + 均匀竖筋」的 2:1 视觉喵。

### 校验结果（PIL）喵
| 项 | 结果喵 |
|---|---|
| 尺寸 | 64×64 ✓（与旧贴图一致）喵 |
| 模式 | RGBA ✓喵 |
| Alpha | min=max=255，**全不透明** ✓喵 |
| 非空 | 10 色、非单色纯块 ✓喵 |
| 与旧图 | 像素不同（确认已覆盖为新图）喵 |
| 旧图备份 | `drill_side_old.png`（= Mindustry `mechanical-drill.png` 逐像素一致）喵 |

## 改动文件清单喵
| 文件 | 动作喵 |
|---|---|
| `src/main/resources/assets/blockdustry/textures/block/drill_side.png` | 覆盖：64×64 新自绘深灰金属侧面喵 |
| `src/main/resources/assets/blockdustry/textures/block/drill_side_old.png` | 新增：原图备份（与 Mindustry `mechanical-drill.png` 逐像素一致）喵 |
| `docs/子agent/T6b_钻头侧面贴图.md` | 新增：本文档喵 |
| `D:\Blockdustry\像素图片绘制尝试\draw_drill_side.py` | 新增：可复用绘制脚本（仓库外草稿区）喵 |
| `D:\Blockdustry\像素图片绘制尝试\drill_side_preview.png` / `drill_side_tiled_preview.png` | 新增：8× 放大与 2 块拼合预览（仓库外草稿区）喵 |

未改：Java、`models/block/drill*.json`、顶面/旋转贴图喵。

## 占用与交接喵
- 占用文件: 上述贴图 + 文档 + 草稿脚本喵
- 交接给: 主会话。需主会话做的整合动作：无代码改动；仅需在 `runClient` 中确认侧面观感（侧面引用 `#side` 已存在，无需改模型）；若坚持物理 64×32 的 2:1 贴图，则需改 `drill_nw/ne/sw/se.json` 的侧面 UV 为每块半幅（本任务约束内未改）喵
- 风险/待人工排查: 侧面 4 个朝向（north/south 互为镜像）会显示同一设计与镜像，属 MC 方块标准行为；南面呈现铜色/铭牌左右镜像，视觉正常喵

## 异常喵
无。
