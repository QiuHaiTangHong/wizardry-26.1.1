package top.begonia.wizardry.core.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.core.loot.RandomSpell;

public final class WizardryLoots {
    private WizardryLoots() {
    }

    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Wizardry.MODID);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<RandomSpell>> RANDOM_SPELL =
            LOOT_FUNCTIONS.register("random_spell", () -> RandomSpell.CODEC);

    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Wizardry.MODID);

    public static void register(IEventBus eventBus) {
        LOOT_FUNCTIONS.register(eventBus);
        LOOT_MODIFIERS.register(eventBus);
    }
}
