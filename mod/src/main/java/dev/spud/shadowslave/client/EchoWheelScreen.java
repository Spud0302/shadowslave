package dev.spud.shadowslave.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.spud.shadowslave.network.payload.EchoActionPayload;
import dev.spud.shadowslave.network.payload.EchoStatusPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;

import java.util.Locale;

/**
 * The Echo command wheel (DESIGN): hold the Echo command key, point at an
 * order and let go (or click it). Follow is at the top, Guard on the right,
 * Stay at the bottom and Attack on the left; the centre calls the Echoes out
 * or sends them home. The server carries the order out.
 */
public final class EchoWheelScreen extends Screen {
    static final String[] ORDERS = {"follow", "guard", "stay", "attack"};
    private static final String[] LABELS = {"Follow", "Guard", "Stay", "Attack"};
    private static final String[] HINTS = {"Keep close; fight back", "Fight what hunts you", "Hold this spot", "What you look at"};
    private static final int INNER = 34;
    private static final int OUTER = 92;
    private static final int STEPS = 24;

    private int hovered = -2;

    public EchoWheelScreen() {
        super(Component.translatable("screen.shadowslave.echo_wheel"));
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, this.width, this.height, 0x60040208, 0x900E0618);
    }

    /** Which order the pointer is over: 0 to 3, -1 for the centre, -2 for nothing. */
    static int sectorAt(double dx, double dy) {
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < INNER) {
            return -1;
        }
        if (distance > OUTER + 24) {
            return -2;
        }
        // Up is 0 and the angle turns clockwise on screen: up, right, down, left.
        double angle = Math.toDegrees(Math.atan2(dx, -dy));
        return (int) Math.floorMod(Math.round(angle / 90.0), 4);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int cx = this.width / 2;
        int cy = this.height / 2;
        hovered = sectorAt(mouseX - cx, mouseY - cy);
        EchoStatusPayload status = ClientEchoState.status();
        long time = this.minecraft.level == null ? 0 : this.minecraft.level.getGameTime();
        float pulse = 0.5F + 0.5F * (float) Math.sin((time + partialTick) * 0.15);

        for (int i = 0; i < 4; i++) {
            boolean over = hovered == i;
            boolean standing = ORDERS[i].equals(status.order());
            int colour = i == 3 ? SpellStyle.FLAW : SpellStyle.ASPECT;
            float start = -45.0F + i * 90.0F + 2.0F;
            float end = start + 86.0F;
            arc(graphics, cx, cy, INNER + 2, OUTER, start, end,
                    SpellStyle.withAlpha(colour, over ? 0.42F + 0.12F * pulse : standing ? 0.24F : 0.12F));
            arc(graphics, cx, cy, OUTER - 1, OUTER + 1, start, end, SpellStyle.withAlpha(colour, over ? 0.95F : 0.55F));
            double mid = Math.toRadians(start + 43.0F);
            int lx = cx + (int) Math.round(Math.sin(mid) * (INNER + OUTER) / 2.0);
            int ly = cy - (int) Math.round(Math.cos(mid) * (INNER + OUTER) / 2.0);
            graphics.drawCenteredString(this.font, LABELS[i], lx, ly - 8, over ? SpellStyle.TEXT : colour);
            graphics.drawCenteredString(this.font, standing ? "◆ standing order" : HINTS[i], lx, ly + 4,
                    over ? SpellStyle.VOICE : SpellStyle.HINT);
        }
        boolean centre = hovered == -1;
        arc(graphics, cx, cy, 0, INNER - 2, 0.0F, 360.0F, SpellStyle.withAlpha(SpellStyle.ABILITY, centre ? 0.38F + 0.1F * pulse : 0.14F));
        arc(graphics, cx, cy, INNER - 3, INNER - 1, 0.0F, 360.0F, SpellStyle.withAlpha(SpellStyle.ABILITY, centre ? 0.95F : 0.55F));
        boolean out = status.summoned() > 0;
        graphics.drawCenteredString(this.font, out ? "Dismiss" : "Call", cx, cy - 9, centre ? SpellStyle.TEXT : SpellStyle.ABILITY);
        graphics.drawCenteredString(this.font, out ? status.summoned() + " out" : status.held().size() + " held", cx, cy + 3, SpellStyle.MUTED);

        graphics.drawCenteredString(this.font, this.title, cx, cy - OUTER - 26, SpellStyle.TITLE);
        String upkeep = out ? String.format(Locale.ROOT, "Upkeep %.1f essence a second", status.upkeep()) : "No Echo is out";
        graphics.drawCenteredString(this.font, upkeep, cx, cy + OUTER + 14, SpellStyle.MUTED);
        graphics.drawCenteredString(this.font, Component.translatable("screen.shadowslave.echo_wheel.hint"), cx, cy + OUTER + 26, SpellStyle.HINT);
    }

    /** A ring segment from {@code start} to {@code end} degrees, clockwise from straight up. */
    private static void arc(GuiGraphics graphics, int cx, int cy, float inner, float outer, float start, float end, int colour) {
        Matrix4f matrix = graphics.pose().last().pose();
        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        int steps = Math.max(2, Math.round(STEPS * (end - start) / 90.0F));
        for (int i = 0; i < steps; i++) {
            double a0 = Math.toRadians(start + (end - start) * i / steps);
            double a1 = Math.toRadians(start + (end - start) * (i + 1) / steps);
            vertex(buffer, matrix, cx, cy, inner, a0, colour);
            vertex(buffer, matrix, cx, cy, inner, a1, colour);
            vertex(buffer, matrix, cx, cy, outer, a1, colour);
            vertex(buffer, matrix, cx, cy, outer, a0, colour);
        }
        graphics.flush();
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, int cx, int cy, float radius, double angle, int colour) {
        buffer.addVertex(matrix, (float) (cx + Math.sin(angle) * radius), (float) (cy - Math.cos(angle) * radius), 0.0F).setColor(colour);
    }

    private void pick(int sector) {
        if (sector >= 0) {
            PacketDistributor.sendToServer(EchoActionPayload.of(ORDERS[sector]));
        } else if (sector == -1) {
            PacketDistributor.sendToServer(EchoActionPayload.of(EchoActionPayload.CALL));
        } else {
            onClose();
            return;
        }
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2F));
        onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            pick(sectorAt(mouseX - this.width / 2.0, mouseY - this.height / 2.0));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ClientKeyMappings.TOGGLE_ECHO_MODE.matches(keyCode, scanCode)) {
            pick(hovered);
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
