package top.begonia.wizardry.core.spell.impl;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemUseAnimation;
import top.begonia.wizardry.core.spell.AbstractSpell;

public class ArrowSpell extends AbstractSpell {
    public ArrowSpell(Identifier identifier) {
        super(identifier, ItemUseAnimation.BOW, false);
    }
}
