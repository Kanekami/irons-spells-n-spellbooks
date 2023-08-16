package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.VisualFallingBlockEntity;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EarthquakeAoe extends AoeEntity implements AntiMagicSusceptible {

    public EarthquakeAoe(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.reapplicationDelay = 25;
        this.setCircular();
    }
    public EarthquakeAoe(Level level) {
        this(EntityRegistry.EARTHQUAKE_AOE.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        var damageSource = SpellRegistry.EARTHQUAKE_SPELL.get().getDamageSource(this, getOwner());
        DamageSources.ignoreNextKnockback(target);
        target.hurt(damageSource, getDamage());
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, slownessAmplifier));
    }

    private int slownessAmplifier;

    public int getSlownessAmplifier() {
        return slownessAmplifier;
    }

    public void setSlownessAmplifier(int slownessAmplifier) {
        this.slownessAmplifier = slownessAmplifier;
    }

    @Override
    public float getParticleCount() {
        return 0f;
    }

    @Override
    public void ambientParticles() {

    }

    int waveAnim = -1;
    @Override
    public void tick() {
        super.tick();
        if (tickCount % 20 == 1) {
            this.playSound(SoundRegistry.EARTHQUAKE_LOOP.get(), 2f, .9f + random.nextFloat() * .15f);
        }
        if (tickCount % reapplicationDelay == 1) {
            //aligns with damage tick
            waveAnim = 0;
            this.playSound(SoundRegistry.EARTHQUAKE_IMPACT.get(), 1.5f, .9f + random.nextFloat() * .2f);
        }
        if (!level.isClientSide) {
            var radius = this.getRadius();
            if (waveAnim >= 0) {
                var circumference = waveAnim * 2 * 3.14f;
                int blocks = (int) circumference;
                float anglePerBlock = 360f / blocks;
                for (int i = 0; i < blocks; i++) {
                    Vec3 vec3 = new Vec3(
                            waveAnim * Mth.cos(anglePerBlock * i),
                            0,
                            waveAnim * Mth.sin(anglePerBlock * i)
                    );
                    BlockPos blockPos = new BlockPos(Utils.moveToRelativeGroundLevel(level, position().add(vec3), 4)).below();
                    createTremorBlock(blockPos);
                }
                if (waveAnim++ >= radius) {
                    waveAnim = -1;
                }
            }
            var level = this.level;
            int intensity = (int) (radius * radius * .09f);
            for (int i = 0; i < intensity; i++) {
                Vec3 vec3 = this.position().add(uniformlyDistributedPointInRadius(radius));
                BlockPos blockPos = new BlockPos(Utils.moveToRelativeGroundLevel(level, vec3, 4)).below();
                createTremorBlock(blockPos);
            }
            //IronsSpellbooks.LOGGER.debug("Earthquake ghostFallingblock: {} {}", blockPos, level.getBlockState(blockPos));
            //VisualFallingBlockEntity fallingblockentity = new VisualFallingBlockEntity(level, blockPos.getX() + 0.5D, blockPos.getY() + 0.55, blockPos.getZ() + 0.5D, level.getBlockState(blockPos));
            //level.addFreshEntity(fallingblockentity);
        }
    }

    protected void createTremorBlock(BlockPos blockPos) {
        if (level.getBlockState(blockPos.below()).isFaceSturdy(level, blockPos, Direction.UP)) {
            var fallingblockentity = new VisualFallingBlockEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), level.getBlockState(blockPos));
            fallingblockentity.setDeltaMovement(0, .1 + random.nextFloat() * .2, 0);
            level.addFreshEntity(fallingblockentity);
        }
    }

    protected Vec3 uniformlyDistributedPointInRadius(float r) {
        var distance = r * (1 - this.random.nextFloat() * this.random.nextFloat());
        var theta = this.random.nextFloat() * 6.282f; // two pi :nerd:
        return new Vec3(
                distance * Mth.cos(theta),
                .2f,
                distance * Mth.sin(theta)
        );
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 3F);
    }

    @Override
    public ParticleOptions getParticle() {
        return ParticleTypes.ENTITY_EFFECT;
    }

    @Override
    public void onAntiMagic(MagicData magicData) {
        discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("Slowness", slownessAmplifier);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.slownessAmplifier = pCompound.getInt("Slowness");
    }
}
