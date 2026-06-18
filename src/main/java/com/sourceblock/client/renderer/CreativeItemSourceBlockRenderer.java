package com.sourceblock.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sourceblock.block.entity.CreativeItemSourceBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * 创造物品源方块渲染器
 * 在方块的6个面上显示物品图标
 */
public class CreativeItemSourceBlockRenderer implements BlockEntityRenderer<CreativeItemSourceBlockEntity> {
    private static final float SCALE = 0.375f; // 物品显示大小

    public CreativeItemSourceBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull CreativeItemSourceBlockEntity blockEntity, float partialTick,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        ItemStack storedItem = blockEntity.getStoredItem();
        
        if (storedItem.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        BakedModel model = itemRenderer.getModel(storedItem, blockEntity.getLevel(), null, 0);
        
        // 使用最大光照值确保物品足够亮
        int light = LightTexture.pack(packedLight, 15); // 方块光照15，天空光照15（最亮）

        // 渲染6个面
        renderItemOnFace(poseStack, bufferSource, storedItem, model, itemRenderer, Direction.UP, light);
        renderItemOnFace(poseStack, bufferSource, storedItem, model, itemRenderer, Direction.DOWN, light);
        renderItemOnFace(poseStack, bufferSource, storedItem, model, itemRenderer, Direction.NORTH, light);
        renderItemOnFace(poseStack, bufferSource, storedItem, model, itemRenderer, Direction.SOUTH, light);
        renderItemOnFace(poseStack, bufferSource, storedItem, model, itemRenderer, Direction.WEST, light);
        renderItemOnFace(poseStack, bufferSource, storedItem, model, itemRenderer, Direction.EAST, light);
    }

    private void renderItemOnFace(PoseStack poseStack, MultiBufferSource bufferSource,
                                  ItemStack item, BakedModel model, ItemRenderer itemRenderer,
                                  Direction face, int packedLight) {
        poseStack.pushPose();

        // 移动到方块中心
        poseStack.translate(0.5, 0.5, 0.5);

        // 根据面的方向旋转和定位 - 让物品正面朝外
        switch (face) {
            case UP:
                // 在顶面，物品朝上
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0, 0, 0.501); // 在外侧
                break;
            case DOWN:
                // 在底面，物品朝下
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                poseStack.translate(0, 0, 0.501); // 在外侧
                break;
            case NORTH:
                // 在北面，物品朝北
                poseStack.translate(0, 0, 0.501); // 在外侧
                break;
            case SOUTH:
                // 在南面，物品朝南
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(0, 0, 0.501); // 在外侧
                break;
            case WEST:
                // 在西面，物品朝西
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.translate(0, 0, 0.501); // 在外侧
                break;
            case EAST:
                // 在东面，物品朝东
                poseStack.mulPose(Axis.YP.rotationDegrees(-90));
                poseStack.translate(0, 0, 0.501); // 在外侧
                break;
        }

        // 缩放物品
        poseStack.scale(SCALE, SCALE, 0.001f); // Z轴压扁，贴在面上

        // 渲染物品
        itemRenderer.render(
            item,
            ItemDisplayContext.GUI,
            false,
            poseStack,
            bufferSource,
            packedLight, // 使用传入的光照值（已经在render方法中设置为最亮）
            OverlayTexture.NO_OVERLAY,
            model
        );

        poseStack.popPose();
    }
}

