# 研究：Jade 各类条全显示黑灰色（颜色没生效）修复喵

## 现象

Jade tooltip 里的进度条（血量/电量/制作进度）全部显示成黑灰色（背景色 `#1a1a1a` 或默认色）喵。
客户端 `ProgressClientProvider` 里明明写了 `SimpleProgressStyle().color(color, color)`，但颜色就是不生效喵。

## 根因（用 javap 反编译 Jade jar 确认）

Jade jar 路径：`D:/Blockdustry/Jade-1.21.1-NeoForge-15.10.6 (1).jar` 喵。

### 根因一：`ProgressView.read()` 返回的 style 永远非 null，`if (pv.style == null)` 永不执行

`ProgressView` 的字节码如下喵：

```
public class snownee.jade.api.view.ProgressView {
  public snownee.jade.api.ui.ProgressStyle style;
  public float progress;
  public net.minecraft.network.chat.Component text;

  public static ProgressView read(CompoundTag tag) {
    ProgressView v = new ProgressView(new SlimProgressStyle());
    v.progress = tag.getFloat("Progress");
    return v;
  }
  public static CompoundTag create(float progress) {
    CompoundTag tag = new CompoundTag();
    tag.putFloat("Progress", progress);
    return tag;
  }
}
```

关键点：`read()` 内部固定 `new SlimProgressStyle()` 塞进 `style`，**style 永远不是 null** 喵。
所以客户端原代码：

```java
ProgressView pv = ProgressView.read(nbt);
if (pv.style == null) {                       // 永远为 false，永不执行！
    pv.style = new SimpleProgressStyle().color(color, color);
}
```

这个 `if` 守卫永远不会成立，颜色赋值从未发生，条用的就是 read 出来的默认样式喵。

### 根因二：默认样式是 `SlimProgressStyle`，color 字段默认 0（透明黑）

`SlimProgressStyle` 字节码：只有一个 `public int color;`，构造时 color 保持 int 默认值 `0` 喵。
它的 `render()` 直接 `DisplayHelper.drawGradientProgress(..., this.color)`，color=0 就是透明黑 → 显示成黑灰喵。
而且 `SlimProgressStyle.color(int,int)` 在 color != color2 时会抛 `UnsupportedOperationException`，也不适合改它喵。

### 为什么服务端 create 不带样式

`ProgressView.create(float)` 只写 `"Progress"` 一个 float，不序列化 style 喵。
服务端 `ViewGroup<CompoundTag>` 只能传 NBT，样式只能在客户端 read 之后再设喵。

### 顺带确认的三点

1. `SimpleProgressStyle.color(int,int)` 同时设置 `color` 与 `color2` 字段喵；render 用 `color` 画已填充段，用 `color2` 画尾部未填段；`color == color2` 时就是纯色实心填充（无横纹），正合 Mindustry 纯色条需求喵。
2. 颜色值 `0xFFff341c` / `0xFFec7b4c` / `0xFFff8947` 是标准 ARGB（高 8 位 alpha=0xFF 不透明），Jade `Color.rgb(int)` 按 `(a,r,g,b)` 解析，正确喵。
3. progress 数值：`ProgressView.create(float)` 写入、`read()` 读回 `progress`，流程无误喵；各来源（`hp/maxHp`、`getPowerStatus()`、`getCraftProgress()`）注释与 PowerGrid 钳制均保证 0..1 喵。

## 修复内容

### 1. `src/main/java/com/blockdustry/jade/ProgressClientProvider.java`

把 `if (pv.style == null)` 改成无条件覆盖样式喵：

```java
ProgressView pv = ProgressView.read(nbt);
// ProgressView.read() 内部固定 new SlimProgressStyle()（color 默认 0=透明黑，条呈黑灰），
// style 永不为 null，之前的 if (pv.style==null) 永不执行导致配色失效；必须无条件覆盖喵
pv.style = new SimpleProgressStyle().color(color, color);
```

`pv.style` 是 public 字段可直接赋值，`color(int,int)` 返回基类 `ProgressStyle`，类型匹配喵。
每条按 ViewGroup.id 命中对应颜色：hp `0xFFff341c`（血红）、power `0xFFec7b4c`（Pal.powerBar 橙）、craft `0xFFff8947`（Pal.ammo 橙）喵。

### 2. 删除血量数字文本行（任务二）

- 删除 `BlockHpComponentProvider.java`（数字「血量: hp / max」文本行提供者）喵。
- 删除 `BlockHpServerDataProvider.java`（血量进度条由 `ProgressServerProvider` 直接读 `BlockHealthApi`，不再需要它同步 hp/maxHp NBT）喵。
- `BlockdustryJadePlugin.java`：移除 `UID_BLOCK_HP` 常量、移除 `register` 里的 `registerBlockDataProvider(BlockHpServerDataProvider.INSTANCE, ...)`、移除 `registerClient` 里的 `registerBlockComponent(BlockHpComponentProvider.INSTANCE, ...)` 喵。

全仓库 grep 确认已无任何对 `BlockHpComponentProvider` / `BlockHpServerDataProvider` / `UID_BLOCK_HP` 的引用（lang 文件里 `blockdustry.jade.block_hp` 翻译 key 保留无碍，属于约束范围外）喵。

## 验证

`./gradlew compileJava --offline` 通过（EXIT=0），保持可编译喵。

## 结论

根因是「`ProgressView.read()` 永远返回非 null 的 `SlimProgressStyle`（默认黑），客户端用 `style==null` 判空导致配色从不执行」喵。
修复为客户端无条件覆盖为 `SimpleProgressStyle().color(color,color)`，同时删掉多余的血量数字文本行及其服务端数据提供者喵。
