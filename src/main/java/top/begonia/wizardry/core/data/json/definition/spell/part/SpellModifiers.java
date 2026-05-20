package top.begonia.wizardry.core.data.json.definition.spell.part;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SpellModifiers(
        Modifier potency,
        Modifier cost,
        Modifier chargeup,
        Modifier progression
) {
    public static final Codec<SpellModifiers> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Modifier.CODEC.optionalFieldOf("potency", new Modifier(1.0f)).forGetter(SpellModifiers::potency),
            Modifier.CODEC.optionalFieldOf("cost", new Modifier(1.0f)).forGetter(SpellModifiers::cost),
            Modifier.CODEC.optionalFieldOf("chargeup", new Modifier(1.0f)).forGetter(SpellModifiers::chargeup),
            Modifier.CODEC.optionalFieldOf("progression", new Modifier(1.0f)).forGetter(SpellModifiers::progression)
    ).apply(instance, SpellModifiers::new));

    public static final StreamCodec<FriendlyByteBuf, SpellModifiers> STREAM_CODEC = StreamCodec.composite(
            Modifier.STREAM_CODEC, SpellModifiers::potency,
            Modifier.STREAM_CODEC, SpellModifiers::cost,
            Modifier.STREAM_CODEC, SpellModifiers::chargeup,
            Modifier.STREAM_CODEC, SpellModifiers::progression,
            SpellModifiers::new
    );

    public SpellModifiers combine(SpellModifiers other) {
        return new SpellModifiers(
                this.potency.multiply(other.potency),
                this.cost.multiply(other.cost),
                this.chargeup.multiply(other.chargeup),
                this.progression.multiply(other.progression)
        );
    }

    public record Modifier(float value) {
        public static final Codec<Modifier> CODEC = Codec.FLOAT.xmap(
                Modifier::new,
                Modifier::value
        );
        public static final StreamCodec<FriendlyByteBuf, Modifier> STREAM_CODEC = ByteBufCodecs.FLOAT.map(
                Modifier::new,
                Modifier::value
        ).cast();

        public float amplified(float scalar) {
            return (this.value - 1) * scalar + 1;
        }

        public float value() {
            return value;
        }

        public Modifier multiply(Modifier other) {
            return new Modifier(this.value * other.value);
        }
    }
}
