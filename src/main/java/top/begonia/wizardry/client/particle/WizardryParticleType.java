package top.begonia.wizardry.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.NonNull;

public final class WizardryParticleType extends ParticleType<WizardryParticleOptions> {
    private final MapCodec<WizardryParticleOptions> codec = MapCodec.unit(new WizardryParticleOptions(this));
    private final StreamCodec<RegistryFriendlyByteBuf, WizardryParticleOptions> streamCodec = StreamCodec.of(
            WizardryParticleType::write,
            this::read
    );

    public WizardryParticleType(boolean overrideLimiter) {
        super(overrideLimiter);
    }

    @Override
    public @NonNull MapCodec<WizardryParticleOptions> codec() {
        return this.codec;
    }

    @Override
    public @NonNull StreamCodec<? super RegistryFriendlyByteBuf, WizardryParticleOptions> streamCodec() {
        return this.streamCodec;
    }

    private static void write(@NonNull RegistryFriendlyByteBuf buf, @NonNull WizardryParticleOptions options) {
        buf.writeFloat(options.r);
        buf.writeFloat(options.g);
        buf.writeFloat(options.b);
        buf.writeFloat(options.fr);
        buf.writeFloat(options.fg);
        buf.writeFloat(options.fb);
        buf.writeFloat(options.scale);
        buf.writeInt(options.lifetime);
    }

    private @NonNull WizardryParticleOptions read(@NonNull RegistryFriendlyByteBuf buf) {
        WizardryParticleOptions options = new WizardryParticleOptions(this);
        options.clr(buf.readFloat(), buf.readFloat(), buf.readFloat());
        options.fade(buf.readFloat(), buf.readFloat(), buf.readFloat());
        options.scale(buf.readFloat());
        options.time(buf.readInt());
        return options;
    }
}
