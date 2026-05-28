package top.begonia.wizardry.core.entity.projectile.bomb;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.core.registry.WizardryItems;

public class SmokeBombEntity extends BombEntity {

    public SmokeBombEntity(EntityType<? extends SmokeBombEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getLifetime() {
        return -1;
    }

    @Override
    protected @NonNull Item getDefaultItem() {
        return WizardryItems.SMOKE_BOMB.get();
    }
}
