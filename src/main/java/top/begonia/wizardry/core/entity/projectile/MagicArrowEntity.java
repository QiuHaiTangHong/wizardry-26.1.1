package top.begonia.wizardry.core.entity.projectile;

import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.core.damage.WizardryDamageType;
import top.begonia.wizardry.core.item.impl.ArtefactItem;
import top.begonia.wizardry.core.registry.WizardryItems;

import java.util.List;

public abstract class MagicArrowEntity extends Arrow {

    public static final double LAUNCH_Y_OFFSET = 0.1;
    public static final int SEEKING_TIME = 15;

    private int blockX = -1;
    private int blockY = -1;
    private int blockZ = -1;
    private BlockState stuckInBlock;
    private int inData;
    private boolean inGround;
    public int arrowShake;
    int ticksInGround;
    int ticksInAir;
    private int knockbackStrength;
    public float damageMultiplier = 1.0f;

    public MagicArrowEntity(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
    }

    public void aim(LivingEntity caster, float speed) {

        this.setOwner(caster);

        this.setPos(
                caster.getX(),
                caster.getY() + (double) caster.getEyeHeight() - LAUNCH_Y_OFFSET,
                caster.getZ()
        );
        this.setRot(caster.getYRot(), caster.getXRot());

        Vec3 lookVec = calculateViewVector(0, this.getYRot());

        this.setPos(
                this.getX() - lookVec.x * 0.16F,
                this.getY() - 0.10000000149011612D,
                this.getZ() - lookVec.z * 0.16F
        );

        this.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, speed * 1.5F, 1.0F);
    }

    public void aim(LivingEntity owner, @NonNull Entity target, float speed, float aimingError) {
        this.setOwner(owner);
        double spawnY = owner.getY() + (double) owner.getEyeHeight() - LAUNCH_Y_OFFSET;
        double dx = target.getX() - owner.getX();
        double dz = target.getZ() - owner.getZ();
        double dy = target.getY() + (double) (target.getBbHeight() / (this.doGravity() ? 3.0F : 2.0F)) - spawnY;
        double horizontalDistance = Mth.sqrt((float) (dx * dx + dz * dz));
        if (horizontalDistance >= 1.0E-7D) {
            this.setPos(owner.getX() + (dx / horizontalDistance), spawnY, owner.getZ() + (dz / horizontalDistance));
            float bulletDropCompensation = this.doGravity() ? (float) horizontalDistance * 0.2F : 0.0F;
            this.shoot(dx, dy + (double) bulletDropCompensation, dz, speed, aimingError);
        }
    }

    public abstract double getDamage();

    public abstract int getLifetime();

    public ResourceKey<DamageType> getDamageType() {
        return WizardryDamageType.MAGIC;
    }

    public boolean doGravity() {
        return true;
    }

    public boolean doDeceleration() {
        return true;
    }

    public boolean doOverpenetration() {
        return false;
    }

    public float getSeekingStrength() {
//        return this.getOwner() instanceof Player player && ArtefactItem.isArtefactActive(player,
//                WizardryItems.RING_SEEKING) ? 2 : 0;
        return 0;
    }

    public void setKnockbackStrength(int knockback) {
        this.knockbackStrength = knockback;
    }

    protected void tickInAir() {
    }

    protected void onEntityHit(EntityHitResult entityHit) {
    }

    protected void onBlockHit(BlockHitResult blockHitResult) {
    }

    @Override
    public void tick() {

        super.tick();

        if (getLifetime() >= 0 && this.tickCount > this.getLifetime()) {
            this.discard();
        }

        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            Vec3 motion = this.getDeltaMovement();
            double horizontalDistance = motion.x * motion.x + motion.z * motion.z;
            float yaw = (float) (Mth.atan2(motion.x, motion.z) * (180.0D / Math.PI));
            float pitch = (float) (Mth.atan2(motion.y, net.minecraft.util.Mth.sqrt((float) horizontalDistance)) * (180.0D / Math.PI));
            this.yRotO = yaw;
            this.xRotO = pitch;
        }

