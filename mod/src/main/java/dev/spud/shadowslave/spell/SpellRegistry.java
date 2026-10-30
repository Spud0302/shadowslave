package dev.spud.shadowslave.spell;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.spell.calling.CallingData;
import dev.spud.shadowslave.spell.truename.TrueNameData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** What each player carries for the Spell: their Callings and True Name. The world's record is saved data, not an attachment. */
public final class SpellRegistry {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ShadowSlaveMod.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CallingData>> CALLINGS = ATTACHMENTS.register("spell_callings",
            () -> AttachmentType.builder(CallingData::empty).serialize(CallingData.CODEC).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TrueNameData>> TRUE_NAME = ATTACHMENTS.register("true_name",
            () -> AttachmentType.builder(TrueNameData::empty).serialize(TrueNameData.CODEC).copyOnDeath().build());

    private SpellRegistry() {
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENTS.register(modEventBus);
    }
}
