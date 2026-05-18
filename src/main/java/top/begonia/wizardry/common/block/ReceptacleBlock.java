package top.begonia.wizardry.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ReceptacleBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final MapCodec<ReceptacleBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    propertiesCodec()
            ).apply(instance, ReceptacleBlock::new)
    );
    protected static final VoxelShape STANDING_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 8.0, 12.0);
    protected static final VoxelShape NORTH_WALL_SHAPE = Block.box(4.0, 2.0, 7.0, 12.0, 10.0, 16.0);
    protected static final VoxelShape SOUTH_WALL_SHAPE = Block.box(4.0, 2.0, 0.0, 12.0, 10.0, 9.0);
    protected static final VoxelShape WEST_WALL_SHAPE = Block.box(7.0, 2.0, 4.0, 16.0, 10.0, 12.0);
    protected static final VoxelShape EAST_WALL_SHAPE = Block.box(0.0, 2.0, 4.0, 9.0, 10.0, 12.0);

    public ReceptacleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NonNull VoxelShape getShape(BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST_WALL_SHAPE;
            case WEST -> WEST_WALL_SHAPE;
            case SOUTH -> SOUTH_WALL_SHAPE;
            case NORTH -> NORTH_WALL_SHAPE;
            default -> STANDING_SHAPE;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos blockPos, @NonNull BlockState blockState) {
        return null;
    }
}
