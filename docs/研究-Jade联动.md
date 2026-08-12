# 研究：方块工业 (blockdustry) 联动 Jade（玉）

## 一、结论速览

- Jade 的插件 API 位于 `snownee.jade.api` 包，核心类：`IWailaPlugin`、`WailaPlugin`（注解）、`IBlockComponentProvider`、`IEntityComponentProvider`、`IServerDataProvider`、`StreamServerDataProvider`、`ITooltip`、`BlockAccessor`、`EntityAccessor`、`IWailaClientRegistration`、`IWailaCommonRegistration`、`IPluginConfig`、`JadeIds`喵
- 最小联动方案：写一个 `IServerDataProvider<BlockAccessor>` 在服务端把方块血量 hp/maxHp 写入同步 NBT，再写一个 `IBlockComponentProvider` 在客户端把 hp/maxHp 追加到 tooltip，用 `@WailaPlugin` 注解插件类并实现 `IWailaPlugin` 完成注册喵
- 血量的数据源是前置库 BlockHealth（modid `blockhealth`），API 为 `BlockHealthApi.getHp(level, pos)` / `getMaxHp(level, pos)`，血量存在服务端 ServerLevel 附件 `block_hp`（`Map<BlockPos, Float>`），客户端读不到，所以必须走 Jade 的服务端数据同步喵
- 依赖推荐 Modrinth Maven：`implementation "maven.modrinth:jade:15.10.1+neoforge"`（对应 NeoForge 1.21.1 的 Jade 版本）喵

## 二、平台差异警告（必须先看）

- 本地 `D:\Blockdustry\Jade` 工程当前检出在 `26.3-fabric` 分支，是为 Fabric 加载器 + 极新 MC（26.x）准备的，其源码里映射名是 `net.minecraft.resources.Identifier`（Yarn 风格新命名）喵
- 方块工业 `D:\Blockdustry\仓库` 是 NeoForge 1.21.1 工程，Jade 侧应对应 `origin/1.21-neoforge` 分支（本地 git 里存在该远程分支），映射名是 `net.minecraft.resources.ResourceLocation`（Mojang 官方映射）喵
- 因此：不能拿本地 26.3-fabric 源码直接编译方块工业；应通过 Maven 依赖官方发布的 1.21.1 NeoForge 版 Jade（`15.10.1+neoforge`），API 类的形状与包名与本地源码一致，只有 `Identifier`/`ResourceLocation` 等映射名不同喵

## 三、Jade 插件 API 关键类

以下均在 `snownee.jade.api` 包（本地 26.3-fabric 源码与 1.21-neoforge 分支结构一致）喵：

