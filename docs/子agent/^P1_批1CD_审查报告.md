# ^P1 批1CD/1B/雷光 审查报告喵

> 独立只读审查（不改任何代码）：审查范围 = 批1C/1D 6 建筑（气动钻/激光钻/爆破钻/硅冶炼/窑炉/塑钢压缩机）+ 批1B 2 建筑（container/bridge_conveyor）+ 雷光(fuse)特效重做 T31喵。
> 审查方式：主审查核对数据/挂载/编译，并分裂 3 个只读子审查员并行核对渲染特效（钻机组/生产组/雷光组）喵。
> 任务登记: `D:\Blockdustry\任务\T39_审查批1CD.md` 喵。

## 〇、审查结论（先给结论）喵

- **数据不串**：size/配方/耗电/产速/特效均逐一对照原版 Blocks.java，**无 A 套 B** 喵。但发现 2 处血量数值与原版不符（爆破钻、硅冶炼）且爆破钻注释数学错误喵。
- **挂载完整性**：8 注册行、CRAFTING_TAB 6+ITEMS_TAB oil、组血量 7 行、科技树 6 节点 parent 链、6 渲染器、ResearchIcons 7 case、lang 7 key、allMaterials OIL **全部齐备且正确**喵。
- **动画特效**：整体忠实度高于批1A；发现 2 个中危动画 bug（激光钻空转 57×、硅冶炼火焰闪烁快 20×）、1 个窑炉 warmup 不同步 bug、若干低危偏差喵。
- **编译**：本批文件 **0 编译错误**；但当前全量 `compileJava` 被**并发 T38 批次的 6 个文件**阻塞（Mender/Wall/Incinerator/WallLightning，非本批范围），需 T38 修完才 BUILD SUCCESSFUL喵。
- **判定**：**不阻断测试**，但建议先修 3 个中危动画 bug（激光空转、硅冶炼火焰、窑炉火焰不灭）与 2 个血量数值再进游戏喵。

---

## 一、数据不串核对表（最高要求，逐条对照原版）喵

原版依据：`Mindustry/core/src/mindustry/content/Blocks.java`（L2878 pneumatic / L2887 laser / L2900 blast / L1069 silicon / L1103 kiln / L1118 plastanium / L2105 bridge / L3225 container）+ `Drill.java` / `GenericCrafter.java` 字段喵。

| 建筑 | size | 配方 requirements | 耗电 | 产速/钻速 | 特效/机制 | 血量(原版) | 实现血量 | 判定 |
|---|---|---|---|---|---|---|---|---|
| pneumatic_drill | 2 ✓ | 铜18+石墨10 ✓ | 无电(原版仅水boost) ✓ | drillTime400→27tick(1.5×) ✓ | pulverizeSmall+mine ✓ | 160 | 160(strength3) | ✓ |
| laser_drill | 3 ✓ | 铜35+石墨30+硅30+钛20 ✓ | 1.10 ✓ | drillTime280+50×硬度 ✓ | pulverizeMedium+mineBig ✓ | 360 | 360(strength3) | ✓ |
| blast_drill | 4 ✓ | 铜65+硅60+钛50+钍75 ✓ | 3 ✓ | drillTime280+50×硬度, itemCap20 ✓ | pulverizeRed(0.03)+mineHuge+drawRim+rotateSpeed6 ✓ | **768**(钍healthScaling0.2) | **1120**(strength6) | ✗ |
| silicon_smelter | 2 ✓ | 铜30+铅25 ✓ | 0.5 ✓ | craftTime40, 煤1沙2→硅1 ✓ | smeltsmoke+DrawFlame#ffef99 ✓ | 160 | **200**(strength4) | ✗ |
| kiln | 2 ✓ | 铜60+石墨30+铅30 ✓ | 0.6 ✓ | craftTime30, 铅1沙1→钢化玻璃1 ✓ | smeltsmoke+DrawFlame#ffc099 ✓ | 160 | 160(strength3) | ✓ |
| plastanium_compressor | 2 ✓ | 硅80+铅115+石墨60+钛80 ✓ | 3 ✓ | craftTime60, 钛2+油0.25/s→塑钢1, liquidCap60 ✓ | formsmoke+plasticburn+DrawFade ✓ | 320 | 320(strength7) | ✓ |
| container | 2 ✓ | 钛100 ✓ | — | itemCapacity300 每类型独立 ✓ | 多类型存储 ✓ | 220(scaledHealth55) | 220(strength4.5) | ✓ |
| bridge_conveyor | 1 ✓ | 铅6+铜6 ✓ | — | range4, speed74(24.67tick), bufferCap14, itemCap10 ✓ | 配对+缓冲传输 ✓ | — | — | ✓ |

