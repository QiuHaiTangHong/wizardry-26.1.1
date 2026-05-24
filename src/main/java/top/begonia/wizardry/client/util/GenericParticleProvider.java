package top.begonia.wizardry.client.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.begonia.wizardry.client.particle.WizardryParticleOptions;

public class GenericParticleProvider implements ParticleProvider<WizardryParticleOptions> {
    private final SpriteSet spriteSet;
    private final ParticleConstructor particleConstructor;

    public GenericParticleProvider(SpriteSet spriteSet, ParticleConstructor particleConstructor) {
        this.spriteSet = spriteSet;
        this.particleConstructor = particleConstructor;
    }

    @Override
    public @Nullable Particle createParticle(@NonNull WizardryParticleOptions options, @NonNull ClientLevel clientLevel, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, @NonNull RandomSource randomSource) {
        return this.particleConstructor.create(options, clientLevel, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
    }

    @FunctionalInterface
    public interface ParticleConstructor {
        Particle create(WizardryParticleOptions options, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet);
    }
}
