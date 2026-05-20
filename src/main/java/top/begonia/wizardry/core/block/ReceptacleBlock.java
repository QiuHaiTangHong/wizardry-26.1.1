package top.begonia.wizardry.core.block;

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
import top.begonia.wizardry.core.constants.ElementEnum;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

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
    private static final double WALL_PARTICLE_OFFSET = 3 / 16d;
    public static final Map<ElementEnum, int[]> PARTICLE_COLOURS = Collections.unmodifiableMap(
            new EnumMap<>(Map.of(
                    ElementEnum.MAGIC, new int[]{0xe4c7cd, 0xfeffbe, 0x9d2cf3},
                    ElementEnum.FIRE, new int[]{0xff9600, 0xfffe67, 0xd02700},
                    ElementEnum.ICE, new int[]{0xa3e8f4, 0xe9f9fc, 0x138397},
                    ElementEnum.LIGHTNING, new int[]{0x409ee1, 0xf5f0ff, 0x225474},
                    ElementEnum.NECROMANCY, new int[]{0xa811ce, 0xf575f5, 0x382366},
                    ElementEnum.EARTH, new int[]{0xa8f408, 0xc8ffb2, 0x795c28},
                    ElementEnum.SORCERY, new int[]{0x56e8e3, 0xe8fcfc, 0x16a64d},
                    ElementEnum.HEALING, new int[]{0xfff69e, 0xfffff6, 0xa18200}
            ))
    );

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
