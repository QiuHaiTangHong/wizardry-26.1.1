package top.begonia.wizardry.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.client.data.WizardryClientDataManager;
import top.begonia.wizardry.client.data.definition.handbook.HandbookData;
import top.begonia.wizardry.client.gui.BookshelfScreen;
import top.begonia.wizardry.client.network.ClientPayloadHandler;
import top.begonia.wizardry.client.particle.impl.*;
import top.begonia.wizardry.client.render.*;
import top.begonia.wizardry.client.util.GenericParticleProvider;
import top.begonia.wizardry.client.gui.ArcaneWorkbenchScreen;
import top.begonia.wizardry.client.model.RobeArmourModel;
import top.begonia.wizardry.client.model.SageArmourModel;
import top.begonia.wizardry.client.model.WizardArmourModel;
import top.begonia.wizardry.core.registry.*;
import top.begonia.wizardry.core.util.ArmourMaterialHelper;
import top.begonia.wizardry.core.data.network.handbook.HandbookRecipesRequest;

@EventBusSubscriber(modid = Wizardry.MODID)
public class ClientEvents {
    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.@NonNull RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ArmourMaterialHelper.ModelLayer.WIZARD_OUTER,
                () -> WizardArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 64));
        event.registerLayerDefinition(ArmourMaterialHelper.ModelLayer.WIZARD_INNER,
                () -> WizardArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 32));
        event.registerLayerDefinition(ArmourMaterialHelper.ModelLayer.SAGE_OUTER,
                () -> SageArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 64));
        event.registerLayerDefinition(ArmourMaterialHelper.ModelLayer.SAGE_INNER,
                () -> SageArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 32));
        event.registerLayerDefinition(ArmourMaterialHelper.ModelLayer.ROBE_OUTER,
                () -> RobeArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 64));
        event.registerLayerDefinition(ArmourMaterialHelper.ModelLayer.ROBE_INNER,
                () -> RobeArmourModel.createLayerDefinition(new CubeDeformation(0.75F), 64, 32));
    }

    @SubscribeEvent
    public static void registerParticleFactories(@NonNull RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(WizardryParticles.DUST.get(), spriteSet -> new GenericParticleProvider(spriteSet, DustParticle::new));
        event.registerSpriteSet(WizardryParticles.FLASH.get(), spriteSet -> new GenericParticleProvider(spriteSet, FlashParticle::new));
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        for (DeferredItem<? extends Item> item : WizardryItems.WIZARD_ARMOUR_ITEMS) {
            event.registerItem(new WizardryArmorRenderer(), item.get());
        }
        for (DeferredHolder<MobEffect, ? extends MobEffect> effect : WizardryMobEffects.EFFECTS.getEntries()) {
            event.registerMobEffect(WizardryPotionRender.INSTANCE, effect.get());
        }
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(@NonNull AddClientReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(Wizardry.MODID, "data_manager"), WizardryClientDataManager.INSTANCE);
    }

    @SubscribeEvent
    public static void registerScreens(@NonNull RegisterMenuScreensEvent event) {
        event.register(WizardryMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
        event.register(WizardryMenus.BOOKSHELF.get(), BookshelfScreen::new);
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
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        ClientPayloadHandler.clearCache();
        HandbookData handbookData = WizardryClientDataManager.INSTANCE.getData(Identifier.fromNamespaceAndPath(Wizardry.MODID, "handbook"), HandbookData.class).orElse(null);
        if (handbookData != null && Minecraft.getInstance().getConnection() != null) {
            Wizardry.LOGGER.info("正在向服务端发送手册配方同步请求...");
            ClientPacketDistributor.sendToServer(new HandbookRecipesRequest(handbookData.recipes()));
        }
    }
}
