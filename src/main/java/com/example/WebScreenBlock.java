package com.example;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class WebScreenBlock extends Block implements EntityBlock {
   public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final MapCodec<WebScreenBlock> CODEC = simpleCodec(WebScreenBlock::new);

   public WebScreenBlock(Properties props) {
      super(props);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   protected MapCodec<? extends Block> codec() {
      return CODEC;
   }

   @Override
   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(FACING);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new WebScreenBlockEntity(pos, state);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
      Direction direction = state.getValue(FACING);
      if (level.isClientSide() && hit.getDirection() == direction) {
         double d0 = direction.getStepZ();
         double d1 = -direction.getStepX();
         Vec3 vec3 = hit.getLocation();
         double d2 = vec3.x - (pos.getX() + 0.5);
         double d3 = vec3.y - (pos.getY() + 0.5);
         double d4 = vec3.z - (pos.getZ() + 0.5);
         WebScreenGroup webscreengroup = WebScreenGroup.compute(level, pos, direction);
         double d5 = 0.0625;
         double d6 = -0.5 + (webscreengroup.myCol == 0 ? d5 : 0.0);
         double d7 = 0.5 - (webscreengroup.myCol == webscreengroup.cols - 1 ? d5 : 0.0);
         double d8 = -0.5 + (webscreengroup.myRow == 0 ? d5 : 0.0);
         double d9 = 0.5 - (webscreengroup.myRow == webscreengroup.rows - 1 ? d5 : 0.0);
         double d10 = d2 * d0 + d4 * d1;
         double d11 = (d10 - d6) / (d7 - d6);
         double d12 = (d9 - d3) / (d9 - d8);
         double d13 = (webscreengroup.myCol + d11) / webscreengroup.cols;
         double d14 = (webscreengroup.rows - 1 - webscreengroup.myRow + d12) / webscreengroup.rows;
         WebScreenInput.click(webscreengroup.controller, d13, d14);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }
}
