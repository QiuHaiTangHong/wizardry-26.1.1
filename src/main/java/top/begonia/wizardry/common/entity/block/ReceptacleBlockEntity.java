package top.begonia.wizardry.common.entity.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import top.begonia.wizardry.common.registry.WizardryBlockEntities;

public class ReceptacleBlockEntity extends BlockEntity {
    public ReceptacleBlockEntity(BlockPos pos, BlockState state) {
        super(WizardryBlockEntities.RECEPTACLE.get(), pos, state);
    }
}
