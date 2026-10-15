package com.example;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WebRemoteItem extends Item {
   public WebRemoteItem(Properties props) {
      super(props);
   }

   @Override
   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      BlockPos blockpos = ctx.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      if (!(blockstate.getBlock() instanceof WebScreenBlock)) {
         return InteractionResult.PASS;
      } else {
         if (level.isClientSide()) {
            WebScreenGroup webscreengroup = WebScreenGroup.compute(level, blockpos, blockstate.getValue(WebScreenBlock.FACING));
            WebScreenInput.openMenu(webscreengroup.controller);
         }

         return InteractionResult.SUCCESS;
      }
   }
}
