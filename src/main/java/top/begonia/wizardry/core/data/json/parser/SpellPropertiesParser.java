package top.begonia.wizardry.core.data.json.parser;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.core.api.data.IDataParser;
import top.begonia.wizardry.core.data.json.definition.spell.SpellProperties;

public class SpellPropertiesParser implements IDataParser<SpellProperties> {
    public static final Identifier PARSER_NAME = Identifier.fromNamespaceAndPath(Wizardry.MODID, "spell_properties_parser");

    @Override
    public Dist getSupportedDist() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public Identifier getIdentifier() {
        return PARSER_NAME;
    }

    @Override
    public SpellProperties parser(JsonElement json) {
        return SpellProperties.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> Wizardry.LOGGER.error("法术配置解析出错: {}", error))
                .orElse(null);
    }
}
