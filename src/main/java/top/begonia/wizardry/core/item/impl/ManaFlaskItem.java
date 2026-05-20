package top.begonia.wizardry.core.item.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.core.constants.ManaFlaskTypeEnum;
import top.begonia.wizardry.core.registry.WizardryComponents;

public class ManaFlaskItem extends Item {
    public ManaFlaskItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull Component getName(@NonNull ItemStack stack) {
        ManaFlaskTypeEnum type = stack.getOrDefault(WizardryComponents.MANA_FLASK_TYPE.get(), ManaFlaskTypeEnum.SMALL);
        return Component.translatable("item." + Wizardry.MODID + "." + type.getSerializedName() + "_mana_flask");
    }
}
