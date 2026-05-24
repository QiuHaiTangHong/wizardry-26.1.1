package top.begonia.wizardry.core.entity.projectile.bomb;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jspecify.annotations.NonNull;

public class FireBombEntity extends BombEntity {

    public FireBombEntity(EntityType<? extends FireBombEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public int getLifetime() {
        return -1;
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        Entity entityHit = hitResult.getEntity();
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult hitResult) {

    }


}
