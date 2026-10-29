package dev.spud.shadowslave.spell.voice;

import java.util.Optional;
import java.util.function.Supplier;

/** The voice that always answers: the written line tables from data. */
public final class WrittenVoice implements SpellVoice {
    private final Supplier<VoiceLines> lines;

    public WrittenVoice(Supplier<VoiceLines> lines) {
        this.lines = lines;
    }

    @Override
    public Optional<String> speak(VoiceRequest request) {
        return lines.get().pick(request);
    }

    @Override
    public boolean trusted() {
        return true;
    }

    @Override
    public String name() {
        return "written";
    }
}
