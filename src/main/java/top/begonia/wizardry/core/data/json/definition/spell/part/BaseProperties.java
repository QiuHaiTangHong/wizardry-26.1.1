package top.begonia.wizardry.core.data.json.definition.spell.part;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

public record BaseProperties(
        Map<String, Float> baseProperties
) {
    public static final Codec<BaseProperties> CODEC = Codec.unboundedMap(Codec.STRING, Codec.FLOAT)
            .xmap(BaseProperties::new, BaseProperties::baseProperties);
    public static final StreamCodec<FriendlyByteBuf, BaseProperties> STREAM_CODEC =
            ByteBufCodecs.fromCodec(CODEC).cast();

    public static BaseProperties getDefault() {
        return new BaseProperties(Map.of());
    }
}