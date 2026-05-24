package top.begonia.wizardry.core.util;

import com.mojang.serialization.Codec;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.equipment.*;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.core.constants.ElementEnum;
import top.begonia.wizardry.core.item.impl.WizardArmourItem;
import top.begonia.wizardry.core.registry.WizardryComponents;
import top.begonia.wizardry.core.registry.WizardrySounds;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ArmourHelper {
    public static final ResourceKey<? extends Registry<EquipmentAsset>> ROOT_ID = EquipmentAssets.ROOT_ID;
    private static final TagKey<Item> NO_REPAIR = ItemTags.create(Identifier.fromNamespaceAndPath(Wizardry.MODID, "non_repairable"));
    private static final Map<Identifier, ModelLayerLocation> OUTER_LAYER_REGISTRY = new ConcurrentHashMap<>();
    private static final Map<Identifier, ModelLayerLocation> INNER_LAYER_REGISTRY = new ConcurrentHashMap<>();

    public static final class ModelLayer {
        public static final ModelLayerLocation ROBE_OUTER = create("robe", "outer");
        public static final ModelLayerLocation ROBE_INNER = create("robe", "inner");
        public static final ModelLayerLocation SAGE_OUTER = create("sage", "outer");
        public static final ModelLayerLocation SAGE_INNER = create("sage", "inner");
        public static final ModelLayerLocation WIZARD_OUTER = create("wizard", "outer");
        public static final ModelLayerLocation WIZARD_INNER = create("wizard", "inner");

        @Contract("_, _ -> new")
        private static @NonNull ModelLayerLocation create(String path, String target) {
            return new ModelLayerLocation(Identifier.fromNamespaceAndPath(Wizardry.MODID, path), target);
        }
    }

    public enum ArmourMaterialType implements StringRepresentable {
        WIZARD(new MaterialBuilder("wizard", ModelLayer.WIZARD_OUTER, ModelLayer.WIZARD_INNER)
                .defense(2, 4, 5, 2)
                .enchantment(15)
                .sound(WizardrySounds.ITEM_ARMOUR_EQUIP_SILK)
                .magicBonus(0.1f, 0.0f)
        ),
        SAGE(new MaterialBuilder("sage", ModelLayer.SAGE_OUTER, ModelLayer.SAGE_INNER)
                .defense(2, 5, 6, 3)
                .enchantment(25)
                .sound(WizardrySounds.ITEM_ARMOUR_EQUIP_SAGE)
                .magicBonus(0.2f, 0.0f)
        ),
        BATTLEMAGE(new MaterialBuilder("battlemage", ModelLayer.ROBE_OUTER, ModelLayer.ROBE_INNER)
                .defense(3, 8, 6, 3)
                .enchantment(15)
                .sound(WizardrySounds.ITEM_ARMOUR_EQUIP_BATTLEMAGE).toughness(1.0F)
                .magicBonus(0.05f, 0.05f)
        ),
        WARLOCK(new MaterialBuilder("warlock", ModelLayer.ROBE_OUTER, ModelLayer.ROBE_INNER)
                .defense(2, 5, 4, 2)
                .enchantment(15)
                .sound(WizardrySounds.ITEM_ARMOUR_EQUIP_WARLOCK)
                .magicBonus(0.1f, 0.1f)
        );
        private final MaterialBuilder builder;

        public static final Codec<ArmourMaterialType> CODEC = StringRepresentable.fromValues(ArmourMaterialType::values);

        ArmourMaterialType(MaterialBuilder builder) {
            this.builder = builder;
        }

        public MaterialBuilder getBuilder() {
            return this.builder;
        }

        @Override
        public @NonNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public static @Nullable ModelLayerLocation getModelLayer(@NonNull ResourceKey<EquipmentAsset> assetId, ArmorType armorType) {
        Identifier id = assetId.identifier();
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

        private float elementalCostReduction = 0.0f;
        private float cooldownReduction = 0.0f;

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

        public MaterialBuilder magicBonus(float elementalCostReduction, float cooldownReduction) {
            this.elementalCostReduction = elementalCostReduction;
            this.cooldownReduction = cooldownReduction;
            return this;
        }

        public String getBaseName() {
            return this.baseName;
        }

        public float getElementalCostReduction() {
            return this.elementalCostReduction;
        }

        public float getCooldownReduction() {
            return this.cooldownReduction;
        }

        public ArmorMaterial build(@NonNull ElementEnum element) {
            String suffix = "_" + element.getSerializedName();
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

    private static @NonNull Map<ArmorType, Integer> makeDefense(int boots, int legs, int chest, int helm) {
        Map<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, boots);
        map.put(ArmorType.LEGGINGS, legs);
        map.put(ArmorType.CHESTPLATE, chest);
        map.put(ArmorType.HELMET, helm);
        map.put(ArmorType.BODY, 0);
        return map;
    }

    public static @NonNull ItemStack generateArmour(
            WizardArmourItem armourItem,
            ElementEnum element,
            ArmourHelper.@NonNull ArmourMaterialType armourMaterialType,
            @NonNull ArmorType armorType
    ) {
        ItemStack itemStack = new ItemStack(armourItem);
        ArmorMaterial armorMaterial = armourMaterialType.getBuilder().build(element);
        itemStack.set(WizardryComponents.ARMOR_MATERIAL_TYPE, armourMaterialType);
        itemStack.set(WizardryComponents.ARMOR_TYPE, armorType);
        itemStack.set(WizardryComponents.ELEMENT, element);
        itemStack.set(DataComponents.MAX_DAMAGE, armorType.getDurability(armorMaterial.durability()));
        itemStack.set(DataComponents.MAX_STACK_SIZE, 1);
        itemStack.set(DataComponents.DAMAGE, 0);
        itemStack.set(DataComponents.ENCHANTABLE, new Enchantable(armorMaterial.enchantmentValue()));
        itemStack.set(DataComponents.ATTRIBUTE_MODIFIERS, armorMaterial.createAttributes(armorType));
        itemStack.set(DataComponents.EQUIPPABLE, Equippable.builder(armorType.getSlot()).setEquipSound(armorMaterial.equipSound()).setAsset(armorMaterial.assetId()).setAllowedEntities(EntityType.PLAYER).build());
        return itemStack;
    }
}
