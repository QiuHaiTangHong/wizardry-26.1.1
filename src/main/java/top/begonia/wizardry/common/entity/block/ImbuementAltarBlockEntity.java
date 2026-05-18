package top.begonia.wizardry.common.entity.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import top.begonia.wizardry.common.registry.WizardryBlockEntities;

public class ImbuementAltarBlockEntity extends BlockEntity {

    public ImbuementAltarBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(WizardryBlockEntities.IMBUEMENT_ALTAR.get(), worldPosition, blockState);
    }
}
