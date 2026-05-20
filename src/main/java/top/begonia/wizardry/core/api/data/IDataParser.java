package top.begonia.wizardry.core.api.data;

import com.google.gson.JsonElement;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;

public interface IDataParser<T extends IData> {
    Dist getSupportedDist();

    Identifier getIdentifier();

    T parser(JsonElement json);
}
