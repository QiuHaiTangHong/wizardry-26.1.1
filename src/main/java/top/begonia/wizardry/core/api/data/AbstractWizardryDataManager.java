package top.begonia.wizardry.core.api.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.Wizardry;

import java.io.Reader;
import java.util.*;

public abstract class AbstractWizardryDataManager extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    protected final Map<Identifier, IDataParser<?>> parserRegistry = new HashMap<>();
    protected static volatile Map<Class<? extends IData>, Map<Identifier, ? extends IData>> storageSnapshot = Map.of();
    protected final static Codec<JsonElement> CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            json -> new Dynamic<>(JsonOps.INSTANCE, json)
    );
    protected final FileToIdConverter lister;

    public AbstractWizardryDataManager(FileToIdConverter path) {
        this.lister = path;
        this.loadParsersSPI();
    }

    private void loadParsersSPI() {
        Dist currentDist = this.getSupportedDist();
        @SuppressWarnings("rawtypes")
        ServiceLoader<IDataParser> loader = ServiceLoader.load(IDataParser.class, IDataParser.class.getClassLoader());
        for (IDataParser<?> parser : loader) {
            Dist targetDist = parser.getSupportedDist();
            if (targetDist != null && targetDist != currentDist) {
                continue;
            }

            Identifier id = parser.getIdentifier();
            if (id != null) {
                parserRegistry.put(id, parser);
                Wizardry.LOGGER.info("已注册 Wizardry 动态解析器 [{}] : {} -> {}",
                        currentDist.name(), id, parser.getClass().getName());
            }
        }
    }

    protected abstract Dist getSupportedDist();

    public static <T extends IData> Optional<T> getData(Identifier id, Class<T> expectedType) {
        Map<Class<? extends IData>, Map<Identifier, ? extends IData>> currentStorage = storageSnapshot;
        Map<Identifier, ? extends IData> typeMap = currentStorage.get(expectedType);
        if (typeMap != null) {
            Object data = typeMap.get(id);
            if (expectedType.isInstance(data)) {
                return Optional.of(expectedType.cast(data));
            }
        }
        return Optional.empty();
    }

    protected boolean listerFilter(Identifier finalId, Identifier location, Resource resource) {
        return true;
    }

    protected Identifier cleanseIdentifier(Identifier originalId) {
        return originalId;
    }

    @Override
    protected @NonNull Map<Identifier, JsonElement> prepare(@NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profilerFiller) {
        Map<Identifier, JsonElement> result = new HashMap<>();
        Codec<Optional<JsonElement>> conditionalCodec = ConditionalOps.createConditionalCodec(CODEC);
        for (Map.Entry<Identifier, Resource> entry : this.lister.listMatchingResources(resourceManager).entrySet()) {
            Identifier location = entry.getKey();
            Identifier originalId = this.lister.fileToId(location);
            Identifier finalId = this.cleanseIdentifier(originalId);
            if (!this.listerFilter(finalId, location, entry.getValue())) {
                Wizardry.LOGGER.debug("资源 '{}' (物理路径: '{}') 未通过 ListerFilter 测试，已跳过", finalId, location);
                continue;
            }
            try (Reader reader = entry.getValue().openAsReader()) {
                conditionalCodec.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).ifSuccess((parsed) -> {
                    if (parsed.isEmpty()) {
                        Wizardry.LOGGER.debug("跳过加载数据文件 '{}'，因为不满足 forge:conditional 加载条件", finalId);
                    } else if (result.putIfAbsent(finalId, parsed.get()) != null) {
                        throw new IllegalStateException("重复的 Wizardry 数据文件，洗涤后逻辑 ID 冲突: " + finalId);
                    }
                }).ifError((error) -> Wizardry.LOGGER.error("无法解析数据文件 '{}': {}", finalId, error));
            } catch (Exception e) {
                Wizardry.LOGGER.error("从物理路径 '{}' 解析 Wizardry 数据时发生异常", location, e);
            }
        }

        return result;
    }

    @Override
    protected void apply(@NonNull Map<Identifier, JsonElement> identifierJsonElementMap, @NonNull ResourceManager resourceManager, @NonNull ProfilerFiller profilerFiller) {
        Map<Class<? extends IData>, Map<Identifier, IData>> workingMap = new HashMap<>();
        int[] totalLoaded = new int[]{0};
        int failedCount = 0;

        for (Map.Entry<Identifier, JsonElement> entry : identifierJsonElementMap.entrySet()) {
            Identifier location = entry.getKey();
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) {
                Wizardry.LOGGER.warn("⚠️ 拒绝解析数据 '{}': 根节点必须是一个 JSON 对象", location);
                failedCount++;
                continue;
            }
            JsonObject jsonObject = element.getAsJsonObject();
            if (!jsonObject.has("parser")) {
                Wizardry.LOGGER.warn("⚠️ 拒绝解析数据 '{}': 找不到必填的 \"parser\" 标识字段", location);
                failedCount++;
                continue;
            }
            String parserStr = jsonObject.get("parser").getAsString();
            Identifier parserId = Identifier.tryParse(parserStr);
            if (parserId == null) {
                Wizardry.LOGGER.error("❌ 解析数据 '{}' 失败: 声明的 parser 命名空间格式非法 -> '{}'", location, parserStr);
                failedCount++;
                continue;
            }
            IDataParser<?> parser = parserRegistry.get(parserId);
            if (parser == null) {
                Wizardry.LOGGER.error("❌ 解析数据 '{}' 失败: 找不到已注册的解析器 '{}'。请检查 SPI 配置或当前端(Dist)是否支持！", location, parserId);
                failedCount++;
                continue;
            }
            try {
                IData result = parser.parser(element);
                if (result == null) {
                    Wizardry.LOGGER.error("❌ 解析数据 '{}' 失败: 解析器 [{}] 无法反序列化此数据，返回了 null (请检查控制台上的 Codec 具体报错)",
                            parser.getClass().getSimpleName(), location);
                    failedCount++;
                    continue;
                }
                workingMap.computeIfAbsent(result.getDataClass(), _ -> new HashMap<>()).put(location, result);
                totalLoaded[0]++;

            } catch (Exception e) {
                Wizardry.LOGGER.error("💥 解析数据 '{}' 时发生严重崩溃！正在使用的解析器: [{}]",
                        location, parser.getClass().getName(), e);
                failedCount++;
            }
        }
        Map<Class<? extends IData>, Map<Identifier, ? extends IData>> immutableStorage = new HashMap<>();
        workingMap.forEach((clazz, innerMap) -> immutableStorage.put(clazz, Map.copyOf(innerMap)));
        storageSnapshot = Map.copyOf(immutableStorage);
        if (failedCount > 0) {
            Wizardry.LOGGER.warn("{} 数据重载完成: 成功加载 {} 项配置，🌟 发现 {} 项配置解析失败（请查看上方警告日志）",
                    Wizardry.MODID, totalLoaded[0], failedCount);
        } else {
            Wizardry.LOGGER.info("{} 数据重载完成: 共通过类型隔离成功加载了 {} 项新配置",
                    Wizardry.MODID, totalLoaded[0]);
        }
    }
}
