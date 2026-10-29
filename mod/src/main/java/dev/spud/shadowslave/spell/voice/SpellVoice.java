package dev.spud.shadowslave.spell.voice;

import java.util.Optional;

/**
 * Something that can phrase one of the Spell's lines (DESIGN, Jack 2026-10-29:
 * "the AI only phrases; the game decides"). The written line tables are the
 * voice that always answers; a later module can {@link SpellVoices#register
 * register} another one in front of it, such as an AI model behind a server
 * option, and fall back to the tables whenever it is off, slow or offline.
 *
 * <p>Calls come on the server thread and must not block: a slow voice should
 * answer from its own cache and return empty while it works, so the written
 * line is used this time.</p>
 */
public interface SpellVoice {
    /** One or two short lines for the request, or empty to let the next voice answer. */
    Optional<String> speak(VoiceRequest request);

    /**
     * Whether the voice's lines are trusted as written. Untrusted voices (an AI)
     * have their lines refused if they state numbers, and are cut to length.
     */
    default boolean trusted() {
        return false;
    }

    /** A short name for logs and the voice command. */
    String name();
}
