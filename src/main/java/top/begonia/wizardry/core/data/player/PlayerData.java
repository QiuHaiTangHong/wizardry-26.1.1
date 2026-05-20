package top.begonia.wizardry.core.data.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import top.begonia.wizardry.core.constants.TierEnum;
import top.begonia.wizardry.core.registry.WizardrySpells;
import top.begonia.wizardry.core.spell.AbstractSpell;

import java.util.*;

public record PlayerData(
        Set<AbstractSpell> spellsDiscovered,
        TierEnum maxTierReached,
        Set<UUID> allies,
        Set<String> allyNames,
        Map<String, Integer> imbuementDurations,
        List<RecentSpellEntry> recentSpells
) {
    private static PlayerData defaultInstance;

    public static PlayerData getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new PlayerData(
                    new HashSet<>(List.of(WizardrySpells.MAGIC_MISSILE.get())),
                    TierEnum.NOVICE,
                    new HashSet<>(),
                    new HashSet<>(),
                    new HashMap<>(),
                    new ArrayList<>()
            );
        }
        return defaultInstance;
    }

    public static final MapCodec<PlayerData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WizardrySpells.SPELLS.getRegistry().get().byNameCodec().listOf()
                    .<Set<AbstractSpell>>xmap(HashSet::new, ArrayList::new)
                    .fieldOf("discoveredSpells").forGetter(PlayerData::spellsDiscovered),
            TierEnum.CODEC.fieldOf("maxTierReached").forGetter(PlayerData::maxTierReached),
            UUIDUtil.CODEC.listOf().<Set<UUID>>xmap(HashSet::new, ArrayList::new)
                    .fieldOf("allies").forGetter(PlayerData::allies),
            Codec.STRING.listOf().<Set<String>>xmap(HashSet::new, ArrayList::new)
                    .fieldOf("allyNames").forGetter(PlayerData::allyNames),
            Codec.unboundedMap(Codec.STRING, Codec.INT)
                    .fieldOf("imbuements").forGetter(PlayerData::imbuementDurations),
            RecentSpellEntry.CODEC.listOf()
                    .fieldOf("recentSpells").forGetter(PlayerData::recentSpells)
    ).apply(instance, PlayerData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.registry(WizardrySpells.SPELLS_KEY)),
            PlayerData::spellsDiscovered,
            TierEnum.STREAM_CODEC,
            PlayerData::maxTierReached,
            ByteBufCodecs.collection(HashSet::new, UUIDUtil.STREAM_CODEC),
            PlayerData::allies,
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.stringUtf8(32767)),
            PlayerData::allyNames,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.stringUtf8(32767), ByteBufCodecs.VAR_INT),
            PlayerData::imbuementDurations,
            RecentSpellEntry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            PlayerData::recentSpells,
            PlayerData::new
    );

    public record RecentSpellEntry(AbstractSpell spell, long timestamp) {
        public static final Codec<RecentSpellEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                WizardrySpells.SPELLS.getRegistry().get().byNameCodec().fieldOf("spell").forGetter(RecentSpellEntry::spell),
                Codec.LONG.fieldOf("timestamp").forGetter(RecentSpellEntry::timestamp)
        ).apply(inst, RecentSpellEntry::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, RecentSpellEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(WizardrySpells.SPELLS_KEY),
                RecentSpellEntry::spell,
                ByteBufCodecs.VAR_LONG,
                RecentSpellEntry::timestamp,
                RecentSpellEntry::new
        );
    }
}
