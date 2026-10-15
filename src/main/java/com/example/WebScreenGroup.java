package com.example;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class WebScreenGroup {
   private static final int MAX = 32;
   public final BlockPos controller;
   public final int cols;
   public final int rows;
   public final int myCol;
   public final int myRow;

   private WebScreenGroup(BlockPos controller, int cols, int rows, int myCol, int myRow) {
      this.controller = controller;
      this.cols = cols;
      this.rows = rows;
      this.myCol = myCol;
      this.myRow = myRow;
   }

   public boolean isController(BlockPos pos) {
      return this.controller.equals(pos);
   }

   public static WebScreenGroup compute(BlockGetter level, BlockPos pos, Direction facing) {
      if (level == null) {
         return standalone(pos);
      } else {
         int i = facing.getStepZ();
         int j = -facing.getStepX();
         BlockPos blockpos = pos;

         for (int k = 0; k < 32; k++) {
            BlockPos blockpos1 = blockpos.offset(-i, 0, -j);
            if (!same(level, blockpos1, facing)) {
               break;
            }

            blockpos = blockpos1;
         }

         for (int j1 = 0; j1 < 32; j1++) {
            BlockPos blockpos2 = blockpos.below();
            if (!same(level, blockpos2, facing)) {
               break;
            }

            blockpos = blockpos2;
         }

         int k1 = 1;

         for (int l1 = 1; l1 < 32 && same(level, blockpos.offset(i * l1, 0, j * l1), facing); l1++) {
            k1 = l1 + 1;
         }

         int i2 = 1;

         for (int l = 1; l < 32 && same(level, blockpos.above(l), facing); l++) {
            i2 = l + 1;
         }

         for (int j2 = 0; j2 < k1; j2++) {
            for (int i1 = 0; i1 < i2; i1++) {
               if (!same(level, blockpos.offset(i * j2, 0, j * j2).above(i1), facing)) {
                  return standalone(pos);
               }
            }
         }

         int k2 = i != 0 ? (pos.getX() - blockpos.getX()) * i : (pos.getZ() - blockpos.getZ()) * j;
         int l2 = pos.getY() - blockpos.getY();
         return k2 >= 0 && k2 < k1 && l2 >= 0 && l2 < i2 ? new WebScreenGroup(blockpos.immutable(), k1, i2, k2, l2) : standalone(pos);
      }
   }

   private static WebScreenGroup standalone(BlockPos pos) {
      return new WebScreenGroup(pos.immutable(), 1, 1, 0, 0);
   }

   private static boolean same(BlockGetter level, BlockPos p, Direction facing) {
      BlockState blockstate = level.getBlockState(p);
      return blockstate.getBlock() instanceof WebScreenBlock && blockstate.getValue(WebScreenBlock.FACING) == facing;
   }
}
