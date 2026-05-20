package top.begonia.wizardry.core.spell.impl;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemUseAnimation;
import top.begonia.wizardry.core.spell.AbstractSpell;

public class Detonate extends AbstractSpell {
    public Detonate(Identifier identifier) {
        super(identifier, ItemUseAnimation.BOW, false); // 默认设置为弓箭动画
    }
}
