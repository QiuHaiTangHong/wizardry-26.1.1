package top.begonia.wizardry.client.particle.impl;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.client.particle.AbstractParticle;
import top.begonia.wizardry.client.particle.WizardryParticleOptions;

public class SparkParticle extends AbstractParticle {
    private final SpriteSet sprites;
    private final int animationOffset;

    public SparkParticle(
            @NonNull WizardryParticleOptions options,
            ClientLevel level,
            double x, double y, double z,
            double xd, double yd, double zd,
            @NonNull SpriteSet sprites
    ) {
        super(options, level, x, y, z, xd, yd, zd, sprites);
        this.sprites = sprites;
        this.animationOffset = 4;
        this.quadSize *= 1.4F;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;

        this.hasPhysics = false;
        this.lifetime = 3;
        this.updateLightningSprite();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.updateLightningSprite();
        }
    }

    private void updateLightningSprite() {
        int frameInGroup = Math.min(this.age, 3);
        int absoluteFrameIndex = this.animationOffset + frameInGroup;
        TextureAtlasSprite sprite = this.getSpriteFromIndex(absoluteFrameIndex);
        this.setSprite(sprite);
    }

    private TextureAtlasSprite getSpriteFromIndex(int index) {
        return this.sprites.get(index, 31);
    }
}
