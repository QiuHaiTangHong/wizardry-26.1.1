package top.begonia.wizardry.core.entity.projectile.bomb;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PoisonBombEntity extends BombEntity {


    public PoisonBombEntity(EntityType<? extends PoisonBombEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getLifetime() {
        return -1;
    }
}
