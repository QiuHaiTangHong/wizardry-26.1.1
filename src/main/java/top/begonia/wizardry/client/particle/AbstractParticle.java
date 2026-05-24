package top.begonia.wizardry.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.core.entity.ICustomHitbox;

import javax.annotation.Nullable;
import java.util.List;

public abstract class AbstractParticle extends SingleQuadParticle {
    protected final SpriteSet sprites;
    protected long seed;
    protected boolean shaded = false;
    protected float initialRed;
    protected float initialGreen;
    protected float initialBlue;
    protected float fadeRed = 0;
    protected float fadeGreen = 0;
    protected float fadeBlue = 0;
    protected float angle;
    protected double radius = 0;
    protected double speed = 0;
    @Nullable
    protected Entity entity = null;
    protected double relativeX, relativeY, relativeZ;
    protected double relativeMotionX, relativeMotionY, relativeMotionZ;
    protected float yaw = Float.NaN;
    protected float pitch = Float.NaN;
    private static final double SPREAD_FACTOR = 0.2;
    private static final double IMPACT_FRICTION = 0.2;
    private double prevVelX, prevVelY, prevVelZ;

    public AbstractParticle(
            @NonNull WizardryParticleOptions options,
            ClientLevel level,
            double x, double y, double z,
            double xd, double yd, double zd,
            @NonNull SpriteSet sprites
    ) {
        super(level, x, y, z, xd, yd, zd, sprites.get(0, 1));
        this.sprites = sprites;
        this.relativeX = x;
        this.relativeY = y;
        this.relativeZ = z;
        this.setSpriteFromAge(sprites);
        this.setParticleSpeed(xd, yd, zd);

        if (options.fr >= 0 && options.fg >= 0 && options.fb >= 0) {
            this.setFadeColour(options.fr, options.fg, options.fb);
        } else {
            this.setFadeColour(this.initialRed, this.initialGreen, this.initialBlue);
        }
        if (options.radius > 0) {
            this.setSpin(options.radius, options.rpt);
        }
        if (!Float.isNaN(options.yaw) && !Float.isNaN(options.pitch)) {
            this.setFacing(options.yaw, options.pitch);
        }
        if (!Double.isNaN(options.tvx) && !Double.isNaN(options.tvy) && !Double.isNaN(options.tvz)) {
            this.setTargetVelocity(options.tvx, options.tvy, options.tvz);
        }

        this.setInitialColor(options.r, options.g, options.b);
        this.setColor(options.r, options.g, options.b);
        this.setSeed(options.seed);
        this.setLength(options.length);
        this.setLifetime(options.lifetime);
        this.scale(options.scale);
        this.setGravity(options.gravity);
        this.setShaded(options.shaded);
        this.setCollisions(options.collide);
        this.setEntity(options.entity);
        this.setTargetPosition(options.tx, options.ty, options.tz);
        this.setTargetEntity(options.target);
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public void setShaded(boolean shaded) {
        this.shaded = shaded;
    }

    public void setGravity(boolean gravity) {
        this.gravity = gravity ? 1.0F : 0.0F;
    }

    public void setCollisions(boolean canCollide) {
        this.hasPhysics = canCollide;
    }

    public void setSpin(double radius, double speed) {
        this.radius = radius;
        this.speed = speed * 2 * Math.PI;
        this.angle = this.random.nextFloat() * (float) Math.PI * 2;

        this.x = relativeX - radius * Mth.cos(angle);
        this.z = relativeZ + radius * Mth.sin(angle);

        this.relativeMotionX = xd;
        this.relativeMotionY = yd;
        this.relativeMotionZ = zd;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
        if (entity != null) {
            this.setPos(entity.getX() + relativeX, entity.getY() + relativeY, entity.getZ() + relativeZ);
            this.xo = this.x;
            this.yo = this.y;
            this.zo = this.z;
            this.relativeMotionX = xd;
            this.relativeMotionY = yd;
            this.relativeMotionZ = zd;
        }
    }

    public void setInitialColor(float r, float g, float b) {
        this.initialRed = r;
        this.initialGreen = g;
        this.initialBlue = b;
    }

    public void setFadeColour(float r, float g, float b) {
        this.fadeRed = r;
        this.fadeGreen = g;
        this.fadeBlue = b;
    }

    public void setFacing(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void setTargetPosition(double x, double y, double z) {
    }

    public void setTargetVelocity(double vx, double vy, double vz) {
    }

    public void setTargetEntity(Entity target) {
    }

    public void setLength(double length) {
    }

    @Override
    protected SingleQuadParticle.@NonNull Layer getLayer() {
        return Layer.bySprite(this.sprite);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        if (this.shaded) {
            return super.getLightCoords(partialTick);
        } else {
            return 15728880;
        }
    }

    protected void updateEntityLinking(float partialTicks) {
        if (this.entity != null) {
            if (this.entity.isRemoved()) {
                this.remove();
                return;
            }
            this.xo = this.x + this.entity.xOld - this.entity.getX() - this.relativeMotionX * (1.0F - partialTicks);
            this.yo = this.y + this.entity.yOld - this.entity.getY() - this.relativeMotionY * (1.0F - partialTicks);
            this.zo = this.z + this.entity.zOld - this.entity.getZ() - this.relativeMotionZ * (1.0F - partialTicks);
        }
    }

    @Override
    public void extract(@NonNull QuadParticleRenderState particleTypeRenderState, @NonNull Camera camera, float partialTickTime) {
        this.updateEntityLinking(partialTickTime);
        Quaternionf rotation = new Quaternionf();
        if (Float.isNaN(this.yaw) || Float.isNaN(this.pitch)) {
            this.getFacingCameraMode().setRotation(rotation, camera, partialTickTime);
        } else {
            float degToRadFactor = 0.017453292F;
            rotation.rotationYXZ(-this.yaw * degToRadFactor, this.pitch * degToRadFactor, 0.0F);
        }
        if (this.roll != 0.0F) {
            rotation.rotateZ(Mth.lerp(partialTickTime, this.oRoll, this.roll));
        }
        this.extractRotatedQuad(particleTypeRenderState, camera, rotation, partialTickTime);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasPhysics && this.onGround) {
            this.xd /= 0.699999988079071D;
            this.zd /= 0.699999988079071D;
        }
        if (this.entity != null || this.radius > 0) {
            double tx = this.relativeX;
            double ty = this.relativeY;
            double tz = this.relativeZ;
            if (this.entity != null) {
                if (this.entity.isRemoved()) {
                    this.remove();
                    return;
                } else {
                    tx += this.entity.getX();
                    ty += this.entity.getY();
                    tz += this.entity.getZ();
                }
            }
            if (this.radius > 0) {
                this.angle += (float) this.speed;
                tx += this.radius * -Mth.cos(this.angle);
                tz += this.radius * Mth.sin(this.angle);
            }
            this.setPos(tx, ty, tz);

            this.relativeX += this.relativeMotionX;
            this.relativeY += this.relativeMotionY;
            this.relativeZ += this.relativeMotionZ;
        }
        float ageFraction = (float) this.age / (float) this.lifetime;
        this.rCol = this.initialRed + (this.fadeRed - this.initialRed) * ageFraction;
        this.gCol = this.initialGreen + (this.fadeGreen - this.initialGreen) * ageFraction;
        this.bCol = this.initialBlue + (this.fadeBlue - this.initialBlue) * ageFraction;
        this.setSpriteFromAge(this.sprites);
        if (this.hasPhysics) {
            if (this.xd == 0 && this.prevVelX != 0) {
                this.yd *= IMPACT_FRICTION;
                this.zd *= IMPACT_FRICTION;
                this.yd += (this.random.nextDouble() * 2 - 1) * this.prevVelX * SPREAD_FACTOR;
                this.zd += (this.random.nextDouble() * 2 - 1) * this.prevVelX * SPREAD_FACTOR;
            }

            if (this.yd == 0 && this.prevVelY != 0) {
                this.xd *= IMPACT_FRICTION;
                this.zd *= IMPACT_FRICTION;
                this.xd += (this.random.nextDouble() * 2 - 1) * this.prevVelY * SPREAD_FACTOR;
                this.zd += (this.random.nextDouble() * 2 - 1) * this.prevVelY * SPREAD_FACTOR;
            }

            if (this.zd == 0 && this.prevVelZ != 0) {
                this.xd *= IMPACT_FRICTION;
                this.yd *= IMPACT_FRICTION;
                this.xd += (this.random.nextDouble() * 2 - 1) * this.prevVelZ * SPREAD_FACTOR;
                this.yd += (this.random.nextDouble() * 2 - 1) * this.prevVelZ * SPREAD_FACTOR;
            }
            double searchRadius = 20.0D;
            AABB searchBox = new AABB(this.x, this.y, this.z, this.x, this.y, this.z).inflate(searchRadius);
            List<Entity> nearbyEntities = this.level.getEntities(null, searchBox);
            Vec3 currentPos = new Vec3(this.x, this.y, this.z);
            Vec3 previousPos = new Vec3(this.xo, this.yo, this.zo);
            for (Entity e : nearbyEntities) {
                if (e instanceof ICustomHitbox customHitbox) {
                    if (customHitbox.calculateIntercept(currentPos, previousPos, 0) != null) {
                        this.remove();
                        break;
                    }
                }
            }
        }
        this.prevVelX = this.xd;
        this.prevVelY = this.yd;
        this.prevVelZ = this.zd;
    }

    protected Vec3 getRenderPos(float partialTicks) {
        double lerpX = Mth.lerp(partialTicks, this.xo, this.x);
        double lerpY = Mth.lerp(partialTicks, this.yo, this.y);
        double lerpZ = Mth.lerp(partialTicks, this.zo, this.z);
        if (this.entity != null) {
            if (this.entity.isRemoved()) {
                this.remove();
            } else {
                double entityMovementX = Mth.lerp(partialTicks, this.entity.xOld, this.entity.getX());
                double entityMovementY = Mth.lerp(partialTicks, this.entity.yOld, this.entity.getY());
                double entityMovementZ = Mth.lerp(partialTicks, this.entity.zOld, this.entity.getZ());
                return new Vec3(
                        lerpX + entityMovementX - this.relativeMotionX * (1.0F - partialTicks),
                        lerpY + entityMovementY - this.relativeMotionY * (1.0F - partialTicks),
                        lerpZ + entityMovementZ - this.relativeMotionZ * (1.0F - partialTicks)
                );
            }
        }
        return new Vec3(lerpX, lerpY, lerpZ);
    }
}
