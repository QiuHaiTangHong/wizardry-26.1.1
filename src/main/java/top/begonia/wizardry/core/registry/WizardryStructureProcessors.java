package top.begonia.wizardry.core.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.core.worldgen.MossifierTemplateProcessor;

public final class WizardryStructureProcessors {
    private WizardryStructureProcessors() {
    }

    private static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, Wizardry.MODID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<MossifierTemplateProcessor>> MOSSIFIER_TEMPLATE =
            STRUCTURE_PROCESSOR_TYPES.register(
                    "mossifier_template",
                    () -> () -> MossifierTemplateProcessor.CODEC
            );

    public static void register(IEventBus eventBus) {
        STRUCTURE_PROCESSOR_TYPES.register(eventBus);
    }
}
