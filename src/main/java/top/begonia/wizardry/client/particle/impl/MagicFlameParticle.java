package top.begonia.wizardry.client.particle.impl;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.client.particle.AbstractParticle;
import top.begonia.wizardry.client.particle.WizardryParticleOptions;

public class MagicFlameParticle extends AbstractParticle {
    public MagicFlameParticle(@NonNull WizardryParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, @NonNull SpriteSet sprites) {
        super(options, level, x, y, z, xd, yd, zd, sprites);
    }
}
