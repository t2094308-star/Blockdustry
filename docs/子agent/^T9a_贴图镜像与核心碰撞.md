# T9a 钻头侧面贴图镜像 + 核心顶部碰撞修复喵

## 任务一：钻头侧面左半贴图左右镜像喵

### 现象喵

钻头 2×2 侧面用 `textures/block/drill_side_left.png`（左列格 nw/sw）与 `drill_side_right.png`（右列格 ne/se）喵。
用户反馈：**左半边左右反转一下，对称才好看**喵。

### 改动喵

用 PIL 对 `src/main/resources/assets/blockdustry/textures/block/drill_side_left.png` 做
`transpose(Image.FLIP_LEFT_RIGHT)`（左右镜像），覆盖保存，保持 64×64、RGBA 全不透明（alpha 全 255）喵。
镜像后左列内容左右翻转，与右半拼接后整体呈镜像对称喵。

## 任务二：核心顶部仍没有碰撞箱喵

### 现象喵

T8c 修复后核心亮度正常，但用户反馈**核心顶部（y=3 处）仍没有碰撞箱**：底部/侧面有碰撞，顶部能穿入、站不住喵。

### 排查与根因喵

1. **AABB 只盖到底部？不是**：`BlockdustryBuildingBlock.getShape` 已覆写返回整组包围盒，
   `coreBlock()` 也传了第 4 参 height=3，基座块的 `getShape` 返回 `Block.box(0,0,0,48,48,48)` 即完整 3×3×3 喵。
2. **getShape 与 getCollisionShape 不一致？不是**：默认 `BlockBehaviour.getCollisionShape` 委托 `state.getShape`，
   二者本就一致（本次仍显式覆写以保证明确）喵。
3. **真正根因——MC 碰撞求解器只扫「实体 AABB + 四周各 1 格」的方块位置**喵：
   - `net.minecraft.world.level.BlockCollisions` 构造时把扫描范围定为
     `floor(box.min-1e-7)-1` .. `floor(box.max+1e-7)+1`（每轴各扩展 1 格），`Level/Entity.collideBoundingBox` 传入的是实体扫掠 AABB 喵。
   - 玩家站在核心顶（脚 y=3，AABB y=3..4.8）时，扫描范围 y=2..5，**基座块（y=0）根本不在扫描范围里**，其 3 高包围盒不会被检查喵。
   - 玩家落到脚 y<2 时基座块才进入范围，被 3 高形状瞬间推回 y=3 → 表现成「顶部穿入 + 弹跳」，无法站立喵。
   - 结论：坑文档 §3 的范式 `Block.box(-dx*16,0,-dz*16,(size-dx)*16,height*16,(size-dz)*16)` 对 height=1 有效
     （1 格在求解器 1 格扩展内），但对 height=3 的顶盖无效喵。

### 修复方案喵

核心放置时在上层（y+1、y+2）也放同款隐形方块（core 模型置空不渲染，仅锚点格 BER 画一次立方体），
每格按「与锚点的 y 层差」返回**对应高度 = (height - layer) 的整组包围盒**，三层拼成完整 3×3×3，
且每层方块位置都在求解器 1 格可达范围内 → 顶部、侧面、底部全部实心喵。

- 基座（layer=0）：`Block.box(-dx*16,0,-dz*16,(size-dx)*16, height*16,     (size-dz)*16)`，覆盖 y=0..3 喵。
- 上层一（layer=1）：高度 height-1=2，覆盖 y=1..3 喵。
- 上层二（layer=2）：高度 height-2=1，覆盖 y=2..3 喵。

### 改动文件喵

1. **`src/main/java/com/blockdustry/building/BlockdustryBuildingBlock.java`**：
   - 显式覆写 `getCollisionShape`（与 `getShape` 同源，保证一致）喵。
   - `getShape`/`getCollisionShape` 都改走 `groupShape(state, height - layerAt(level, pos))` 喵。
   - 新增私有 `groupShape(state, h)`（整组包围盒，h=该层向上覆盖格数）与 `layerAt(level, pos)`（读 BE 锚点算 y 层差，无 BE/锚点按基座层 0）喵。
   - `onRemove` 联动破坏由单层改为扫 `height` 层（否则上层隐形格会残留浮空）喵。
2. **`src/main/java/com/blockdustry/building/BlockdustryBuildingItem.java`**：
   - 预检由 `size×size` 改为 `size×size×height`（防止放到天花板里顶掉方块）喵。
   - 放置循环由单层改为 `height` 层，每层设 CORNER + anchor + team 喵。

### 验证喵

- `./gradlew compileJava` BUILD SUCCESSFUL（改动后增量编译通过，仅存既有的 EventBusSubscriber 过时警告）喵。
- 放置核心预期：3×3×3 立方体底部/侧面/顶部全部实心，玩家可站立于顶、不可从顶部穿入喵。

## 坑补充（已并入 docs/研究-渲染与模型坑.md §3）喵

- 多格建筑包围盒高度 > 1 时，**基座块的大包围盒在实体站到顶部时超出碰撞求解器扫描范围**（求解器只扫实体 AABB + 四周各 1 格的方块位置），
  必须在对应层也放置隐形碰撞格，并按「与锚点的层差」裁剪每层包围盒高度 = (height - layer) 喵。

## 交接喵

- 占用文件喵：
  - 已改 `src/main/resources/assets/blockdustry/textures/block/drill_side_left.png`（左右镜像）
  - 已改 `src/main/java/com/blockdustry/building/BlockdustryBuildingBlock.java`
  - 已改 `src/main/java/com/blockdustry/building/BlockdustryBuildingItem.java`
  - 已写本文件 `docs/子agent/T9a_贴图镜像与核心碰撞.md`
  - 已补 `docs/研究-渲染与模型坑.md` §3
- 建议 `runClient` 放置核心目测：可站立于顶、不可穿入；放置钻头目测侧面左半镜像对称喵。
