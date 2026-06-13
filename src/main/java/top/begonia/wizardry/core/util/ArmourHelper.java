package top.begonia.wizardry.core.util;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.equipment.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.client.model.RobeArmourModel;
import top.begonia.wizardry.client.model.SageArmourModel;
import top.begonia.wizardry.client.model.WizardArmourModel;
import top.begonia.wizardry.core.constants.ElementEnum;
import top.begonia.wizardry.core.item.impl.WizardArmourItem;
import top.begonia.wizardry.core.registry.WizardryComponents;
import top.begonia.wizardry.core.registry.WizardrySounds;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ArmourHelper {
    public static final ResourceKey<? extends Registry<EquipmentAsset>> ROOT_ID = EquipmentAssets.ROOT_ID;
    private static final TagKey<Item> NO_REPAIR = ItemTags.create(Identifier.fromNamespaceAndPath(Wizardry.MODID, "non_repairable"));
    private static final Map<ResourceKey<EquipmentAsset>, ArmorModelSet<? extends HumanoidModel<?>>> BAKED_ARMOUR_REGISTRY = new ConcurrentHashMap<>();

    public static final class ModelLayers {
        private static final Set<ModelLayerLocation> ALL_MODELS = Sets.newHashSet();

        public static final ArmorModelSet<ModelLayerLocation> ROBE = registerArmorSet("robe");
        public static final ArmorModelSet<ModelLayerLocation> SAGE = registerArmorSet("sage");
        public static final ArmorModelSet<ModelLayerLocation> WIZARD = registerArmorSet("wizard");

        private static ModelLayerLocation createLocation(String model, String layer) {
            return new ModelLayerLocation(Identifier.fromNamespaceAndPath(Wizardry.MODID, model), layer);
        }

        private static ArmorModelSet<ModelLayerLocation> registerArmorSet(String modelId) {
            return new ArmorModelSet<>(register(modelId, "helmet"), register(modelId, "chestplate"), register(modelId, "leggings"), register(modelId, "boots"));
        }

        private static ModelLayerLocation register(String model, String layer) {
            ModelLayerLocation result = createLocation(model, layer);
            if (!ALL_MODELS.add(result)) {
                throw new IllegalStateException("Duplicate registration for " + result);
            } else {
                return result;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static @Nullable <S extends HumanoidRenderState, A extends HumanoidModel<S>> A getModelLayer(
            @NonNull ResourceKey<EquipmentAsset> assetId,
            EquipmentSlot slot
    ) {
        ArmorModelSet<? extends HumanoidModel<?>> modelSet = BAKED_ARMOUR_REGISTRY.get(assetId);
        if (modelSet != null) {
            return (A) modelSet.get(slot);
        }
        return null;
    }

    public enum ArmourMaterialType implements StringRepresentable {
        WIZARD(new MaterialBuilder<>("wizard", ModelLayers.WIZARD, WizardArmourModel::new)
                .defense(2, 4, 5, 2)
                .enchantment(15)
                .sound(WizardrySounds.ITEM_ARMOUR_EQUIP_SILK)
                .magicBonus(0.1f, 0.0f)
        ),
        SAGE(new MaterialBuilder<>("sage", ModelLayers.SAGE, SageArmourModel::new)
                .defense(2, 5, 6, 3)
                .enchantment(25)
                .sound(WizardrySounds.ITEM_ARMOUR_EQUIP_SAGE)
                .magicBonus(0.2f, 0.0f)
        ),
        BATTLEMAGE(new MaterialBuilder<>("battlemage", ModelLayers.ROBE, RobeArmourModel::new)
                .defense(3, 8, 6, 3)
                .enchantment(15)
                .sound(WizardrySounds.ITEM_ARMOUR_EQUIP_BATTLEMAGE).toughness(1.0F)
                .magicBonus(0.05f, 0.05f)
        ),
        WARLOCK(new MaterialBuilder<>("warlock", ModelLayers.ROBE, RobeArmourModel::new)
                .defense(2, 5, 4, 2)
                .enchantment(15)
                .sound(WizardrySounds.ITEM_ARMOUR_EQUIP_WARLOCK)
                .magicBonus(0.1f, 0.1f)
        );
        private final MaterialBuilder<?, ?> builder;

        public static final Codec<ArmourMaterialType> CODEC = StringRepresentable.fromValues(ArmourMaterialType::values);

        ArmourMaterialType(MaterialBuilder<?, ?> builder) {
            this.builder = builder;
        }

        public MaterialBuilder<?, ?> getBuilder() {
            return this.builder;
        }

        @Override
        public @NonNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public static class MaterialBuilder<S extends HumanoidRenderState, M extends HumanoidModel<S>> {
        private final String baseName;
        private final ArmorModelSet<ModelLayerLocation> layerLocations;
        private final Function<ModelPart, M> modelConstructor;
        private Map<ArmorType, Integer> defense = makeDefense(0, 0, 0, 0);
        private int enchantment = 10;
        private Holder<SoundEvent> sound = SoundEvents.ARMOR_EQUIP_LEATHER;
        private float toughness = 0.0F;
        private float knockback = 0.0F;
        private int durabilityMultiplier = 15;

        private float elementalCostReduction = 0.0f;
        private float cooldownReduction = 0.0f;

        public MaterialBuilder(String baseName, ArmorModelSet<ModelLayerLocation> layerLocations, Function<ModelPart, M> modelConstructor) {
            this.baseName = baseName;
            this.layerLocations = layerLocations;
            this.modelConstructor = modelConstructor;
        }

        public MaterialBuilder<S, M> defense(int boots, int legs, int chest, int helm) {
            this.defense = makeDefense(boots, legs, chest, helm);
            return this;
        }

        public MaterialBuilder<S, M> enchantment(int enchantment) {
            this.enchantment = enchantment;
            return this;
        }

        public MaterialBuilder<S, M> sound(Holder<SoundEvent> sound) {
            this.sound = sound;
            return this;
        }

        public MaterialBuilder<S, M> toughness(float toughness) {
            this.toughness = toughness;
            return this;
        }

        public MaterialBuilder<S, M> knockback(float knockback) {
            this.knockback = knockback;
            return this;
        }

        public MaterialBuilder<S, M> durability(int multiplier) {
            this.durabilityMultiplier = multiplier;
            return this;
        }

        public MaterialBuilder<S, M> magicBonus(float elementalCostReduction, float cooldownReduction) {
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

            BAKED_ARMOUR_REGISTRY.computeIfAbsent(assetId, _ -> ArmorModelSet.bake(
                    this.layerLocations,
                    Minecraft.getInstance().getEntityModels(),
                    this.modelConstructor
            ));

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
