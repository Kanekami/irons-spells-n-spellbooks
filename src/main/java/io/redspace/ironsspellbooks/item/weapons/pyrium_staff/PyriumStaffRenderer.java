package io.redspace.ironsspellbooks.item.weapons.pyrium_staff;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class PyriumStaffRenderer extends BlockEntityWithoutLevelRenderer {
    private final ItemRenderer renderer;
    public final BakedModel haftModel;
    public final PyriumStaffHeadModel headModel;

    public PyriumStaffRenderer(ItemRenderer renderDispatcher, EntityModelSet modelSet) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), modelSet);
        this.renderer = renderDispatcher;
        this.haftModel = renderer.getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(new ResourceLocation(IronsSpellbooks.MODID, "item/pyrium_staff_haft")));
        this.headModel = new PyriumStaffHeadModel(modelSet.bakeLayer(PyriumStaffHeadModel.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        if (transformType == ItemDisplayContext.GUI) {
            Lighting.setupForEntityInInventory();
            render(poseStack, bufferSource, itemStack, transformType, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false);

            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            Lighting.setupFor3DItems();
        } else {
            boolean leftHand = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            render(poseStack, bufferSource, itemStack, transformType, combinedLightIn, combinedOverlayIn, leftHand);
        }
        poseStack.popPose();
    }

    private static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/fire_boss/tyros_flame.png");
    static int frameCount = 8;
    static int ticksPerFrame = 1;

    private void render(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack itemStack, ItemDisplayContext transformType, int combinedLightIn, int combinedOverlayIn, boolean leftHanded) {
        // render item part
        renderer.render(itemStack, transformType, leftHanded, poseStack, bufferSource, combinedLightIn, combinedOverlayIn, haftModel);
        // render modelled part
        poseStack.pushPose();
//        haftModel.applyTransform(transformType, poseStack, leftHanded);
        ItemTransform transform = haftModel.getTransforms().getTransform(transformType);

        VertexConsumer vertexconsumer1 = ItemRenderer.getFoilBufferDirect(
                bufferSource, headModel.renderType(), false, itemStack.hasFoil()
        );


        // manual adjustments
//        poseStack.translate(0,-2,-1.5/16f);
        applyTransform(transform, leftHanded, poseStack);
        poseStack.mulPose(Axis.ZP.rotationDegrees(135));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90));
        poseStack.translate(0, -18.42 / 16f, 0);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        headModel.renderToBuffer(poseStack, vertexconsumer1, combinedLightIn, combinedOverlayIn);

        if (transformType != ItemDisplayContext.GUI) {
            poseStack.pushPose();
            poseStack.scale(1.0F, -1.0F, 1.0F);
            poseStack.translate(0, -1.45, -0.8 / 16f);
            poseStack.mulPose(Axis.YP.rotationDegrees(45f));
            poseStack.scale(.5f, .5f, .5f);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
            Matrix4f poseMatrix = poseStack.last().pose();
            int a = MinecraftInstanceHelper.getPlayer().tickCount;
            int anim = (a / ticksPerFrame) % frameCount;
            float uvMin = anim / (float) frameCount;
            float uvMax = (anim + 1) / (float) frameCount;
            float halfsqrt2 = 0.7071f;
            for (int i = 0; i < 4; i++) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90f));
                consumer.addVertex(poseMatrix, 0, 0, -halfsqrt2).setColor(255, 255, 255, 255).setUv(0f, uvMax).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
                consumer.addVertex(poseMatrix, 0, 1, -halfsqrt2).setColor(255, 255, 255, 255).setUv(0f, uvMin).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
                consumer.addVertex(poseMatrix, 0, 1, halfsqrt2).setColor(255, 255, 255, 255).setUv(1f, uvMin).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
                consumer.addVertex(poseMatrix, 0, 0, halfsqrt2).setColor(255, 255, 255, 255).setUv(1f, uvMax).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(0f, 1f, 0f);
            }
            poseStack.popPose();
        }

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
            pPoseStack.translate((float) i * transform.translation.x(), transform.translation.y(), transform.translation.z());
            pPoseStack.mulPose(new Quaternionf().rotationXYZ(f * (float) (Math.PI / 180.0), f1 * (float) (Math.PI / 180.0), f2 * (float) (Math.PI / 180.0)));
            pPoseStack.scale(transform.scale.x(), transform.scale.y(), transform.scale.x());
            //pPoseStack.mulPose(net.neoforged.neoforge.common.util.TransformationHelper.quatFromXYZ(rightRotation.x(), rightRotation.y() * (pLeftHand ? -1 : 1), rightRotation.z() * (pLeftHand ? -1 : 1), true));
        }
    }
}
