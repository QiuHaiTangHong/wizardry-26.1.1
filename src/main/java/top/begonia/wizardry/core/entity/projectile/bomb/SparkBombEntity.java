package top.begonia.wizardry.core.entity.projectile.bomb;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class SparkBombEntity extends BombEntity {

    public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";

    public SparkBombEntity(EntityType<? extends SparkBombEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getLifetime() {
        return -1;
    }
}
