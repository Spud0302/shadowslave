package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.TrueNamePayload;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The Spell grants a True Name for a deed done outside a Nightmare (ch. 364):
 * over its web of stars it types out the deed, then the name. A name granted
 * by an appraisal is shown on the appraisal screen instead.
 */
public final class TrueNameRevealScreen extends NightmareAppraisalScreen {
    public TrueNameRevealScreen(TrueNamePayload payload) {
        super(Component.translatable("screen.shadowslave.true_name"), script(payload), payload.name().hashCode());
    }

    static List<Line> script(TrueNamePayload payload) {
        List<Line> lines = new ArrayList<>();
        lines.add(new Line(Kind.VOICE, payload.spellLine().isEmpty() ? "The Spell has been watching you." : payload.spellLine(),
                SpellStyle.VOICE, 500));
        lines.add(new Line(Kind.GAP, "", 0, 0));
        if (!payload.deedLine().isEmpty()) {
            lines.add(new Line(Kind.DETAIL, "[" + payload.deedLine() + "]", SpellStyle.TEXT, 400));
            lines.add(new Line(Kind.GAP, "", 0, 0));
        }
        lines.add(new Line(Kind.LABEL, "You have received a True Name.", SpellStyle.TEXT, 300));
        lines.add(new Line(Kind.LABEL, "Your True Name is:", SpellStyle.TEXT, 300));
        lines.add(new Line(Kind.GRADE, payload.name(), GRADE_COLOR, 500));
        lines.add(new Line(Kind.GAP, "", 0, 0));
        lines.add(new Line(Kind.DETAIL, "It is hidden from others. Those who learn it gain an edge over you; "
                + "keep it, or show it on your Soul's Name page.", SpellStyle.HINT, 0));
        return lines;
    }
}
