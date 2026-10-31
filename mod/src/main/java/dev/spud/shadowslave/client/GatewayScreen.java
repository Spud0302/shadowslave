package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.GatewayActionPayload;
import dev.spud.shadowslave.network.payload.GatewayStatusPayload;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * A claimed Citadel's Gateway (Run C), over the Spell's web: on the left its
 * hunger meter, any raid under way, its anchors and the Gateway Links; on the
 * right each upgrade with what it gives now, what the next tier gives and
 * costs (green for what you carry, red for what you lack), and a button to
 * build or repair it. Across the foot, the clan's wars, truces and whether its
 * supply line is cut (Run I); the heading names the clan's seat. Everything is
 * decided on the server; the screen only asks and shows the answer.
 */
public class GatewayScreen extends Screen {
    private static final int ROW = 44;
    private static final int LEFT = 176;
    private static final int[] STAGE_COLORS = {SpellStyle.ASPECT, 0xFFE8B04A, 0xFFE07A3A, SpellStyle.FLAW};
    private static final String[] STAGE_NAMES = {"Fed", "Hungry", "Starving", "Forsaken"};

    private final SpellWeb web;
    private final long openedAt = Util.getMillis();
    private GatewayStatusPayload status;

    public GatewayScreen(GatewayStatusPayload status) {
        super(Component.translatable("screen.shadowslave.gateway"));
        this.status = status;
        this.web = new SpellWeb(status.citadel().hashCode(), 120, 14, 44);
    }

    public String citadel() {
        return status.citadel();
    }

    /** The server answered a request: show the Gateway as it stands now. */
    public void update(GatewayStatusPayload status) {
        this.status = status;
        rebuildWidgets();
    }

    private int panelWidth() {
        return Math.min(this.width - 16, 580);
    }

    private int left() {
        return (this.width - panelWidth()) / 2;
    }

    private int top() {
        return Math.max(6, (this.height - panelHeight()) / 2);
    }

    private int panelHeight() {
        return Math.min(this.height - 12, 34 + Math.max(6, status.upgrades().size()) * ROW + 26 + warHeight());
    }

    /** Where the war strip begins, under the upgrades. */
    private int warTop() {
        return top() + 34 + Math.max(6, status.upgrades().size()) * ROW + 2;
    }

    private List<net.minecraft.util.FormattedCharSequence> warLines() {
        List<net.minecraft.util.FormattedCharSequence> lines = new java.util.ArrayList<>();
        for (String line : status.war()) {
            lines.addAll(this.font.split(Component.literal(line), panelWidth() - 60));
        }
        return lines.size() > 4 ? lines.subList(0, 4) : lines;
    }

    private int warHeight() {
        return status.war().isEmpty() ? 0 : 6 + warLines().size() * 10;
    }

    @Override
    protected void init() {
        int x = left();
        int y = top();
        int right = x + panelWidth() - 8;
        int rowTop = y + 34;
        for (GatewayStatusPayload.Upgrade upgrade : status.upgrades()) {
            if (upgrade.damaged()) {
                Button repair = Button.builder(Component.literal("Repair"), button -> send(GatewayActionPayload.REPAIR, upgrade.id()))
                        .bounds(right - 52, rowTop + 2, 52, 14).build();
                repair.active = upgrade.canRepair();
                addRenderableWidget(repair);
            } else if (upgrade.tier() < upgrade.maxTier()) {
                String label = "Build " + numeral(upgrade.tier() + 1);
                Button build = Button.builder(Component.literal(label), button -> send(GatewayActionPayload.UPGRADE, upgrade.id()))
                        .bounds(right - 52, rowTop + 2, 52, 14).build();
                build.active = upgrade.canPay() && status.stage() == 0;
                addRenderableWidget(build);
            }
            rowTop += ROW;
        }
        int linkTop = linksTop();
        for (GatewayStatusPayload.Link link : status.links()) {
            if (linkTop + 16 > y + panelHeight() - 18) {
                break;
            }
            Button travel = Button.builder(Component.literal(this.font.plainSubstrByWidth(link.name(), LEFT - 60) + " · " + link.distance() + "m"),
                    button -> {
                        send(GatewayActionPayload.TRAVEL, link.citadel());
                        onClose();
                    }).bounds(x + 8, linkTop, LEFT - 16, 14).build();
            addRenderableWidget(travel);
            linkTop += 16;
        }
    }

