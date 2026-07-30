package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.SoulSnapshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** First client presentation of server-authoritative Soul state. */
public final class SoulScreen extends Screen {
    private static final int PANEL_BACKGROUND = 0xE6120F1C;
    private static final int PANEL_BORDER = 0xFF6E4A8E;
    private static final int TITLE_COLOR = 0xFFD9B3FF;
    private static final int LABEL_COLOR = 0xFF8E8798;
    private static final int VALUE_COLOR = 0xFFF2EDF7;
    private static final int ASPECT_COLOR = 0xFF79DDF2;
    private static final int FLAW_COLOR = 0xFFF08080;

    private final SoulSnapshot snapshot;

    public SoulScreen(SoulSnapshot snapshot) {
        super(Component.translatable("screen.shadowslave.soul"));
        this.snapshot = snapshot;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int panelWidth = Math.min(360, Math.max(220, this.width - 32));
        int panelHeight = 190;
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, PANEL_BORDER);
        graphics.fill(left, top, right, bottom, PANEL_BACKGROUND);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 14, TITLE_COLOR);

        int labelX = left + 22;
        int valueX = left + 118;
        int lineY = top + 46;

        drawRow(graphics, "Spell state", humanize(snapshot.spellState()), labelX, valueX, lineY, VALUE_COLOR);
        drawRow(graphics, "Soul Rank", humanize(snapshot.soulRank()), labelX, valueX, lineY + 22, VALUE_COLOR);
        drawRow(graphics, "Aspect", snapshot.displayedAspect(), labelX, valueX, lineY + 44, ASPECT_COLOR);
        drawRow(graphics, "Flaw", snapshot.displayedFlaw(), labelX, valueX, lineY + 66, FLAW_COLOR);
        drawRow(
                graphics,
                "Origin",
                snapshot.importedFromDatapack() ? "Imported from datapack" : "Native Java soul",
                labelX,
                valueX,
                lineY + 88,
                VALUE_COLOR
        );

        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.shadowslave.soul.close_hint"),
                this.width / 2,
                bottom - 22,
                LABEL_COLOR
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawRow(
            GuiGraphics graphics,
            String label,
            String value,
            int labelX,
            int valueX,
            int y,
            int valueColor
    ) {
        graphics.drawString(this.font, Component.literal(label + ":"), labelX, y, LABEL_COLOR, false);
        graphics.drawString(this.font, Component.literal(value), valueX, y, valueColor, false);
    }

    private static String humanize(String value) {
        if (value.isBlank()) {
            return "—";
        }

        String spaced = value.replace('_', ' ');
        return spaced.substring(0, 1).toUpperCase(Locale.ROOT) + spaced.substring(1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