### 问题项（数据维度）喵
- **【中】爆破钻头组血量与原版不符，且注释/清单数学错误**：`BlastDrillRegistrar.java:40` strength=6 → 实际单格血 10+10×6=**70**、组血 70×16=**1120**；但 `BlastDrillBlock.java` 注释与整合清单、`BlockdustryBlocks.java:396` 均写「strength6→单格80、组血1280」。原版 blast-drill 因需求含钍(healthScaling=0.2) → health = 16×40×1.2 = **768**。三者（实际1120/注释1280/原版768）互不相符。修复建议：`BlastDrillRegistrar.java:40` strength 改 3.8（→48/格，组血768）；或至少把注释数学改对喵。
- **【中】硅冶炼厂组血量与原版不符**：`SiliconSmelterRegistrar.java:42` strength=4 → 单格 50、组血 **200**，原版 160。与同族 kiln（strength3→160）不一致，疑套用 graphite_press 的 strength4。修复建议：strength 改 3（→40/格、组血160）喵。

---

## 二、动画特效同步迁移（用户重点，子审查员核对）喵

### 2.1 钻机组（气动/激光/爆破）喵
**通过项**：三层/五层结构齐全（blast 含 rim）；blast rotateSpeed=6 用对、未误用他机；rim 脉冲 alpha=warmup×0.6×(0.7+|sin(2πt/3)|×0.3) 与原版 absin 完全一致、HEAT=#ff5512 对；全亮 0xF000F0+NO_OVERLAY 每顶点、共面 y 偏移、单一 RenderType 不交错；getRenderBoundingBox 均扩整组（2/3/4 格）；粒子频率对齐（pulverizeMedium→CRIT、mineBig→ITEM、pulverizeRed→Dust#ffa480、mineHuge→ITEM 8粒）；耗电停摆逻辑对喵。

**问题项**：
- **【中】激光钻 rotator 空转约 57×**：`LaserDrillBlockEntityRenderer.java:45/55` 用 `Axis.YP.rotation()`（弧度）画旋转，原版 spinSprite 的 timeDrilled×rotateSpeed 是**度数**（rotateSpeed=2°），激光钻头空转速度错误。修复：改 `rotationDegrees(timeDrilled×2)`（blast 已用对，参考 Blast:84）喵。
- **【中】激光钻缺 mine item 矿团层**：`LaserDrillBlockEntityRenderer.java:46-48` 未画原版 drawMineItem 的矿团 tint 层，且 `LaserDrillBlockEntity.java:77-79` 无 `getDominantItem()`，渲染器拿不到当前矿石。修复：加 getter + tint 层（参考 blast/pneumatic）喵。
- **【低】气动钻 rotator 用 `gameTime×0.3×warmup` 弧度**（`PneumaticDrillBlockEntityRenderer.java:53`），非原版 `timeDrilled×rotateSpeed(=2°)`；pneumatic BE 无 timeDrilled 同步。建议补 timeDrilled 并 `rotationDegrees(timeDrilled×2)`喵。
- **【低】两套矿色表不一致**：`PneumaticDrillBlockEntity.mineColor`（RAW_IRON=216,175,147）与 `BlastDrillBlockEntity.oreColor`（RAW_IRON=d8d7d8）同矿颜色不同，且均非原版 item.color。建议统一喵。
- **【低】blast rim 用 entityTranslucent 近似 additive**（暗色不增亮）；spinSprite 的 mod90°+alpha 淡入未还原喵。

### 2.2 生产组（硅冶炼/窑炉/塑钢/容器）喵
**通过项**：silicon 尾焰 #ffef99 正确、冒烟完整还原 smeltsmoke；kiln 火焰 #ffc099+白芯、G=0.3/R=0.06/cr 随机、/20 转秒正确；plastanium DrawFade 白线稿、alpha=absin(3,0.6)×warmup、无火焰；formsmoke #f1e479、plasticburn #e9ead3、0.04 概率；全亮+NO_OVERLAY+y 偏移；配方/耗电/停摆全部实现喵。