    private int linksTop() {
        return top() + 34 + 74 + (status.raid().isEmpty() ? 0 : 30) + 36;
    }

    private void send(int action, String target) {
        PacketDistributor.sendToServer(new GatewayActionPayload(status.citadel(), action, target));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float time = (Util.getMillis() - openedAt) / 1000.0F;
        web.render(graphics, this.font, 0, 0, this.width, this.height, time, 1.0F, 1.0F);
        int x = left();
        int y = top();
        graphics.fill(x, y, x + panelWidth(), y + panelHeight(), SpellStyle.PANEL);
        graphics.renderOutline(x, y, panelWidth(), panelHeight(), SpellStyle.BORDER);
        graphics.fill(x + LEFT, y + 30, x + LEFT + 1, status.war().isEmpty() ? y + panelHeight() - 6 : warTop() - 4, SpellStyle.RULE);
        if (!status.war().isEmpty()) {
            graphics.fill(x + 6, warTop() - 3, x + panelWidth() - 6, warTop() - 2, SpellStyle.RULE);
        }
        graphics.fill(x + 6, y + 28, x + panelWidth() - 6, y + 29, SpellStyle.RULE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = left();
        int y = top();
        int width = panelWidth();

        // Heading: the Citadel and who holds it.
        graphics.pose().pushPose();
        graphics.pose().translate(x + 8, y + 6, 0);
        graphics.pose().scale(1.5F, 1.5F, 1.0F);
        graphics.drawString(this.font, status.name(), 0, 0, SpellStyle.TITLE, false);
        graphics.pose().popPose();
        String held = (status.seat() ? "Seat of " : "Held by ") + status.clan() + " · " + status.members() + (status.members() == 1 ? " member" : " members")
                + " · " + status.rank() + " Citadel";
        graphics.drawString(this.font, held, x + width - 8 - this.font.width(held), y + 10, SpellStyle.MUTED, false);

        renderLeft(graphics, x + 8, y + 34);
        renderUpgrades(graphics, x + LEFT + 8, y + 34, x + width - 8);
        renderWar(graphics, x + 8, warTop() + 1);

        if (!status.message().isEmpty()) {
            String message = this.font.plainSubstrByWidth(status.message(), width - 16);
            graphics.drawString(this.font, message, x + (width - this.font.width(message)) / 2, y + panelHeight() - 13, SpellStyle.VOICE, false);
        }
    }

    private void renderLeft(GuiGraphics graphics, int x, int y) {
        int column = LEFT - 16;
        int stage = Math.max(0, Math.min(3, status.stage()));
        graphics.drawString(this.font, "Provisions", x, y, SpellStyle.TEXT, false);
        String stageName = STAGE_NAMES[stage];
        graphics.drawString(this.font, stageName, x + column - this.font.width(stageName), y, STAGE_COLORS[stage], false);
        int barY = y + 12;
        graphics.fill(x, barY, x + column, barY + 8, 0xFF1A1424);
        float share = status.capacity() <= 0 ? 0 : Math.min(1.0F, status.provisions() / status.capacity());
        int filled = Math.round(column * share);
        if (filled > 0) {
            graphics.fillGradient(x, barY, x + filled, barY + 8, STAGE_COLORS[stage], SpellStyle.withAlpha(STAGE_COLORS[stage], 0.55F));
        }
        graphics.renderOutline(x - 1, barY - 1, column + 2, 10, SpellStyle.BORDER);
        graphics.drawString(this.font, Math.round(status.provisions()) + " / " + Math.round(status.capacity())
                + " · eats " + Math.round(status.perDay()) + " a day", x, barY + 12, SpellStyle.MUTED, false);
        int lineY = barY + 24;
        for (var line : this.font.split(Component.literal(status.stageLine()), column)) {
            graphics.drawString(this.font, line, x, lineY, STAGE_COLORS[stage], false);
            lineY += 10;
        }
        lineY = y + 52;
        for (var line : this.font.split(Component.literal("Hold soul shards or materials and use the Gateway to feed it."), column)) {
            graphics.drawString(this.font, line, x, lineY, SpellStyle.HINT, false);
            lineY += 10;
        }
        lineY = y + 74;
        if (!status.raid().isEmpty()) {
            for (var line : this.font.split(Component.literal(status.raid()), column).stream().limit(3).toList()) {
                graphics.drawString(this.font, line, x, lineY, SpellStyle.FLAW, false);
                lineY += 10;
            }
            lineY = y + 104;
        }
        graphics.drawString(this.font, "Anchored here", x, lineY, SpellStyle.TEXT, false);
        String anchors = status.anchors() + " / " + status.anchorSlots();
        graphics.drawString(this.font, anchors, x + column - this.font.width(anchors), lineY, SpellStyle.ASPECT, false);
        lineY += 18;
        graphics.drawString(this.font, "Gateway Links", x, lineY, SpellStyle.TEXT, false);
        String links = status.travelCost() < 0 ? "not built" : status.travelCost() + " essence a trip";
        graphics.drawString(this.font, links, x + column - this.font.width(links), lineY, status.travelCost() < 0 ? SpellStyle.HINT : SpellStyle.ASPECT, false);
        if (status.travelCost() >= 0 && status.links().isEmpty()) {
            graphics.drawString(this.font, "Your clan holds no other Citadel.", x, lineY + 12, SpellStyle.HINT, false);
        }
    }

    /** The clan's wars, truces and supply line, in a strip across the foot of the screen. */
    private void renderWar(GuiGraphics graphics, int x, int y) {
        if (status.war().isEmpty()) {
            return;
        }
        graphics.drawString(this.font, "War", x, y, SpellStyle.FLAW, false);
        int lineY = y;
        for (var line : warLines()) {
            graphics.drawString(this.font, line, x + 44, lineY, 0xFFE07A3A, false);
            lineY += 10;
        }
    }

    private void renderUpgrades(GuiGraphics graphics, int x, int y, int right) {
        int textRight = right - 58;
        for (GatewayStatusPayload.Upgrade upgrade : status.upgrades()) {
            String name = upgrade.name() + " " + (upgrade.tier() == 0 ? "" : numeral(upgrade.tier()));
            graphics.drawString(this.font, name, x, y, upgrade.tier() == 0 ? SpellStyle.MUTED : SpellStyle.ABILITY, false);
            String pips = pips(upgrade.tier(), upgrade.maxTier());
            int after = x + this.font.width(name) + 4;
            graphics.drawString(this.font, pips, after, y, SpellStyle.RULE | 0xFF000000, false);
            String state = upgrade.damaged() ? "Damaged" : !upgrade.works() ? "Dark: the Gateway hungers" : "";
            if (!state.isEmpty()) {
                graphics.drawString(this.font, state, after + this.font.width(pips) + 6, y, upgrade.damaged() ? SpellStyle.FLAW : 0xFFE8B04A, false);
            }
            String detail = upgrade.tier() >= upgrade.maxTier() ? upgrade.now()
                    : upgrade.tier() == 0 ? numeral(1) + ": " + upgrade.next()
                    : "Now: " + upgrade.now() + " " + numeral(upgrade.tier() + 1) + ": " + upgrade.next();
            int lineY = y + 10;
            for (var line : this.font.split(Component.literal(detail), textRight - x).stream().limit(2).toList()) {
                graphics.drawString(this.font, line, x, lineY, SpellStyle.VOICE, false);
                lineY += 10;
            }
            List<GatewayStatusPayload.Need> needs = upgrade.damaged() ? upgrade.repair() : upgrade.cost();
            int needX = x;
            if (!needs.isEmpty()) {
                String label = upgrade.damaged() ? "Repair: " : "Cost: ";
                graphics.drawString(this.font, label, needX, y + 31, SpellStyle.HINT, false);
                needX += this.font.width(label);
            }
            for (GatewayStatusPayload.Need need : needs) {
                String text = need.text();
                if (needX + this.font.width(text) > right) {
                    graphics.drawString(this.font, "…", needX, y + 31, SpellStyle.HINT, false);
                    break;
                }
                graphics.drawString(this.font, text, needX, y + 31, need.have() ? 0xFF7FD68A : 0xFFE07070, false);
                needX += this.font.width(text) + 8;
            }
            y += ROW;
        }
    }

    private static String pips(int tier, int max) {
        StringBuilder pips = new StringBuilder();
        for (int i = 0; i < max; i++) {
            pips.append(i < tier ? '◆' : '◇');
        }
        return pips.toString();
    }

    static String numeral(int tier) {
        return switch (tier) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> Integer.toString(tier);
        };
    }
}