| 类/接口 | 作用 | 关键方法 |
|---|---|---|
| `IWailaPlugin` | 插件入口接口 | `register(IWailaCommonRegistration)`、`registerClient(IWailaClientRegistration)` |
| `@WailaPlugin` | 插件入口注解，`value()` 填「所需 modid」，空则始终加载 | `value() default ""` |
| `IBlockComponentProvider` | 方块 tooltip 客户端渲染（extends `IComponentProvider<BlockAccessor>`） | `appendTooltip(ITooltip, BlockAccessor, IPluginConfig)`、`getUid()` |
| `IEntityComponentProvider` | 实体 tooltip 客户端渲染 | 同上，参数为 `EntityAccessor` |
| `IServerDataProvider<T>` | 服务端同步数据到客户端 | `appendServerData(CompoundTag, T)`、`shouldRequestData(T)`、`getUid()` |
| `StreamServerDataProvider<T,D>` | IServerDataProvider 的流式便捷实现，配合 `StreamCodec` 自动编解码 | `streamData(T)`、`streamCodec()`、`decodeFromData(T)` |
| `ITooltip` | 可变的 tooltip 容器 | `add(Component)`、`append(Component)`、`add(int, LayoutElement)`、`replace(Identifier, ...)`、`get(Identifier)` |
| `BlockAccessor` | 方块目标上下文（extends `Accessor<BlockHitResult>`） | `getBlock()`、`getBlockState()`、`getBlockEntity()`、`getPosition()`、`getLevel()`、`getPlayer()`、`getServerData()`、`getHitResult()`、`showDetails()`、`getPickedResult()` |
| `EntityAccessor` | 实体目标上下文 | `getEntity()` 等 |
| `IWailaClientRegistration` | 客户端注册入口 | `registerBlockComponent(IComponentProvider<BlockAccessor>, Class<? extends Block>)`、`registerBlockIcon(...)`、`registerEntityComponent(...)`、`addConfig(ResourceLocation, ...)`、`addConfigListener(...)`、回调注册（`addRayTraceCallback` 等） |
| `IWailaCommonRegistration` | 服务端注册入口 | `registerBlockDataProvider(IServerDataProvider<BlockAccessor>, Class<?>)`、`registerEntityDataProvider(...)`、`registerItemStorage`、`registerFluidStorage`、`registerEnergyStorage`、`registerProgress` |
| `IPluginConfig` | 插件配置（客户端可开关每个 provider） | `get(Identifier)`、`getString(...)`、`getEnum(...)` 等 |
| `IJadeProvider` | provider 基接口 | `getUid()`（`ResourceLocation`）、`getDefaultPriority()` |
| `IToggleableProvider` | 可开关 provider 接口 | `enabledByDefault()`、`isRequired()` |
| `JadeIds` | 内置 provider/config 的 id 常量 | `JADE(String)`、`MC(String)`、`ACCESS(String)` |
| `Accessor<T>` | 所有 accessor 的基接口 | `getLevel()`、`getPlayer()`、`getServerData()`、`encodeAsNbt(...)`、`decodeFromNbt(...)`、`verifyData(...)` |
| `ui.JadeUI` / `ui.Element` | UI 元素（图标、文本、进度条） | `JadeUI.text(Component)`、`JadeUI.smallItem(ItemStack)`、`JadeUI.progress(...)` |

## 四、插件加载机制（两种加载器）

- Fabric：在 `fabric.mod.json` 的 `entrypoints` 里加 `"jade": ["全类名.ExamplePlugin"]`，Jade 通过 `FabricLoader.getEntrypointContainers("jade", IWailaPlugin.class)` 发现插件喵
- NeoForge（1.21.1，本工程用的）：Jade 在 `FMLLoadCompleteEvent` 里扫描所有 mod 的 `ModFileScanData`，找带 `@WailaPlugin` 注解且 `value()`（所需 modid）已加载的类，反射实例化并调用 `register`/`registerClient`喵
  - 因此方块工业的插件类只要放在 blockdustry 自己的 jar 里并加 `@WailaPlugin` 就会被发现，无需注册任何 entrypoint 喵
  - 若要「Jade 在时插件才生效」可不写 value；`@WailaPlugin("blockdustry")` 这种写法是给独立附加 mod 用的（要求目标 mod 已加载）喵

## 五、依赖方式

推荐 Modrinth Maven（官方文档推荐），在 `build.gradle` 增加喵：

```groovy
repositories {
    maven { url = "https://api.modrinth.com/maven" }
}

dependencies {
    // 1.21.1 NeoForge 版 Jade；版本号去 https://modrinth.com/mod/jade/versions 查最新喵
    implementation "maven.modrinth:jade:15.10.1+neoforge"
}
```

说明喵：

- `implementation` 可同时满足编译与开发环境运行（NeoForge 不会自动把依赖塞进产物 jar，Jade 在正式环境由玩家自行安装），与官方文档一致喵
- 若不想让 Jade 出现在发布产物的依赖信息里，可改用 `compileOnly "maven.modrinth:jade:15.10.1+neoforge"`，但这样 runClient 开发环境没有 Jade，需再加 `localRuntime "maven.modrinth:jade:15.10.1+neoforge"`（本工程 build.gradle 已声明 `localRuntime` 配置并挂到 runtimeClasspath）喵
- Curse Maven 备选：`compileOnly "curse.maven:jade-324717:<fileId>"`，需加 `maven { url = "https://www.cursemaven.com" }`，fileId 去 curseforge 页面查对应 1.21.1 NeoForge 文件的 id 喵

