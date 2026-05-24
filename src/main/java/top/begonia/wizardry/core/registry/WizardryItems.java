package top.begonia.wizardry.core.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.core.block.*;
import top.begonia.wizardry.core.constants.ArtefactTypeEnum;
import top.begonia.wizardry.core.constants.ElementEnum;
import top.begonia.wizardry.core.constants.ManaFlaskTypeEnum;
import top.begonia.wizardry.core.constants.TierEnum;
import top.begonia.wizardry.core.item.impl.*;
import top.begonia.wizardry.core.spell.AbstractSpell;
import top.begonia.wizardry.core.util.ArmourMaterialHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class WizardryItems {
    private WizardryItems() {
    }

    public static final List<DeferredItem<? extends Item>> WIZARD_ARMOUR_ITEMS = new ArrayList<>();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Wizardry.MODID);

    public static <T extends Item> @NonNull DeferredItem<T> registerItem(String name, Function<Item.Properties, T> itemFactory, WizardryCreativeTabs.TabsEnum tab) {
        DeferredItem<T> item = ITEMS.register(name, (identifier) -> {
            Item.Properties properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, identifier));
            return itemFactory.apply(properties);
        });
        WizardryCreativeTabs.addToTabs(tab, item);
        return item;
    }

    private static @NonNull DeferredItem<WizardArmourItem> registerItem(String name, ArmourMaterialHelper.@NonNull MaterialBuilder builder, @NonNull ArmorType type, @Nullable ElementEnum element) {
        ArmorMaterial material = builder.build(element);
        Item.Properties properties = new Item.Properties().durability(type.getDurability(material.durability())).enchantable(material.enchantmentValue()).attributes(material.createAttributes(type)).component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot()).setEquipSound(material.equipSound()).setAsset(material.assetId()).setAllowedEntities(EntityType.PLAYER).build());
        DeferredItem<WizardArmourItem> item = ITEMS.register(name, identifier -> {
            properties.setId(ResourceKey.create(Registries.ITEM, identifier));
            return new WizardArmourItem(element, properties);
        });
        WIZARD_ARMOUR_ITEMS.add(item);
        WizardryCreativeTabs.addToTabs(WizardryCreativeTabs.TabsEnum.GEAR, item);
        return item;
    }

    public static <T extends Item> @NonNull DeferredItem<T> registerItemWithSubItems(String name, Function<Item.Properties, T> itemFactory, WizardryCreativeTabs.TabsEnum tab, BiConsumer<BuildCreativeModeTabContentsEvent, T> tabPopulator) {
        DeferredItem<T> item = ITEMS.register(name, (identifier) -> {
            Item.Properties properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, identifier));
            return itemFactory.apply(properties);
        });
        WizardryCreativeTabs.addSpecialToTabs(tab, (event) -> tabPopulator.accept(event, item.get()));

        return item;
    }

    public static final DeferredHolder<Item, WandItem> WAND = registerItemWithSubItems(
            "wand",
            properties -> new WandItem(properties.stacksTo(1)),
            WizardryCreativeTabs.TabsEnum.GEAR,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (ElementEnum element : ElementEnum.values()) {
                    for (TierEnum tier : TierEnum.values()) {
                        ItemStack stack = new ItemStack(item);
                        stack.set(WizardryComponents.TIER, tier);
                        stack.set(WizardryComponents.ELEMENT, element);
                        buildCreativeModeTabContentsEvent.accept(stack);
                    }
                }
            }
    );

    //法术书
    public static final DeferredHolder<Item, SpellBookItem> SPELL_BOOK = registerItemWithSubItems(
            "spell_book",
            properties -> new SpellBookItem(properties.stacksTo(16)),
            WizardryCreativeTabs.TabsEnum.SPELLS,
            (buildCreativeModeTabContentsEvent, item) -> {
                buildCreativeModeTabContentsEvent.getParameters().holders().lookup(WizardrySpells.SPELLS_KEY).ifPresent(registry -> {
                    List<Holder.Reference<AbstractSpell>> sortedSpells = new ArrayList<>(registry.listElements().toList());
                    sortedSpells.sort(Comparator.comparing(Holder.Reference::value));
                    for (Holder.Reference<AbstractSpell> spellHolder : sortedSpells) {
                        if (spellHolder.is(WizardrySpells.NONE)) {
                            continue;
                        }
                        AbstractSpell spell = spellHolder.value();
                        if (spell.applicableForItem(item)) {
                            ItemStack spellBook = new ItemStack(item);
                            spellBook.set(WizardryComponents.SPELL.get(), spellHolder);
                            buildCreativeModeTabContentsEvent.accept(spellBook);
                        }
                    }
                });
            }
    );

    //法术卷轴
    public static final DeferredHolder<Item, ScrollItem> SCROLL = registerItemWithSubItems(
            "scroll",
            properties -> new ScrollItem(properties.stacksTo(16)),
            WizardryCreativeTabs.TabsEnum.SPELLS,
            (buildCreativeModeTabContentsEvent, item) -> {
                buildCreativeModeTabContentsEvent.getParameters().holders().lookup(WizardrySpells.SPELLS_KEY).ifPresent(registry -> {
                    List<Holder.Reference<AbstractSpell>> sortedSpells = new ArrayList<>(registry.listElements().toList());
                    sortedSpells.sort(Comparator.comparing(Holder.Reference::value));
                    for (Holder.Reference<AbstractSpell> spellHolder : sortedSpells) {
                        if (spellHolder.is(WizardrySpells.NONE)) {
                            continue;
                        }
                        AbstractSpell spell = spellHolder.value();
                        if (spell.applicableForItem(item)) {
                            ItemStack scroll = new ItemStack(item);
                            scroll.set(WizardryComponents.SPELL.get(), spellHolder);
                            buildCreativeModeTabContentsEvent.accept(scroll);
                        }
                    }
                });
            }
    );

    // --- 基础法师套装 (WIZARD) ---
    public static final DeferredItem<WizardArmourItem> WIZARD_HAT = registerItem("wizard_hat", ArmourMaterialHelper.WIZARD, ArmorType.HELMET, null);
    public static final DeferredItem<WizardArmourItem> WIZARD_ROBE = registerItem("wizard_robe", ArmourMaterialHelper.WIZARD, ArmorType.CHESTPLATE, null);
    public static final DeferredItem<WizardArmourItem> WIZARD_LEGGINGS = registerItem("wizard_leggings", ArmourMaterialHelper.WIZARD, ArmorType.LEGGINGS, null);
    public static final DeferredItem<WizardArmourItem> WIZARD_BOOTS = registerItem("wizard_boots", ArmourMaterialHelper.WIZARD, ArmorType.BOOTS, null);

    // --- 火元素 (FIRE) ---
    public static final DeferredItem<WizardArmourItem> WIZARD_HAT_FIRE = registerItem("wizard_hat_fire", ArmourMaterialHelper.WIZARD, ArmorType.HELMET, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> WIZARD_ROBE_FIRE = registerItem("wizard_robe_fire", ArmourMaterialHelper.WIZARD, ArmorType.CHESTPLATE, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> WIZARD_LEGGINGS_FIRE = registerItem("wizard_leggings_fire", ArmourMaterialHelper.WIZARD, ArmorType.LEGGINGS, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> WIZARD_BOOTS_FIRE = registerItem("wizard_boots_fire", ArmourMaterialHelper.WIZARD, ArmorType.BOOTS, ElementEnum.FIRE);

    // --- 冰元素 (ICE) ---
    public static final DeferredItem<WizardArmourItem> WIZARD_HAT_ICE = registerItem("wizard_hat_ice", ArmourMaterialHelper.WIZARD, ArmorType.HELMET, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> WIZARD_ROBE_ICE = registerItem("wizard_robe_ice", ArmourMaterialHelper.WIZARD, ArmorType.CHESTPLATE, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> WIZARD_LEGGINGS_ICE = registerItem("wizard_leggings_ice", ArmourMaterialHelper.WIZARD, ArmorType.LEGGINGS, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> WIZARD_BOOTS_ICE = registerItem("wizard_boots_ice", ArmourMaterialHelper.WIZARD, ArmorType.BOOTS, ElementEnum.ICE);

    // --- 闪电元素 (LIGHTNING) ---
    public static final DeferredItem<WizardArmourItem> WIZARD_HAT_LIGHTNING = registerItem("wizard_hat_lightning", ArmourMaterialHelper.WIZARD, ArmorType.HELMET, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> WIZARD_ROBE_LIGHTNING = registerItem("wizard_robe_lightning", ArmourMaterialHelper.WIZARD, ArmorType.CHESTPLATE, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> WIZARD_LEGGINGS_LIGHTNING = registerItem("wizard_leggings_lightning", ArmourMaterialHelper.WIZARD, ArmorType.LEGGINGS, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> WIZARD_BOOTS_LIGHTNING = registerItem("wizard_boots_lightning", ArmourMaterialHelper.WIZARD, ArmorType.BOOTS, ElementEnum.LIGHTNING);

    // --- 亡灵元素 (NECROMANCY) ---
    public static final DeferredItem<WizardArmourItem> WIZARD_HAT_NECROMANCY = registerItem("wizard_hat_necromancy", ArmourMaterialHelper.WIZARD, ArmorType.HELMET, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> WIZARD_ROBE_NECROMANCY = registerItem("wizard_robe_necromancy", ArmourMaterialHelper.WIZARD, ArmorType.CHESTPLATE, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> WIZARD_LEGGINGS_NECROMANCY = registerItem("wizard_leggings_necromancy", ArmourMaterialHelper.WIZARD, ArmorType.LEGGINGS, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> WIZARD_BOOTS_NECROMANCY = registerItem("wizard_boots_necromancy", ArmourMaterialHelper.WIZARD, ArmorType.BOOTS, ElementEnum.NECROMANCY);

    // --- 大地元素 (EARTH) ---
    public static final DeferredItem<WizardArmourItem> WIZARD_HAT_EARTH = registerItem("wizard_hat_earth", ArmourMaterialHelper.WIZARD, ArmorType.HELMET, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> WIZARD_ROBE_EARTH = registerItem("wizard_robe_earth", ArmourMaterialHelper.WIZARD, ArmorType.CHESTPLATE, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> WIZARD_LEGGINGS_EARTH = registerItem("wizard_leggings_earth", ArmourMaterialHelper.WIZARD, ArmorType.LEGGINGS, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> WIZARD_BOOTS_EARTH = registerItem("wizard_boots_earth", ArmourMaterialHelper.WIZARD, ArmorType.BOOTS, ElementEnum.EARTH);

    // --- 术法元素 (SORCERY) ---
    public static final DeferredItem<WizardArmourItem> WIZARD_HAT_SORCERY = registerItem("wizard_hat_sorcery", ArmourMaterialHelper.WIZARD, ArmorType.HELMET, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> WIZARD_ROBE_SORCERY = registerItem("wizard_robe_sorcery", ArmourMaterialHelper.WIZARD, ArmorType.CHESTPLATE, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> WIZARD_LEGGINGS_SORCERY = registerItem("wizard_leggings_sorcery", ArmourMaterialHelper.WIZARD, ArmorType.LEGGINGS, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> WIZARD_BOOTS_SORCERY = registerItem("wizard_boots_sorcery", ArmourMaterialHelper.WIZARD, ArmorType.BOOTS, ElementEnum.SORCERY);

    // --- 治疗元素 (HEALING) ---
    public static final DeferredItem<WizardArmourItem> WIZARD_HAT_HEALING = registerItem("wizard_hat_healing", ArmourMaterialHelper.WIZARD, ArmorType.HELMET, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> WIZARD_ROBE_HEALING = registerItem("wizard_robe_healing", ArmourMaterialHelper.WIZARD, ArmorType.CHESTPLATE, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> WIZARD_LEGGINGS_HEALING = registerItem("wizard_leggings_healing", ArmourMaterialHelper.WIZARD, ArmorType.LEGGINGS, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> WIZARD_BOOTS_HEALING = registerItem("wizard_boots_healing", ArmourMaterialHelper.WIZARD, ArmorType.BOOTS, ElementEnum.HEALING);

    // 无元素 (None)
    public static final DeferredItem<WizardArmourItem> SAGE_HAT = registerItem("sage_hat", ArmourMaterialHelper.SAGE, ArmorType.HELMET, null);
    public static final DeferredItem<WizardArmourItem> SAGE_ROBE = registerItem("sage_robe", ArmourMaterialHelper.SAGE, ArmorType.CHESTPLATE, null);
    public static final DeferredItem<WizardArmourItem> SAGE_LEGGINGS = registerItem("sage_leggings", ArmourMaterialHelper.SAGE, ArmorType.LEGGINGS, null);
    public static final DeferredItem<WizardArmourItem> SAGE_BOOTS = registerItem("sage_boots", ArmourMaterialHelper.SAGE, ArmorType.BOOTS, null);

    // 火元素 (FIRE)
    public static final DeferredItem<WizardArmourItem> SAGE_HAT_FIRE = registerItem("sage_hat_fire", ArmourMaterialHelper.SAGE, ArmorType.HELMET, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> SAGE_ROBE_FIRE = registerItem("sage_robe_fire", ArmourMaterialHelper.SAGE, ArmorType.CHESTPLATE, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> SAGE_LEGGINGS_FIRE = registerItem("sage_leggings_fire", ArmourMaterialHelper.SAGE, ArmorType.LEGGINGS, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> SAGE_BOOTS_FIRE = registerItem("sage_boots_fire", ArmourMaterialHelper.SAGE, ArmorType.BOOTS, ElementEnum.FIRE);

    // 冰元素 (ICE)
    public static final DeferredItem<WizardArmourItem> SAGE_HAT_ICE = registerItem("sage_hat_ice", ArmourMaterialHelper.SAGE, ArmorType.HELMET, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> SAGE_ROBE_ICE = registerItem("sage_robe_ice", ArmourMaterialHelper.SAGE, ArmorType.CHESTPLATE, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> SAGE_LEGGINGS_ICE = registerItem("sage_leggings_ice", ArmourMaterialHelper.SAGE, ArmorType.LEGGINGS, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> SAGE_BOOTS_ICE = registerItem("sage_boots_ice", ArmourMaterialHelper.SAGE, ArmorType.BOOTS, ElementEnum.ICE);

    // 闪电元素 (LIGHTNING)
    public static final DeferredItem<WizardArmourItem> SAGE_HAT_LIGHTNING = registerItem("sage_hat_lightning", ArmourMaterialHelper.SAGE, ArmorType.HELMET, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> SAGE_ROBE_LIGHTNING = registerItem("sage_robe_lightning", ArmourMaterialHelper.SAGE, ArmorType.CHESTPLATE, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> SAGE_LEGGINGS_LIGHTNING = registerItem("sage_leggings_lightning", ArmourMaterialHelper.SAGE, ArmorType.LEGGINGS, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> SAGE_BOOTS_LIGHTNING = registerItem("sage_boots_lightning", ArmourMaterialHelper.SAGE, ArmorType.BOOTS, ElementEnum.LIGHTNING);

    // 亡灵元素 (NECROMANCY)
    public static final DeferredItem<WizardArmourItem> SAGE_HAT_NECROMANCY = registerItem("sage_hat_necromancy", ArmourMaterialHelper.SAGE, ArmorType.HELMET, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> SAGE_ROBE_NECROMANCY = registerItem("sage_robe_necromancy", ArmourMaterialHelper.SAGE, ArmorType.CHESTPLATE, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> SAGE_LEGGINGS_NECROMANCY = registerItem("sage_leggings_necromancy", ArmourMaterialHelper.SAGE, ArmorType.LEGGINGS, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> SAGE_BOOTS_NECROMANCY = registerItem("sage_boots_necromancy", ArmourMaterialHelper.SAGE, ArmorType.BOOTS, ElementEnum.NECROMANCY);

    // 大地元素 (EARTH)
    public static final DeferredItem<WizardArmourItem> SAGE_HAT_EARTH = registerItem("sage_hat_earth", ArmourMaterialHelper.SAGE, ArmorType.HELMET, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> SAGE_ROBE_EARTH = registerItem("sage_robe_earth", ArmourMaterialHelper.SAGE, ArmorType.CHESTPLATE, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> SAGE_LEGGINGS_EARTH = registerItem("sage_leggings_earth", ArmourMaterialHelper.SAGE, ArmorType.LEGGINGS, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> SAGE_BOOTS_EARTH = registerItem("sage_boots_earth", ArmourMaterialHelper.SAGE, ArmorType.BOOTS, ElementEnum.EARTH);

    // 术法元素 (SORCERY)
    public static final DeferredItem<WizardArmourItem> SAGE_HAT_SORCERY = registerItem("sage_hat_sorcery", ArmourMaterialHelper.SAGE, ArmorType.HELMET, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> SAGE_ROBE_SORCERY = registerItem("sage_robe_sorcery", ArmourMaterialHelper.SAGE, ArmorType.CHESTPLATE, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> SAGE_LEGGINGS_SORCERY = registerItem("sage_leggings_sorcery", ArmourMaterialHelper.SAGE, ArmorType.LEGGINGS, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> SAGE_BOOTS_SORCERY = registerItem("sage_boots_sorcery", ArmourMaterialHelper.SAGE, ArmorType.BOOTS, ElementEnum.SORCERY);

    // 治疗元素 (HEALING)
    public static final DeferredItem<WizardArmourItem> SAGE_HAT_HEALING = registerItem("sage_hat_healing", ArmourMaterialHelper.SAGE, ArmorType.HELMET, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> SAGE_ROBE_HEALING = registerItem("sage_robe_healing", ArmourMaterialHelper.SAGE, ArmorType.CHESTPLATE, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> SAGE_LEGGINGS_HEALING = registerItem("sage_leggings_healing", ArmourMaterialHelper.SAGE, ArmorType.LEGGINGS, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> SAGE_BOOTS_HEALING = registerItem("sage_boots_healing", ArmourMaterialHelper.SAGE, ArmorType.BOOTS, ElementEnum.HEALING);

    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_HELMET = registerItem("battlemage_helmet", ArmourMaterialHelper.BATTLEMAGE, ArmorType.HELMET, null);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_CHESTPLATE = registerItem("battlemage_chestplate", ArmourMaterialHelper.BATTLEMAGE, ArmorType.CHESTPLATE, null);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_LEGGINGS = registerItem("battlemage_leggings", ArmourMaterialHelper.BATTLEMAGE, ArmorType.LEGGINGS, null);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_BOOTS = registerItem("battlemage_boots", ArmourMaterialHelper.BATTLEMAGE, ArmorType.BOOTS, null);

    // 火元素 (FIRE)
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_HELMET_FIRE = registerItem("battlemage_helmet_fire", ArmourMaterialHelper.BATTLEMAGE, ArmorType.HELMET, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_CHESTPLATE_FIRE = registerItem("battlemage_chestplate_fire", ArmourMaterialHelper.BATTLEMAGE, ArmorType.CHESTPLATE, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_LEGGINGS_FIRE = registerItem("battlemage_leggings_fire", ArmourMaterialHelper.BATTLEMAGE, ArmorType.LEGGINGS, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_BOOTS_FIRE = registerItem("battlemage_boots_fire", ArmourMaterialHelper.BATTLEMAGE, ArmorType.BOOTS, ElementEnum.FIRE);

    // 冰元素 (ICE)
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_HELMET_ICE = registerItem("battlemage_helmet_ice", ArmourMaterialHelper.BATTLEMAGE, ArmorType.HELMET, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_CHESTPLATE_ICE = registerItem("battlemage_chestplate_ice", ArmourMaterialHelper.BATTLEMAGE, ArmorType.CHESTPLATE, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_LEGGINGS_ICE = registerItem("battlemage_leggings_ice", ArmourMaterialHelper.BATTLEMAGE, ArmorType.LEGGINGS, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_BOOTS_ICE = registerItem("battlemage_boots_ice", ArmourMaterialHelper.BATTLEMAGE, ArmorType.BOOTS, ElementEnum.ICE);

    // 闪电元素 (LIGHTNING)
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_HELMET_LIGHTNING = registerItem("battlemage_helmet_lightning", ArmourMaterialHelper.BATTLEMAGE, ArmorType.HELMET, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_CHESTPLATE_LIGHTNING = registerItem("battlemage_chestplate_lightning", ArmourMaterialHelper.BATTLEMAGE, ArmorType.CHESTPLATE, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_LEGGINGS_LIGHTNING = registerItem("battlemage_leggings_lightning", ArmourMaterialHelper.BATTLEMAGE, ArmorType.LEGGINGS, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_BOOTS_LIGHTNING = registerItem("battlemage_boots_lightning", ArmourMaterialHelper.BATTLEMAGE, ArmorType.BOOTS, ElementEnum.LIGHTNING);

    // 亡灵元素 (NECROMANCY)
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_HELMET_NECROMANCY = registerItem("battlemage_helmet_necromancy", ArmourMaterialHelper.BATTLEMAGE, ArmorType.HELMET, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_CHESTPLATE_NECROMANCY = registerItem("battlemage_chestplate_necromancy", ArmourMaterialHelper.BATTLEMAGE, ArmorType.CHESTPLATE, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_LEGGINGS_NECROMANCY = registerItem("battlemage_leggings_necromancy", ArmourMaterialHelper.BATTLEMAGE, ArmorType.LEGGINGS, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_BOOTS_NECROMANCY = registerItem("battlemage_boots_necromancy", ArmourMaterialHelper.BATTLEMAGE, ArmorType.BOOTS, ElementEnum.NECROMANCY);

    // 大地元素 (EARTH)
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_HELMET_EARTH = registerItem("battlemage_helmet_earth", ArmourMaterialHelper.BATTLEMAGE, ArmorType.HELMET, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_CHESTPLATE_EARTH = registerItem("battlemage_chestplate_earth", ArmourMaterialHelper.BATTLEMAGE, ArmorType.CHESTPLATE, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_LEGGINGS_EARTH = registerItem("battlemage_leggings_earth", ArmourMaterialHelper.BATTLEMAGE, ArmorType.LEGGINGS, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_BOOTS_EARTH = registerItem("battlemage_boots_earth", ArmourMaterialHelper.BATTLEMAGE, ArmorType.BOOTS, ElementEnum.EARTH);

    // 术法元素 (SORCERY)
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_HELMET_SORCERY = registerItem("battlemage_helmet_sorcery", ArmourMaterialHelper.BATTLEMAGE, ArmorType.HELMET, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_CHESTPLATE_SORCERY = registerItem("battlemage_chestplate_sorcery", ArmourMaterialHelper.BATTLEMAGE, ArmorType.CHESTPLATE, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_LEGGINGS_SORCERY = registerItem("battlemage_leggings_sorcery", ArmourMaterialHelper.BATTLEMAGE, ArmorType.LEGGINGS, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_BOOTS_SORCERY = registerItem("battlemage_boots_sorcery", ArmourMaterialHelper.BATTLEMAGE, ArmorType.BOOTS, ElementEnum.SORCERY);

    // 治疗元素 (HEALING)
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_HELMET_HEALING = registerItem("battlemage_helmet_healing", ArmourMaterialHelper.BATTLEMAGE, ArmorType.HELMET, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_CHESTPLATE_HEALING = registerItem("battlemage_chestplate_healing", ArmourMaterialHelper.BATTLEMAGE, ArmorType.CHESTPLATE, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_LEGGINGS_HEALING = registerItem("battlemage_leggings_healing", ArmourMaterialHelper.BATTLEMAGE, ArmorType.LEGGINGS, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> BATTLEMAGE_BOOTS_HEALING = registerItem("battlemage_boots_healing", ArmourMaterialHelper.BATTLEMAGE, ArmorType.BOOTS, ElementEnum.HEALING);

    public static final DeferredItem<WizardArmourItem> WARLOCK_HOOD = registerItem("warlock_hood", ArmourMaterialHelper.WARLOCK, ArmorType.HELMET, null);
    public static final DeferredItem<WizardArmourItem> WARLOCK_ROBE = registerItem("warlock_robe", ArmourMaterialHelper.WARLOCK, ArmorType.CHESTPLATE, null);
    public static final DeferredItem<WizardArmourItem> WARLOCK_LEGGINGS = registerItem("warlock_leggings", ArmourMaterialHelper.WARLOCK, ArmorType.LEGGINGS, null);
    public static final DeferredItem<WizardArmourItem> WARLOCK_BOOTS = registerItem("warlock_boots", ArmourMaterialHelper.WARLOCK, ArmorType.BOOTS, null);

    // 火元素 (FIRE)
    public static final DeferredItem<WizardArmourItem> WARLOCK_HOOD_FIRE = registerItem("warlock_hood_fire", ArmourMaterialHelper.WARLOCK, ArmorType.HELMET, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> WARLOCK_ROBE_FIRE = registerItem("warlock_robe_fire", ArmourMaterialHelper.WARLOCK, ArmorType.CHESTPLATE, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> WARLOCK_LEGGINGS_FIRE = registerItem("warlock_leggings_fire", ArmourMaterialHelper.WARLOCK, ArmorType.LEGGINGS, ElementEnum.FIRE);
    public static final DeferredItem<WizardArmourItem> WARLOCK_BOOTS_FIRE = registerItem("warlock_boots_fire", ArmourMaterialHelper.WARLOCK, ArmorType.BOOTS, ElementEnum.FIRE);

    // 冰元素 (ICE)
    public static final DeferredItem<WizardArmourItem> WARLOCK_HOOD_ICE = registerItem("warlock_hood_ice", ArmourMaterialHelper.WARLOCK, ArmorType.HELMET, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> WARLOCK_ROBE_ICE = registerItem("warlock_robe_ice", ArmourMaterialHelper.WARLOCK, ArmorType.CHESTPLATE, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> WARLOCK_LEGGINGS_ICE = registerItem("warlock_leggings_ice", ArmourMaterialHelper.WARLOCK, ArmorType.LEGGINGS, ElementEnum.ICE);
    public static final DeferredItem<WizardArmourItem> WARLOCK_BOOTS_ICE = registerItem("warlock_boots_ice", ArmourMaterialHelper.WARLOCK, ArmorType.BOOTS, ElementEnum.ICE);

    // 闪电元素 (LIGHTNING)
    public static final DeferredItem<WizardArmourItem> WARLOCK_HOOD_LIGHTNING = registerItem("warlock_hood_lightning", ArmourMaterialHelper.WARLOCK, ArmorType.HELMET, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> WARLOCK_ROBE_LIGHTNING = registerItem("warlock_robe_lightning", ArmourMaterialHelper.WARLOCK, ArmorType.CHESTPLATE, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> WARLOCK_LEGGINGS_LIGHTNING = registerItem("warlock_leggings_lightning", ArmourMaterialHelper.WARLOCK, ArmorType.LEGGINGS, ElementEnum.LIGHTNING);
    public static final DeferredItem<WizardArmourItem> WARLOCK_BOOTS_LIGHTNING = registerItem("warlock_boots_lightning", ArmourMaterialHelper.WARLOCK, ArmorType.BOOTS, ElementEnum.LIGHTNING);

    // 亡灵元素 (NECROMANCY)
    public static final DeferredItem<WizardArmourItem> WARLOCK_HOOD_NECROMANCY = registerItem("warlock_hood_necromancy", ArmourMaterialHelper.WARLOCK, ArmorType.HELMET, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> WARLOCK_ROBE_NECROMANCY = registerItem("warlock_robe_necromancy", ArmourMaterialHelper.WARLOCK, ArmorType.CHESTPLATE, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> WARLOCK_LEGGINGS_NECROMANCY = registerItem("warlock_leggings_necromancy", ArmourMaterialHelper.WARLOCK, ArmorType.LEGGINGS, ElementEnum.NECROMANCY);
    public static final DeferredItem<WizardArmourItem> WARLOCK_BOOTS_NECROMANCY = registerItem("warlock_boots_necromancy", ArmourMaterialHelper.WARLOCK, ArmorType.BOOTS, ElementEnum.NECROMANCY);

    // 大地元素 (EARTH)
    public static final DeferredItem<WizardArmourItem> WARLOCK_HOOD_EARTH = registerItem("warlock_hood_earth", ArmourMaterialHelper.WARLOCK, ArmorType.HELMET, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> WARLOCK_ROBE_EARTH = registerItem("warlock_robe_earth", ArmourMaterialHelper.WARLOCK, ArmorType.CHESTPLATE, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> WARLOCK_LEGGINGS_EARTH = registerItem("warlock_leggings_earth", ArmourMaterialHelper.WARLOCK, ArmorType.LEGGINGS, ElementEnum.EARTH);
    public static final DeferredItem<WizardArmourItem> WARLOCK_BOOTS_EARTH = registerItem("warlock_boots_earth", ArmourMaterialHelper.WARLOCK, ArmorType.BOOTS, ElementEnum.EARTH);

    // 术法元素 (SORCERY)
    public static final DeferredItem<WizardArmourItem> WARLOCK_HOOD_SORCERY = registerItem("warlock_hood_sorcery", ArmourMaterialHelper.WARLOCK, ArmorType.HELMET, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> WARLOCK_ROBE_SORCERY = registerItem("warlock_robe_sorcery", ArmourMaterialHelper.WARLOCK, ArmorType.CHESTPLATE, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> WARLOCK_LEGGINGS_SORCERY = registerItem("warlock_leggings_sorcery", ArmourMaterialHelper.WARLOCK, ArmorType.LEGGINGS, ElementEnum.SORCERY);
    public static final DeferredItem<WizardArmourItem> WARLOCK_BOOTS_SORCERY = registerItem("warlock_boots_sorcery", ArmourMaterialHelper.WARLOCK, ArmorType.BOOTS, ElementEnum.SORCERY);

    // 治疗元素 (HEALING)
    public static final DeferredItem<WizardArmourItem> WARLOCK_HOOD_HEALING = registerItem("warlock_hood_healing", ArmourMaterialHelper.WARLOCK, ArmorType.HELMET, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> WARLOCK_ROBE_HEALING = registerItem("warlock_robe_healing", ArmourMaterialHelper.WARLOCK, ArmorType.CHESTPLATE, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> WARLOCK_LEGGINGS_HEALING = registerItem("warlock_leggings_healing", ArmourMaterialHelper.WARLOCK, ArmorType.LEGGINGS, ElementEnum.HEALING);
    public static final DeferredItem<WizardArmourItem> WARLOCK_BOOTS_HEALING = registerItem("warlock_boots_healing", ArmourMaterialHelper.WARLOCK, ArmorType.BOOTS, ElementEnum.HEALING);

    public static final DeferredItem<ArtefactItem> RING_CONDENSING = registerItem(
            "ring_condensing",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_SIPHONING = registerItem(
            "ring_siphoning",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_BATTLEMAGE = registerItem(
            "ring_battlemage",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_COMBUSTION = registerItem(
            "ring_combustion",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_FIRE_MELEE = registerItem(
            "ring_fire_melee",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_FIRE_BIOME = registerItem(
            "ring_fire_biome",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_DISINTEGRATION = registerItem(
            "ring_disintegration",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_METEOR = registerItem(
            "ring_meteor",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_ICE_MELEE = registerItem(
            "ring_ice_melee",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_ICE_BIOME = registerItem(
            "ring_ice_biome",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_ARCANE_FROST = registerItem(
            "ring_arcane_frost",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_SHATTERING = registerItem(
            "ring_shattering",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_LIGHTNING_MELEE = registerItem(
            "ring_lightning_melee",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_STORM = registerItem(
            "ring_storm",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_SEEKING = registerItem(
            "ring_seeking",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_HAMMER = registerItem(
            "ring_hammer",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_STORMCLOUD = registerItem(
            "ring_stormcloud",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_SOULBINDING = registerItem(
            "ring_soulbinding",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_LEECHING = registerItem(
            "ring_leeching",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_NECROMANCY_MELEE = registerItem(
            "ring_necromancy_melee",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_MIND_CONTROL = registerItem(
            "ring_mind_control",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_POISON = registerItem(
            "ring_poison",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_EARTH_MELEE = registerItem(
            "ring_earth_melee",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_EARTH_BIOME = registerItem(
            "ring_earth_biome",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_FULL_MOON = registerItem(
            "ring_full_moon",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_EVOKER = registerItem(
            "ring_evoker",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_EXTRACTION = registerItem(
            "ring_extraction",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_MANA_RETURN = registerItem(
            "ring_mana_return",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_BLOCKWRANGLER = registerItem(
            "ring_blockwrangler",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_CONJURER = registerItem(
            "ring_conjurer",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_DEFENDER = registerItem(
            "ring_defender",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_PALADIN = registerItem(
            "ring_paladin",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> RING_INTERDICTION = registerItem(
            "ring_interdiction",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.RING)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );

    public static final DeferredItem<ArtefactItem> AMULET_ARCANE_DEFENCE = registerItem(
            "amulet_arcane_defence",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_WARDING = registerItem(
            "amulet_warding",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_WISDOM = registerItem(
            "amulet_wisdom",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_FIRE_PROTECTION = registerItem(
            "amulet_fire_protection",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_FIRE_CLOAKING = registerItem(
            "amulet_fire_cloaking",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_ICE_IMMUNITY = registerItem(
            "amulet_ice_immunity",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_ICE_PROTECTION = registerItem(
            "amulet_ice_protection",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_FROST_WARDING = registerItem(
            "amulet_frost_warding",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_POTENTIAL = registerItem(
            "amulet_potential",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_CHANNELING = registerItem(
            "amulet_channeling",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_LICH = registerItem(
            "amulet_lich",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_WITHER_IMMUNITY = registerItem(
            "amulet_wither_immunity",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_GLIDE = registerItem(
            "amulet_glide",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_BANISHING = registerItem(
            "amulet_banishing",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_ANCHORING = registerItem(
            "amulet_anchoring",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_RECOVERY = registerItem(
            "amulet_recovery",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_TRANSIENCE = registerItem(
            "amulet_transience",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_RESURRECTION = registerItem(
            "amulet_resurrection",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_AUTO_SHIELD = registerItem(
            "amulet_auto_shield",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> AMULET_ABSORPTION = registerItem(
            "amulet_absorption",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.AMULET)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );

    public static final DeferredItem<ArtefactItem> CHARM_HAGGLER = registerItem(
            "charm_haggler",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_EXPERIENCE_TOME = registerItem(
            "charm_experience_tome",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_MOVE_SPEED = registerItem(
            "charm_move_speed",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_SPELL_DISCOVERY = registerItem(
            "charm_spell_discovery",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_AUTO_SMELT = registerItem(
            "charm_auto_smelt",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_LAVA_WALKING = registerItem(
            "charm_lava_walking",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_STORM = registerItem(
            "charm_storm",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_MINION_HEALTH = registerItem(
            "charm_minion_health",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_MINION_VARIANTS = registerItem(
            "charm_minion_variants",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_UNDEAD_HELMETS = registerItem(
            "charm_undead_helmets",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_HUNGER_CASTING = registerItem(
            "charm_hunger_casting",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_FLIGHT = registerItem(
            "charm_flight",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_GROWTH = registerItem(
            "charm_growth",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_ABSEILING = registerItem(
            "charm_abseiling",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_SILK_TOUCH = registerItem(
            "charm_silk_touch",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_SIXTH_SENSE = registerItem(
            "charm_sixth_sense",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_STOP_TIME = registerItem(
            "charm_stop_time",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_LIGHT = registerItem(
            "charm_light",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_TRANSPORTATION = registerItem(
            "charm_transportation",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_BLACK_HOLE = registerItem(
            "charm_black_hole",
            properties -> new ArtefactItem(properties.rarity(Rarity.EPIC).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_MOUNT_TELEPORTING = registerItem(
            "charm_mount_teleporting",
            properties -> new ArtefactItem(properties.rarity(Rarity.RARE).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );
    public static final DeferredItem<ArtefactItem> CHARM_FEEDING = registerItem(
            "charm_feeding",
            properties -> new ArtefactItem(properties.rarity(Rarity.UNCOMMON).component(WizardryComponents.ARTEFACT_TYPE.get(), ArtefactTypeEnum.CHARM)),
            WizardryCreativeTabs.TabsEnum.GEAR
    );

    public static final DeferredItem<BlockItem> ARCANE_WORKBENCH = registerItem(
            "arcane_workbench",
            properties -> new BlockItem(WizardryBlocks.ARCANE_WORKBENCH.get(), properties.useBlockDescriptionPrefix()),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<BlockItem> CRYSTAL_ORE = registerItem(
            "crystal_ore",
            properties -> new BlockItem(WizardryBlocks.CRYSTAL_ORE.get(), properties.useBlockDescriptionPrefix()),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<BlockItem> CRYSTAL_FLOWER = registerItem(
            "crystal_flower",
            properties -> new BlockItem(
                    WizardryBlocks.CRYSTAL_FLOWER.get(),
                    properties.useBlockDescriptionPrefix()
            ),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<BlockItem> TRANSPORTATION_STONE = registerItem(
            "transportation_stone",
            properties -> new BlockItem(WizardryBlocks.TRANSPORTATION_STONE.get(), properties.useBlockDescriptionPrefix()),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<BlockItem> CRYSTAL_BLOCK = registerItemWithSubItems(
            "crystal_block",
            properties -> new BlockItem(WizardryBlocks.CRYSTAL_BLOCK.get(), properties.useBlockDescriptionPrefix()) {
                @Override
                public @NonNull Component getName(@NonNull ItemStack itemStack) {
                    ElementEnum element = ElementEnum.MAGIC;
                    BlockItemStateProperties stateProperties = itemStack.get(DataComponents.BLOCK_STATE);
                    if (stateProperties != null) {
                        ElementEnum foundElement = stateProperties.get(CrystalBlock.ELEMENT);
                        if (foundElement != null) {
                            element = foundElement;
                        }
                    }
                    return Component.translatable("block." + Wizardry.MODID + "." + element.getSerializedName() + "_crystal_block");
                }
            },
            WizardryCreativeTabs.TabsEnum.WIZARDRY,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (ElementEnum element : ElementEnum.values()) {
                    ItemStack stack = new ItemStack(item);
                    stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY
                            .with(CrystalBlock.ELEMENT, element));
                    buildCreativeModeTabContentsEvent.accept(stack);
                }
            }
    );
    public static final DeferredItem<BlockItem> RUNESTONE = registerItemWithSubItems(
            "runestone",
            properties -> new BlockItem(WizardryBlocks.RUNESTONE.get(), properties.useBlockDescriptionPrefix()),
            WizardryCreativeTabs.TabsEnum.WIZARDRY,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (ElementEnum element : ElementEnum.values()) {
                    if (element == ElementEnum.MAGIC) {
                        continue;
                    }
                    ItemStack stack = new ItemStack(item);
                    stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(CrystalBlock.ELEMENT, element));
                    buildCreativeModeTabContentsEvent.accept(stack);
                }
            }
    );
    public static final DeferredItem<BlockItem> RUNESTONE_PEDESTAL = registerItemWithSubItems(
            "runestone_pedestal",
            properties -> new BlockItem(WizardryBlocks.RUNESTONE_PEDESTAL.get(), properties.useBlockDescriptionPrefix()),
            WizardryCreativeTabs.TabsEnum.WIZARDRY,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (ElementEnum element : ElementEnum.values()) {
                    if (element == ElementEnum.MAGIC) {
                        continue;
                    }
                    ItemStack stack = new ItemStack(item);
                    stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY
                            .with(CrystalBlock.ELEMENT, element));
                    buildCreativeModeTabContentsEvent.accept(stack);
                }
            }
    );
    public static final DeferredItem<BlockItem> GILDED_WOOD = registerItemWithSubItems(
            "gilded_wood",
            props -> new BlockItem(WizardryBlocks.GILDED_WOOD.get(), props.useBlockDescriptionPrefix()) {
                @Override
                public @NonNull Component getName(@NonNull ItemStack itemStack) {
                    BlockItemStateProperties stateProperties = itemStack.get(DataComponents.BLOCK_STATE);
                    String woodName = "";
                    if (stateProperties != null) {
                        WoodTypeEnum woodType = stateProperties.get(GildedWoodBlock.GILDED_WOOD_TYPE);
                        if (woodType != null) {
                            woodName = woodType.getSerializedName();
                        }
                    }
                    return Component.translatable("block." + Wizardry.MODID + "." + woodName + "_gilded_wood");
                }
            },
            WizardryCreativeTabs.TabsEnum.WIZARDRY,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (WoodTypeEnum wood : WoodTypeEnum.values()) {
                    ItemStack stack = new ItemStack(item);
                    stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(GildedWoodBlock.GILDED_WOOD_TYPE, wood));
                    buildCreativeModeTabContentsEvent.accept(stack);
                }
            }
    );
    public static final DeferredItem<BlockItem> BOOKSHELF = registerItemWithSubItems(
            "bookshelf",
            props -> new BlockItem(WizardryBlocks.BOOKSHELF.get(), props.useBlockDescriptionPrefix()) {
                @Override
                public @NonNull Component getName(@NonNull ItemStack itemStack) {
                    BlockItemStateProperties stateProperties = itemStack.get(DataComponents.BLOCK_STATE);
                    String woodName = "";
                    if (stateProperties != null) {
                        WoodTypeEnum woodType = stateProperties.get(BookshelfBlock.BOOKSHELF_WOOD_TYPE);
                        if (woodType != null) {
                            woodName = woodType.getSerializedName();
                        }
                    }
                    return Component.translatable("block." + Wizardry.MODID + "." + woodName + "_bookshelf");
                }
            },
            WizardryCreativeTabs.TabsEnum.WIZARDRY,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (WoodTypeEnum wood : WoodTypeEnum.values()) {
                    ItemStack stack = new ItemStack(item);
                    stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(BookshelfBlock.BOOKSHELF_WOOD_TYPE, wood));
                    buildCreativeModeTabContentsEvent.accept(stack);
                }
            }
    );
    public static final DeferredItem<BlockItem> LECTERN = registerItemWithSubItems(
            "lectern",
            props -> new BlockItem(WizardryBlocks.LECTERN.get(), props.useBlockDescriptionPrefix()) {
                @Override
                public @NonNull Component getName(@NonNull ItemStack itemStack) {
                    BlockItemStateProperties stateProperties = itemStack.get(DataComponents.BLOCK_STATE);
                    String woodName = "";
                    if (stateProperties != null) {
                        WoodTypeEnum woodType = stateProperties.get(LecternBlock.LECTERN_WOOD_TYPE);
                        if (woodType != null) {
                            woodName = woodType.getSerializedName();
                        }
                    }
                    return Component.translatable("block." + Wizardry.MODID + "." + woodName + "_lectern");
                }
            },
            WizardryCreativeTabs.TabsEnum.WIZARDRY,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (WoodTypeEnum wood : WoodTypeEnum.values()) {
                    ItemStack stack = new ItemStack(item);
                    stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(LecternBlock.LECTERN_WOOD_TYPE, wood));
                    buildCreativeModeTabContentsEvent.accept(stack);
                }
            }
    );
    public static final DeferredItem<BlockItem> IMBUEMENT_ALTAR = registerItem(
            "imbuement_altar",
            props -> new BlockItem(WizardryBlocks.IMBUEMENT_ALTAR.get(), props.useBlockDescriptionPrefix()),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<BlockItem> RECEPTACLE = registerItem(
            "receptacle",
            props -> new BlockItem(WizardryBlocks.RECEPTACLE.get(), props.useBlockDescriptionPrefix()),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<MagicCrystalItem> MAGIC_CRYSTAL = registerItemWithSubItems(
            "magic_crystal",
            MagicCrystalItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (ElementEnum element : ElementEnum.values()) {
                    ItemStack crystalStack = new ItemStack(item);
                    crystalStack.set(WizardryComponents.ELEMENT.get(), element);
                    buildCreativeModeTabContentsEvent.accept(crystalStack);
                }
            }
    );
    public static final DeferredItem<Item> CRYSTAL_SHARD = registerItem(
            "crystal_shard",
            Item::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<Item> GRAND_CRYSTAL = registerItem(
            "grand_crystal",
            Item::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<WizardHandbookItem> WIZARD_HANDBOOK = registerItem(
            "wizard_handbook",
            properties -> new WizardHandbookItem(properties.stacksTo(1)),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<ArcaneTomeItem> ARCANE_TOME = registerItemWithSubItems(
            "arcane_tome",
            properties -> new ArcaneTomeItem(properties.stacksTo(1)),
            WizardryCreativeTabs.TabsEnum.WIZARDRY,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (TierEnum tier : TierEnum.values()) {
                    if (tier == TierEnum.NOVICE) {
                        continue;
                    }
                    ItemStack arcaneTomeStack = new ItemStack(item);
                    arcaneTomeStack.set(WizardryComponents.TIER.get(), tier);
                    buildCreativeModeTabContentsEvent.accept(arcaneTomeStack);
                }
            }
    );
    public static final DeferredItem<Item> RUINED_SPELL_BOOK = registerItem(
            "ruined_spell_book",
            SpellBookItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<Item> BLANK_SCROLL = registerItem(
            "blank_scroll",
            BlankScrollItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<Item> MAGIC_SILK = registerItem(
            "magic_silk",
            Item::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<ManaFlaskItem> MANA_FLASK = registerItemWithSubItems(
            "mana_flask",
            properties -> new ManaFlaskItem(properties.stacksTo(16)),
            WizardryCreativeTabs.TabsEnum.WIZARDRY,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (ManaFlaskTypeEnum type : ManaFlaskTypeEnum.values()) {
                    ItemStack crystalStack = new ItemStack(item);
                    crystalStack.set(WizardryComponents.MANA_FLASK_TYPE.get(), type);
                    buildCreativeModeTabContentsEvent.accept(crystalStack);
                }
            }
    );
    public static final DeferredItem<Item> IDENTIFICATION_SCROLL = registerItem(
            "identification_scroll",
            IdentificationScrollItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<WandUpgradeItem> STORAGE_UPGRADE = registerItem(
            "storage_upgrade",
            WandUpgradeItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<WandUpgradeItem> SIPHON_UPGRADE = registerItem(
            "siphon_upgrade",
            WandUpgradeItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<WandUpgradeItem> CONDENSER_UPGRADE = registerItem(
            "condenser_upgrade",
            WandUpgradeItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<WandUpgradeItem> RANGE_UPGRADE = registerItem(
            "range_upgrade",
            WandUpgradeItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<WandUpgradeItem> DURATION_UPGRADE = registerItem(
            "duration_upgrade",
            WandUpgradeItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<WandUpgradeItem> COOLDOWN_UPGRADE = registerItem(
            "cooldown_upgrade",
            WandUpgradeItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<WandUpgradeItem> BLAST_UPGRADE = registerItem(
            "blast_upgrade",
            WandUpgradeItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<WandUpgradeItem> ATTUNEMENT_UPGRADE = registerItem(
            "attunement_upgrade",
            WandUpgradeItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<WandUpgradeItem> MELEE_UPGRADE = registerItem(
            "melee_upgrade",
            WandUpgradeItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<Item> ASTRAL_DIAMOND = registerItem(
            "astral_diamond",
            properties -> new Item(properties.rarity(Rarity.RARE)),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<Item> PURIFYING_ELIXIR = registerItem(
            "purifying_elixir",
            PurifyingElixirItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredHolder<Item, ArmourUpgradeItem> RESPLENDENT_THREAD = registerItem(
            "resplendent_thread",
            properties -> new ArmourUpgradeItem(properties.stacksTo(1).rarity(Rarity.EPIC)),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredHolder<Item, ArmourUpgradeItem> CRYSTAL_SILVER_PLATING = registerItem(
            "crystal_silver_plating",
            properties -> new ArmourUpgradeItem(properties.stacksTo(1).rarity(Rarity.EPIC)),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredHolder<Item, ArmourUpgradeItem> ETHEREAL_CRYSTALWEAVE = registerItem(
            "ethereal_crystalweave",
            properties -> new ArmourUpgradeItem(properties.stacksTo(1).rarity(Rarity.EPIC)),
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<FireBombItem> FIRE_BOMB = registerItem(
            "fire_bomb",
            FireBombItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<PoisonBombItem> POISON_BOMB = registerItem(
            "poison_bomb",
            PoisonBombItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<SmokeBombItem> SMOKE_BOMB = registerItem(
            "smoke_bomb",
            SmokeBombItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<SparkBombItem> SPARK_BOMB = registerItem(
            "spark_bomb",
            SparkBombItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY
    );
    public static final DeferredItem<SpectralDustItem> SPECTRAL_DUST = registerItemWithSubItems(
            "spectral_dust",
            SpectralDustItem::new,
            WizardryCreativeTabs.TabsEnum.WIZARDRY,
            (buildCreativeModeTabContentsEvent, item) -> {
                for (ElementEnum element : ElementEnum.values()) {
                    if (element == ElementEnum.MAGIC) {
                        continue;
                    }
                    ItemStack stack = new ItemStack(item);
                    stack.set(WizardryComponents.ELEMENT.get(), element);
                    buildCreativeModeTabContentsEvent.accept(stack);
                }
            }
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}