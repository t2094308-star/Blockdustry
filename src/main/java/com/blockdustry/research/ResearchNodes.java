package com.blockdustry.research;

import java.util.List;

import com.blockdustry.building.BlockdustryBlocks;
import com.blockdustry.building.ElevatorBlocks;
import com.blockdustry.building.FuseArcRegistrar;
import com.blockdustry.distribution.GateRegistrar;
import com.blockdustry.distribution.JunctionRegistrar;
import com.blockdustry.distribution.SorterRegistrar;

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
                // —— 批1A 物流扩展：junction/distributor（conveyor 下）、sorter/gate（router 下）——
                ResearchNode.builder("junction")
                        .parent("conveyor")
                        .unlockBlock(JunctionRegistrar.JUNCTION.get())
                        .buildRequirement(copper, 3)      // Mindustry junction = 铜×3 喵
                        .build(),
                ResearchNode.builder("distributor")
                        .parent("conveyor")
                        .unlockBlock(JunctionRegistrar.DISTRIBUTOR.get())
                        .buildRequirement(copper, 4)      // Mindustry distributor = 铜×4 铅×4 喵
                        .buildRequirement(lead, 4)
                        .build(),
                ResearchNode.builder("sorter")
                        .parent("router")
                        .unlockBlock(SorterRegistrar.SORTER.get())
                        .buildRequirement(lead, 2)        // Mindustry sorter = 铅×2 铜×2 喵
                        .buildRequirement(copper, 2)
                        .build(),
                ResearchNode.builder("inverted_sorter")
                        .parent("sorter")
                        .unlockBlock(SorterRegistrar.INVERTED_SORTER.get())
                        .buildRequirement(lead, 2)        // Mindustry inverted-sorter = 铅×2 铜×2 喵
                        .buildRequirement(copper, 2)
                        .build(),
                ResearchNode.builder("overflow_gate")
                        .parent("router")
                        .unlockBlock(GateRegistrar.OVERFLOW_GATE.get())
                        .buildRequirement(copper, 4)      // Mindustry overflow-gate = 铅×2 铜×4 喵
                        .buildRequirement(lead, 2)
                        .build(),
                ResearchNode.builder("underflow_gate")
                        .parent("router")
                        .unlockBlock(GateRegistrar.UNDERFLOW_GATE.get())
                        .buildRequirement(copper, 4)      // Mindustry underflow-gate = 铅×2 铜×4 喵
                        .buildRequirement(lead, 2)
                        .build(),

                // —— 批1B 存储+桥梁：container、bridge_conveyor（router 下）喵 ——
                ResearchNode.builder("container")
                        .parent("router")                 // Mindustry SerpuloTechTree: router → container 喵
                        .unlockBlock(com.blockdustry.storage.ContainerRegistrar.CONTAINER.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 100) // Mindustry container = 钛×100 喵
                        .build(),
                ResearchNode.builder("bridge_conveyor")
                        .parent("router")                 // Mindustry SerpuloTechTree: router → itemBridge 喵
                        .unlockBlock(com.blockdustry.distribution.BridgeRegistrar.BRIDGE.get())
                        .buildRequirement(lead, 6)        // Mindustry itemBridge = 铅×6 铜×6 喵
                        .buildRequirement(copper, 6)
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
                // —— 批1C/1D：钻机链 graphitePress→pneumatic→laser→blast；生产 siliconSmelter/kiln/plastaniumCompressor 喵 ——
                ResearchNode.builder("pneumatic_drill")
                        .parent("graphite_press")     // Mindustry SerpuloTechTree: graphitePress → pneumaticDrill 喵
                        .unlockBlock(com.blockdustry.building.PneumaticDrillRegistrar.PNEUMATIC_DRILL.get())
                        .buildRequirement(copper, 18) // Mindustry pneumatic-drill = 铜×18 石墨×10 喵
                        .buildRequirement(graphite, 10)
                        .build(),
                ResearchNode.builder("laser_drill")
                        .parent("pneumatic_drill")    // 原版链 pneumatic→laser（T34 协调建议）喵
                        .unlockBlock(com.blockdustry.building.LaserDrillRegistrar.LASER_DRILL.get())
                        .buildRequirement(copper, 35) // Mindustry laser-drill = 铜×35 石墨×30 硅×30 钛×20 喵
                        .buildRequirement(graphite, 30)
                        .buildRequirement(silicon, 30)
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 20)
                        .build(),
                ResearchNode.builder("blast_drill")
                        .parent("laser_drill")
                        .unlockBlock(com.blockdustry.building.BlastDrillRegistrar.BLAST_DRILL.get())
                        .buildRequirement(copper, 65) // Mindustry blast-drill = 铜×65 硅×60 钛×50 钍×75 喵
                        .buildRequirement(silicon, 60)
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 50)
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.THORIUM.get(), 75)
                        .build(),
                ResearchNode.builder("silicon_smelter")
                        .parent("graphite_press")     // Mindustry SerpuloTechTree: graphitePress → siliconSmelter 喵
                        .unlockBlock(com.blockdustry.building.SiliconSmelterRegistrar.SILICON_SMELTER.get())
                        .buildRequirement(copper, 30) // Mindustry silicon-smelter = 铜×30 铅×25 喵
                        .buildRequirement(lead, 25)
                        .build(),
                ResearchNode.builder("kiln")
                        .parent("graphite_press")     // Mindustry SerpuloTechTree: graphitePress → kiln 喵
                        .unlockBlock(com.blockdustry.production.KilnRegistrar.KILN.get())
                        .buildRequirement(copper, 60) // Mindustry kiln = 铜×60 石墨×30 铅×30 喵
                        .buildRequirement(graphite, 30)
                        .buildRequirement(lead, 30)
                        .build(),
                ResearchNode.builder("plastanium_compressor")
                        .parent("graphite_press")     // 原版 parent=sporePress 未迁移，先挂 production 分支喵
                        .unlockBlock(com.blockdustry.building.PlastaniumCompressorRegistrar.PLASTANIUM_COMPRESSOR.get())
                        .buildRequirement(silicon, 80) // Mindustry plastanium-compressor = 硅×80 铅×115 石墨×60 钛×80 喵
                        .buildRequirement(lead, 115)
                        .buildRequirement(graphite, 60)
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 80)
                        .build(),
                // —— 批1C 生产扩展：pyratiteMixer/phaseWeaver + pulverizer→incinerator（原版链 graphitePress→…、kiln→pulverizer→incinerator）喵 ——
                ResearchNode.builder("pyratite_mixer")
                        .parent("graphite_press")            // Mindustry: graphitePress → pyratiteMixer 喵
                        .unlockBlock(com.blockdustry.production.PyratiteMixerRegistrar.PYRATITE_MIXER.get())
                        .buildRequirement(copper, 50)        // pyratite-mixer = 铜×50 铅×25 喵
                        .buildRequirement(lead, 25)
                        .build(),
                ResearchNode.builder("pulverizer")
                        .parent("kiln")                      // Mindustry: kiln → pulverizer 喵
                        .unlockBlock(com.blockdustry.production.PulverizerRegistrar.PULVERIZER.get())
                        .buildRequirement(copper, 30)        // pulverizer = 铜×30 铅×25 喵
                        .buildRequirement(lead, 25)
                        .build(),
                ResearchNode.builder("incinerator")
                        .parent("pulverizer")                // Mindustry: pulverizer → incinerator 喵
                        .unlockBlock(com.blockdustry.production.IncineratorRegistrar.INCINERATOR.get())
                        .buildRequirement(graphite, 5)       // incinerator = 石墨×5 铅×15 喵
                        .buildRequirement(lead, 15)
                        .build(),
                ResearchNode.builder("phase_weaver")
                        .parent("plastanium_compressor")     // Mindustry: plastaniumCompressor → phaseWeaver 喵
                        .unlockBlock(com.blockdustry.building.PhaseWeaverRegistrar.PHASE_WEAVER.get())
                        .buildRequirement(silicon, 130)      // phase-weaver = 硅×130 铅×120 钍×75 喵
                        .buildRequirement(lead, 120)
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.THORIUM.get(), 75)
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

                // —— 批1E 电力扩展：combustion→solar、power_node→powerNodeLarge→diode→surgeTower、battery→batteryLarge、mender/forceProjector 喵 ——
                ResearchNode.builder("solar_panel")
                        .parent("combustion_generator")     // Mindustry: combustionGenerator → solarPanel 喵
                        .unlockBlock(com.blockdustry.power.SolarPanelRegistrar.SOLAR_PANEL.get())
                        .buildRequirement(lead, 10)          // solar-panel = 铅×10 硅×8 喵
                        .buildRequirement(silicon, 8)
                        .build(),
                ResearchNode.builder("solar_panel_large")
                        .parent("solar_panel")
                        .unlockBlock(com.blockdustry.power.SolarPanelRegistrar.SOLAR_PANEL_LARGE.get())
                        .buildRequirement(lead, 60)          // solar-panel-large = 铅×60 硅×70 相织布×15 喵
                        .buildRequirement(silicon, 70)
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.PHASE_FABRIC.get(), 15)
                        .build(),
                ResearchNode.builder("power_node_large")
                        .parent("power_node")
                        .unlockBlock(com.blockdustry.power.PowerNodeLargeRegistrar.POWER_NODE_LARGE.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 5) // power-node-large = 钛×5 铅×10 硅×3 喵
                        .buildRequirement(lead, 10)
                        .buildRequirement(silicon, 3)
                        .build(),
                ResearchNode.builder("battery_large")
                        .parent("battery")
                        .unlockBlock(com.blockdustry.power.BatteryLargeRegistrar.BATTERY_LARGE.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 20) // battery-large = 钛×20 铅×50 硅×30 喵
                        .buildRequirement(lead, 50)
                        .buildRequirement(silicon, 30)
                        .build(),
                ResearchNode.builder("diode")
                        .parent("power_node_large")          // Mindustry: powerNodeLarge → diode 喵
                        .unlockBlock(com.blockdustry.building.DiodeSurgeTowerRegistrar.DIODE.get())
                        .buildRequirement(silicon, 10)       // diode = 硅×10 塑钢×5 钢化玻璃×10 喵
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.PLASTANIUM.get(), 5)
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.METAGLASS.get(), 10)
                        .build(),
                ResearchNode.builder("surge_tower")
                        .parent("diode")                     // Mindustry: diode → surgeTower 喵
                        .unlockBlock(com.blockdustry.building.DiodeSurgeTowerRegistrar.SURGE_TOWER.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 7) // surge-tower = 钛7 铅10 硅15 涌电合金15 喵
                        .buildRequirement(lead, 10)
                        .buildRequirement(silicon, 15)
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.SURGE_ALLOY.get(), 15)
                        .build(),
                ResearchNode.builder("mender")
                        .parent("power_node")                // Mindustry: powerNode → mender 喵
                        .unlockBlock(com.blockdustry.building.MenderRegistrar.MENDER.get())
                        .buildRequirement(lead, 30)          // mender = 铅×30 铜×25 喵
                        .buildRequirement(copper, 25)
                        .build(),
                ResearchNode.builder("force_projector")
                        .parent("mender")                    // 原版 parent=mend-projector（未迁），临时挂 mender 喵
                        .unlockBlock(com.blockdustry.building.ForceProjectorRegistrar.FORCE_PROJECTOR.get())
                        .buildRequirement(lead, 100)         // force-projector = 铅×100 钛×75 硅×125 喵
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 75)
                        .buildRequirement(silicon, 125)
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

                // —— 批1F/2A 墙体：turret→copper→copperLarge→scrap→titanium→{大钛墙,door,高级墙}（原版 SerpuloTechTree L264-295）喵 ——
                ResearchNode.builder("copper_wall")
                        .parent("turret")                    // Mindustry: duo → copperWall 喵
                        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.COPPER_WALL.get())
                        .costMultiplier(0.1f)                // Mindustry copperWall researchCostMultiplier=0.1 喵
                        .buildRequirement(copper, 6)         // copper-wall = 铜×6 喵
                        .build(),
                ResearchNode.builder("copper_wall_large")
                        .parent("copper_wall")
                        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.COPPER_WALL_LARGE.get())
                        .buildRequirement(copper, 24)        // copper-wall-large = 4×铜×6 喵
                        .build(),
                ResearchNode.builder("scrap_wall")
                        .parent("copper_wall_large")
                        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.SCRAP_WALL.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.SCRAP.get(), 6) // scrap-wall = 废料×6 喵
                        .build(),
                ResearchNode.builder("scrap_wall_large")
                        .parent("scrap_wall")
                        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.SCRAP_WALL_LARGE.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.SCRAP.get(), 24) // scrap-wall-large = 4×废料×6 喵
                        .build(),
                ResearchNode.builder("titanium_wall")
                        .parent("copper_wall_large")         // Mindustry: copperWallLarge → titaniumWall 喵
                        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.TITANIUM_WALL.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 6) // titanium-wall = 钛×6 喵
                        .build(),
                ResearchNode.builder("titanium_wall_large")
                        .parent("titanium_wall")
                        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.TITANIUM_WALL_LARGE.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 24) // = 4×钛×6 喵
                        .build(),
                ResearchNode.builder("door")
                        .parent("titanium_wall")             // Mindustry: titaniumWall → door 喵
                        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.DOOR.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 6) // door = 钛×6 硅×4 喵
                        .buildRequirement(silicon, 4)
                        .build(),
                ResearchNode.builder("door_large")
                        .parent("door")
                        .unlockBlock(com.blockdustry.defense.DefenseRegistrar.DOOR_LARGE.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.TITANIUM.get(), 24) // door-large = 4×(钛6硅4) 喵
                        .buildRequirement(silicon, 16)
                        .build(),
                ResearchNode.builder("plastanium_wall")
                        .parent("titanium_wall")             // Mindustry: titaniumWall → plastaniumWall 喵
                        .unlockBlock(com.blockdustry.defense.AdvancedWallRegistrar.PLASTANIUM_WALL.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.PLASTANIUM.get(), 5) // = 塑钢×5+钢化玻璃×2 喵
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.METAGLASS.get(), 2)
                        .build(),
                ResearchNode.builder("plastanium_wall_large")
                        .parent("plastanium_wall")
                        .unlockBlock(com.blockdustry.defense.AdvancedWallRegistrar.PLASTANIUM_WALL_LARGE.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.PLASTANIUM.get(), 20)
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.METAGLASS.get(), 8)
                        .build(),
                ResearchNode.builder("thorium_wall")
                        .parent("titanium_wall")             // Mindustry: titaniumWall → thoriumWall 喵
                        .unlockBlock(com.blockdustry.defense.AdvancedWallRegistrar.THORIUM_WALL.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.THORIUM.get(), 6) // thorium-wall = 钍×6 喵
                        .build(),
                ResearchNode.builder("thorium_wall_large")
                        .parent("thorium_wall")
                        .unlockBlock(com.blockdustry.defense.AdvancedWallRegistrar.THORIUM_WALL_LARGE.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.THORIUM.get(), 24)
                        .build(),
                ResearchNode.builder("surge_wall")
                        .parent("thorium_wall")              // Mindustry: thoriumWall → surgeWall 喵
                        .unlockBlock(com.blockdustry.defense.AdvancedWallRegistrar.SURGE_WALL.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.SURGE_ALLOY.get(), 6) // surge-wall = 巨浪合金×6 喵
                        .build(),
                ResearchNode.builder("surge_wall_large")
                        .parent("surge_wall")
                        .unlockBlock(com.blockdustry.defense.AdvancedWallRegistrar.SURGE_WALL_LARGE.get())
                        .buildRequirement(com.blockdustry.item.BlockdustryItems.SURGE_ALLOY.get(), 24)
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
