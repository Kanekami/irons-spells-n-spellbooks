package io.redspace.ironsspellbooks.item.weapons.pyrium_staff;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class PyriumStaffRenderer extends BlockEntityWithoutLevelRenderer {
    private final ItemRenderer renderer;
    public final BakedModel haftModel;
    public final PyriumStaffHeadModel tridentModel;

    public PyriumStaffRenderer(ItemRenderer renderDispatcher, EntityModelSet modelSet) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), modelSet);
        this.renderer = renderDispatcher;
        this.haftModel = renderer.getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(new ResourceLocation(IronsSpellbooks.MODID, "item/pyrium_staff_haft")));
        this.tridentModel = new PyriumStaffHeadModel(modelSet.bakeLayer(PyriumStaffHeadModel.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        BakedModel model;
        if (transformType == ItemDisplayContext.GUI) {
            Lighting.setupForFlatItems();
            render(poseStack, bufferSource, itemStack, transformType, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false);

            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            Lighting.setupFor3DItems();
        } else {
            boolean leftHand = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            render(poseStack, bufferSource, itemStack, transformType, combinedLightIn, combinedOverlayIn, leftHand);
        }
        poseStack.popPose();
    }

    private void render(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack itemStack, ItemDisplayContext transformType, int combinedLightIn, int combinedOverlayIn, boolean leftHanded) {
        // render item part
        renderer.render(itemStack, transformType, leftHanded, poseStack, bufferSource, combinedLightIn, combinedOverlayIn, haftModel);
        // render modelled part
        poseStack.pushPose();
//        haftModel.applyTransform(transformType, poseStack, leftHanded);
//        poseStack.scale(1.0F, -1.0F, -1.0F);
        ItemTransform transform = haftModel.getTransforms().getTransform(transformType);

        VertexConsumer vertexconsumer1 = ItemRenderer.getFoilBufferDirect(
                bufferSource, tridentModel.renderType(), false, itemStack.hasFoil()
        );


        // manual adjustments
//        poseStack.translate(0,-2,-1.5/16f);
        applyTransform(transform,leftHanded,poseStack);
        poseStack.mulPose(Axis.ZP.rotationDegrees(135));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90));
        poseStack.translate(0,-26/32f,0);
        poseStack.scale(0.5f,0.5f,0.5f);
        tridentModel.renderToBuffer(poseStack, vertexconsumer1, combinedLightIn, combinedOverlayIn);
        poseStack.popPose();
    }

    public void applyTransform(ItemTransform transform, boolean pLeftHand, PoseStack pPoseStack) {
        if (transform != ItemTransform.NO_TRANSFORM) {
            float f = transform.rotation.x();
            float f1 = transform.rotation.y();
            float f2 = transform.rotation.z();
            if (pLeftHand) {
                f1 = -f1;
                f2 = -f2;
            }

            int i = pLeftHand ? -1 : 1;
            pPoseStack.translate((float)i * transform.translation.x(), transform.translation.y(), transform.translation.z());
            pPoseStack.mulPose(new Quaternionf().rotationXYZ(f * (float) (Math.PI / 180.0), f1 * (float) (Math.PI / 180.0), f2 * (float) (Math.PI / 180.0)));
            pPoseStack.scale(transform.scale.x(), transform.scale.y(), transform.scale.x());
            //pPoseStack.mulPose(net.neoforged.neoforge.common.util.TransformationHelper.quatFromXYZ(rightRotation.x(), rightRotation.y() * (pLeftHand ? -1 : 1), rightRotation.z() * (pLeftHand ? -1 : 1), true));
        }
    }
}
