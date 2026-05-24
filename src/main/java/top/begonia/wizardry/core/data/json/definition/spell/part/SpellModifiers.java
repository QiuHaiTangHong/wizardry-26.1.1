package top.begonia.wizardry.core.data.json.definition.spell.part;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SpellModifiers {
    private final Modifier potency;
    private final Modifier cost;
    private final Modifier chargeup;
    private final Modifier progression;
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

    public SpellModifiers() {
        this.potency = new Modifier(1.0f);
        this.cost = new Modifier(1.0f);
        this.chargeup = new Modifier(1.0f);
        this.progression = new Modifier(1.0f);
    }

    public SpellModifiers(Modifier potency, Modifier cost, Modifier chargeup, Modifier progression) {
        this.potency = potency;
        this.cost = cost;
        this.chargeup = chargeup;
        this.progression = progression;
    }

    public SpellModifiers combine(SpellModifiers other) {
        this.potency.multiply(other.potency);
        this.cost.multiply(other.cost);
        this.chargeup.multiply(other.chargeup);
        this.progression.multiply(other.progression);
        return this;
    }

    public Modifier potency() {
        return potency;
    }

    public Modifier cost() {
        return cost;
    }

    public Modifier chargeup() {
        return chargeup;
    }

    public Modifier progression() {
        return progression;
    }

    public void reset() {
        this.potency.setValue(1.0f);
        this.cost.setValue(1.0f);
        this.chargeup.setValue(1.0f);
        this.progression.setValue(1.0f);
    }

    public static class Modifier {

        private float value; // 🌟 核心改为可变

        public Modifier(float value) {
            this.value = value;
        }

        public static final Codec<Modifier> CODEC = Codec.FLOAT.xmap(Modifier::new, Modifier::value);
        public static final StreamCodec<FriendlyByteBuf, Modifier> STREAM_CODEC = ByteBufCodecs.FLOAT.map(Modifier::new, Modifier::value).cast();

        public float value() {
            return this.value;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public float amplified(float scalar) {
            return (this.value - 1) * scalar + 1;
        }

        public Modifier multiply(Modifier other) {
            this.value *= other.value;
            return this;
        }
    }
}
