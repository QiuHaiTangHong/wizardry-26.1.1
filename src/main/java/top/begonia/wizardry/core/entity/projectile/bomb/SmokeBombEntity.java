package top.begonia.wizardry.core.entity.projectile.bomb;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class SmokeBombEntity extends BombEntity {

    public SmokeBombEntity(EntityType<? extends SmokeBombEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getLifetime() {
        return -1;
    }
}
