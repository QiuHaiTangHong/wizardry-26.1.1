package top.begonia.wizardry.core.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.core.entity.projectile.bomb.FireBombEntity;
import top.begonia.wizardry.core.spell.AbstractSpell;
import top.begonia.wizardry.core.spell.impl.*;
import top.begonia.wizardry.core.spell.impl.projectile.ProjectileSpell;

public final class WizardrySpells {
    public static final ResourceKey<Registry<AbstractSpell>> SPELLS_KEY = ResourceKey.createRegistryKey(
            Identifier.fromNamespaceAndPath(Wizardry.MODID, "spells")
    );
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SPELLS_KEY, Wizardry.MODID);

    public static final DeferredHolder<AbstractSpell, None> NONE = SPELLS.register("none", None::new);
    public static final DeferredHolder<AbstractSpell, SpellArrow> MAGIC_MISSILE = SPELLS.register("magic_missile", SpellArrow::new);

    public static final DeferredHolder<AbstractSpell, ProjectileSpell<FireBombEntity>> FIRE_BOMB = SPELLS.register(
            "fire_bomb",
            () -> {
                ProjectileSpell<FireBombEntity> spell = new ProjectileSpell<>(
                        Identifier.fromNamespaceAndPath(Wizardry.MODID, "fire_bomb"),
                        WizardryEntities.FIRE_BOMB,
                        () -> new ItemStack(WizardryItems.FIRE_BOMB.get()),
                        FireBombEntity::new
                );
                spell.soundValues(0.5f, 0.4f, 0.2f);
                return spell;
            }
    );

    public static void register(IEventBus modBus) {
        SPELLS.makeRegistry(builder -> builder
                .sync(true)
                .maxId(65535)
        );
        SPELLS.register(modBus);
    }
}