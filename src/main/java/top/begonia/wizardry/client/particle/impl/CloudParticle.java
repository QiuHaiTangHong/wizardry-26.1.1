package top.begonia.wizardry.client.particle.impl;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.client.particle.AbstractParticle;

public class CloudParticle extends AbstractParticle {
    private static final SingleQuadParticle.Layer CLOUD_LAYER = new SingleQuadParticle.Layer(
            true,
            TextureAtlas.LOCATION_PARTICLES,
            RenderPipelines.TRANSLUCENT_PARTICLE
    );

    public CloudParticle(
            ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites,
            RandomSource randomSource
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, randomSource);
        this.setColor(1.0F, 1.0F, 1.0F);
        this.lifetime = 48 + randomSource.nextInt(12);
        this.quadSize *= 6.0F;
        this.gravity = 0.0F;
        this.alpha = 0.0F;
        this.hasPhysics = false;
        this.setSprite(sprites.get(randomSource));
    }

    @Override
    protected SingleQuadParticle.@NonNull Layer getLayer() {
        return CLOUD_LAYER;
    }

    @Override
    public void tick() {
        super.tick();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float fadeTime = (float) this.lifetime * 0.3F;
        float fadeInAlpha = (float) this.age / fadeTime;
        float fadeOutAlpha = (float) (this.lifetime - this.age) / fadeTime;
        this.alpha = Mth.clamp(Math.min(fadeInAlpha, fadeOutAlpha), 0.0F, 1.0F);
    }
}
