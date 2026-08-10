package com.blockdustry;

import java.util.HashMap;
import java.util.Map;

import com.blockdustry.team.BlockdustryTeam;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class BlockdustryAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Blockdustry.MODID);

    // 方块队伍：Map<BlockPos, Team> 挂 ServerLevel，持久化（键 "x,y,z"，值队伍名）喵
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Map<BlockPos, BlockdustryTeam>>> BLOCK_TEAM =
            ATTACHMENT_TYPES.register("block_team",
                    () -> AttachmentType.<Map<BlockPos, BlockdustryTeam>>builder(() -> new HashMap<>())
                            .serialize(getBlockTeamSerializer())
                            .build());

    // 实体/玩家队伍：Entity attachment，持久化（随实体 NBT 保存）喵
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BlockdustryTeam>> ENTITY_TEAM =
            ATTACHMENT_TYPES.register("entity_team",
                    () -> AttachmentType.<BlockdustryTeam>builder(() -> BlockdustryTeam.NEUTRAL)
                            .serialize(getEntityTeamSerializer())
                            .build());

    private static IAttachmentSerializer<CompoundTag, Map<BlockPos, BlockdustryTeam>> getBlockTeamSerializer() {
        return new IAttachmentSerializer<>() {
            @Override
            public Map<BlockPos, BlockdustryTeam> read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                Map<BlockPos, BlockdustryTeam> map = new HashMap<>();
                for (String key : tag.getAllKeys()) {
                    String[] p = key.split(",");
                    map.put(new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2])),
                            BlockdustryTeam.valueOf(tag.getString(key)));
                }
                return map;
            }

            @Override
            public CompoundTag write(Map<BlockPos, BlockdustryTeam> attachment, HolderLookup.Provider provider) {
                if (attachment.isEmpty()) return null;
                CompoundTag tag = new CompoundTag();
                attachment.forEach((pos, team) -> tag.putString(pos.getX() + "," + pos.getY() + "," + pos.getZ(), team.name()));
                return tag;
            }
        };
    }

    private static IAttachmentSerializer<CompoundTag, BlockdustryTeam> getEntityTeamSerializer() {
        return new IAttachmentSerializer<>() {
            @Override
            public BlockdustryTeam read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                return BlockdustryTeam.valueOf(tag.getString("team"));
            }

            @Override
            public CompoundTag write(BlockdustryTeam attachment, HolderLookup.Provider provider) {
                CompoundTag tag = new CompoundTag();
                tag.putString("team", attachment.name());
                return tag;
            }
        };
    }
}
