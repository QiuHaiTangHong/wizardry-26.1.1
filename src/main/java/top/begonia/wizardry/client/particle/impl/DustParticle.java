package top.begonia.wizardry.client.particle.impl;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import top.begonia.wizardry.client.particle.AbstractParticle;
import top.begonia.wizardry.client.particle.WizardryParticleOptions;

public class DustParticle extends AbstractParticle {
    public DustParticle(WizardryParticleOptions options, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(options, level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        this.setSize(0.01F, 0.01F);
        this.quadSize *= (this.random.nextFloat() + 0.2F) * options.scale;
    }
}
