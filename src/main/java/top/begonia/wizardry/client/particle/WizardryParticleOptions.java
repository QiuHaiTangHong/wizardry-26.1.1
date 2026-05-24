package top.begonia.wizardry.client.particle;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

public class WizardryParticleOptions implements ParticleOptions {
    private final ParticleType<?> type;
    public float r = -1, g = -1, b = -1;
    public float fr = -1, fg = -1, fb = -1;
    public double radius;
    public double rpt;
    public int lifetime = -1;
    public boolean gravity;
    public boolean shaded;
    public boolean collide;
    public float scale = 1;
    public Entity entity = null;
    public float yaw = Float.NaN, pitch = Float.NaN;
    public double tx = Double.NaN, ty = Double.NaN, tz = Double.NaN;
    public double tvx = Double.NaN, tvy = Double.NaN, tvz = Double.NaN;
    public Entity target;
    public long seed;
    public double length = -1;

    public WizardryParticleOptions(ParticleType<?> type) {
        this.type = type;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull WizardryParticleOptions create(ParticleType<?> type) {
        return new WizardryParticleOptions(type);
    }

    public WizardryParticleOptions clr(float r, float g, float b) {
        this.r = Math.clamp(r, 0, 1);
        this.g = Math.clamp(g, 0, 1);
        this.b = Math.clamp(b, 0, 1);
        return this;
    }

    public WizardryParticleOptions clr(int r, int g, int b) {
        return this.clr(r / 255f, g / 255f, b / 255f);
    }

    public WizardryParticleOptions clr(int hex) {
        return this.clr((hex & 0xFF0000) >> 16, (hex & 0xFF00) >> 8, hex & 0xFF);
    }

    public WizardryParticleOptions fade(float r, float g, float b) {
        this.fr = Math.clamp(r, 0, 1);
        this.fg = Math.clamp(g, 0, 1);
        this.fb = Math.clamp(b, 0, 1);
        return this;
    }

    public WizardryParticleOptions fade(int r, int g, int b) {
        return this.fade(r / 255f, g / 255f, b / 255f);
    }

    public WizardryParticleOptions fade(int hex) {
        return this.fade((hex & 0xFF0000) >> 16, (hex & 0xFF00) >> 8, hex & 0xFF);
    }

    public WizardryParticleOptions scale(float scale) {
        this.scale = scale;
        return this;
    }

    public WizardryParticleOptions time(int lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public WizardryParticleOptions seed(long seed) {
        this.seed = seed;
        return this;
    }

    public WizardryParticleOptions spin(double radius, double speed) {
        this.radius = radius;
        this.rpt = speed;
        return this;
    }

    public WizardryParticleOptions gravity(boolean gravity) {
        this.gravity = gravity;
        return this;
    }

    public WizardryParticleOptions shaded(boolean shaded) {
        this.shaded = shaded;
        return this;
    }

    public WizardryParticleOptions collide(boolean collide) {
        this.collide = collide;
        return this;
    }

    public WizardryParticleOptions entity(Entity entity) {
        this.entity = entity;
        return this;
    }

    public WizardryParticleOptions face(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        return this;
    }

    public WizardryParticleOptions face(@NonNull Direction direction) {
        return face(direction.toYRot(), direction.getAxis().isVertical() ? direction.getAxisDirection().getStep() * 90 : 0);
    }

    public WizardryParticleOptions target(double x, double y, double z) {
        this.tx = x;
        this.ty = y;
        this.tz = z;
        return this;
    }

    public WizardryParticleOptions target(@NonNull Vec3 pos) {
        return target(pos.x, pos.y, pos.z);
    }

    public WizardryParticleOptions target(Entity target) {
        this.target = target;
        return this;
    }

    public WizardryParticleOptions length(double length) {
        this.length = length;
        return this;
    }

    @Override
    public @NonNull ParticleType<?> getType() {
        return this.type;
    }
}
