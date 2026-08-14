package com.blockdustry.defense;

import java.util.function.Supplier;

import com.blockdustry.BlockdustryTeams;
import com.blockdustry.building.BlockdustryBuildingBlock;
import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// 门方块（Mindustry Door，size 1/2）：关闭时实心、开启时可通过（Door.checkSolid = !open）喵。
// open 状态存于 DoorBlockEntity（Mindustry DoorBuild.open）而非方块状态，避免 2×2 四象限 × 开合的 blockstate 组合爆炸喵。
// 碰撞：仅覆写 getCollisionShape（开门时空形状），getShape 保持满形状——保证开门后仍可右键点选关闭（Mindustry 可点门格开关）喵
public class DoorBlock extends BlockdustryBuildingBlock {

    public DoorBlock(Properties properties, Supplier<BlockEntityType<?>> entityType, int size) {
        super(properties, entityType, size);
    }

    // 空手右键（Mindustry DoorBuild.tapped → configure(!open)）：
    // 服务端做开关 + 连锁 + 音效 + 特效；同队或 derelict 才可交互（Mindustry interactable）喵
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.CONSUME;
        if (level.getBlockEntity(pos) instanceof DoorBlockEntity door) {
            BlockdustryTeam playerTeam = BlockdustryTeams.getTeam(player);
            if (!door.getTeam().canInteract(playerTeam)) {
                return InteractionResult.PASS; // 异队不可开关喵
            }
            door.toggle();
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    // 碰撞箱：开门时无碰撞（可通过）；关门时整组实心（super 组包围盒，见 BlockdustryBuildingBlock.getCollisionShape）喵
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (isOpen(level, pos)) return Shapes.empty();
        return super.getCollisionShape(state, level, pos, context);
    }

    // 读门开状态：该格 BE（非锚点格内部转发锚点，2×2 四格一致）喵
    private static boolean isOpen(BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof DoorBlockEntity door && door.isOpen();
    }
}
