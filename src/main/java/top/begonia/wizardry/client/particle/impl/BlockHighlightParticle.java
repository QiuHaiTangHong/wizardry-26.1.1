package top.begonia.wizardry.client.particle.impl;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.client.particle.AbstractParticle;
import top.begonia.wizardry.client.util.GeometryUtils;

public class BlockHighlightParticle extends AbstractParticle {
    private static final SingleQuadParticle.Layer HIGHLIGHT_LAYER = new SingleQuadParticle.Layer(
            true,
            TextureAtlas.LOCATION_PARTICLES,
            RenderPipelines.LIGHTNING
    );

    public BlockHighlightParticle(
            ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites,
            RandomSource randomSource
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites, randomSource);
        this.gravity = 0.0F;
        this.lifetime = 160;
        this.quadSize = 5.0F * (1.0F + 2.0F * (float) GeometryUtils.ANTI_Z_FIGHTING_OFFSET);
        this.hasPhysics = false;
    }

    @Override
    protected SingleQuadParticle.@NonNull Layer getLayer() {
        return HIGHLIGHT_LAYER;
    }

    @Override
    public void tick() {
        super.tick();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        if (this.age > this.lifetime / 2) {
            this.alpha = 1.0F - ((float) this.age - this.lifetime / 2.0F) / (this.lifetime / 2.0F);
        }
        Direction facing = Direction.getNearest((int) xd, (int) yd, (int) zd, Direction.UP);
        BlockPos currentPos = BlockPos.containing(this.x, this.y, this.z);
        BlockPos attachedPos = currentPos.relative(facing.getOpposite());
        if (!this.level.getBlockState(attachedPos).isSolid()) {
            this.remove();
        }
    }
}
