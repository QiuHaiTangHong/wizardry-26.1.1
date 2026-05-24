package top.begonia.wizardry.core.spell.impl;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import top.begonia.wizardry.core.data.json.definition.spell.part.SpellModifiers;
import top.begonia.wizardry.core.spell.AbstractSpell;

public class None extends AbstractSpell {
    public None(Identifier identifier) {
        super(identifier, ItemUseAnimation.NONE, false);
    }
}
