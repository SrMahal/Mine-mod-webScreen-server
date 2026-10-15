package com.example.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class WebScreenRenderState extends BlockEntityRenderState {
   public Direction facing = Direction.NORTH;
   public BlockPos controller = BlockPos.ZERO;
   public int cols = 1;
   public int rows = 1;
   public int myCol = 0;
   public int myRow = 0;
}
