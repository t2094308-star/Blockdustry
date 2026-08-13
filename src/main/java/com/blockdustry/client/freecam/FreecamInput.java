package com.blockdustry.client.freecam;

import net.minecraft.client.Options;
import net.minecraft.client.player.KeyboardInput;

// 空操作输入：freecam 期间替换玩家的 input，使身体原地站立不动喵
public class FreecamInput extends KeyboardInput {
    public FreecamInput(Options options) {
        super(options);
    }

    // 1.21.1 签名是 tick(boolean, float)；覆写为空即不读任何按键喵
    @Override
    public void tick(boolean slowMovement, float multiplier) {
        // NO-OP：身体冻结喵
    }
}
