package top.begonia.wizardry.core.entity.projectile.bomb;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.client.particle.WizardryParticleOptions;
import top.begonia.wizardry.core.damage.WizardryDamageSource;
import top.begonia.wizardry.core.damage.WizardryDamageType;
import top.begonia.wizardry.core.registry.*;
import top.begonia.wizardry.core.spell.AbstractSpell;
import top.begonia.wizardry.core.util.EntityUtils;

import java.util.List;

public class FireBombEntity extends BombEntity {

    public FireBombEntity(EntityType<? extends BombEntity> type, Level level) {
        super(type, level);
    }

    public FireBombEntity(EntityType<? extends BombEntity> type, LivingEntity owner, Level level, ItemStack itemStack) {
        super(type, owner, level, itemStack);
    }

    public FireBombEntity(EntityType<? extends BombEntity> type, double x, double y, double z, Level level, ItemStack itemStack) {
        super(type, x, y, z, level, itemStack);
    }

    @Override
    public int getLifetime() {
        return -1;
    }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {
        Entity entityHit = hitResult instanceof EntityHitResult entityHitResult ? entityHitResult.getEntity() : null;
        if (entityHit != null) {
            float damage = WizardrySpells.FIRE_BOMB.get().getBaseProperty(AbstractSpell.DIRECT_DAMAGE) * damageMultiplier;
            entityHit.hurt(
                    WizardryDamageSource.causeIndirectMagicDamage(
                            this,
                            this.getOwner(),
                            WizardryDamageType.FIRE,
                            false
                    ),
                    damage
            );
            if (!entityHit.level().isClientSide() && !entityHit.fireImmune()) {
                entityHit.igniteForSeconds(
                        WizardrySpells.FIRE_BOMB
                                .get()
                                .getBaseProperty("burn_duration") / 20.0f
                );
            }
        }

        // Particle effect
        if (this.level().isClientSide()) {
            this.level().addParticle(
                    WizardryParticleOptions
                            .create(WizardryParticles.FLASH.get())
                            .scale(5 * blastMultiplier)
                            .clr(1, 0.6f, 0),
                    this.position().x(), this.position().y(), this.position().z(),
                    0.0D, 0.0D, 0.0D
            );

            for (int i = 0; i < 60 * blastMultiplier; i++) {
                this.level().addParticle(
                        WizardryParticleOptions
                                .create(WizardryParticles.MAGIC_FIRE.get())
                                .scale(2 + this.random.nextFloat())
                                .time(10 + this.random.nextInt(4))
                                .radius(2 * blastMultiplier),
                        this.getX(), this.getY(), this.getZ(),
                        0.0D, 0.0D, 0.0D
                );
                this.level().addParticle(
                        WizardryParticleOptions
                                .create(WizardryParticles.DARK_MAGIC.get())
                                .time(10 + this.random.nextInt(4))
                                .radius(2 * blastMultiplier)
                                .clr(1.0f, 0.2f + this.random.nextFloat() * 0.4f, 0.0f),
                        this.getX(), this.getY(), this.getZ(),
                        0.0D, 0.0D, 0.0D
                );
            }

            this.level().addParticle(
                    ParticleTypes.EXPLOSION,
                    this.getX(), this.getY(), this.getZ(),
                    0, 0, 0
            );
        }

        if (!this.level().isClientSide()) {
            this.playSound(WizardrySounds.ENTITY_FIREBOMB_SMASH.get(), 1.5F, this.random.nextFloat() * 0.4F + 0.6F);
            this.playSound(WizardrySounds.ENTITY_FIREBOMB_FIRE.get(), 1, 1);
            double range = WizardrySpells.FIRE_BOMB.get().getBaseProperty(AbstractSpell.BLAST_RADIUS) * blastMultiplier;
            List<LivingEntity> targets = EntityUtils.getLivingWithinRadius(
                    range,
                    this.getX(), this.getY(), this.getZ(),
                    this.level()
            );
            AbstractSpell spellInstance = WizardrySpells.FIRE_BOMB.get();
            float splashDamage = spellInstance.getBaseProperty(AbstractSpell.SPLASH_DAMAGE) * this.damageMultiplier;
            float burnDurationTicks = spellInstance.getBaseProperty(AbstractSpell.BURN_DURATION);
            for (LivingEntity target : targets) {
                if (target != entityHit && target != this.getOwner() && !target.fireImmune()) {
                    target.hurt(
                            WizardryDamageSource.causeIndirectMagicDamage(
                                    this,
                                    this.getOwner(),
                                    WizardryDamageType.FIRE,
                                    false
                            ),
                            splashDamage
                    );
                    target.igniteForSeconds(burnDurationTicks / 20.0f);
                }
            }

            this.discard();
        }
    }

    @Override
    protected @NonNull Item getDefaultItem() {
        return WizardryItems.FIRE_BOMB.get();
    }
}
