package top.begonia.wizardry.client.render;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.core.util.ArmourHelper;

import javax.annotation.Nullable;
import java.util.Map;

public class WizardryArmorRenderer implements IClientItemExtensions {
    private final Map<ModelLayerLocation, HumanoidModel<?>> modelLayerLocationHumanoidModelMap;

    public WizardryArmorRenderer(Map<ModelLayerLocation, HumanoidModel<?>> modelLayerLocationHumanoidModelMap) {
        this.modelLayerLocationHumanoidModelMap = modelLayerLocationHumanoidModelMap;
    }

    @Override
    public @NonNull Model<?> getHumanoidArmorModel(@NonNull ItemStack itemStack, EquipmentClientInfo.@NonNull LayerType layerType, @NonNull Model original) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return original;
        ArmorType armorType = getArmorTypeFromSlot(equippable.slot());
        if (armorType == null) return original;
        ModelLayerLocation layerLocation = ArmourHelper.getModelLayer(equippable.assetId().get(), armorType);
        if (layerLocation == null) return original;
        Model<?> customModel = this.modelLayerLocationHumanoidModelMap.get(layerLocation);
        return customModel != null ? customModel : original;
    }

    @Contract(pure = true)
    @Nullable
    private ArmorType getArmorTypeFromSlot(@NonNull EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> ArmorType.HELMET;
            case CHEST -> ArmorType.CHESTPLATE;
            case LEGS -> ArmorType.LEGGINGS;
            case FEET -> ArmorType.BOOTS;
            default -> null;
        };
    }
}