//        BlockPos blockpos = new BlockPos(this.blockX, this.blockY, this.blockZ);
//        BlockState iblockstate = this.level().getBlockState(blockpos);
//        if (iblockstate.getMaterial() != Material.AIR) {
//            AxisAlignedBB axisalignedbb = iblockstate.getCollisionBoundingBox(this.world, blockpos);
//
//            if (axisalignedbb != Block.NULL_AABB
//                    && axisalignedbb.offset(blockpos).contains(new Vec3d(this.posX, this.posY, this.posZ))) {
//                this.inGround = true;
//            }
//        }
//
//        if (this.arrowShake > 0) {
//            --this.arrowShake;
//        }
//
//        // When the arrow is in the ground
//        if (this.inGround) {
//            ++this.ticksInGround;
//            this.tickInGround();
//        }
//        // When the arrow is in the air
//        else {
//
//            this.tickInAir();
//
//            this.ticksInGround = 0;
//            ++this.ticksInAir;
//
//            // Does a ray trace to determine whether the projectile will hit a block in the next tick
//
//            Vec3d vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
//            Vec3d vec3d = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
//            RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d1, vec3d, false, true, false);
//            vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
//            vec3d = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
//
//            if (raytraceresult != null) {
//                vec3d = new Vec3d(raytraceresult.hitVec.x, raytraceresult.hitVec.y,
//                        raytraceresult.hitVec.z);
//            }
//
//            // Uses bounding boxes to determine whether the projectile will hit an entity in the next tick, and if so
//            // overwrites the block hit with an entity
//
//            Entity entity = null;
//            List<?> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox()
//                    .expand(this.motionX, this.motionY, this.motionZ).grow(1.0D, 1.0D, 1.0D));
//            double d0 = 0.0D;
//            int i;
//            float f1;
//
//            for (i = 0; i < list.size(); ++i) {
//                Entity entity1 = (Entity) list.get(i);
//
//                if (entity1.canBeCollidedWith() && (entity1 != this.getCaster() || this.ticksInAir >= 5)) {
//                    f1 = 0.3F;
//                    AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().grow((double) f1, (double) f1,
//                            (double) f1);
//                    RayTraceResult RayTraceResult1 = axisalignedbb1.calculateIntercept(vec3d1, vec3d);
//
//                    if (RayTraceResult1 != null) {
//                        double d1 = vec3d1.distanceTo(RayTraceResult1.hitVec);
//
//                        if (d1 < d0 || d0 == 0.0D) {
//                            entity = entity1;
//                            d0 = d1;
//                        }
//                    }
//                }
//            }
//
//            if (entity != null) {
//                raytraceresult = new RayTraceResult(entity);
//            }
//
//            // Players that are considered invulnerable to the caster allow the projectile to pass straight through
//            // them.
//            if (raytraceresult != null && raytraceresult.entityHit != null
//                    && raytraceresult.entityHit instanceof EntityPlayer) {
//                EntityPlayer entityplayer = (EntityPlayer) raytraceresult.entityHit;
//
//                if (entityplayer.capabilities.disableDamage || this.getCaster() instanceof EntityPlayer
//                        && !((EntityPlayer) this.getCaster()).canAttackPlayer(entityplayer)) {
//                    raytraceresult = null;
//                }
//            }
//
//            // If the arrow hits something
//            if (raytraceresult != null) {
//                // If the arrow hits an entity
//                if (raytraceresult.entityHit != null) {
//                    DamageSource damagesource = null;
//
//                    if (this.getCaster() == null) {
//                        damagesource = DamageSource.causeThrownDamage(this, this);
//                    } else {
//                        damagesource = MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), this.getDamageType()).setProjectile();
//                    }
//
//                    if (raytraceresult.entityHit.attackEntityFrom(damagesource,
//                            (float) (this.getDamage() * this.damageMultiplier))) {
//                        if (raytraceresult.entityHit instanceof EntityLivingBase) {
//                            EntityLivingBase entityHit = (EntityLivingBase) raytraceresult.entityHit;
//
//                            this.onEntityHit(entityHit);
//
//                            if (this.knockbackStrength > 0) {
//                                float f4 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
//
//                                if (f4 > 0.0F) {
//                                    raytraceresult.entityHit.addVelocity(
//                                            this.motionX * (double) this.knockbackStrength * 0.6000000238418579D
//                                                    / (double) f4,
//                                            0.1D, this.motionZ * (double) this.knockbackStrength * 0.6000000238418579D
//                                                    / (double) f4);
//                                }
//                            }
//
//                            // Thorns enchantment
//                            if (this.getCaster() != null) {
//                                EnchantmentHelper.applyThornEnchantments(entityHit, this.getCaster());
//                                EnchantmentHelper.applyArthropodEnchantments(this.getCaster(), entityHit);
//                            }
//
//                            if (this.getCaster() != null && raytraceresult.entityHit != this.getCaster()
//                                    && raytraceresult.entityHit instanceof EntityPlayer
//                                    && this.getCaster() instanceof EntityPlayerMP) {
//                                ((EntityPlayerMP) this.getCaster()).connection
//                                        .sendPacket(new SPacketChangeGameState(6, 0.0F));
//                            }
//                        }
//
//                        if (!(raytraceresult.entityHit instanceof EntityEnderman) && !this.doOverpenetration()) {
//                            this.setDead();
//                        }
//                    } else {
//                        if (!this.doOverpenetration()) this.setDead();
//
//                        // Was the 'rebound' that happened when entities were immune to damage
//                        /* this.motionX *= -0.10000000149011612D; this.motionY *= -0.10000000149011612D; this.motionZ *=
//                         * -0.10000000149011612D; this.rotationYaw += 180.0F; this.prevRotationYaw += 180.0F;
//                         * this.ticksInAir = 0; */
//                    }
//                }
//                // If the arrow hits a block
//                else {
//                    this.blockX = raytraceresult.getBlockPos().getX();
//                    this.blockY = raytraceresult.getBlockPos().getY();
//                    this.blockZ = raytraceresult.getBlockPos().getZ();
//                    this.stuckInBlock = this.world.getBlockState(raytraceresult.getBlockPos());
//                    this.motionX = (double) ((float) (raytraceresult.hitVec.x - this.posX));
//                    this.motionY = (double) ((float) (raytraceresult.hitVec.y - this.posY));
//                    this.motionZ = (double) ((float) (raytraceresult.hitVec.z - this.posZ));
//                    // f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ *
//                    // this.motionZ);
//                    // this.posX -= this.motionX / (double)f2 * 0.05000000074505806D;
//                    // this.posY -= this.motionY / (double)f2 * 0.05000000074505806D;
//                    // this.posZ -= this.motionZ / (double)f2 * 0.05000000074505806D;
//                    // this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
//                    this.inGround = true;
//                    this.arrowShake = 7;
//
//                    this.onBlockHit(raytraceresult);
//
//                    if (this.stuckInBlock.getMaterial() != Material.AIR) {
//                        this.stuckInBlock.getBlock().onEntityCollision(this.world, raytraceresult.getBlockPos(),
//                                this.stuckInBlock, this);
//                    }
//                }
//            }
//
//            // Seeking
//            if (getSeekingStrength() > 0) {
//
//                Vec3d velocity = new Vec3d(motionX, motionY, motionZ);
//
//                RayTraceResult hit = RayTracer.rayTrace(world, this.getPositionVector(),
//                        this.getPositionVector().add(velocity.scale(SEEKING_TIME)), getSeekingStrength(), false,
//                        true, false, EntityLivingBase.class, RayTracer.ignoreEntityFilter(null));
//
//                if (hit != null && hit.entityHit != null) {
//
//                    if (AllyDesignationSystem.isValidTarget(getCaster(), hit.entityHit)) {
//
//                        Vec3d direction = new Vec3d(hit.entityHit.posX, hit.entityHit.posY + hit.entityHit.height / 2,
//                                hit.entityHit.posZ).subtract(this.getPositionVector()).normalize().scale(velocity.length());
//
//                        motionX = motionX + 2 * (direction.x - motionX) / SEEKING_TIME;
//                        motionY = motionY + 2 * (direction.y - motionY) / SEEKING_TIME;
//                        motionZ = motionZ + 2 * (direction.z - motionZ) / SEEKING_TIME;
//                    }
//                }
//            }
//
//            this.posX += this.motionX;
//            this.posY += this.motionY;
//            this.posZ += this.motionZ;
//            // f2 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
//            this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
//
//            // for (this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f2) * 180.0D / Math.PI);
//            // this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
//            // {
//            // ;
//            // }
//
//            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
//                this.prevRotationPitch += 360.0F;
//            }
//
//            while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
//                this.prevRotationYaw -= 360.0F;
//            }
//
//            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
//                this.prevRotationYaw += 360.0F;
//            }
//
//            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
//            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
//
//            float f3 = 0.99F;
//
//            if (this.isInWater()) {
//                for (int l = 0; l < 4; ++l) {
//                    float f4 = 0.25F;
//                    this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double) f4,
//                            this.posY - this.motionY * (double) f4, this.posZ - this.motionZ * (double) f4, this.motionX,
//                            this.motionY, this.motionZ);
//                }
//
//                f3 = 0.8F;
//            }
//
//            if (this.isWet()) {
//                this.extinguish();
//            }
//
//            if (this.doDeceleration()) {
//                this.motionX *= (double) f3;
//                this.motionY *= (double) f3;
//                this.motionZ *= (double) f3;
//            }
//
//            if (this.doGravity()) this.motionY -= 0.05;
//
//            this.setPosition(this.posX, this.posY, this.posZ);
//            this.doBlockCollisions();
//        }
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.blockX = valueInput.getIntOr("xTile", 0);
        this.blockY = valueInput.getIntOr("yTile", 0);
        this.blockZ = valueInput.getIntOr("zTile", 0);
        this.inData = valueInput.getIntOr("inData", 0);
        this.damageMultiplier = valueInput.getFloatOr("damageMultiplier", 0.0F);
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("xTile", this.blockX);
        valueOutput.putInt("yTile", this.blockY);
        valueOutput.putInt("zTile", this.blockZ);
        valueOutput.putInt("inData", this.inData);
        valueOutput.putFloat("damageMultiplier", this.damageMultiplier);
    }
}