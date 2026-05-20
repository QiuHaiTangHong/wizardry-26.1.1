package top.begonia.wizardry.core.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.core.registry.WizardryStructureProcessors;

import javax.annotation.Nullable;

public class MossifierTemplateProcessor extends StructureProcessor {
    public static final MapCodec<MossifierTemplateProcessor> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("mossiness").forGetter(p -> p.mossiness),
            ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("height_weight").forGetter(p -> p.heightWeight),
            Codec.INT.fieldOf("ground_level").forGetter(p -> p.groundLevel)
    ).apply(instance, MossifierTemplateProcessor::new));

    private final float mossiness;
    private final float heightWeight;
    private final int groundLevel;

    public MossifierTemplateProcessor(float mossiness, float heightWeight, int groundLevel) {
        this.mossiness = mossiness;
        this.heightWeight = heightWeight;
        this.groundLevel = groundLevel;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
            @NonNull LevelReader level,
            @NonNull BlockPos blockPos,
            @NonNull BlockPos relativePos,
            StructureTemplate.@NonNull StructureBlockInfo originalInfo,
            StructureTemplate.@NonNull StructureBlockInfo info,
            @NonNull StructurePlaceSettings settings
    ) {
        float chance = mossiness - heightWeight * (info.pos().getY() - groundLevel);
        if (settings.getRandom(info.pos()).nextFloat() < chance) {
            BlockState currentState = info.state();
            if (currentState.is(Blocks.COBBLESTONE)) {
                return new StructureTemplate.StructureBlockInfo(info.pos(), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), info.nbt());
            } else if (currentState.is(Blocks.STONE_BRICKS)) {
                return new StructureTemplate.StructureBlockInfo(info.pos(), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), info.nbt());
            }
        }

        return info;
    }

    @Override
    protected @NonNull StructureProcessorType<?> getType() {
        return WizardryStructureProcessors.MOSSIFIER_TEMPLATE.get();
    }
}
