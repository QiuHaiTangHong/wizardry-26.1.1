package top.begonia.wizardry.common.item.impl;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class IdentificationScrollItem extends Item {
    public IdentificationScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(@NonNull ItemStack itemStack) {
        return true;
    }
}
