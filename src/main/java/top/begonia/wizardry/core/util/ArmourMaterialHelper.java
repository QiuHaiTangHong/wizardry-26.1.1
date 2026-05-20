package top.begonia.wizardry.core.util;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.core.constants.ElementEnum;
import top.begonia.wizardry.core.registry.WizardrySounds;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ArmourMaterialHelper {
    public static final ResourceKey<? extends Registry<EquipmentAsset>> ROOT_ID = EquipmentAssets.ROOT_ID;
    private static final TagKey<Item> NO_REPAIR = ItemTags.create(Identifier.fromNamespaceAndPath(Wizardry.MODID, "non_repairable"));
    private static final Map<Identifier, ModelLayerLocation> OUTER_LAYER_REGISTRY = new HashMap<>();
    private static final Map<Identifier, ModelLayerLocation> INNER_LAYER_REGISTRY = new HashMap<>();

    public static final class ModelLayer {
        public static final ModelLayerLocation ROBE_OUTER = create("robe", "outer");
        public static final ModelLayerLocation ROBE_INNER = create("robe", "inner");
        public static final ModelLayerLocation SAGE_OUTER = create("sage", "outer");
        public static final ModelLayerLocation SAGE_INNER = create("sage", "inner");
        public static final ModelLayerLocation WIZARD_OUTER = create("wizard", "outer");
        public static final ModelLayerLocation WIZARD_INNER = create("wizard", "inner");

        private static ModelLayerLocation create(String path, String target) {
            return new ModelLayerLocation(Identifier.fromNamespaceAndPath(Wizardry.MODID, path), target);
        }
    }

    public static final MaterialBuilder WIZARD = new MaterialBuilder("wizard", ModelLayer.WIZARD_OUTER, ModelLayer.WIZARD_INNER)
            .defense(2, 4, 5, 2).enchantment(15).sound(WizardrySounds.ITEM_ARMOUR_EQUIP_SILK);

    public static final MaterialBuilder SAGE = new MaterialBuilder("sage", ModelLayer.SAGE_OUTER, ModelLayer.SAGE_INNER)
            .defense(2, 5, 6, 3).enchantment(25).sound(WizardrySounds.ITEM_ARMOUR_EQUIP_SAGE);

    public static final MaterialBuilder BATTLEMAGE = new MaterialBuilder("battlemage", ModelLayer.ROBE_OUTER, ModelLayer.ROBE_INNER)
            .defense(3, 8, 6, 3).enchantment(15).sound(WizardrySounds.ITEM_ARMOUR_EQUIP_BATTLEMAGE).toughness(1.0F);

    public static final MaterialBuilder WARLOCK = new MaterialBuilder("warlock", ModelLayer.ROBE_OUTER, ModelLayer.ROBE_INNER)
            .defense(2, 5, 4, 2).enchantment(15).sound(WizardrySounds.ITEM_ARMOUR_EQUIP_WARLOCK);

    public static @Nullable ModelLayerLocation getModelLayer(Optional<ResourceKey<EquipmentAsset>> assetId, ArmorType armorType) {
        if (assetId.isEmpty()) return null;
        Identifier id = assetId.get().identifier();
        return (armorType == ArmorType.LEGGINGS) ? INNER_LAYER_REGISTRY.get(id) : OUTER_LAYER_REGISTRY.get(id);
    }

    public static class MaterialBuilder {
        private final String baseName;
        private final ModelLayerLocation outerLayer;
        private final ModelLayerLocation innerLayer;
        private Map<ArmorType, Integer> defense = makeDefense(0, 0, 0, 0);
        private int enchantment = 10;
        private Holder<SoundEvent> sound = SoundEvents.ARMOR_EQUIP_LEATHER;
        private float toughness = 0.0F;
        private float knockback = 0.0F;
        private int durabilityMultiplier = 15;

        public MaterialBuilder(String baseName, ModelLayerLocation outer, ModelLayerLocation inner) {
            this.baseName = baseName;
            this.outerLayer = outer;
            this.innerLayer = inner;
        }

        public MaterialBuilder defense(int boots, int legs, int chest, int helm) {
            this.defense = makeDefense(boots, legs, chest, helm);
            return this;
        }

        public MaterialBuilder enchantment(int enchantment) {
            this.enchantment = enchantment;
            return this;
        }

        public MaterialBuilder sound(Holder<SoundEvent> sound) {
            this.sound = sound;
            return this;
        }

        public MaterialBuilder toughness(float toughness) {
            this.toughness = toughness;
            return this;
        }

        public MaterialBuilder knockback(float knockback) {
            this.knockback = knockback;
            return this;
        }

        public MaterialBuilder durability(int multiplier) {
            this.durabilityMultiplier = multiplier;
            return this;
        }

        public ArmorMaterial build(@Nullable ElementEnum element) {
            String suffix = element == null ? "" : "_" + element.getSerializedName();
            Identifier loc = Identifier.fromNamespaceAndPath(Wizardry.MODID, baseName + "_armour" + suffix);
            ResourceKey<EquipmentAsset> assetId = ResourceKey.create(ROOT_ID, loc);

            OUTER_LAYER_REGISTRY.put(loc, this.outerLayer);
            INNER_LAYER_REGISTRY.put(loc, this.innerLayer);

            return new ArmorMaterial(
                    this.durabilityMultiplier,
                    this.defense,
                    this.enchantment,
                    this.sound,
                    this.toughness,
                    this.knockback,
                    NO_REPAIR,
                    assetId
            );
        }
    }

    private static Map<ArmorType, Integer> makeDefense(int boots, int legs, int chest, int helm) {
        return Map.of(ArmorType.BOOTS, boots, ArmorType.LEGGINGS, legs, ArmorType.CHESTPLATE, chest, ArmorType.HELMET, helm, ArmorType.BODY, 0);
    }
}
