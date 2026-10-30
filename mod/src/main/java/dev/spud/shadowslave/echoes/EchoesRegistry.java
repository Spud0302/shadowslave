package dev.spud.shadowslave.echoes;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Echoes for every player (Run F): the echo remnant a slain creature can
 * leave, the bound Echo it becomes at a Weaving Table, the Echoes a soul
 * holds, and the bond that marks a summoned one in the world. The older
 * {@code echo} package is the pre-rework preview Echo and is left as it was.
 */
public final class EchoesRegistry {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ShadowSlaveMod.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ShadowSlaveMod.MOD_ID);

    /** Creatures that leave no remnant, whatever their rank. */
    public static final TagKey<EntityType<?>> NO_REMNANT = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "echo/no_remnant"));

    /** The creature an echo remnant was left by. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EchoBody>> REMNANT_OF =
            COMPONENTS.registerComponentType("remnant_of", builder -> builder
                    .persistent(EchoBody.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(EchoBody.CODEC)));

    /** The Echo a bound Echo item carries while it is out of a soul. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EchoRecord>> ECHO =
            COMPONENTS.registerComponentType("echo", builder -> builder
                    .persistent(EchoRecord.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(EchoRecord.CODEC)));

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SoulEchoes>> SOUL_ECHOES = ATTACHMENTS.register("soul_echoes",
            () -> AttachmentType.builder(() -> SoulEchoes.EMPTY).serialize(SoulEchoes.CODEC).copyOnDeath().build());

    /** Set on a summoned Echo's body. Saved, so a body left in the world after a crash can be found and sent home. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<java.util.Optional<EchoBond>>> ECHO_BOND = ATTACHMENTS.register("echo_bond",
            () -> AttachmentType.<java.util.Optional<EchoBond>>builder(java.util.Optional::empty)
                    .serialize(EchoBond.CODEC.xmap(java.util.Optional::of, bond -> bond.orElseThrow()), java.util.Optional::isPresent).build());

    public static final DeferredItem<EchoRemnantItem> ECHO_REMNANT = ITEMS.registerItem("echo_remnant",
            EchoRemnantItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    public static final DeferredItem<BoundEchoItem> BOUND_ECHO = ITEMS.registerItem("bound_echo",
            BoundEchoItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    private EchoesRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        COMPONENTS.register(modEventBus);
        ATTACHMENTS.register(modEventBus);
        // Remnants and Echoes only exist with a creature in them, so they stay out of the creative tabs.
    }
}
