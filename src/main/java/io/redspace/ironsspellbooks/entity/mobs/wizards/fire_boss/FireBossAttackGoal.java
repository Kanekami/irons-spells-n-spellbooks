package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackKeyframe;
import io.redspace.ironsspellbooks.entity.mobs.wizards.GenericAnimatedWarlockAttackGoal;
import io.redspace.ironsspellbooks.particle.FlameStrikeParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.Vec3;

public class FireBossAttackGoal extends GenericAnimatedWarlockAttackGoal<FireBossEntity> {
    private static final AttributeModifier MODIFIER_FIRE_BALLER = new AttributeModifier(IronsSpellbooks.id("fireballer"), 0.50, AttributeModifier.Operation.ADD_VALUE);

    @Override
    public float getStrafeMultiplier() {
        return 1.5f;
    }

    public FireBossAttackGoal(FireBossEntity abstractSpellCastingMob, double pSpeedModifier, int minAttackInterval, int maxAttackInterval) {
        super(abstractSpellCastingMob, pSpeedModifier, minAttackInterval, maxAttackInterval);
    }

    @Override
    /**
     * Expose to package as protected
     */
    protected void doMovement(double distanceSquared) {
        super.doMovement(distanceSquared);
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    @Override
    protected void onHitFrame(AttackKeyframe attackKeyframe, float meleeRange) {
        super.onHitFrame(attackKeyframe, meleeRange);
        if (attackKeyframe instanceof FireBossAttackKeyframe fireKeyframe) {
            boolean mirrored = fireKeyframe.swingData.mirrored();
            boolean vertical = fireKeyframe.swingData.vertical();
            Vec3 forward = mob.getForward();
            float reach = 2 * mob.getScale();
            Vec3 hitLocation = mob.getBoundingBox().getCenter().add(mob.getForward().multiply(reach, 0.5, reach));
            MagicManager.spawnParticles(mob.level,
                    new FlameStrikeParticleOptions((float) forward.x, (float) forward.y, (float) forward.z, mirrored, vertical, mob.getScale()), hitLocation.x, hitLocation.y, hitLocation.z, 1, 0, 0, 0, 0, true);
        }
    }

    @Override
    public void stop() {
        super.stop();
        mob.getAttribute(AttributeRegistry.CAST_TIME_REDUCTION).removeModifier(MODIFIER_FIRE_BALLER);
    }

    int fireballcooldown;

    @Override
    protected void doLunge(Vec3 vector, float meleeRange) {
        // due to greatly varying melee ranges based on specific attacks, we want to scale the lunge vector based on our distance to the target to effectively reach them
        // if the average "normal" distance is ~4 blocks, create ratio of current:normal and scale the vector by said amount
        float avgMultiplier = mob.distanceTo(target) * .25f;
        super.doLunge(vector.scale(avgMultiplier), meleeRange);
    }

    @Override
    protected void handleAttackLogic(double distanceSquared) {
        var meleeRange = meleeRange();
        if (fireballcooldown > 0) {
            // poor man's way to clean up the fireball attribute
            if (fireballcooldown == 20 * 10 - 20) {
                mob.getAttribute(AttributeRegistry.CAST_TIME_REDUCTION).removeModifier(MODIFIER_FIRE_BALLER);
            }
            fireballcooldown--;
        } else {
            // if we are very ranged (and preferably high in the sky) launch down a fireball
            if (!mob.onGround() && distanceSquared > meleeRange * meleeRange * 2 * 2) {
                if (!isActing()) {
                    // insta-cast that fireball
                    mob.getAttribute(AttributeRegistry.CAST_TIME_REDUCTION).addOrUpdateTransientModifier(MODIFIER_FIRE_BALLER);
                    mob.initiateCastSpell(SpellRegistry.FIREBALL_SPELL.get(), 5);
                    fireballcooldown = 20 * 10;
                    return;
                }
            }
        }
        if (meleeAnimTimer > 0 && currentAttack != null) {
            // in order to make more seamless animation -> action transitions, we cut our ai pause short if we have encountered our last frame
            // the animation still plays out, creating a smoother overall transition
            int shortcut = 5;
            if (meleeAnimTimer < shortcut) {
                if (currentAttack.attacks.keySet().intStream().noneMatch(i -> i > currentAttack.lengthInTicks - shortcut)) {
                    meleeAnimTimer = 0;
                }
            }
        }
        super.handleAttackLogic(distanceSquared);
    }

    @Override
    protected double movementSpeed() {
        return this.meleeMoveSpeedModifier;
    }

    @Override
    public void playSwingSound() {
        mob.playSound(SoundRegistry.HELLRAZOR_SWING.get(), 1, Mth.randomBetweenInclusive(mob.getRandom(), 9, 11) * .1f);
    }
}
