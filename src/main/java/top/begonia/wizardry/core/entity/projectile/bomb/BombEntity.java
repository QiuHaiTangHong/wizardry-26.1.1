package top.begonia.wizardry.core.entity.projectile.bomb;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.core.entity.projectile.MagicProjectileEntity;

public abstract class BombEntity extends MagicProjectileEntity {

    public float blastMultiplier = 1.0f;
    protected static final EntityDataAccessor<Float> BLAST_MULTIPLIER_ACCESSOR =
            SynchedEntityData.defineId(BombEntity.class, EntityDataSerializers.FLOAT);

    public BombEntity(EntityType<? extends BombEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.blastMultiplier = valueInput.getFloatOr("blastMultiplier", 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putFloat("blastMultiplier", this.blastMultiplier);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NonNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BLAST_MULTIPLIER_ACCESSOR, 1.0F);
    }

    @Override
    public void recreateFromPacket(@NonNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        int ownerId = packet.getData();
        if (ownerId != 0) {
            Entity owner = this.level().getEntity(ownerId);
            if (owner != null) {
                this.setOwner(owner);
            }
        }
    }
}