**问题项**：
- **【中】硅冶炼尾焰闪烁快 20×**：`SiliconSmelterBlockEntityRenderer.java:91-92` 火焰 absin 用 MC tick 直接算（周期 5/8 tick=0.25/0.4s），缺 `/20` 换算（kiln L64 已 /20）。修复：`t=(gameTime+partialTick)/20f`喵。
- **【中】硅冶炼渲染器缺 getRenderBoundingBox**：光晕/冒烟超出锚点 1×1×1 剔除边界，余光时特效被剔。修复：加 2×2+inflate AABB（其余三渲染器均有）喵。
- **【中】窑炉 warmup 衰减不同步（火焰不灭 bug，主审查发现）**：`KilnBlockEntity.java:121` 在 `warmup = producing?…:…` 之后 `if(!producing) return;` 提前返回，跳过末尾的 warmup 同步块（L134-139）→ 断电/缺料后服务端 warmup 衰减，客户端始终读到旧 warmup（≈1）→ **窑炉火焰在断料断电后不灭**。对比 SiliconSmelter（同步块在 producing 块外）无此问题。修复：把 warmup 同步移到 return 之前，或删去提前 return 喵。
- **【低】容器渲染器图标偏位**：`ContainerBlockEntityRenderer.java:42` 图标放 (1.5,1.5)，2×2 中心应为 (1.0,1.0)（锚点=最小角）喵。
- **【低】窑炉内圈白芯 alpha 用呼吸值 fa**（`KilnBlockEntityRenderer.java:106`），原版白芯 alpha=warmup 恒定（silicon L100 才对）喵。
- **【低】窑炉光晕 alpha 实 0.35，注释/原版 0.65，偏暗**（`KilnBlockEntityRenderer.java:111`）喵。
- **【低】塑钢压缩机耗油速率与 craftTime 换算不一致**：`PlastaniumCompressorBlockEntity.java:36` OIL_PER_TICK=0.0125（按 0.25/20tps），但 craftTime 60 帧→60 MC tick（1:1，实 3 秒）→ 每 craft 实际耗油 0.75，为原版 0.25 的 3×。整机相对原版慢 3× 且耗油 3×（油是占位物品，影响小，但数据不一致）喵。
- **【低】craft 库存按类型独立封顶 10**：原版 GenericCrafter itemCapacity=10 为共享上限，本实现各类型各存 10（硅/窑/塑钢均如此），既有惯例，记录喵。

### 2.3 雷光(fuse)特效重做 T31 喵
**通过项**：碎片长条（沿速度三角、apex 弹尖、宽 2.125×fout 收窄、白→a9d8ff、无火焰色、全亮+NO_OVERLAY+双面+y0.06）；命中白闪（活体 8 根白线 12tick、pierce 保留；建筑命中停住+stopped 防重复扣血）；炮口闪光（7 根白→a9d8ff、±50°、25·finpow、2→7 单位，逐项吻合 Fx.lightningShoot）；后坐 pow(top,1.8)×5/8（原版 recoil=5，修正正确）；热区（ab3400、alpha=heat）；线光/点光（每 2.5 格+暖黄 fbd367、全亮）；RenderType 不交错、全 setNormal、全 NO_OVERLAY；实体 updateInterval(1)+双构造 noPhysics+客户端 move；数据（damage66/range11.25/reload35/3发20°/rotateSpeed5、Pal a9d8ff/fbd367/ab3400）逐一吻合喵。

**问题项**：
- **【中】命中建筑后客户端弹丸继续滑行**：`FireBulletEntity.java:148/150` 服务端 `setDeltaMovement(ZERO)` 未同步客户端，客户端 tick 仍按出生速度 `move()` 滑行 12 tick≈21 格，白闪滑离命中点，违背「停在命中点播白闪」。修复：加 DATA_STOPPED 同步位（客户端停 move）喵。
- **【低】热区用 entityTranslucent 正常混叠压暗转盘**（`FuseBlockEntityRenderer.java:137-145`，暗色 ab3400 非 additive 增亮），需游戏内确认；过暗须自定义 additive RenderType 喵。
- **【低】炮口闪光固定种子**（`FuseBlockEntityRenderer.java:104`，blockPos hash），每轮闪光几何完全一致，缺随机爆开。建议混入轮次计数喵。
- **【低】线光半径偏细**：`FireBulletRenderer.java:91` billboard 半径≈1.7×fout 格，远小于原版 stroke≈5.3 格喵。
- **【信息】转盘中心与开火原点差 0.5 格**（`FuseBlockEntityRenderer.java:69` 与 `getCenter().add(1,0,1)` 之差，既有偏差非 T31 引入）喵。

