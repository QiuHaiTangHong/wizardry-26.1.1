package top.begonia.wizardry.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class AbstractWizardArmourModel<T extends HumanoidRenderState> extends HumanoidModel<T> {
    public AbstractWizardArmourModel(ModelPart root) {
        super(root);
    }

    @Contract("_, _, _, _ -> new")
    protected static @NonNull ArmorModelSetExtension<MeshDefinition> createArmorMeshSetExtension(
            @NonNull Function<CubeDeformation, MeshDefinition> baseFactory,
            @NonNull Map<EquipmentSlot, Set<String>> partsPerSlot,
            @NonNull CubeDeformation innerDeformation,
            @NonNull CubeDeformation outerDeformation
    ) {
        MeshDefinition head = baseFactory.apply(outerDeformation);
        head.getRoot().retainPartsAndChildren(partsPerSlot.get(EquipmentSlot.HEAD));
        MeshDefinition chest = baseFactory.apply(outerDeformation);
        chest.getRoot().retainExactParts(partsPerSlot.get(EquipmentSlot.CHEST));
        MeshDefinition legs = baseFactory.apply(innerDeformation);
        legs.getRoot().retainExactParts(partsPerSlot.get(EquipmentSlot.LEGS));
        MeshDefinition feet = baseFactory.apply(outerDeformation);
        feet.getRoot().retainExactParts(partsPerSlot.get(EquipmentSlot.FEET));
        return new ArmorModelSetExtension<>(head, chest, legs, feet);
    }

    public static @NonNull ArmorModelSetExtension<MeshDefinition> createArmorMeshSetExtension(
            @NonNull Function<CubeDeformation, MeshDefinition> baseFactory,
            @NonNull CubeDeformation innerDeformation,
            @NonNull CubeDeformation outerDeformation
    ) {
        return createArmorMeshSetExtension(baseFactory, ADULT_ARMOR_PARTS_PER_SLOT, innerDeformation, outerDeformation);
    }

    public static @NonNull ArmorModelSetExtension<MeshDefinition> createArmorMeshSetExtension(
            @NonNull CubeDeformation innerDeformation,
            @NonNull CubeDeformation outerDeformation
    ) {
        return createArmorMeshSetExtension(AbstractWizardArmourModel::createBaseArmorMesh, innerDeformation, outerDeformation);
    }

    protected static @NonNull MeshDefinition createBaseArmorMesh(CubeDeformation cubeDeformation) {
        MeshDefinition mesh = createMesh(cubeDeformation, 0.0F);
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation.extend(-0.1F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, cubeDeformation.extend(-0.1F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        return mesh;
    }

    @Override
    public void setupAnim(@NonNull T state) {
        super.setupAnim(state);
        if (state instanceof AvatarRenderState avatarRenderState) {
            boolean showBody = !avatarRenderState.isSpectator;
            this.body.visible = showBody;
            this.rightArm.visible = showBody;
            this.leftArm.visible = showBody;
            this.rightLeg.visible = showBody;
            this.leftLeg.visible = showBody;
            this.hat.visible = avatarRenderState.showHat;
        }
    }
}
