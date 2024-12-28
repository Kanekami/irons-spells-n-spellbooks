package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.util.DefaultBipedBoneIdents;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;

public class FireBossModel extends AbstractSpellCastingMobModel {
    public static final ResourceLocation TEXTURE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/tyros.png");
    public static final ResourceLocation TEXTURE_SOUL_MODE = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/tyros_soul_mode.png");
    public static final ResourceLocation MODEL = new ResourceLocation(IronsSpellbooks.MODID, "geo/tyros.geo.json");
    private static final float tilt = 15 * Mth.DEG_TO_RAD;
    private static final Vector3f forward = new Vector3f(0, 0, Mth.sin(tilt) * -12);

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        if (object instanceof FireBossEntity fireBossEntity && fireBossEntity.isSoulMode()) {
            return TEXTURE_SOUL_MODE;
        }
        return TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return MODEL;
    }

    float leglerp = 1f;
    float isAnimatingDampener;
    int lastTick;

    @Override
    public void setCustomAnimations(AbstractSpellCastingMob entity, long instanceId, AnimationState<AbstractSpellCastingMob> animationState) {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        if (entity instanceof FireBossEntity fireBossEntity) {
            handleParticles(fireBossEntity);
            float partialTick = animationState.getPartialTick();
            Vector2f limbSwing = getLimbSwing(entity, entity.walkAnimation, partialTick);
            if (entity.isAnimating()) {
                isAnimatingDampener = Mth.lerp(.3f * partialTick, isAnimatingDampener, 0);
            } else {
                isAnimatingDampener = Mth.lerp(.1f * partialTick, isAnimatingDampener, 1);
            }
            if (entity.getMainHandItem().is(ItemRegistry.HELLRAZOR)) {
                GeoBone rightArm = this.getAnimationProcessor().getBone(PartNames.RIGHT_ARM);
                GeoBone rightHand = this.getAnimationProcessor().getBone(DefaultBipedBoneIdents.RIGHT_HAND_BONE_IDENT);
                Vector3f armPose = new Vector3f(-30, -30, 10);
                armPose.mul(Mth.DEG_TO_RAD * isAnimatingDampener);
                transformStack.pushRotation(rightArm, armPose);

                Vector3f scythePos = new Vector3f(-5, 0, -48);
                scythePos.mul(Mth.DEG_TO_RAD * isAnimatingDampener);
                transformStack.pushRotation(rightHand, scythePos);

                if (!entity.isAnimating()) {
                    float walkDampener = (Mth.cos(limbSwing.y() * 0.6662F + (float) Math.PI) * 2.0F * limbSwing.x() * 0.5F) * -.75f;
                    transformStack.pushRotation(rightArm, walkDampener, 0, 0);
                }
            }
        }


        super.setCustomAnimations(entity, instanceId, animationState);
    }

    public void handleParticles(FireBossEntity entity) {
        GeoBone particleEmitter = this.getAnimationProcessor().getBone("particle_emitter");
        GeoBone body = this.getAnimationProcessor().getBone("body");
        if (entity.isSpawning()) {
            body.setTrackingMatrices(true);
            if (lastTick != entity.tickCount) {
                int particles = 10 * entity.spawnTimer / FireBossEntity.SPAWN_ANIM_TIME;
                lastTick = entity.tickCount;
                Vector3d pos = body.getWorldPosition();
                for (int i = 0; i < particles; i++) {
                    Vec3 random = Utils.getRandomVec3(0.5);
                    entity.level.addParticle(ParticleRegistry.EMBEROUS_ASH_PARTICLE.get(), pos.x + random.x, pos.y + 1 + random.y * 2, pos.z + random.z, 200, 0, 0);
                }
            }
        } else {
            body.setTrackingMatrices(false);
        }
        if (entity.isSoulMode()) {
            particleEmitter.setTrackingMatrices(true);
            if (lastTick != entity.tickCount) {
                lastTick = entity.tickCount;
                Vec3 movement = entity.getDeltaMovement().multiply(2, 0.125, 2).add(0, 0.1, 0);
                Vector3d headPos = particleEmitter.getWorldPosition().add(movement.x, movement.y, movement.z);
                for (int i = 0; i < 3; i++) {
                    Vec3 random = Utils.getRandomVec3(0.25).add(movement.multiply(2, 1, 2));
                    entity.level.addParticle(ParticleHelper.FIRE, headPos.x + random.x, headPos.y + random.y, headPos.z + random.z, 0, 0, 0);
                }
            }
        } else {
            particleEmitter.setTrackingMatrices(false);
        }
    }

    @Override
    protected Vector2f getLimbSwing(AbstractSpellCastingMob entity, WalkAnimationState walkAnimationState, float partialTick) {
        Vector2f swing = super.getLimbSwing(entity, walkAnimationState, partialTick);
        swing.mul(0.6f, 1f); return swing;
    }
    //    @Override
//    protected Vector2f getLimbSwing(AbstractSpellCastingMob entity, WalkAnimationState walkAnimationState, float partialTick) {
//        Vector2f swing = super.getLimbSwing(entity, walkAnimationState, partialTick);
//        if (!entity.onGround()) {
//            swing.mul(leglerp);
//            leglerp = Mth.lerp(.2f * partialTick, leglerp, 0.5f);
//        } else if (leglerp < 1) {
//            leglerp = Mth.lerp(.2f * partialTick, leglerp, 1.01f);
//        }
//        return swing;
//    }
}