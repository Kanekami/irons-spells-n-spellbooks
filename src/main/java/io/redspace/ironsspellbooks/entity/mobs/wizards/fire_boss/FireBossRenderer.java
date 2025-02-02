package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss;


import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.util.Color;

public class FireBossRenderer extends AbstractSpellCastingMobRenderer {

    public FireBossRenderer(EntityRendererProvider.Context context) {
        super(context, new FireBossModel());
        this.shadowRadius = 0.65f;
        addRenderLayer(new FireBossSoulLayer(this));
        addRenderLayer(new FireBossFlameLayer(this));
    }

    @Override
    public void render(AbstractSpellCastingMob entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity instanceof FireBossEntity fireBossEntity && fireBossEntity.isSpawning()) {
            float f = fireBossEntity.getSpawnWalkPercent(partialTick);
            if (f == 0) {
                // not visible, dont render
                return;
            }
            shadowRadius = Mth.lerp(f, 2, .65f);
            shadowStrength = Mth.lerp(f, 0, 1);
        } else {
            shadowStrength = 1;
            shadowRadius = .65f;
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, Math.clamp(packedLight + 100, 0, LightTexture.FULL_BLOCK));
    }

    @Override
    public int getPackedOverlay(AbstractSpellCastingMob animatable, float u, float partialTick) {
        //disable hurt/death red flashing
        return OverlayTexture.NO_OVERLAY;
    }

    static final int deathFadeTime = 120;

    @Override
    public Color getRenderColor(AbstractSpellCastingMob animatable, float partialTick, int packedLight) {
        Color color = super.getRenderColor(animatable, partialTick, packedLight);
        float f = 1f;
        if (animatable.deathTime > 160 - deathFadeTime) {
            f = Mth.clamp((160 - animatable.deathTime) / (float) deathFadeTime, 0, 1f);
        } else if (animatable instanceof FireBossEntity fireBoss && fireBoss.isSpawning()) {
            f = fireBoss.getSpawnWalkPercent(partialTick);
        }
        if (!animatable.isInvisible() && f != 1) {
            color = new Color(RenderHelper.colorf(1f, 1f, 1f, f));
        }

        return color;
    }


    @Override
    public RenderType getRenderType(AbstractSpellCastingMob animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        if (animatable.isDeadOrDying() || animatable instanceof FireBossEntity fireBoss && (fireBoss.isSpawning() || fireBoss.isDespawning())) {
            return RenderType.entityTranslucent(texture);
        }
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    protected float getDeathMaxRotation(AbstractSpellCastingMob animatable) {
        return 0;
    }
}