---

## 三、挂载完整性（主审查逐行核对）喵

| 挂载点 | 内容 | 判定 |
|---|---|---|
| `Blockdustry.java` 构造器 | ContainerRegistrar/BridgeRegistrar/PneumaticDrillRegistrar/LaserDrillRegistrar/BlastDrillRegistrar/SiliconSmelterRegistrar/KilnRegistrar/PlastaniumCompressorRegistrar 8 行 register（L60-68） | ✓ |
| `BlockdustryBlocks.java` CRAFTING_TAB | 6 建筑 accept（L160-165） | ✓ |
| `BlockdustryBlocks.java` ITEMS_TAB | PlastaniumCompressorRegistrar.OIL（L261） | ✓ |
| `BlockdustryBlocks.java` registerBlockHealthDefaults | 7 行：pneumatic(2)/silicon(2)/kiln(2)/plastanium(2)/container(2)/laser(3)/blast(4)（L386-396） | ✓ |
| `BlockdustryClient.java` registerRenderers | 6 渲染器 + container + bridge + FIRE_BULLET（L74-83） | ✓ |
| `ResearchNodes.java` | pneumatic→graphite_press、**laser→pneumatic_drill**、blast→laser_drill、silicon/kiln/plastanium→graphite_press、container/bridge→router；parent 全集存在（L89-157） | ✓（laser 已纠偏挂 pneumatic，符合原版链 graphitePress→pneumatic→laser→blast） |
| `ResearchIcons.java` | nodeTexture 7 case 齐（L48-55）；itemTexture 有 titanium/plastanium | ✓ |
| lang zh_cn/en_us | 6 块 + oil + container + bridge 全（8 key） | ✓ |
| `BlockdustryItems.java` allMaterials | OIL（L50） | ✓ |

### 问题项（挂载维度）喵
- **【低】pneumatic 科技树图标未叠顶盖**：整合清单要求 `drawNodeIcon` 对 pneumatic_drill 也叠 `pneumatic_drill_top.png`，但 `ResearchIcons.java:108` 只特判 `"drill"`，pneumatic 只显示 base 图（pneumatic_drill_top.png 已拷入 research/blocks/ 未用）。纯图标美观，可后续补喵。
- **【低】pneumatic 放置不强制有矿**：`BlockdustryBuildingItem.java:43` 的「须放矿石上」只判 `DRILL`，pneumatic 是普通 BlockdustryBuildingItem（blast/laser 各自覆写了 place 预检）。与机械钻不一致，清单已注明喵。

---

## 四、已知风险点重点审查喵

1. **blast size4 绕过 Corner 方案**：`BlastDrillBlock.java` 覆写 getShape/getCollisionShape，用 BE anchor 反推 dx/dz 算整组 AABB，blockstate 9 corner 全指向空模型、视觉全由 BER 画。**基本可靠**：碰撞（anchor 计算）与渲染（BER）均正确，放置后 16 格 BE 的 anchor 都已设好喵。边缘瑕疵：无 BE anchor 的「放置瞬间」兜底用 corner（`cornerDxFb/DzFb` 只覆盖 0..2）对 dx=3/dz=3 格映射错，仅影响放置瞬时且随后被 BE anchor 覆盖，**低**喵。
2. **plastanium 石油占位供料**：**跑得通**。OIL 是 DeferredItem，进 ITEMS_TAB + allMaterials（ItemSource 可供给），BE `acceptsItem` 收油、`oilBuffer` 按 0.0125/tick 消耗、油尽停摆。但耗油量/速度换算偏 3×（见 §2.2）喵。
3. **科技树链 laser→pneumatic**：`ResearchNodes.java:122` laser parent 已改为 pneumatic_drill，**合理**，符合原版 graphitePress→pneumaticDrill→laserDrill→blastDrill 链喵。
4. **itemBridge 配对/缓冲**：配对（lastPlaced 静态 + onPlace 触发 + linkValid 同块/同队/直线/距离2..range）与缓冲传输（FIFO + speed74帧→24.67tick + timerAccept 4帧→1.33tick）实现正确喵。边缘：`lastPlaced` 静态跨维度未校验 `level` 相等（多维度放置可能串配），**低**喵。
5. **container 多物品存储**：`Map<Item,Integer>` + per-type 容量 300（separateItemCapacity 语义）、rotation 轮询卸货、NBT 持久化正确喵。偏差：原版容器不主动输出（靠 unloader 取），本实现 tick 主动 dumpItem 卸邻居，属无 unloader 下的既有设计取舍，**低/信息**喵。

