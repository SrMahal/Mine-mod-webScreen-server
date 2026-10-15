package com.example.client;

import com.example.WebScreenBlock;
import com.example.WebScreenBlockEntity;
import com.example.WebScreenGroup;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class WebScreenRenderer implements BlockEntityRenderer<WebScreenBlockEntity, WebScreenRenderState> {
   private static final int FULL_BRIGHT = 15728880;
   private static final int WHITE = -1;

   public WebScreenRenderer(Context ctx) {
   }

   public WebScreenRenderState createRenderState() {
      return new WebScreenRenderState();
   }

   public void extractRenderState(WebScreenBlockEntity be, WebScreenRenderState state, float partialTick, Vec3 cameraPos, CrumblingOverlay crumbling) {
      BlockEntityRenderState.extractBase(be, state, crumbling);
      state.facing = be.getBlockState().getValue(WebScreenBlock.FACING);
      WebScreenGroup webscreengroup = WebScreenGroup.compute(be.getLevel(), be.getBlockPos(), state.facing);
      state.controller = webscreengroup.controller;
      state.cols = webscreengroup.cols;
      state.rows = webscreengroup.rows;
      state.myCol = webscreengroup.myCol;
      state.myRow = webscreengroup.myRow;
   }

   public void submit(WebScreenRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
      ScreenManager.Screen screenmanager$screen = ScreenManager.getOrCreate(state.controller);
      if (screenmanager$screen != null) {
         WebTexture webtexture = screenmanager$screen.texture();
         webtexture.uploadIfDirty();
         if (webtexture.hasFrame()) {
            Direction direction = state.facing;
            float f = direction.getStepX();
            float f1 = direction.getStepZ();
            float f2 = -f;
            float f3 = 0.501F;
            float f4 = 0.5F + f * f3;
            float f5 = 0.5F;
            float f6 = 0.5F + f1 * f3;
            float f7 = 0.0625F;
            float f8 = state.myCol == 0 ? f7 : 0.0F;
            float f9 = state.myCol == state.cols - 1 ? f7 : 0.0F;
            float f10 = state.myRow == 0 ? f7 : 0.0F;
            float f11 = state.myRow == state.rows - 1 ? f7 : 0.0F;
            float f12 = -0.5F + f8;
            float f13 = 0.5F - f9;
            float f14 = -0.5F + f10;
            float f15 = 0.5F - f11;
            float f16 = (float)state.myCol / state.cols;
            float f17 = (float)(state.myCol + 1) / state.cols;
            float f18 = (float)(state.rows - 1 - state.myRow) / state.rows;
            float f19 = (float)(state.rows - state.myRow) / state.rows;
            RenderType rendertype = RenderTypes.entitySolid(webtexture.id());
            collector.submitCustomGeometry(poseStack, rendertype, (pose, vc) -> {
               vertex(pose, vc, direction, f4 + f1 * f12, f5 + f14, f6 + f2 * f12, f16, f19);
               vertex(pose, vc, direction, f4 + f1 * f13, f5 + f14, f6 + f2 * f13, f17, f19);
               vertex(pose, vc, direction, f4 + f1 * f13, f5 + f15, f6 + f2 * f13, f17, f18);
               vertex(pose, vc, direction, f4 + f1 * f12, f5 + f15, f6 + f2 * f12, f16, f18);
            });
         }
      }
   }

   private static void vertex(Pose pose, VertexConsumer vc, Direction f, float x, float y, float z, float u, float v) {
      vc.addVertex(pose, x, y, z)
         .setColor(-1)
         .setUv(u, v)
         .setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(15728880)
         .setNormal(pose, f.getStepX(), f.getStepY(), f.getStepZ());
   }
}
