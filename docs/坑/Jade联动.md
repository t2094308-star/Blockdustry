# 坑：Jade 联动 / 进度条 / 各类条 喵

## 1. ProgressView.read 出来的 style 永不为 null 喵
- `ProgressView.read()` 内部固定 `new SlimProgressStyle()`（color 默认 0=透明黑），`pv.style` 永不为 null → 客户端 `if (pv.style == null)` 永不执行，配色失效，条全黑灰喵
- 修复：**无条件** `pv.style = new SimpleProgressStyle().color(color, color)`，按 ViewGroup.id 配色（血 #ff341c / 电 #ec7b4c / 进度 #ff8947），color2 同色避免横纹喵

## 2. 各类条颜色（忠于 Mindustry）喵
- 血量条纯红 `#ff341c`（无红绿渐变）、电量条 `Pal.powerBar` 橙 `#ec7b4c`（非蓝）、进度条 `Pal.ammo` 橙 `#ff8947`、物品条绿 `#2ea756`；背景深灰 `#1a1a1a`、白字、左→右纯色喵

## 3. 进度条注册机制 喵
- 服务端 `registerProgress(IServerExtensionProvider<CompoundTag>, Class)`，`getGroups` 返回 `List<ViewGroup<CompoundTag>>`（`ProgressView.create(progress)` 打包）；客户端 `registerProgressClient(IClientExtensionProvider<CompoundTag,ProgressView>)`（**单参，无 Class**），`getClientGroups` 读 `ProgressView.read` 喵
- 多格建筑读**锚点格**数据（进度/库存只在锚点格跑，否则从格显示 0）喵

## 4. 血量/数据同步 喵
- 血量在服务端（ServerLevel attachment），客户端读不到；Jade 用 IServerDataProvider 服务端写入同步 NBT + 客户端 IBlockComponentProvider 读喵
- `registerBlockComponent`/`registerBlockDataProvider` 的第二个参数是 `Class<?>`（Block.class 覆盖所有）喵
