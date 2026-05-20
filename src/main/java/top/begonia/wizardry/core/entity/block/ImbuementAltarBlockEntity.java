package top.begonia.wizardry.core.entity.block;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import top.begonia.wizardry.client.util.GeometryUtils;
import top.begonia.wizardry.core.block.ReceptacleBlock;
import top.begonia.wizardry.core.constants.ElementEnum;
import top.begonia.wizardry.core.registry.WizardryBlockEntities;
import top.begonia.wizardry.core.registry.WizardrySounds;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.UUID;

public class ImbuementAltarBlockEntity extends BlockEntity {
    private static final int IMBUEMENT_DURATION = 140;

    private ItemStack stack = ItemStack.EMPTY;
    private int imbuementTimer;
    private ElementEnum displayElement;
    @Nullable
    private Player lastUser;
    @Nullable
    private UUID lastUserUUID;

    public ImbuementAltarBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(WizardryBlockEntities.IMBUEMENT_ALTAR.get(), worldPosition, blockState);
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
        checkRecipe();
    }

    public void setLastUser(@Nullable Player player) {
        this.lastUser = player;
    }

    public void checkRecipe() {

        if (getResult().isEmpty()) {
            imbuementTimer = 0;
        } else if (imbuementTimer == 0) {
            imbuementTimer = 1;
        } else {
            return;
        }

        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ItemStack getStack() {
        return stack;
    }

    public void tick(@NonNull Level level, BlockPos pos, @NonNull BlockState state) {
        if (lastUserUUID != null && lastUser == null && level instanceof ServerLevel serverLevel) {
            this.lastUser = serverLevel.getServer().getPlayerList().getPlayer(lastUserUUID);
        }
        if (imbuementTimer > 0) {
            if (imbuementTimer == 1) {
                level.playSound(null, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        WizardrySounds.BLOCK_IMBUEMENT_ALTAR_IMBUE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            ItemStack result = getResult();

            if (result.isEmpty()) {
                imbuementTimer = 0;

            } else {

                if (imbuementTimer++ >= IMBUEMENT_DURATION) {
                    this.stack = result;
                    consumeReceptacleContents();
                    imbuementTimer = 0;
                    displayElement = null;
                    if (lastUser instanceof ServerPlayer serverPlayer) {
//                        WizardryAdvancementTriggers.imbuement_altar.trigger((EntityPlayerMP) lastUser, this.stack);
                    }
                }

                if (level.isClientSide() && level.getRandom().nextInt(2) == 0) {

                    ElementEnum[] elements = getReceptacleElements();

                    Vec3 centre = GeometryUtils.getCentre(this.worldPosition.above());

                    for (int i = 0; i < elements.length; i++) {

                        if (elements[i] == null) continue;

                        Vec3 offset = new Vec3(Direction.from2DDataValue(i).getUnitVec3i());
                        Vec3 vec = GeometryUtils.getCentre(this.worldPosition).add(0, 0.3, 0).add(offset.scale(0.7));

                        int[] colours = ReceptacleBlock.PARTICLE_COLOURS.get(elements[i]);

//                        ParticleBuilder.create(Type.DUST, level.getRandom(), vec.x, vec.y, vec.z, 0.1, false)
//                                .vel(centre.subtract(vec).scale(0.02)).clr(colours[1]).fade(colours[2]).time(50).spawn(world);
                    }
                }
            }
        }
    }

    public ElementEnum getDisplayElement() {
        return displayElement;
    }

    public float getImbuementProgress() {
        return (float) imbuementTimer / IMBUEMENT_DURATION;
    }

    private @NonNull ItemStack getResult() {

        boolean actuallyCrafting = imbuementTimer >= IMBUEMENT_DURATION - 1 && level instanceof ServerLevel;
        ElementEnum[] elements = getReceptacleElements();

        ItemStack result = ItemStack.EMPTY;
//                getImbuementResult(stack, elements, actuallyCrafting, level, lastUser);

        if (result.isEmpty()) {
            displayElement = null;
        } else if (Arrays.stream(elements).distinct().count() == 1) {
            displayElement = elements[0];
        } else {
            displayElement = ElementEnum.MAGIC;
        }

        return result;
    }

    private ElementEnum @NonNull [] getReceptacleElements() {

        ElementEnum[] elements = new ElementEnum[4];

        for (Direction side : Direction.Plane.HORIZONTAL) {

            if (this.level != null) {
                BlockEntity blockEntity = this.level.getBlockEntity(this.worldPosition.relative(side));
                if (blockEntity instanceof ReceptacleBlockEntity receptacleBlockEntity) {
                    elements[side.get2DDataValue()] = receptacleBlockEntity.getElement();
                } else {
                    elements[side.get2DDataValue()] = null;
                }
            }
        }
        return elements;
    }

    private void consumeReceptacleContents() {

        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (this.level != null) {
                BlockEntity blockEntity = this.level.getBlockEntity(this.worldPosition.relative(side));
                if (blockEntity instanceof ReceptacleBlockEntity receptacleBlockEntity) {
                    receptacleBlockEntity.setElement(null);
                }
            }
        }
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        input.read("item", ItemStack.CODEC).ifPresentOrElse(
                parsedStack -> this.stack = parsedStack,
                () -> this.stack = ItemStack.EMPTY
        );
        input.read("imbuementTimer", Codec.INT).ifPresent(timer -> this.imbuementTimer = timer);
        input.read("lastUser", UUIDUtil.CODEC).ifPresent(uuid -> this.lastUserUUID = uuid);
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        if (!this.stack.isEmpty()) {
            output.store("item", ItemStack.CODEC, this.stack);
        }
        output.store("imbuementTimer", Codec.INT, this.imbuementTimer);
        if (this.lastUserUUID != null) {
            output.store("lastUser", UUIDUtil.CODEC, this.lastUserUUID);
        } else if (this.lastUser != null) {
            output.store("lastUser", UUIDUtil.CODEC, this.lastUser.getUUID());
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

//    public static ItemStack getImbuementResult(ItemStack input, ElementEnum[] receptacleElements, boolean fullLootGen, Level level, Player lastUser) {
//
//        ItemStack eventResult = ItemStack.EMPTY;
//
//        if (level != null && MinecraftForge.EVENT_BUS.post(new ImbuementActivateEvent(input, receptacleElements, world, lastUser, eventResult))) {
//            if (eventResult != ItemStack.EMPTY) return eventResult;
//        }
//
//        if (input.getItem() instanceof ItemWizardArmour && ((ItemWizardArmour) input.getItem()).element == null) {
//
//            if (Arrays.stream(receptacleElements).distinct().count() == 1 && receptacleElements[0] != null) { // All the same element
//
//                ItemStack result = new ItemStack(ItemWizardArmour.getArmour(receptacleElements[0], ((ItemWizardArmour) input.getItem()).armourClass, ((ItemWizardArmour) input.getItem()).armorType));
//
//                result.setTagCompound(input.getTagCompound());
//                ((IManaStoringItem) result.getItem()).setMana(result, ((ItemWizardArmour) input.getItem()).getMana(input));
//
//                return result;
//            }
//        }
//
//        if ((input.getItem() == WizardryItems.magic_crystal || input.getItem() == Item.getItemFromBlock(WizardryBlocks.crystal_block))
//                && input.getMetadata() == 0) {
//
//            if (Arrays.stream(receptacleElements).distinct().count() == 1 && receptacleElements[0] != null) {
//                return new ItemStack(input.getItem(), input.getCount(), receptacleElements[0].ordinal());
//            }
//        }
//
//        if (input.getItem() == WizardryItems.RUINED_SPELL_BOOK.get()) {
//
//            if (!ArrayUtils.contains(receptacleElements, null)) {
//
//                if (fullLootGen) {
//                    ElementEnum element = receptacleElements[world.rand.nextInt(receptacleElements.length)];
//                    LootTable table = world.getLootTableManager().getLootTableFromLocation(
//                            WizardryLoot.RUINED_SPELL_BOOK_LOOT_TABLES[element.ordinal() - 1]);
//                    LootContext context = new LootContext.Builder((WorldServer) world).withPlayer(lastUser)
//                            .withLuck(lastUser == null ? 0 : lastUser.getLuck()).build();
//
//                    List<ItemStack> stacks = table.generateLootForPools(world.rand, context);
//                    return stacks.isEmpty() ? ItemStack.EMPTY : stacks.get(0);
//                }
//
//                return new ItemStack(WizardryItems.SPELL_BOOK);
//            }
//        }
//
//        return ItemStack.EMPTY;
//    }
}
