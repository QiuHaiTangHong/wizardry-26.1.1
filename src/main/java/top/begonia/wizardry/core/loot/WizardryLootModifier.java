package top.begonia.wizardry.core.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.Wizardry;

import java.util.Set;

@EventBusSubscriber(modid = Wizardry.MODID)
public final class WizardryLootModifier {
    private static final ResourceKey<LootTable> WIZARD_DUNG_ADDITIONS = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Wizardry.MODID, "chests/dungeon_additions")
    );

    private static final ResourceKey<LootTable> JUNGLE_DISPENSER_ADDITIONS = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Wizardry.MODID, "chests/jungle_dispenser_additions")
    );

    public static final ResourceKey<LootTable> JUNK_ADDITIONS = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Wizardry.MODID, "gameplay/fishing/junk_additions")
    );

    public static final ResourceKey<LootTable> TREASURE_ADDITIONS = ResourceKey.create(
            Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(Wizardry.MODID, "gameplay/fishing/treasure_additions")
    );

    private static final Set<ResourceKey<LootTable>> VANILLA_DUNGEONS = Set.of(
            BuiltInLootTables.SIMPLE_DUNGEON,
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.DESERT_PYRAMID,
            BuiltInLootTables.END_CITY_TREASURE
    );

    @SubscribeEvent
    public static void onLootTableLoad(@NonNull LootTableLoadEvent event) {
        ResourceKey<LootTable> key = event.getKey();
        if (VANILLA_DUNGEONS.contains(key)) {
            injectPoolDirectly(event.getTable(), WIZARD_DUNG_ADDITIONS, "wizardry_dungeon_addon");
        }

        if (key.equals(BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER)) {
            injectPoolDirectly(event.getTable(), JUNGLE_DISPENSER_ADDITIONS, "wizardry_dispenser_addon");
        }

        if (key.equals(BuiltInLootTables.FISHING_JUNK)) {
            injectPoolDirectly(event.getTable(), JUNK_ADDITIONS, "wizardry_fishing_junk_addon");
        } else if (key.equals(BuiltInLootTables.FISHING_TREASURE)) {
            injectPoolDirectly(event.getTable(), TREASURE_ADDITIONS, "wizardry_fishing_treasure_addon");
        }
    }

    private static void injectPoolDirectly(@NonNull LootTable lootTable, ResourceKey<LootTable> targetTable, String poolName) {
        LootPool additionalPool = LootPool.lootPool()
                .name(poolName)
                .setRolls(ConstantValue.exactly(1.0F))
                .add(NestedLootTable.lootTableReference(targetTable).setWeight(1))
                .build();
        lootTable.addPool(additionalPool);
    }
}
