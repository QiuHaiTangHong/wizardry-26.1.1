package top.begonia.wizardry.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.jspecify.annotations.NonNull;

public class RobeArmourModel<T extends HumanoidRenderState> extends AbstractWizardArmourModel<T> {
    private final ModelPart robe;

    public RobeArmourModel(ModelPart root) {
        super(root);
        this.robe = root.getChild("body").getChild("robe");
    }

    public static @NonNull ArmorModelSetExtension<MeshDefinition> createArmorMeshSetExtension(
            @NonNull CubeDeformation innerDeformation,
            @NonNull CubeDeformation outerDeformation
    ) {
        return AbstractWizardArmourModel.createArmorMeshSetExtension(
                RobeArmourModel::createBaseArmorMesh,
                innerDeformation,
                outerDeformation
        );
    }

    protected static @NonNull MeshDefinition createBaseArmorMesh(CubeDeformation cubeDeformation) {
        MeshDefinition mesh = AbstractWizardArmourModel.createMesh(cubeDeformation, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .mirror()
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 11.0F, 4.0F, cubeDeformation),
                PartPose.ZERO
        );
        body.addOrReplaceChild("robe",
                CubeListBuilder.create()
                        .texOffs(40, 32)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 9.0F, 4.0F, cubeDeformation),
                PartPose.offset(0.0F, 12.0F, 0.0F)
        );
        return mesh;
    }

    @Override
    public void setupAnim(@NonNull T state) {
        super.setupAnim(state);
        if (state.isCrouching) {
            this.robe.z = 4.0F;
        } else {
            this.robe.z = 0.0F;
        }
        this.robe.xRot = (this.leftLeg.xRot + this.rightLeg.xRot) / 2.0F;
        this.robe.yRot = this.body.yRot;
        this.robe.zRot = (this.leftLeg.zRot + this.rightLeg.zRot) / 2.0F;
    }
}
