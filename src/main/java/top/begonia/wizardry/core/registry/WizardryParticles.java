package top.begonia.wizardry.core.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.client.particle.WizardryParticleType;

public final class WizardryParticles {
    private WizardryParticles() {
    }

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Wizardry.MODID);

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> BEAM =
            PARTICLES.register("beam", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> BLOCK_HIGHLIGHT =
            PARTICLES.register("block_highlight", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> BUFF =
            PARTICLES.register("buff", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> CLOUD =
            PARTICLES.register("cloud", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> DARK_MAGIC =
            PARTICLES.register("dark_magic", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> DUST =
            PARTICLES.register("dust", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> FLASH =
            PARTICLES.register("flash", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> GUARDIAN_BEAM =
            PARTICLES.register("guardian_beam", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> ICE =
            PARTICLES.register("ice", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> LEAF =
            PARTICLES.register("leaf", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> LIGHTNING =
            PARTICLES.register("lightning", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> LIGHTNING_PULSE =
            PARTICLES.register("lightning_pulse", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> MAGIC_BUBBLE =
            PARTICLES.register("magic_bubble", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> MAGIC_FIRE =
            PARTICLES.register("magic_fire", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> PATH =
            PARTICLES.register("path", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> SCORCH =
            PARTICLES.register("scorch", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> SNOW =
            PARTICLES.register("snow", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> SPARK =
            PARTICLES.register("spark", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> SPARKLE =
            PARTICLES.register("sparkle", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> SPHERE =
            PARTICLES.register("sphere", () -> new WizardryParticleType(false));

    public static final DeferredHolder<ParticleType<?>, WizardryParticleType> VINE =
            PARTICLES.register("vine", () -> new WizardryParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
