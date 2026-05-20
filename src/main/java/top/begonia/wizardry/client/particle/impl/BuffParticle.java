package top.begonia.wizardry.client.particle.impl;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.client.particle.AbstractParticle;

public class BuffParticle extends AbstractParticle {
    private final boolean mirror;

    public BuffParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites, RandomSource randomSource) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, randomSource);
        this.xd = 0.0D;
        this.yd = 0.162D;
        this.zd = 0.0D;
        this.mirror = this.random.nextBoolean();
        this.lifetime = 15;
        this.hasPhysics = false;
    }

    @Override
    public void extract(@NonNull QuadParticleRenderState renderState, @NonNull Camera camera, float partialTickTime) {
        this.updateEntityLinking(partialTickTime);
        Vec3 renderCenter = this.getRenderPos(partialTickTime);
        Vec3 cameraPos = camera.position();
        float renderX = (float) (renderCenter.x - cameraPos.x);
        float renderY = (float) (renderCenter.y - cameraPos.y);
        float renderZ = (float) (renderCenter.z - cameraPos.z);
        float ageFraction = (float) this.age / (float) this.lifetime;
        float f = 0.875F - 0.125F * Mth.floor(ageFraction * 8.0F - 0.000001F);
        float g = f + 0.125F;
        float textureScrollOffset = ((float) this.age + partialTickTime) / (float) this.lifetime * -2.0F;
        var sprite = this.sprites.get(this.age, this.lifetime);
        float atlasU0 = sprite.getU0();
        float atlasU1 = sprite.getU1();
        float atlasV0 = sprite.getV0();
        float atlasV1 = sprite.getV1();
        float scale = 0.6F;
        float yScale = 0.7F * scale;
        float dx = mirror ? -scale : scale;
        float dz = scale;
        Quaternionf baseRotation = new Quaternionf();
        if (Float.isNaN(this.yaw) || Float.isNaN(this.pitch)) {
            this.getFacingCameraMode().setRotation(baseRotation, camera, partialTickTime);
        } else {
            float degToRadFactor = 0.017453292F;
            baseRotation.rotationYXZ(-this.yaw * degToRadFactor, this.pitch * degToRadFactor, 0.0F);
        }
        int colorInt = this.getColorTint();
        int lightCoords = this.getLightCoords(partialTickTime);
        float[][] faceTemplates = new float[][]{
                {-dx, -dz, dx, -dz, 0.00F, 0.25F},
                {dx, -dz, dx, dz, 0.25F, 0.50F},
                {dx, dz, -dx, dz, 0.50F, 0.75F},
                {-dx, dz, -dx, -dz, 0.75F, 1.00F}
        };

        for (float[] template : faceTemplates) {
            Vector3f localCenter = new Vector3f((template[0] + template[2]) / 2.0F, 0.0F, (template[1] + template[3]) / 2.0F);
            localCenter.rotate(baseRotation);
            float u0 = Mth.lerp(template[4] + textureScrollOffset, atlasU0, atlasU1);
            float u1 = Mth.lerp(template[5] + textureScrollOffset, atlasU0, atlasU1);
            float v0 = Mth.lerp(f, atlasV0, atlasV1);
            float v1 = Mth.lerp(g, atlasV0, atlasV1);
            renderState.add(
                    SingleQuadParticle.Layer.bySprite(this.sprite),
                    renderX + localCenter.x, renderY, renderZ + localCenter.z,
                    baseRotation.x, baseRotation.y, baseRotation.z, baseRotation.w,
                    yScale,
                    u0, u1, v0, v1,
                    colorInt,
                    lightCoords
            );
        }
    }

    private int getColorTint() {
        int r = Mth.clamp((int) (this.rCol * 255.0F), 0, 255);
        int g = Mth.clamp((int) (this.gCol * 255.0F), 0, 255);
        int b = Mth.clamp((int) (this.bCol * 255.0F), 0, 255);
        int a = Mth.clamp((int) (this.alpha * 255.0F), 0, 255);
        return a << 24 | r << 16 | g << 8 | b;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age > this.lifetime / 2) {
            this.alpha = 2.0F - 2.0F * (float) this.age / (float) this.lifetime;
        }
    }
}
