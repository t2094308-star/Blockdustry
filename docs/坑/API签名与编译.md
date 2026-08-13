# 坑：API 签名与编译/构建喵

> 主题：NeoForge/MC API 签名变化、编译错误、Gradle 构建缓存坑喵

## 1. SavedData.Factory 签名（1.21.1 变化）喵

- 1.21.1 的 `SavedData.Factory` 构造器签名是 `Factory(Supplier<T>, BiFunction<CompoundTag, HolderLookup.Provider, T>)`——**第一个参数是无参构造器、第二个是反序列化器**，且反序列化器必须接收 `(CompoundTag, HolderLookup.Provider)` 喵。
- **坑**：按旧版（1.20.x）写 `Function<CompoundTag, T>` 或参数顺序颠倒（`load` 在前、`new` 在后）都会报「方法引用无效：需要 CompoundTag 找到 没有参数」编译错误喵。
- 正确写法：
  ```java
  new SavedData.Factory<MyData>(MyData::new, MyData::load) // load(CompoundTag, HolderLookup.Provider) 喵
  ```
- `save`/`load` 签名也要带 `HolderLookup.Provider`：`save(CompoundTag, HolderLookup.Provider)`、`load(CompoundTag, HolderLookup.Provider)` 喵。
- 反例：`docs/子agent/T16_科技树深入实现.md` 里的旧写法 `Factory<>(load, new)` 是错的，勿照抄喵。

## 2. 全量重编译才会暴露既有错误（UP-TO-DATE 缓存掩盖）喵

- `compileJava` 在无改动时显示 `UP-TO-DATE`，不会重新编译；若仓库里存在一个「曾编译通过、后来被改坏」的文件，增量编译不会发现，构建照样成功喵。
- **坑**：改动任意一个 Java 文件触发重编译时，会一次性暴露所有既有错误（可能与本任务无关），导致构建失败喵。
- 处置：修好既有错误再继续；别误以为是自己的改动导致喵。

## 3. `--rerun-tasks` 会被运行中的游戏占文件锁喵

- 若 Minecraft 客户端（runClient）正在运行，`./gradlew --rerun-tasks` 强制重跑所有任务会尝试重新下载/选择 `client-extra.jar`，报「另一个程序正在使用此文件，进程无法访问」喵。
- 处置：先关游戏再 `--rerun-tasks`；或不用它，只跑 `./gradlew compileJava processResources`（正常增量即可，资源改动会自动同步）喵。

## 4. `BlockEvent.EntityPlaceEvent.getLevel()` 返回 LevelReader 喵

- NeoForge 1.21.1 的 `BlockEvent.EntityPlaceEvent.getLevel()` 返回 `LevelReader`，**没有 `isClientSide()`**（那是 Level 的方法）喵。
- 坑：直接写 `event.getLevel().isClientSide()` 编译报错「找不到符号」喵。
- 正确写法：`if (!(event.getLevel() instanceof ServerLevel level)) return;`，后面用 `level` 做服务端逻辑喵。
- 参考：`ResearchGateHandler.onPlace`（docs/子agent/T18_科技树实现A.md）喵。

## 5. `BuiltInRegistries.X.getValue(RL)` 不存在（1.21.1 改名 get）喵

- 1.21.1 的 `Registry` 接口方法名是 `get(ResourceLocation)`（`@Nullable T get(RL)`），**没有 `getValue`** 喵。
- 坑：写 `BuiltInRegistries.ITEM.getValue(rl)` 报「找不到符号：方法 getValue(ResourceLocation)」喵。
- 正确写法：`Item item = BuiltInRegistries.ITEM.get(rl); if (item != null && item != Items.AIR) ...`（返回可能 null，需判空）喵。

## 6. `Screen.mouseScrolled` 是 4 参数（1.21.1）喵

- 1.20.5+ `Screen.mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)`，**横向/纵向两个 amount**，不是旧版 3 参数喵。
- 坑：覆写 3 参数版本报「方法不会覆盖或实现超类型的方法」喵。
- 正确写法：`@Override public boolean mouseScrolled(double mx, double my, double horiz, double vert)`，滚动量用 `vert` 喵。

## 7. `GuiGraphics.renderTooltip` 要 `FormattedCharSequence` 列表（1.21.1）喵

- 1.21.1 的 `GuiGraphics.renderTooltip(Font, List<? extends FormattedCharSequence>, int, int)`，**参数是 `FormattedCharSequence` 不是 `Component`** 喵。
- 坑：传 `List<Component>` 报「找不到合适的方法」喵。
- 正确写法：`List<FormattedCharSequence> lines = new ArrayList<>(); lines.add(Component.literal(...).getVisualOrderText()); g.renderTooltip(font, lines, x, y);` 喵。

## 8. `Entity.playSound` 是 3 参数（无 SoundSource，1.21.1）喵

- 1.21.1 `Entity/Player.playSound(SoundEvent, float volume, float pitch)`，**没有 SoundSource 参数**（那是 `Level.playSound(...)` 才有的）喵。
- 坑：写 `player.playSound(sound, SoundSource.PLAYERS, 0.6f, 1.4f)` 报「找不到合适的方法」喵。
- 正确写法：`player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.6f, 1.4f);` 喵。

## 9. `GuiGraphics.pose()` 返回 PoseStack（pushPose/popPose/scale 3 参，1.21.1）喵

- 1.21.1 `GuiGraphics.pose()` 返回 `PoseStack`（**不是** Matrix3x2Stack）喵。
- 坑 1：`pose().push()/pop()` 报「找不到符号」——正确是 `pushPose()/popPose()` 喵。
- 坑 2：`pose().scale(s, s)` 报「需要 float,float,float」——正确是 `scale(s, s, 1f)` 喵。
- 放大物品图标（大图预览）写法：
  ```java
  g.pose().pushPose();
  g.pose().scale(s, s, 1f);                 // s = 目标尺寸/16f 喵
  g.renderItem(stack, (int)(x / s), (int)(y / s)); // 用除 s 的坐标抵消缩放，画在屏幕 (x,y) 喵
  g.pose().popPose();
  ```