## 六、最小实现方案（显示方块当前血量 hp/maxHp）

数据流喵：

1. 服务端：`IServerDataProvider.appendServerData` 读 `BlockHealthApi`，把 hp/maxHp 写进同步 `CompoundTag` 喵
2. Jade 把 tag 以 ~250ms 间隔同步给客户端喵
3. 客户端：`IBlockComponentProvider.appendTooltip` 从 `accessor.getServerData()` 读出 hp/maxHp，`tooltip.add(...)` 显示喵

### 6.1 build.gradle 追加

```groovy
repositories {
    maven { url = "https://api.modrinth.com/maven" }
}
dependencies {
    implementation "maven.modrinth:jade:15.10.1+neoforge"
}
```

### 6.2 neoforge.mods.toml 追加可选依赖（Jade 只需客户端）

```toml
# Jade（玉）可选依赖喵；没装时方块工业照常运行，只是不显示血量喵
[[dependencies.blockdustry]]
    modId="jade"
    type="optional"
    versionRange="[15.0.0,)"
    ordering="AFTER"
    side="CLIENT"
```

### 6.3 插件入口 BlockdustryJadePlugin.java

包路径建议 `com.blockdustry.jade`（新增目录）喵：

```java
package com.blockdustry.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

// Jade 扫描本 mod 的 jar 发现此注解，自动加载插件喵
@WailaPlugin
public class BlockdustryJadePlugin implements IWailaPlugin {
    // 本 provider 的全局唯一 id，也是 config 开关的 key 喵
    public static final ResourceLocation UID_BLOCK_HP =
            ResourceLocation.fromNamespaceAndPath("blockdustry", "block_hp");

    @Override
    public void register(IWailaCommonRegistration registration) {
        // 对任意方块都尝试同步血量；若只想覆盖方块工业的建筑，可改成 BlockdustryBuildingBlock.class 喵
        registration.registerBlockDataProvider(BlockHpServerDataProvider.INSTANCE, Block.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(BlockHpComponentProvider.INSTANCE, Block.class);
    }
}
```

### 6.4 服务端数据提供者 BlockHpServerDataProvider.java

```java
package com.blockdustry.jade;

import com.blockdustry.lib.BlockHealthApi;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class BlockHpServerDataProvider implements IServerDataProvider<BlockAccessor> {
    public static final BlockHpServerDataProvider INSTANCE = new BlockHpServerDataProvider();
    public static final String KEY_HP = "blockdustry_hp";
    public static final String KEY_MAX_HP = "blockdustry_max_hp";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        // 此方法只在逻辑服务端被调用，accessor.getLevel() 是 ServerLevel，能读到 blockhealth 附件喵
        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        float max = BlockHealthApi.getMaxHp(level, pos);
        if (max <= 0f) {
            return; // 免疫方块（基岩等）不显示血量喵
        }
        float hp = BlockHealthApi.getHp(level, pos);
        data.putFloat(KEY_HP, hp);
        data.putFloat(KEY_MAX_HP, max);
    }

    @Override
    public ResourceLocation getUid() {
        return BlockdustryJadePlugin.UID_BLOCK_HP;
    }
}
```

### 6.5 客户端渲染提供者 BlockHpComponentProvider.java

