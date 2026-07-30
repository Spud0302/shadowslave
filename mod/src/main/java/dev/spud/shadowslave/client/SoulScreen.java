package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.SoulSnapshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Read-only presentation of server-authoritative Soul and revealed identity state. */
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

        int panelWidth = Math.min(500, Math.max(300, this.width - 32));
        int panelHeight = Math.min(330, Math.max(286, this.height - 28));
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        graphics.fill(left - 1, top - 1, right + 1, bottom + 1, PANEL_BORDER);
        graphics.fill(left, top, right, bottom, PANEL_BACKGROUND);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 12, TITLE_COLOR);

        int labelX = left + 20;
        int valueX = left + 132;
        int lineY = top + 38;
        int step = 20;

        drawRow(graphics, "Status", humanize(snapshot.spellState()), labelX, valueX, lineY, VALUE_COLOR);
        drawRow(graphics, "Path", humanize(snapshot.awakeningPath()), labelX, valueX, lineY + step, VALUE_COLOR);
        drawRow(graphics, "Soul Rank", humanize(snapshot.displayedSoulRank()), labelX, valueX, lineY + step * 2, VALUE_COLOR);
        drawRow(graphics, "Aspect", snapshot.displayedAspect(), labelX, valueX, lineY + step * 3, ASPECT_COLOR);
        drawRow(graphics, "Aspect Rank", humanize(snapshot.displayedAspectRank()), labelX, valueX, lineY + step * 4, ASPECT_COLOR);
        drawRow(graphics, "Ability", compactId(snapshot.displayedAbility()), labelX, valueX, lineY + step * 5, ASPECT_COLOR);
        drawRow(graphics, "Flaw", snapshot.displayedFlaw(), labelX, valueX, lineY + step * 6, FLAW_COLOR);
        drawRow(graphics, "Flaw Effect", compactId(snapshot.displayedFlawEffect()), labelX, valueX, lineY + step * 7, FLAW_COLOR);
        drawRow(
                graphics,
                "Origin",
                snapshot.importedFromDatapack() ? "Imported datapack identity" : "Native Java identity",
                labelX,
                valueX,
                lineY + step * 8,
                VALUE_COLOR
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal("Preview controls: /shadowslave preview_begin · /shadowslave kindle"),
                this.width / 2,
                bottom - 38,
                LABEL_COLOR
        );
        graphics.drawCenteredString(
                this.font,
                Component.translatable("screen.shadowslave.soul.close_hint"),
                this.width / 2,
                bottom - 20,
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
        if (value.isBlank() || "—".equals(value)) {
            return "—";
        }
        String spaced = value.replace('_', ' ');
        return spaced.substring(0, 1).toUpperCase(Locale.ROOT) + spaced.substring(1);
    }

    private static String compactId(String value) {
        if (value.isBlank() || "—".equals(value)) {
            return "—";
        }
        int slash = value.lastIndexOf('/');
        int colon = value.indexOf(':');
        int start = Math.max(slash, colon);
        return humanize(start >= 0 ? value.substring(start + 1) : value);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
