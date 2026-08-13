package com.blockdustry.research;

import java.util.List;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.building.ElevatorBlocks;
import com.blockdustry.building.FuseArcRegistrar;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

// 科技树静态定义（从零重做）：忠实复刻 Mindustry SerpuloTechTree 子树，数据全按原版喵
// - 树结构：coreShard 为根；conveyor→router、mechanicalDrill→(graphitePress / combustionGenerator→powerNode→battery)、
//   duo→(scatter→fuse / arc)、groundFactory、sandbox 源、自定义 elevator（无 Mindustry 等价，取小成本）喵
// - 建造配方/研究成本覆盖/成本倍率全部取 Mindustry Blocks.java 原版数值（钍→硅替代，Blockdustry 无钍）喵
// - 研究需求由 ResearchTree 按 Mindustry 公式从 buildRequirements 自动计算；有 researchCost 覆盖的用覆盖值喵
public final class ResearchNodes {
    private ResearchNodes() {}

    public static List<ResearchNode> all() {
        // 材料映射：Mindustry 铜→MC 铜锭；铅/石墨/硅→Blockdustry 自研材料；fuse 钍→硅替代（Blockdustry 无钍）喵
        Item copper = Items.COPPER_INGOT;
        Item lead = BlockdustryBlocks.LEAD.get();
        Item graphite = BlockdustryBlocks.GRAPHITE.get();
        Item silicon = BlockdustryBlocks.SILICON.get();

        return List.of(
                // —— 根：coreShard（Mindustry alwaysUnlocked=true）喵 ——
                ResearchNode.builder("core")
                        .unlockBlock(BlockdustryBlocks.CORE.get())
                        .defaultUnlocked(true)
                        .buildRequirement(copper, 1000)   // Mindustry coreShard = 铜×1000 铅×800 喵
                        .buildRequirement(lead, 800)
                        .build(),

                // —— 物流：conveyor→router（原版 conveyor→junction→router，无 junction 直接挂 conveyor 下）喵 ——
                ResearchNode.builder("conveyor")
                        .parent("core")
                        .unlockBlock(BlockdustryBlocks.CONVEYOR.get())
                        .researchCost(copper, 5)          // Mindustry conveyor.researchCost = 铜×5 喵
                        .buildRequirement(copper, 1)      // Mindustry conveyor = 铜×1 喵
                        .build(),
                ResearchNode.builder("router")
                        .parent("conveyor")
                        .unlockBlock(BlockdustryBlocks.ROUTER.get())
                        .buildRequirement(copper, 3)      // Mindustry router = 铜×3 喵
                        .build(),

                // —— 生产：mechanicalDrill 下挂 graphitePress 与 combustionGenerator 喵 ——
                ResearchNode.builder("drill")
                        .parent("core")
                        .unlockBlock(BlockdustryBlocks.DRILL.get())
                        .researchCost(copper, 10)         // Mindustry mechanical-drill.researchCost = 铜×10 喵
                        .buildRequirement(copper, 12)     // Mindustry mechanical-drill = 铜×12 喵
                        .build(),
                ResearchNode.builder("graphite_press")
                        .parent("drill")
                        .unlockBlock(BlockdustryBlocks.GRAPHITE_PRESS.get())
                        .buildRequirement(copper, 75)     // Mindustry graphite-press = 铜×75 铅×30 喵
                        .buildRequirement(lead, 30)
                        .build(),
                ResearchNode.builder("combustion_generator")
                        .parent("drill")
                        .unlockBlock(BlockdustryBlocks.COMBUSTION_GENERATOR.get())
                        .buildRequirement(copper, 25)     // Mindustry combustion-generator = 铜×25 铅×15 喵
                        .buildRequirement(lead, 15)
                        .build(),

                // —— 电力：combustionGenerator→powerNode→battery 喵 ——
                ResearchNode.builder("power_node")
                        .parent("combustion_generator")
                        .unlockBlock(BlockdustryBlocks.POWER_NODE.get())
                        .buildRequirement(copper, 2)      // Mindustry power-node = 铜×2 铅×6 喵
                        .buildRequirement(lead, 6)
                        .build(),
                ResearchNode.builder("battery")
                        .parent("power_node")
                        .unlockBlock(BlockdustryBlocks.BATTERY.get())
                        .buildRequirement(copper, 5)      // Mindustry battery = 铜×5 铅×20 喵
                        .buildRequirement(lead, 20)
                        .build(),

                // —— 炮塔：duo 下挂 scatter→fuse 与 arc（fuse 原版父链 hail→salvo→ripple 均无，直接挂 scatter）喵 ——
                ResearchNode.builder("turret")
                        .parent("core")
                        .unlockBlock(BlockdustryBlocks.TURRET.get())
                        .buildRequirement(copper, 35)     // Mindustry duo = 铜×35 喵
                        .build(),
                ResearchNode.builder("scatter")
                        .parent("turret")
                        .unlockBlock(BlockdustryBlocks.SCATTER.get())
                        .buildRequirement(copper, 85)     // Mindustry scatter = 铜×85 铅×45 喵
                        .buildRequirement(lead, 45)
                        .build(),
                ResearchNode.builder("fuse")
                        .parent("scatter")
                        .unlockBlock(FuseArcRegistrar.FUSE.get())
                        .buildRequirement(copper, 225)    // Mindustry fuse = 铜×225 石墨×225 钍×100 喵
                        .buildRequirement(graphite, 225)
                        .buildRequirement(silicon, 100)   // 钍→硅替代（Blockdustry 无钍）喵
                        .build(),
                ResearchNode.builder("arc")
                        .parent("turret")
                        .unlockBlock(FuseArcRegistrar.ARC.get())
                        .buildRequirement(copper, 50)     // Mindustry arc = 铜×50 铅×50 喵
                        .buildRequirement(lead, 50)
                        .build(),

                // —— 单位工厂：groundFactory（researchCostMultiplier=0.5）喵 ——
                ResearchNode.builder("unit_factory")
                        .parent("core")
                        .unlockBlock(BlockdustryBlocks.UNIT_FACTORY.get())
                        .costMultiplier(0.5f)             // Mindustry ground-factory researchCostMultiplier = 0.5 喵
                        .buildRequirement(copper, 50)     // Mindustry ground-factory = 铜×50 铅×120 硅×80 喵
                        .buildRequirement(lead, 120)
                        .buildRequirement(silicon, 80)
                        .build(),

                // —— 沙盒调试源（Mindustry sandbox alwaysUnlocked）喵 ——
                ResearchNode.builder("power_source")
                        .parent("core")
                        .unlockBlock(BlockdustryBlocks.POWER_SOURCE.get())
                        .defaultUnlocked(true)
                        .build(),
                ResearchNode.builder("item_source")
                        .parent("core")
                        .unlockBlock(BlockdustryBlocks.ITEM_SOURCE.get())
                        .defaultUnlocked(true)
                        .build(),

                // —— 自定义提升机（无 Mindustry 等价，取小配方）喵 ——
                ResearchNode.builder("elevator")
                        .parent("core")
                        .unlockBlock(ElevatorBlocks.ELEVATOR.get())
                        .buildRequirement(copper, 3)
                        .build()
        );
    }
}
