package top.begonia.wizardry.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.core.registry.WizardryItems;

public class RobeArmourModel<T extends HumanoidRenderState> extends HumanoidModel<T> {

    ModelPart robe;

    public RobeArmourModel(ModelPart root) {
        super(root);
        this.robe = root.getChild("robe");
    }

    public static @NonNull LayerDefinition createLayerDefinition(CubeDeformation delta, int textureWidth, int textureHeight) {
        MeshDefinition mesh = HumanoidModel.createMesh(delta, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .mirror()
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 11.0F, 4.0F, delta),
                PartPose.ZERO
        );
        root.addOrReplaceChild("robe",
                CubeListBuilder.create()
                        .texOffs(40, 32)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 9.0F, 4.0F, delta),
                PartPose.offset(0.0F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, textureWidth, textureHeight);
    }

    @Override
    public void setupAnim(@NonNull T state) {
        super.setupAnim(state);
        this.head.visible = false;
        this.hat.visible = false;
        this.body.visible = false;
        this.rightArm.visible = false;
        this.leftArm.visible = false;
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;
        this.robe.visible = false;
        if (state.headEquipment.is(WizardryItems.ARMOUR.get())) {
            this.head.visible = true;
            this.hat.visible = true;
        }
        if (state.chestEquipment.is(WizardryItems.ARMOUR.get())) {
            this.body.visible = true;
            this.rightArm.visible = true;
            this.leftArm.visible = true;
            this.robe.visible = true;
        }
        if (state.legsEquipment.is(WizardryItems.ARMOUR.get())) {
            this.rightLeg.visible = true;
            this.leftLeg.visible = true;
        }
        if (state.feetEquipment.is(WizardryItems.ARMOUR.get())) {
            this.rightLeg.visible = true;
            this.leftLeg.visible = true;
        }
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
