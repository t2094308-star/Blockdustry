package com.blockdustry.building;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// 爆破钻头方块（Mindustry blast-drill）：原版 size=4（4×4 占地，128px 贴图）喵。
// 基础类 BlockdustryBuildingBlock 的 Corner 枚举只支持 1/2/3 格（9 值），4×4 需 16 格无法用 corner 区分，
// 故这里覆写 getShape/getCollisionShape：按「本格相对锚点的 dx/dz」直接算整组包围盒，绕开 corner 限制喵
public class BlastDrillBlock extends BlockdustryBuildingBlock {

    public BlastDrillBlock(Properties properties, Supplier<BlockEntityType<?>> entityType) {
        // size=4，height=1（Mindustry 爆破钻头为单层平铺建筑，视觉高度靠 BER 叠画钻头/光晕）喵
        super(properties, entityType, 4);
    }

    // 碰撞/形状：4×4×1 整组包围盒，从本格视角返回组 AABB（与基类 getShape 语义一致，等价世界坐标整组一个实心体）喵
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return groupShape4(state, level, pos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return groupShape4(state, level, pos);
    }

    private VoxelShape groupShape4(BlockState state, BlockGetter level, BlockPos pos) {
        int dx, dz;
        if (level.getBlockEntity(pos) instanceof BlockdustryBuildingEntity be && be.getAnchor() != null) {
            BlockPos anchor = be.getAnchor();
            dx = pos.getX() - anchor.getX();
            dz = pos.getZ() - anchor.getZ();
        } else {
            // 无锚点（放置瞬间）：按 corner 兜底算 dx/dz（corner 对 4×4 有重复，只影响放置瞬间的短暂碰撞，可接受）喵
            Corner c = state.getValue(CORNER);
            dx = cornerDxFb(c);
            dz = cornerDzFb(c);
        }
        int size = 4;
        return Block.box(-dx * 16.0, 0.0, -dz * 16.0,
                (size - dx) * 16.0, 16.0, (size - dz) * 16.0);
    }

    // 与基类 cornerDx/cornerDz 相同逻辑的私有副本（基类方法 private 不可复用）喵
    private static int cornerDxFb(Corner c) {
        return switch (c) {
            case N, C, S -> 1;
            case NE, E, SE -> 2;
            default -> 0;
        };
    }

    private static int cornerDzFb(Corner c) {
        return switch (c) {
            case SW, S, SE -> 2;
            case W, C, E -> 1;
            default -> 0;
        };
    }
}