```java
package com.blockdustry.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class BlockHpComponentProvider implements IBlockComponentProvider {
    public static final BlockHpComponentProvider INSTANCE = new BlockHpComponentProvider();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data == null || !data.contains(BlockHpServerDataProvider.KEY_MAX_HP)) {
            return; // 服务端没同步（未装 Jade 或免疫方块），跳过喵
        }
        int hp = data.getInt(BlockHpServerDataProvider.KEY_HP);
        int max = data.getInt(BlockHpServerDataProvider.KEY_MAX_HP);
        tooltip.add(Component.translatable("blockdustry.jade.block_hp", hp, max));
    }

    @Override
    public ResourceLocation getUid() {
        return BlockdustryJadePlugin.UID_BLOCK_HP;
    }
}
```

### 6.6 语言文件追加（config 开关翻译 + 血量行翻译）

`assets/blockdustry/lang/zh_cn.json` 追加喵：

```json
{
  "config.jade.plugin_blockdustry.block_hp": "方块工业：显示方块血量",
  "blockdustry.jade.block_hp": "血量: %s / %s"
}
```

`assets/blockdustry/lang/en_us.json` 追加喵：

```json
{
  "config.jade.plugin_blockdustry.block_hp": "Blockdustry: Block HP",
  "blockdustry.jade.block_hp": "HP: %s / %s"
}
```

说明喵：Jade 会自动为每个 `registerBlockComponent` 的 provider 生成一个 config 开关，其翻译键格式为 `config.jade.plugin_<命名空间>.<路径>`，所以 `config.jade.plugin_blockdustry.block_hp` 对应 UID `blockdustry:block_hp` 喵

## 七、注意事项

- 血量在服务端（ServerLevel 附件），客户端读不到 `BlockHealthApi`；要让「客户端没装 Jade、服务端装了」也能显示需要另做自定义网络包，不在本次最小范围内喵
- 本方案依赖服务端也有 Jade（或单机存档，integrated server 即同一进程）：`IServerDataProvider` 只在服务端执行，服务端没 Jade 时 `accessor.getServerData()` 为空，客户端 provider 直接 return，不报错喵
- 优先级：默认 `getDefaultPriority()` 为 BODY（约 0 位置），如果想让血量行靠前/靠后可覆写，`值越小越靠前`，`>5000` 的元素在 Lite 模式下不折叠喵
- 如果想做成进度条（血量条），用 `snownee.jade.api.view.ProgressView` + `registerProgress`/`registerProgressClient`，或用 `JadeUI.progress(...)` 直接画条，属可选的增强喵
- 数据同步频率约 250ms，血量连续变化时显示略有延迟，属 Jade 的节流机制，正常现象喵
- 若担心 `registerBlockDataProvider(..., Block.class)` 对所有方块都触发数据请求偏多，可改为注册 `BlockdustryBuildingBlock.class`（方块工业建筑基类，`D:\Blockdustry\仓库\src\main\java\com\blockdustry\building\BlockdustryBuildingBlock.java`）只覆盖自家方块喵

## 八、参考资料

- Jade 官方插件开发文档（getting-started）：本仓库 `D:\Blockdustry\Jade` 的 `origin/docs-1.X` 分支 `docs/plugins22/getting-started.md` 喵
- 本地 Jade 源码（Fabric 26.3）：`D:\Blockdustry\Jade\src\main\java\snownee\jade\api\*.java`，示例插件 `snownee\jade\test\ExamplePlugin.java`、`ExampleDataProvider.java`、`ExampleComponentProvider.java` 喵
- NeoForge 版插件加载逻辑：`D:\Blockdustry\Jade` 的 `origin/1.21-neoforge` 分支 `src/main/java/snownee/jade/util/CommonProxy.java`（`loadComplete` 方法，`ModFileScanData` 扫描 `@WailaPlugin`）喵
- 前置库 BlockHealth：`D:\Blockdustry\子仓库(方块血量)\src\main\java\com\blockdustry\lib\BlockHealthApi.java` 喵
- 在线资料：https://jademc.readthedocs.io/en/latest/ ，Modrinth Jade https://modrinth.com/mod/jade ，CurseForge https://www.curseforge.com/minecraft/mc-mods/jade 喵
