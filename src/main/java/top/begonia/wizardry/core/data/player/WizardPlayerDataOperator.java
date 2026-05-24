package top.begonia.wizardry.core.data.player;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.Wizardry;
import top.begonia.wizardry.core.config.ServerConfig;
import top.begonia.wizardry.core.constants.TierEnum;
import top.begonia.wizardry.core.data.json.definition.spell.part.SpellModifiers;
import top.begonia.wizardry.core.data.packet.CastContinuousSpellPacket;
import top.begonia.wizardry.core.enchantment.Imbuement;
import top.begonia.wizardry.core.event.SpellCastEvent;
import top.begonia.wizardry.core.registry.WizardryAttachment;
import top.begonia.wizardry.core.registry.WizardrySpells;
import top.begonia.wizardry.core.spell.AbstractSpell;
import top.begonia.wizardry.core.spell.impl.None;

import java.util.*;

@EventBusSubscriber(modid = Wizardry.MODID)
public class WizardPlayerDataOperator {
    private final Player player;
    private final WizardPlayerData data;
    private AbstractSpell castCommandSpell;
    private int castCommandTick;
    private SpellModifiers castCommandModifiers;
    private int castCommandDuration;
    public double prevMotionY;
    private static final int IMBUEMENT_UPDATE_INTERVAL = 20;

    private WizardPlayerDataOperator(Player player, WizardPlayerData data) {
        this.player = player;
        this.data = data;
    }

    public static WizardPlayerDataOperator get(Player player) {
        WizardPlayerData currentData = player.getData(WizardryAttachment.WIZARD_PLAYER_DATA.get());
        return new WizardPlayerDataOperator(player, currentData);
    }

    public boolean hasSpellBeenDiscovered(AbstractSpell spell) {
        return this.data.spellsDiscovered().contains(spell) || spell instanceof None;
    }

    public boolean discoverSpell(AbstractSpell spell) {
        if (spell instanceof None) {
            return false;
        }
        return this.data.spellsDiscovered().add(spell);
    }

    public void setTierReached(TierEnum tier) {
        if (!hasReachedTier(tier)) {
            this.data.setMaxTierReached(tier);
        }
    }

    public boolean hasReachedTier(@NonNull TierEnum tier) {
        return tier.level >= this.data.maxTierReached().level;
    }

    public void trackRecentSpell(AbstractSpell spell) {
        this.data.recentSpells().add(new WizardPlayerData.RecentSpellEntry(spell, this.player.level().getGameTime()));
    }

    public int countRecentCasts(AbstractSpell spell) {
        long currentTime = this.player.level().getGameTime();
        return (int) this.data.recentSpells().stream().filter(entry -> entry.spell() == spell && (currentTime - entry.timestamp()) < ServerConfig.recentSpellExpiryTime).count();
    }

    public void setImbuementDuration(Holder<Enchantment> enchantmentHolder, int duration) {
//        if (enchantmentHolder.value() instanceof Imbuement) {
//            String registryKey = enchantmentHolder.unwrapKey().orElseThrow().identifier().toString();
//            this.data.imbuementDurations().put(registryKey, duration);
//        } else {
//            throw new IllegalArgumentException("Attempted to set an imbuement duration for something that isn't an Imbuement!");
//        }
    }

    @SuppressWarnings("unlikely-arg-type")
    public int getImbuementDuration(Enchantment enchantment) {
//        Integer i = this.data.imbuementDurations().get(enchantment);
//        return i == null ? 0 : i;
        return 0;
    }

    private void updateImbuedItems() {
//        Set<Imbuement> activeImbuements = new HashSet<Imbuement>();
//
//        for (ItemStack stack : player.getInventory().mainInventory) {
//            updateImbutedItem(stack, activeImbuements);
//        }
//        for (ItemStack stack : player.getInventory().armorInventory) {
//            updateImbutedItem(stack, activeImbuements);
//        }
//        for (ItemStack stack : player.getInventory().offHandInventory) {
//            updateImbutedItem(stack, activeImbuements);
//        }
//
//        this.data.imbuementDurations().keySet().retainAll(activeImbuements);
    }

    private void updateImbutedItem(ItemStack stack, Set<Imbuement> activeImbuements) {
//        if (stack.isItemEnchanted()) {
//
//            NBTTagList enchantmentList = stack.getItem() == Items.ENCHANTED_BOOK ?
//                    ItemEnchantedBook.getEnchantments(stack) : stack.getEnchantmentTagList();
//
//            Iterator<NBTBase> iterator = enchantmentList.iterator();
//            // For each of the item's enchantments
//            while (iterator.hasNext()) {
//                NBTTagCompound enchantmentTag = (NBTTagCompound) iterator.next();
//                Enchantment enchantment = Enchantment.getEnchantmentByID(enchantmentTag.getShort("id"));
//                // Ignores the enchantment unless it is an imbuement
//                if (enchantment instanceof Imbuement) {
//                    int duration = this.getImbuementDuration(enchantment);
//                    // If the imbuement is still active:
//                    if (duration > 0) {
//                        // Decrements the timer
//                        this.imbuementDurations.put((Imbuement) enchantment, duration - IMBUEMENT_UPDATE_INTERVAL);
//                        // Adds this imbuement to the set of imbuements that need to be kept
//                        activeImbuements.add((Imbuement) enchantment);
//                    } else {
//                        // Otherwise, removes the enchantment from the item
//                        ((Imbuement) enchantment).onImbuementRemoval(stack);
//                        iterator.remove(); // FIXME: Apparently this can cause a CME
//                    }
//                }
//            }
//        }
    }

