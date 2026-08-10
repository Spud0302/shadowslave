package dev.spud.shadowslave.attachment;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.memory.MemoryOwnershipData;
import dev.spud.shadowslave.migration.ImportedIdentityData;
import dev.spud.shadowslave.preview.PreviewPowerData;
import dev.spud.shadowslave.soul.SoulData;
import dev.spud.shadowslave.soul.identity.AttributeOwnershipData;
import dev.spud.shadowslave.soul.identity.SoulIdentityData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Registers persistent, server-authoritative attachments owned by the mod. */
public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ShadowSlaveMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SoulData>> SOUL =
            ATTACHMENTS.register("soul", () -> AttachmentType.builder(SoulData::uninfected)
                    .serialize(SoulData.CODEC.codec()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SoulIdentityData>> IDENTITY =
            ATTACHMENTS.register("identity", () -> AttachmentType.builder(SoulIdentityData::empty)
                    .serialize(SoulIdentityData.CODEC.codec()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AttributeOwnershipData>> ATTRIBUTES =
            ATTACHMENTS.register("attributes", () -> AttachmentType.builder(AttributeOwnershipData::empty)
                    .serialize(AttributeOwnershipData.CODEC.codec()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MemoryOwnershipData>> MEMORIES =
            ATTACHMENTS.register("memories", () -> AttachmentType.builder(MemoryOwnershipData::empty)
                    .serialize(MemoryOwnershipData.CODEC.codec()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ImportedIdentityData>> IMPORTED_IDENTITY =
            ATTACHMENTS.register("imported_identity", () -> AttachmentType.builder(ImportedIdentityData::empty)
                    .serialize(ImportedIdentityData.CODEC.codec()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PreviewPowerData>> PREVIEW_POWER =
            ATTACHMENTS.register("preview_power", () -> AttachmentType.builder(PreviewPowerData::empty)
                    .serialize(PreviewPowerData.CODEC.codec()).copyOnDeath().build());

    private ModAttachments() {}

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
