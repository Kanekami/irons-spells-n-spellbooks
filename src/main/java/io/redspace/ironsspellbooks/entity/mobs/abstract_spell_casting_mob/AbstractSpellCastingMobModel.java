package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.util.DefaultBipedBoneIdents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractSpellCastingMobModel extends DefaultedEntityGeoModel<AbstractSpellCastingMob> {

    public AbstractSpellCastingMobModel(/*ResourceLocation assetSubpath*/) {
        super(IronsSpellbooks.id("spellcastingmob"));
    }

    protected TransformStack transformStack = new TransformStack();

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return AbstractSpellCastingMob.modelResource;
    }

    @Override
    public abstract ResourceLocation getTextureResource(AbstractSpellCastingMob mob);

    @Override
    public ResourceLocation getAnimationResource(AbstractSpellCastingMob animatable) {
        return AbstractSpellCastingMob.animationInstantCast;
    }

    boolean a, b;

    @Override
    public void handleAnimations(AbstractSpellCastingMob entity, long instanceId, AnimationState<AbstractSpellCastingMob> animationState, float partialTick) {
        Stream<GeoBone> bones = getBonesForStackReset();
        bones.forEach((bone) -> {
            var snapshot = bone.getInitialSnapshot();
            bone.updatePosition(snapshot.getOffsetX(), snapshot.getOffsetY(), snapshot.getOffsetZ());
            bone.updateRotation(snapshot.getRotX(), snapshot.getRotY(), snapshot.getRotZ());
            bone.resetStateChanges();
        });
        super.handleAnimations(entity, instanceId, animationState, partialTick);
    }

    protected Stream<GeoBone> getBonesForStackReset() {
        return Stream.of(
                this.getAnimationProcessor().getBone(PartNames.HEAD),
                this.getAnimationProcessor().getBone(PartNames.BODY),
                this.getAnimationProcessor().getBone("torso"),
                this.getAnimationProcessor().getBone(PartNames.RIGHT_ARM),
                this.getAnimationProcessor().getBone(PartNames.LEFT_ARM),
                this.getAnimationProcessor().getBone(PartNames.RIGHT_LEG),
                this.getAnimationProcessor().getBone(PartNames.LEFT_LEG),
                this.getAnimationProcessor().getBone(DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT),
                this.getAnimationProcessor().getBone(DefaultBipedBoneIdents.LEFT_HAND_BONE_IDENT)
        );
    }

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, long instanceId, AnimationState<AbstractSpellCastingMob> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);
        if (!(Minecraft.getInstance().isPaused() || !entity.shouldBeExtraAnimated())) {
            var partialTick = animationState.getPartialTick();
            GeoBone head = this.getAnimationProcessor().getBone(PartNames.HEAD);
            GeoBone body = this.getAnimationProcessor().getBone(PartNames.BODY);
            GeoBone torso = this.getAnimationProcessor().getBone("torso");
            GeoBone rightArm = this.getAnimationProcessor().getBone(PartNames.RIGHT_ARM);
            GeoBone leftArm = this.getAnimationProcessor().getBone(PartNames.LEFT_ARM);
            GeoBone rightLeg = this.getAnimationProcessor().getBone(PartNames.RIGHT_LEG);
            GeoBone leftLeg = this.getAnimationProcessor().getBone(PartNames.LEFT_LEG);
            List<GeoBone> bones = List.of(head, body, torso, rightArm, leftArm, rightLeg, leftLeg);

        /*
            Head Controls
         */
            if (!entity.isAnimating() || entity.shouldAlwaysAnimateHead()) {
                transformStack.pushRotation(head,
                        Mth.lerp(partialTick, -entity.xRotO, -entity.getXRot()) * Mth.DEG_TO_RAD,
                        Mth.lerp(partialTick,
                                Mth.wrapDegrees(-entity.yHeadRotO + entity.yBodyRotO) * Mth.DEG_TO_RAD,
                                Mth.wrapDegrees(-entity.yHeadRot + entity.yBodyRot) * Mth.DEG_TO_RAD
                        ),
                        0);
            }
            Vector2f limbSwing = getLimbSwing(entity, entity.walkAnimation, partialTick);
            float limbSwingAmount = limbSwing.x;
            float limbSwingSpeed = limbSwing.y;

        /*
            Leg Controls
         */
            if (entity.isPassenger() && entity.getVehicle().shouldRiderSit()) {
                //If we are riding something, pose ourselves sitting
                transformStack.pushRotation(rightLeg,
                        1.4137167F,
                        -(float) Math.PI / 10F,
                        -0.07853982F
                );
                transformStack.pushRotation(leftLeg,
                        1.4137167F,
                        (float) Math.PI / 10F,
                        0.07853982F
                );
            } else if (!entity.isAnimating() || entity.shouldAlwaysAnimateLegs()) {
                float strength = .75f;
                Vec3 facing = entity.getForward().multiply(1, 0, 1).normalize();
                Vec3 momentum = entity.getDeltaMovement().multiply(1, 0, 1).normalize();
                Vec3 facingOrth = new Vec3(-facing.z, 0, facing.x);
                float directionForward = (float) facing.dot(momentum);
                float directionSide = (float) facingOrth.dot(momentum) * .35f; //scale side to side movement so they dont rip off thier own legs
                float rightLateral = -Mth.sin(limbSwingSpeed * 0.6662F) * 4 * limbSwingAmount;
                float leftLateral = -Mth.sin(limbSwingSpeed * 0.6662F - Mth.PI) * 4 * limbSwingAmount;
                transformStack.pushPosition(rightLeg, rightLateral * directionSide, Mth.cos(limbSwingSpeed * 0.6662F) * 4 * strength * limbSwingAmount, rightLateral * directionForward);
                transformStack.pushRotation(rightLeg, Mth.cos(limbSwingSpeed * 0.6662F) * 1.4F * limbSwingAmount * strength, 0, 0);

                transformStack.pushPosition(leftLeg, leftLateral * directionSide, Mth.cos(limbSwingSpeed * 0.6662F - Mth.PI) * 4 * strength * limbSwingAmount, leftLateral * directionForward);
                transformStack.pushRotation(leftLeg, Mth.cos(limbSwingSpeed * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount * strength, 0, 0);

                if (entity.bobBodyWhileWalking()) {
                    transformStack.pushPosition(body, 0, Mth.abs(Mth.cos((limbSwingSpeed * 1.2662F - Mth.PI * .5f) * .5f)) * 2 * strength * limbSwingAmount, 0);
                }
            }
        /*
            Arm Controls
         */
            if (!entity.isAnimating()) {
                transformStack.pushRotation(rightArm, Mth.cos(limbSwingSpeed * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F, 0, 0);
                transformStack.pushRotation(leftArm, Mth.cos(limbSwingSpeed * 0.6662F) * 2.0F * limbSwingAmount * 0.5F, 0, 0);
                bobBone(rightArm, entity.tickCount + partialTick, 1);
                bobBone(leftArm, entity.tickCount + partialTick, -1);
                if (entity.isDrinkingPotion()) {
                    transformStack.pushRotation(entity.isLeftHanded() ? leftArm : rightArm,
                            35 * Mth.DEG_TO_RAD,
                            (entity.isLeftHanded() ? -25 : 25) * Mth.DEG_TO_RAD,
                            (entity.isLeftHanded() ? 15 : -15) * Mth.DEG_TO_RAD
                    );
                }
            }
            transformStack.popStack();
        }
    }

    protected void resetSnapshot(GeoBone bone) {
        bone.getInitialSnapshot().updateRotation(0, 0, 0);
        bone.getInitialSnapshot().updateOffset(0, 0, 0);
    }

    protected void bobBone(GeoBone bone, float offset, float multiplier) {
        float z = multiplier * (Mth.cos(offset * 0.09F) * 0.05F + 0.05F);
        float x = multiplier * Mth.sin(offset * 0.067F) * 0.05F;
        transformStack.pushRotation(bone, x, 0, z);
    }

    /**
     * @param walkAnimationState
     * @return x: amount, y: speed
     */
    protected Vector2f getLimbSwing(AbstractSpellCastingMob entity, WalkAnimationState walkAnimationState, float partialTick) {
        float limbSwingAmount = 0;
        float limbSwingSpeed = 0;
        if (entity.isAlive()) {
            limbSwingAmount = walkAnimationState.speed(partialTick);
            limbSwingSpeed = walkAnimationState.position(partialTick);
            if (entity.isBaby()) {
                limbSwingSpeed *= 3.0F;
            }

            if (limbSwingAmount > 1.0F) {
                limbSwingAmount = 1.0F;
            }
        }
        return new Vector2f(limbSwingAmount, limbSwingSpeed);
    }


}