    public boolean toggleAlly(Player player) {
        if (this.isPlayerAlly(player)) {
            this.data.allies().remove(player.getUUID());
            this.data.allyNames().remove(player.getScoreboardName());
            return false;
        } else {
            this.data.allies().add(player.getUUID());
            this.data.allyNames().add(player.getScoreboardName());
            return true;
        }
    }

    public boolean isPlayerAlly(@NonNull Player player) {
        return this.data.allies().contains(player.getUUID()) || this.player.isPassengerOfSameVehicle(player);
    }

    public boolean isPlayerAlly(UUID playerUUID) {
        if (this.data.allies().contains(playerUUID)) {
            return true;
        }
        Scoreboard scoreboard = this.player.level().getScoreboard();
        PlayerTeam currentTeam = scoreboard.getPlayersTeam(this.player.getScoreboardName());
        if (currentTeam == null) {
            return false;
        }
        return currentTeam.getPlayers().stream().anyMatch(this.data.allyNames()::contains);
    }

    public void startCastingContinuousSpell(AbstractSpell spell, SpellModifiers modifiers, int duration) {

        this.castCommandSpell = spell;
        this.castCommandModifiers = modifiers;
        this.castCommandDuration = duration;

        if (!this.player.level().isClientSide()) {
            CastContinuousSpellPacket packet = new CastContinuousSpellPacket(this.player.getId(), spell, modifiers, duration);
            PacketDistributor.sendToPlayersInDimension((ServerLevel) this.player.level(), packet);
        }
    }

    public void stopCastingContinuousSpell() {
        this.castCommandSpell = WizardrySpells.NONE.get();
        this.castCommandTick = 0;
        this.castCommandModifiers.reset();

        if (!this.player.level().isClientSide()) {
            CastContinuousSpellPacket packet = new CastContinuousSpellPacket(this.player.getId(), WizardrySpells.NONE.get(), this.castCommandModifiers, this.castCommandDuration);
            PacketDistributor.sendToPlayersInDimension((ServerLevel) this.player.level(), packet);
        }
    }

    public void updateContinuousSpellCasting() {

        if (this.castCommandSpell != null && this.castCommandSpell.isContinuous) {

            if (castCommandTick >= castCommandDuration) {
                this.stopCastingContinuousSpell();
                return;
            }

            if (NeoForge.EVENT_BUS.post(new SpellCastEvent.Tick(SpellCastEvent.Source.COMMAND, castCommandSpell, player, castCommandModifiers, castCommandTick)).isCanceled()) {
                this.stopCastingContinuousSpell();
                return;
            }

            if (this.castCommandSpell.cast(player.level(), player, InteractionHand.MAIN_HAND, castCommandTick, this.castCommandModifiers) && this.castCommandTick == 0) {
                NeoForge.EVENT_BUS.post(new SpellCastEvent.Post(SpellCastEvent.Source.COMMAND, castCommandSpell, player, castCommandModifiers));
            }

            castCommandTick++;

        } else {
            this.castCommandTick = 0;
        }
    }

    public boolean isCasting() {
        return this.castCommandSpell != null && this.castCommandSpell != WizardrySpells.NONE.get();
    }

    public AbstractSpell currentlyCasting() {
        return this.castCommandSpell;
    }

    private void update() {
        //TODO
//        if (this.data.getSelectedMinion() != null && this.data.getSelectedMinion().get() == null) {
//            this.data.setSelectedMinion(null);
//        }
        this.prevMotionY = this.player.getDeltaMovement().y;
        if (this.player.tickCount % IMBUEMENT_UPDATE_INTERVAL == 0) {
            this.updateImbuedItems();
        }
        this.updateContinuousSpellCasting();
        if (this.player.tickCount % 60 == 0) {
            long currentTime = this.player.level().getGameTime();
            this.data.recentSpells().removeIf(entry -> (currentTime - entry.timestamp()) >= ServerConfig.recentSpellExpiryTime);
        }
    }

    public WizardPlayerData getDataInstance() {
        return this.data;
    }

    public void copyFrom(WizardPlayerDataOperator oldData, boolean respawn) {
        WizardPlayerData source = oldData.data;
        WizardPlayerData target = this.data;
        target.allies().clear();
        target.allies().addAll(source.allies());
        target.allyNames().clear();
        target.allyNames().addAll(source.allyNames());
        target.spellsDiscovered().clear();
        target.spellsDiscovered().addAll(source.spellsDiscovered());
        target.recentSpells().clear();
        target.recentSpells().addAll(source.recentSpells());
        target.setMaxTierReached(source.maxTierReached());
        target.imbuementDurations().clear();
        source.imbuementDurations().forEach((key, duration) -> {
            if (!respawn) {
                target.imbuementDurations().put(key, duration);
            }
        });
        this.player.setData(WizardryAttachment.WIZARD_PLAYER_DATA.get(), target);
    }

    @SubscribeEvent
    public static void onPlayerCloneEvent(PlayerEvent.@NonNull Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        WizardPlayerDataOperator oldWizardData = WizardPlayerDataOperator.get(oldPlayer);
        WizardPlayerDataOperator newWizardData = WizardPlayerDataOperator.get(newPlayer);
        newWizardData.copyFrom(oldWizardData, event.isWasDeath());
    }
}
