package top.begonia.wizardry.core.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class RandomSpell extends LootItemConditionalFunction {
    public static final MapCodec<RandomSpell> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).and(
            instance.group(
                    Codec.BOOL.optionalFieldOf("ignore_weighting", false).forGetter(f -> f.ignoreWeighting),
                    Codec.FLOAT.optionalFieldOf("undiscovered_bias", 0.0f).forGetter(f -> f.undiscoveredBias)
            )
    ).apply(instance, (predicates, ignoreWeighting, undiscoveredBias) ->
            new RandomSpell(ignoreWeighting, undiscoveredBias, predicates)
    ));

    private final boolean ignoreWeighting;
    private final float undiscoveredBias;

    protected RandomSpell(boolean ignoreWeighting, float undiscoveredBias, List<LootItemCondition> predicates) {
        super(predicates);
        this.ignoreWeighting = ignoreWeighting;
        this.undiscoveredBias = undiscoveredBias;
    }

    @Override
    public @NonNull MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

    @Override
    protected @NonNull ItemStack run(@NonNull ItemStack stack, @NonNull LootContext context) {
        return stack;
    }
}
