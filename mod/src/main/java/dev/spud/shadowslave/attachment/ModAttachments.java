package dev.spud.shadowslave.attachment;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.soul.SoulData;
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
            ATTACHMENTS.register("soul", () -> AttachmentType.builder(SoulData::mundane)
                    .serialize(SoulData.CODEC.codec())
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
