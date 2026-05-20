package top.begonia.wizardry.client.util;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GenericParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet spriteSet;
    private final ParticleConstructor particleConstructor;

    public GenericParticleProvider(SpriteSet spriteSet, ParticleConstructor particleConstructor) {
        this.spriteSet = spriteSet;
        this.particleConstructor = particleConstructor;
    }

    @Override
    public @Nullable Particle createParticle(
            @NonNull SimpleParticleType particleType,
            @NonNull ClientLevel clientLevel,
            double x, double y, double z,
            double xa, double ya, double za,
            @NonNull RandomSource randomSource
    ) {
        return this.particleConstructor.create(clientLevel, x, y, z, xa, ya, za, this.spriteSet, randomSource);
    }

    @FunctionalInterface
    public interface ParticleConstructor {
        Particle create(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteSet, RandomSource randomSource);
    }
}
