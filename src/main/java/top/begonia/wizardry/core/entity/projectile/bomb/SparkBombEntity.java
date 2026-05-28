package top.begonia.wizardry.core.entity.projectile.bomb;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.core.registry.WizardryItems;

public class SparkBombEntity extends BombEntity {

    public SparkBombEntity(EntityType<? extends SparkBombEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getLifetime() {
        return -1;
    }

    @Override
    protected @NonNull Item getDefaultItem() {
        return WizardryItems.SPARK_BOMB.get();
    }

}
