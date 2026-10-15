package com.example;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WebScreenBlockEntity extends BlockEntity {
   public WebScreenBlockEntity(BlockPos pos, BlockState state) {
      super(WebScreenBlocks.WEB_SCREEN_BE, pos, state);
   }
}