---

## 五、编译状态喵

- **本批范围文件：0 编译错误**喵。6 个报错全在并发 T38 批次的文件：`entities/WallLightningEntity.java`（缺符号）、`production/IncineratorRegistrar.java:38/40`（缺符号）、`building/MenderBlockEntity.java:218`（`NbtUtils.readBlockPos` 参数错）、`building/WallBlockEntity.java:14` 与 `WallLightningEntity.java:50`（`包WallRegistrar不存在`）喵。
- 首轮 `./gradlew clean compileJava` 还遇到 Gradle 缓存写入失败（Windows 文件 mode，`Could not pack tree`），属环境问题；`--no-build-cache` 复跑暴露上述 6 个真实编译错误喵。
- **结论**：需 T38 批次修复其 6 个文件后，全量才 BUILD SUCCESSFUL；本批挂载所引用的注册类（8 Registrar/BE/Renderer）均已编译通过喵。
- 资源完整性：8 建筑 blockstate/models/item/textures 全存在、贴图像素级=原版（blast 128/laser 96/其余 64）、research 图标 10 张齐、全部 JSON 语法合法（0 bad）喵。

---

## 六、问题清单汇总（按严重度）喵

| 严重度 | 位置 | 问题 | 建议 |
|---|---|---|---|
| 中 | LaserDrillBlockEntityRenderer.java:45/55 | 弧度当度数，激光钻空转约 57× | 改 rotationDegrees(timeDrilled×2) |
| 中 | LaserDrillBlockEntityRenderer.java:46-48 + LaserDrillBlockEntity.java:77-79 | 缺 mine item 矿团层 + 无 getDominantItem | 加 getter + tint 层 |
| 中 | SiliconSmelterBlockEntityRenderer.java:91-92 | 尾焰 absin 缺 /20，闪烁快 20× | t=(gameTime+partialTick)/20f |
| 中 | SiliconSmelterBlockEntityRenderer.java | 缺 getRenderBoundingBox，特效余光被剔 | 加 2×2+inflate AABB |
| 中 | KilnBlockEntity.java:121 | `if(!producing) return;` 跳过 warmup 同步，断料/断电火焰不灭 | 同步块移到 return 前 |
| 中 | FireBulletEntity.java:148/150 | 命中建筑后客户端继续滑行，白闪滑离命中点 | 加 DATA_STOPPED 同步位 |
| 中 | BlastDrillRegistrar.java:40 | 组血 1120 ≠ 原版 768，注释/清单写 1280 数学错误 | strength→3.8 或改注释 |
| 中 | SiliconSmelterRegistrar.java:42 | 组血 200 ≠ 原版 160（套用 graphite_press strength4） | strength→3 |
| 低 | 多文件 | 两套矿色表不一致；pneumatic rotator 弧度非度数；blast rim 非真 additive；炮口闪光固定种子；线光偏细；窑炉白芯/光晕 alpha；容器图标偏位；plastanium 耗油 3×；craft 库存独立封顶；itemBridge 跨维度 lastPlaced | 见 §二 各行 |
| 信息 | 各清单 | container 主动卸货（无 unloader）；液体 boost 未实现；pneumatic 放置不强制有矿 | 已记录，后续单开 |

## 七、给主会话的 50 字结论喵
本批 8 建筑+雷光数据不串、挂载全齐、本批编译零错喵。可测但建议先修：激光钻空转、硅冶炼火焰快、窑炉火焰不灭、爆破/硅组血量、命中建筑白闪滑行喵。当前全量编译被并发 T38 的 6 文件阻塞，与本文无关喵。
