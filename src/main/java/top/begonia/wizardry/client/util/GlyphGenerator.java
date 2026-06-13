package top.begonia.wizardry.client.util;

import net.minecraft.resources.Identifier;
import top.begonia.wizardry.core.registry.WizardrySpells;
import top.begonia.wizardry.core.spell.AbstractSpell;

import java.util.HashMap;
import java.util.Map;

public final class GlyphGenerator {
    private static Map<Identifier, String> randomNames = new HashMap<>();
    private static Map<Identifier, String> randomDescriptions = new HashMap<>();

    private static final GlyphGenerator INSTANCE = new GlyphGenerator();

    private GlyphGenerator() {
    }

    public static GlyphGenerator getInstance() {
        return INSTANCE;
    }

    public void update(Map<Identifier, String> names, Map<Identifier, String> descriptions) {
        randomNames = new HashMap<>(names);
        randomDescriptions = new HashMap<>(descriptions);
    }

    public String getGlyphName(AbstractSpell spell) {
        Identifier id = WizardrySpells.SPELLS.getRegistry().get().getKey(spell);
        return id != null ? randomNames.getOrDefault(id, "unknown") : "";
    }

    public String getGlyphDescription(AbstractSpell spell) {
        Identifier id = WizardrySpells.SPELLS.getRegistry().get().getKey(spell);
        return id != null ? randomDescriptions.getOrDefault(id, "unknown description") : "";
    }
}
