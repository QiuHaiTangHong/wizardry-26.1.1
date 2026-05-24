package top.begonia.wizardry.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.client.data.definition.particle.ParticleParserContextData;
import top.begonia.wizardry.client.data.manager.WizardryClientDataManager;
import top.begonia.wizardry.client.data.definition.handbook.HandbookData;
import top.begonia.wizardry.client.data.parser.ParticleParser;
import top.begonia.wizardry.client.gui.BookshelfScreen;
import top.begonia.wizardry.client.model.loader.SpecialModelLoader;
import top.begonia.wizardry.client.network.ClientPayloadHandler;
import top.begonia.wizardry.client.particle.impl.*;
import top.begonia.wizardry.client.render.*;
import top.begonia.wizardry.client.render.unbaked.GlowUnbakedItemModel;
import top.begonia.wizardry.client.gui.ArcaneWorkbenchScreen;
import top.begonia.wizardry.client.model.RobeArmourModel;
import top.begonia.wizardry.client.model.SageArmourModel;
import top.begonia.wizardry.client.model.WizardArmourModel;
import top.begonia.wizardry.core.api.data.event.DataParserBefore;
import top.begonia.wizardry.core.api.data.event.RegisterParticleEvent;
import top.begonia.wizardry.core.registry.*;
import top.begonia.wizardry.core.util.ArmourHelper;
import top.begonia.wizardry.core.data.network.handbook.HandbookRecipesRequest;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = Wizardry.MODID)
public class ClientEvents {
    private static final Map<ModelLayerLocation, HumanoidModel<?>> modelLayerLocationHumanoidModelMap = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.@NonNull RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ArmourHelper.ModelLayer.WIZARD_OUTER,
                () -> WizardArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 64));
        event.registerLayerDefinition(ArmourHelper.ModelLayer.WIZARD_INNER,
                () -> WizardArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 32));
        event.registerLayerDefinition(ArmourHelper.ModelLayer.SAGE_OUTER,
                () -> SageArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 64));
        event.registerLayerDefinition(ArmourHelper.ModelLayer.SAGE_INNER,
                () -> SageArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 32));
        event.registerLayerDefinition(ArmourHelper.ModelLayer.ROBE_OUTER,
                () -> RobeArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 64));
        event.registerLayerDefinition(ArmourHelper.ModelLayer.ROBE_INNER,
                () -> RobeArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 32));
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.@NonNull AddLayers event) {
        EntityModelSet modelSet = event.getEntityModels();
        modelLayerLocationHumanoidModelMap.put(ArmourHelper.ModelLayer.WIZARD_OUTER, new WizardArmourModel<>(modelSet.bakeLayer(ArmourHelper.ModelLayer.WIZARD_OUTER)));
        modelLayerLocationHumanoidModelMap.put(ArmourHelper.ModelLayer.WIZARD_INNER, new WizardArmourModel<>(modelSet.bakeLayer(ArmourHelper.ModelLayer.WIZARD_INNER)));
        modelLayerLocationHumanoidModelMap.put(ArmourHelper.ModelLayer.SAGE_OUTER, new SageArmourModel<>(modelSet.bakeLayer(ArmourHelper.ModelLayer.SAGE_OUTER)));
        modelLayerLocationHumanoidModelMap.put(ArmourHelper.ModelLayer.SAGE_INNER, new SageArmourModel<>(modelSet.bakeLayer(ArmourHelper.ModelLayer.SAGE_INNER)));
        modelLayerLocationHumanoidModelMap.put(ArmourHelper.ModelLayer.ROBE_OUTER, new RobeArmourModel<>(modelSet.bakeLayer(ArmourHelper.ModelLayer.ROBE_OUTER)));
        modelLayerLocationHumanoidModelMap.put(ArmourHelper.ModelLayer.ROBE_INNER, new RobeArmourModel<>(modelSet.bakeLayer(ArmourHelper.ModelLayer.ROBE_INNER)));
    }

    @SubscribeEvent
    public static void onRegisterItemModels(@NonNull RegisterItemModelsEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(Wizardry.MODID, "special_item"),
                GlowUnbakedItemModel.MAP_CODEC
        );
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.@NonNull RegisterRenderers event) {
        event.registerEntityRenderer(WizardryEntities.FIRE_BOMB.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(WizardryEntities.POISON_BOMB.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(WizardryEntities.SMOKE_BOMB.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(WizardryEntities.SPARK_BOMB.get(), ThrownItemRenderer::new);

    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.@NonNull RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                WizardryBlockEntities.ARCANE_WORKBENCH.get(),
                ArcaneWorkbenchRender::new
        );
        event.registerBlockEntityRenderer(
                WizardryBlockEntities.BOOKSHELF.get(),
                BookshelfRender::new
        );
        event.registerBlockEntityRenderer(
                WizardryBlockEntities.IMBUEMENT_ALTAR.get(),
                ImbuementAltarRender::new
        );
        event.registerBlockEntityRenderer(
                WizardryBlockEntities.LECTERN.get(),
                LecternRender::new
        );
    }

    @SubscribeEvent
    public static void onRegisterModelLoaders(ModelEvent.@NonNull RegisterLoaders event) {
        event.register(SpecialModelLoader.ID, SpecialModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerParticleFactories(@NonNull RegisterParticleEvent event) {
        event.register(WizardryParticles.BEAM.get(), BeamParticle::new);
        event.register(WizardryParticles.BLOCK_HIGHLIGHT.get(), BlockHighlightParticle::new);
        event.register(WizardryParticles.BUFF.get(), BuffParticle::new);
        event.register(WizardryParticles.CLOUD.get(), CloudParticle::new);
        event.register(WizardryParticles.DARK_MAGIC.get(), DarkMagicParticle::new);
        event.register(WizardryParticles.DUST.get(), DustParticle::new);
        event.register(WizardryParticles.FLASH.get(), FlashParticle::new);
        event.register(WizardryParticles.GUARDIAN_BEAM.get(), GuardianBeamParticle::new);
        event.register(WizardryParticles.ICE.get(), IceParticle::new);
        event.register(WizardryParticles.LEAF.get(), LeafParticle::new);
        event.register(WizardryParticles.LIGHTNING.get(), LightningParticle::new);
        event.register(WizardryParticles.LIGHTNING_PULSE.get(), LightningPulseParticle::new);
        event.register(WizardryParticles.MAGIC_BUBBLE.get(), MagicBubbleParticle::new);
        event.register(WizardryParticles.MAGIC_FIRE.get(), MagicFlameParticle::new);
        event.register(WizardryParticles.PATH.get(), PathParticle::new);
        event.register(WizardryParticles.SCORCH.get(), ScorchParticle::new);
        event.register(WizardryParticles.SNOW.get(), SnowParticle::new);
        event.register(WizardryParticles.SPARK.get(), SparkParticle::new);
        event.register(WizardryParticles.SPARKLE.get(), SparkleParticle::new);
        event.register(WizardryParticles.SPHERE.get(), SphereParticle::new);
        event.register(WizardryParticles.VINE.get(), VineParticle::new);
    }

    @SubscribeEvent
    public static void onDataParserBefore(@NonNull DataParserBefore event) {
        event.registry(ParticleParser.PARSER_NAME, (_) -> {
            ParticleParserContextData parserContext = new ParticleParserContextData();
            ModLoader.postEvent(new RegisterParticleEvent(parserContext.getParticleHolders()));
            return parserContext;
        });
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(@NonNull RegisterClientExtensionsEvent event) {
        event.registerItem(new WizardryArmorRenderer(modelLayerLocationHumanoidModelMap), WizardryItems.ARMOUR.get());
        for (DeferredHolder<MobEffect, ? extends MobEffect> effect : WizardryMobEffects.EFFECTS.getEntries()) {
            event.registerMobEffect(WizardryPotionRender.INSTANCE, effect.get());
        }
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(@NonNull AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Wizardry.MODID, "data_manager"), WizardryClientDataManager.getInstance());
    }

    @SubscribeEvent
    public static void registerScreens(@NonNull RegisterMenuScreensEvent event) {
        event.register(
                WizardryMenus.ARCANE_WORKBENCH.get(),
                ArcaneWorkbenchScreen::new
        );
        event.register(
                WizardryMenus.BOOKSHELF.get(),
                BookshelfScreen::new
        );
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientPayloadHandler.clearCache();
        HandbookData handbookData = WizardryClientDataManager.getInstance().getData(Identifier.fromNamespaceAndPath(Wizardry.MODID, "handbook"), HandbookData.class).orElse(null);
        if (handbookData != null && Minecraft.getInstance().getConnection() != null) {
            Wizardry.LOGGER.info("正在向服务端发送手册配方同步请求...");
            ClientPacketDistributor.sendToServer(new HandbookRecipesRequest(handbookData.recipes()));
        }
    }
}
