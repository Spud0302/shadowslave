package dev.spud.shadowslave.client;

import dev.spud.shadowslave.creature.CreatureRank;
import dev.spud.shadowslave.dreamrealm.DreamRealm;
import dev.spud.shadowslave.network.payload.FieldGuidePinRequestPayload;
import dev.spud.shadowslave.network.payload.FieldGuideStatusPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The Field Guide: a parchment book with four tabs. Creatures lists every
 * page the player has started and shows only what they have learned. Map is
 * the waking world, filled in only where they have been, with toggleable
 * layers. Dream Realm is the same map of the Dream Realm, where each Seed
 * pin also says where its Gate would open in the waking world. Callings keeps
 * every Calling the Spell has made to the player, newest first, with how often
 * they want them.
 */
public class FieldGuideScreen extends Screen {
    static final int WIDTH = 360;
    static final int HEIGHT = 232;
    static final int PAGE = 0xFFEADFC2;
    static final int PAGE_SHADE = 0xFFDCCDA6;
    static final int LEATHER = 0xFF4E321D;
    static final int LEATHER_EDGE = 0xFF2E1D10;
    static final int INK = 0xFF3B2A1A;
    static final int INK_MUTED = 0xFF7A6448;
    static final int RULE = 0xFFB59E74;
    static final int ACCENT = 0xFF8E2B2B;
    static final float[] ZOOMS = {0.125F, 0.25F, 0.5F, 1.0F, 2.0F};

    enum Tab {
        CREATURES("Creatures"), MAP("Map"), DREAM("Dream Realm"), CALLINGS("Callings");

        final String label;

        Tab(String label) {
            this.label = label;
        }
    }

    private Tab tab = Tab.CREATURES;
    private int selected;
    private int pageScroll;
    private final double[] centreX = new double[Tab.values().length];
    private final double[] centreZ = new double[Tab.values().length];
    private final int[] zoom = {2, 2, 2, 2};
    private int callingsScroll;
    private final boolean[] centred = new boolean[Tab.values().length];
    private boolean dragging;
    private EditBox pinName;
    private int pendingX;
    private int pendingZ;
    private boolean pending;

    public FieldGuideScreen() {
        super(Component.literal("Field Guide"));
    }

    /** Opens on a tab, e.g. for a picture of the map. */
    public FieldGuideScreen(Tab tab) {
        this();
        this.tab = tab;
    }

    int left() {
        return (width - WIDTH) / 2;
    }

    int top() {
        return (height - HEIGHT) / 2;
    }

    @Override
    protected void init() {
        pinName = new EditBox(font, 0, 0, 120, 14, Component.literal("Pin name"));
        pinName.setMaxLength(FieldGuidePinRequestPayload.MAX_LABEL);
        pinName.setHint(Component.literal("Name this place"));
        pinName.setVisible(false);
        addRenderableWidget(pinName);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fillGradient(0, 0, width, height, 0x90100A04, 0xB0100A04);
    }

    // Drawing

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = left();
        int top = top();
        graphics.fill(left - 6, top - 6, left + WIDTH + 6, top + HEIGHT + 6, LEATHER_EDGE);
        graphics.fill(left - 5, top - 5, left + WIDTH + 5, top + HEIGHT + 5, LEATHER);
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, PAGE);
        graphics.fillGradient(left, top + HEIGHT - 30, left + WIDTH, top + HEIGHT, PAGE, PAGE_SHADE);
        graphics.drawString(font, "Field Guide", left + 8, top + 7, INK, false);
        drawTabs(graphics, mouseX, mouseY);
        graphics.fill(left + 6, top + 19, left + WIDTH - 6, top + 20, RULE);
        switch (tab) {
            case CREATURES -> drawCreatures(graphics, mouseX, mouseY);
            case MAP, DREAM -> drawMap(graphics, mouseX, mouseY);
            case CALLINGS -> drawCallings(graphics, mouseX, mouseY);
        }
        if (pending) {
            drawPinPrompt(graphics, mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        if (isMap() && !pending) {
            drawMapTooltip(graphics, mouseX, mouseY);
        }
    }

    private void drawTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = left() + WIDTH - 8;
        for (int i = Tab.values().length - 1; i >= 0; i--) {
            Tab each = Tab.values()[i];
            int w = font.width(each.label) + 10;
            x -= w;
            boolean active = each == tab;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= top() + 3 && mouseY < top() + 18;
            graphics.fill(x, top() + 3, x + w, top() + 18, active ? PAGE_SHADE : hover ? 0xFFE3D6B4 : PAGE);
            if (active) {
                graphics.fill(x, top() + 17, x + w, top() + 19, ACCENT);
            }
            graphics.drawString(font, each.label, x + 5, top() + 7, active ? INK : INK_MUTED, false);
            x -= 2;
        }
    }

    private Tab tabAt(double mouseX, double mouseY) {
        int x = left() + WIDTH - 8;
        for (int i = Tab.values().length - 1; i >= 0; i--) {
            Tab each = Tab.values()[i];
            int w = font.width(each.label) + 10;
            x -= w;
            if (mouseX >= x && mouseX < x + w && mouseY >= top() + 3 && mouseY < top() + 18) {
                return each;
            }
            x -= 2;
        }
        return null;
    }

    // Creatures tab

    private List<FieldGuideStatusPayload.Creature> creatures() {
        return ClientFieldGuideState.status().creatures();
    }

    private void drawCreatures(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = left();
        int top = top() + 26;
        List<FieldGuideStatusPayload.Creature> creatures = creatures();
        graphics.fill(left + 108, top, left + 109, top() + HEIGHT - 8, RULE);
        if (creatures.isEmpty()) {
            graphics.drawString(font, "No pages yet", left + 10, top + 2, INK_MUTED, false);
            drawWrapped(graphics, "Look at a Nightmare Creature through a spyglass and hold it in view to start its page. "
                    + "Each sighting and each kill reveals more.", left + 118, top + 2, WIDTH - 128, INK);
            return;
        }
        selected = Math.max(0, Math.min(selected, creatures.size() - 1));
        for (int i = 0; i < creatures.size(); i++) {
            FieldGuideStatusPayload.Creature creature = creatures.get(i);
            int y = top + i * 14;
            boolean hover = mouseX >= left + 6 && mouseX < left + 104 && mouseY >= y - 2 && mouseY < y + 12;
            if (i == selected) {
                graphics.fill(left + 6, y - 2, left + 104, y + 12, PAGE_SHADE);
                graphics.fill(left + 6, y - 2, left + 8, y + 12, ACCENT);
            } else if (hover) {
                graphics.fill(left + 6, y - 2, left + 104, y + 12, 0xFFE3D6B4);
            }
            graphics.drawString(font, font.plainSubstrByWidth(creature.name(), 84), left + 11, y + 1, INK, false);
            int pips = creature.sections().size();
            int total = pips + creature.locked();
            for (int p = 0; p < total; p++) {
                graphics.fill(left + 98 - (total - p) * 4, y + 10, left + 101 - (total - p) * 4, y + 11, p < pips ? ACCENT : RULE);
            }
        }
        graphics.drawString(font, creatures.size() + (creatures.size() == 1 ? " page" : " pages"),
                left + 10, top() + HEIGHT - 16, INK_MUTED, false);
        drawPage(graphics, creatures.get(selected));
    }

    private void drawPage(GuiGraphics graphics, FieldGuideStatusPayload.Creature creature) {
        int x = left() + 118;
        int w = WIDTH - 128;
        int top = top() + 24;
        int bottom = top() + HEIGHT - 8;
        graphics.enableScissor(x - 2, top, x + w + 2, bottom);
        int y = top + 2 - pageScroll;
        graphics.drawString(font, Component.literal(creature.name()).withStyle(style -> style.withBold(true)), x, y, INK, false);
        y += 12;
        String counts = "Seen " + creature.sightings() + "   Slain " + creature.kills();
        graphics.drawString(font, counts, x, y, INK_MUTED, false);
        y += 11;
        if (!creature.ranks().isEmpty()) {
            graphics.drawString(font, "Met as: " + String.join(", ", creature.ranks()), x, y, INK_MUTED, false);
            y += 11;
        }
        if (!creature.summary().isEmpty()) {
            y = drawWrapped(graphics, creature.summary(), x, y + 2, w, INK) + 2;
        }
        for (FieldGuideStatusPayload.Section section : creature.sections()) {
            graphics.fill(x, y + 3, x + w, y + 4, RULE);
            y += 7;
            graphics.drawString(font, section.title(), x, y, ACCENT, false);
            y = drawWrapped(graphics, section.text(), x, y + 11, w, INK) + 1;
        }
        if (creature.locked() > 0) {
            graphics.fill(x, y + 3, x + w, y + 4, RULE);
            y += 7;
            String hidden = creature.locked() == 1 ? "1 entry not yet learned." : creature.locked() + " entries not yet learned.";
            y = drawWrapped(graphics, hidden + (creature.hint().isEmpty() ? "" : " " + creature.hint() + "."), x, y, w, INK_MUTED);
        }
        if (!creature.lastDimension().isEmpty()) {
            y += 4;
            graphics.drawString(font, "Last met near " + creature.lastX() + ", " + creature.lastZ() + " (" + dimensionName(creature.lastDimension()) + ")",
                    x, y, INK_MUTED, false);
            y += 10;
        }
        graphics.disableScissor();
        int overflow = (y + pageScroll) - bottom;
        pageScroll = Math.max(0, Math.min(pageScroll, Math.max(0, overflow + 4)));
    }

    private int drawWrapped(GuiGraphics graphics, String text, int x, int y, int width, int colour) {
        for (FormattedCharSequence line : font.split(Component.literal(text), width)) {
            graphics.drawString(font, line, x, y, colour, false);
            y += 10;
        }
        return y;
    }

    String dimensionName(String dimension) {
        if (dimension.equals(ClientFieldGuideState.status().realm()) || dimension.equals(DreamRealm.LEVEL.location().toString())) {
            return "Dream Realm";
        }
        if (dimension.equals(Level.OVERWORLD.location().toString())) {
            return "waking world";
        }
        if (dimension.equals(Level.NETHER.location().toString())) {
            return "Nether";
        }
        return dimension;
    }

    // Callings tab

    private boolean isMap() {
        return tab == Tab.MAP || tab == Tab.DREAM;
    }

    private String frequencyLabel() {
        return "Callings: " + SpellClientConfig.frequency().displayName();
    }

    private boolean overFrequency(double mouseX, double mouseY) {
        int w = font.width(frequencyLabel()) + 12;
        int x = left() + WIDTH - 8 - w;
        int y = top() + HEIGHT - 20;
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 14;
    }

    private void drawCallings(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = left() + 12;
        int w = WIDTH - 24;
        int top = top() + 24;
        int bottom = top() + HEIGHT - 24;
        List<dev.spud.shadowslave.network.payload.SpellCallingsPayload.Entry> heard = ClientCallingsState.heard();
        if (heard.isEmpty()) {
            graphics.drawString(font, "No Callings yet", x, top + 4, INK_MUTED, false);
            drawWrapped(graphics, "Now and then the Spell calls to the Awakened: when a core is nearly full, when a Seed "
                    + "of the Nightmare they need grows near, when they reach a new region, or when a Citadel stands unclaimed. "
                    + "What it says is kept here.", x, top + 18, w, INK);
        } else {
            graphics.enableScissor(x - 2, top, x + w + 2, bottom);
            int y = top + 4 - callingsScroll;
            for (dev.spud.shadowslave.network.payload.SpellCallingsPayload.Entry entry : heard) {
                String heading = "Day " + entry.day() + (entry.title().isEmpty() ? "" : "  \u00B7  " + entry.title());
                graphics.drawString(font, heading, x, y, ACCENT, false);
                y = drawWrapped(graphics, "\u201C" + entry.text() + "\u201D", x + 6, y + 11, w - 6, INK) + 3;
                graphics.fill(x, y, x + w, y + 1, RULE);
                y += 5;
            }
            graphics.disableScissor();
            int overflow = (y + callingsScroll) - bottom;
            callingsScroll = Math.max(0, Math.min(callingsScroll, Math.max(0, overflow + 4)));
        }
        graphics.fill(left() + 6, bottom + 1, left() + WIDTH - 6, bottom + 2, RULE);
        graphics.drawString(font, heard.size() == 1 ? "1 Calling heard" : heard.size() + " Callings heard",
                x, top() + HEIGHT - 16, INK_MUTED, false);
        String label = frequencyLabel();
        int bw = font.width(label) + 12;
        int bx = left() + WIDTH - 8 - bw;
        int by = top() + HEIGHT - 20;
        boolean hover = overFrequency(mouseX, mouseY);
        graphics.fill(bx, by, bx + bw, by + 14, hover ? 0xFFE3D6B4 : PAGE_SHADE);
        graphics.renderOutline(bx, by, bw, 14, RULE);
        graphics.drawString(font, label, bx + 6, by + 3, INK, false);
    }

    // Map tabs

    String mapDimension() {
        String realm = ClientFieldGuideState.status().realm().isEmpty() ? DreamRealm.LEVEL.location().toString() : ClientFieldGuideState.status().realm();
        if (tab == Tab.DREAM) {
            return realm;
        }
        LocalPlayer player = minecraft == null ? null : minecraft.player;
        String here = player == null ? Level.OVERWORLD.location().toString() : player.level().dimension().location().toString();
        return here.equals(realm) ? Level.OVERWORLD.location().toString() : here;
    }

    FieldGuideMapRenderer.View view() {
        int i = tab.ordinal();
        if (!centred[i]) {
            centreOnPlayer();
        }
        return new FieldGuideMapRenderer.View(left() + 8, top() + 24, WIDTH - 16, HEIGHT - 50, centreX[i], centreZ[i], ZOOMS[zoom[i]]);
    }

    private void centreOnPlayer() {
        int i = tab.ordinal();
        centred[i] = true;
        LocalPlayer player = minecraft == null ? null : minecraft.player;
        if (player == null) {
            return;
        }
        String here = player.level().dimension().location().toString();
        String shown = mapDimension();
        double x = player.getX();
        double z = player.getZ();
        int ratio = Math.max(1, ClientFieldGuideState.status().ratio());
        String realm = ClientFieldGuideState.status().realm();
        if (here.equals(realm) && !shown.equals(realm)) {
            x *= ratio;
            z *= ratio;
        } else if (!here.equals(realm) && shown.equals(realm)) {
            x /= ratio;
            z /= ratio;
        }
        centreX[i] = x;
        centreZ[i] = z;
    }

    private Set<ClientFieldGuideState.Layer> layers() {
        Set<ClientFieldGuideState.Layer> layers = EnumSet.noneOf(ClientFieldGuideState.Layer.class);
        for (ClientFieldGuideState.Layer layer : ClientFieldGuideState.Layer.values()) {
            if (layer == ClientFieldGuideState.Layer.THREATS && tab == Tab.DREAM) {
                continue;
            }
            layers.add(layer);
        }
        return layers;
    }

    private void drawMap(GuiGraphics graphics, int mouseX, int mouseY) {
        String dimension = mapDimension();
        FieldGuideMapRenderer.View view = view();
        FieldGuideStatusPayload status = ClientFieldGuideState.status();
        graphics.enableScissor(view.left(), view.top(), view.left() + view.width(), view.top() + view.height());
        FieldGuideMapRenderer.drawTerrain(graphics, dimension, view);
        graphics.disableScissor();
        if (ClientFieldGuideState.shown(ClientFieldGuideState.Layer.DANGER)) {
            FieldGuideMapRenderer.drawDanger(graphics, dimension, view, status);
        }
        graphics.enableScissor(view.left(), view.top(), view.left() + view.width(), view.top() + view.height());
        for (Marker marker : markers(dimension, view)) {
            marker.draw(graphics);
        }
        LocalPlayer player = minecraft.player;
        if (player != null && player.level().dimension().location().toString().equals(dimension)) {
            FieldGuideMapRenderer.drawPlayer(graphics, view.screenX(player.getX()), view.screenY(player.getZ()), player.getYRot());
        }
        if (pending) {
            FieldGuideMapRenderer.drawPin(graphics, "place", view.screenX(pendingX), view.screenY(pendingZ), true);
        }
        graphics.disableScissor();
        graphics.renderOutline(view.left() - 1, view.top() - 1, view.width() + 2, view.height() + 2, INK_MUTED);

        if (ClientFieldGuideState.chunks(dimension).isEmpty()) {
            String message = tab == Tab.DREAM ? "You have not dreamt yet. Sleep beside a lit Dream Candle to enter the Dream Realm."
                    : "Nothing mapped here yet. Walk and the guide fills in.";
            List<FormattedCharSequence> lines = font.split(Component.literal(message), view.width() - 60);
            int y = view.top() + view.height() / 2 - lines.size() * 5;
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, view.left() + (view.width() - font.width(line)) / 2, y, INK, false);
                y += 10;
            }
        }
        drawZoomButtons(graphics, view, mouseX, mouseY);
        if (ClientFieldGuideState.shown(ClientFieldGuideState.Layer.DANGER)) {
            drawLegend(graphics, view, dimension, status);
        }
        drawLayerBar(graphics, mouseX, mouseY);
        String place = tab == Tab.DREAM ? regionName(view.centreX(), view.centreZ()) : dimensionName(dimension);
        graphics.drawString(font, place, left() + 8 + 0, top() + HEIGHT - 12, INK_MUTED, false);
        if (view.contains(mouseX, mouseY)) {
            int bx = (int) Math.floor(view.blockX(mouseX));
            int bz = (int) Math.floor(view.blockZ(mouseY));
            String coords = bx + ", " + bz;
            graphics.drawString(font, coords, left() + WIDTH - 8 - font.width(coords), top() + HEIGHT - 12, INK_MUTED, false);
        }
    }

    private String regionName(double x, double z) {
        double distance = Math.sqrt(x * x + z * z);
        for (FieldGuideStatusPayload.Region region : ClientFieldGuideState.status().regions()) {
            if (distance >= region.min() && distance < region.max()) {
                return region.name() + " (" + CreatureRank.byOrdinal(region.rank()).displayName() + " region)";
            }
        }
        return "Dream Realm";
    }

    private void drawLegend(GuiGraphics graphics, FieldGuideMapRenderer.View view, String dimension, FieldGuideStatusPayload status) {
        Set<Integer> ratings = new java.util.TreeSet<>();
        status.danger().stream().filter(d -> d.dimension().equals(dimension)).forEach(d -> ratings.add(d.rating()));
        if (dimension.equals(status.realm()) && !ClientFieldGuideState.chunks(dimension).isEmpty()) {
            status.regions().forEach(region -> ratings.add(region.rank()));
        }
        if (ratings.isEmpty()) {
            return;
        }
        int x = view.left() + 4;
        int y = view.top() + view.height() - 4 - ratings.size() * 10 - 11;
        int w = 74;
        graphics.fill(x, y, x + w, view.top() + view.height() - 4, 0xD0EADFC2);
        graphics.renderOutline(x, y, w, view.top() + view.height() - 4 - y, RULE);
        graphics.drawString(font, "Danger", x + 4, y + 3, INK, false);
        int row = y + 13;
        for (int rating : ratings) {
            graphics.fill(x + 4, row, x + 11, row + 7, 0xFF000000 | FieldGuideMapRenderer.dangerColour(rating));
            graphics.drawString(font, CreatureRank.byOrdinal(rating).displayName(), x + 15, row, INK, false);
            row += 10;
        }
    }

    private void drawZoomButtons(GuiGraphics graphics, FieldGuideMapRenderer.View view, int mouseX, int mouseY) {
        String[] labels = {"+", "-", "o"};
        for (int i = 0; i < labels.length; i++) {
            int x = view.left() + view.width() - 16;
            int y = view.top() + 4 + i * 15;
            boolean hover = mouseX >= x && mouseX < x + 12 && mouseY >= y && mouseY < y + 12;
            graphics.fill(x, y, x + 12, y + 12, hover ? 0xFFF3EAD3 : 0xE0EADFC2);
            graphics.renderOutline(x, y, 12, 12, INK_MUTED);
            graphics.drawString(font, labels[i], x + 6 - font.width(labels[i]) / 2, y + 2, INK, false);
        }
    }

    private int zoomButtonAt(FieldGuideMapRenderer.View view, double mouseX, double mouseY) {
        for (int i = 0; i < 3; i++) {
            int x = view.left() + view.width() - 16;
            int y = view.top() + 4 + i * 15;
            if (mouseX >= x && mouseX < x + 12 && mouseY >= y && mouseY < y + 12) {
                return i;
            }
        }
        return -1;
    }

    private void drawLayerBar(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = left() + 8;
        int y = top() + HEIGHT - 24;
        for (ClientFieldGuideState.Layer layer : layers()) {
            int w = font.width(layer.label) + 14;
            boolean on = ClientFieldGuideState.shown(layer);
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 10;
            graphics.fill(x, y + 1, x + 8, y + 9, INK_MUTED);
            graphics.fill(x + 1, y + 2, x + 7, y + 8, on ? layerColour(layer) : PAGE);
            graphics.drawString(font, layer.label, x + 11, y + 1, hover ? INK : on ? INK : INK_MUTED, false);
            x += w + 4;
        }
    }

    private int layerColour(ClientFieldGuideState.Layer layer) {
        return switch (layer) {
            case PINS -> FieldGuideMapRenderer.pinColour("place", true);
            case GATES -> FieldGuideMapRenderer.pinColour("gate", true);
            case DANGER -> 0xFF000000 | FieldGuideMapRenderer.dangerColour(1);
            case CREATURES -> INK;
            case THREATS -> 0xFFB3261E;
        };
    }

    private ClientFieldGuideState.Layer layerAt(double mouseX, double mouseY) {
        int x = left() + 8;
        int y = top() + HEIGHT - 24;
        for (ClientFieldGuideState.Layer layer : layers()) {
            int w = font.width(layer.label) + 14;
            if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + 10) {
                return layer;
            }
            x += w + 4;
        }
        return null;
    }

    /** Something drawn on the map that can be hovered. */
    record Marker(String kind, int x, int y, boolean active, List<Component> lines, FieldGuideStatusPayload.Pin pin) {
        void draw(GuiGraphics graphics) {
            switch (kind) {
                case "threat" -> FieldGuideMapRenderer.drawThreat(graphics, x, y);
                case "creature" -> FieldGuideMapRenderer.drawCreature(graphics, x, y);
                default -> FieldGuideMapRenderer.drawPin(graphics, kind, x, y, active);
            }
        }
    }

    List<Marker> markers(String dimension, FieldGuideMapRenderer.View view) {
        FieldGuideStatusPayload status = ClientFieldGuideState.status();
        List<Marker> markers = new ArrayList<>();
        if (ClientFieldGuideState.shown(ClientFieldGuideState.Layer.CREATURES)) {
            for (FieldGuideStatusPayload.Creature creature : status.creatures()) {
                if (creature.lastDimension().equals(dimension)) {
                    markers.add(new Marker("creature", view.screenX(creature.lastX()), view.screenY(creature.lastZ()), true,
                            List.of(Component.literal(creature.name()), muted("Last met here, " + creature.lastX() + ", " + creature.lastZ())), null));
                }
            }
        }
        if (tab != Tab.DREAM && ClientFieldGuideState.shown(ClientFieldGuideState.Layer.THREATS)) {
            for (FieldGuideStatusPayload.Pin pin : status.pins()) {
                if (pin.kind().equals("seed") && dimension.equals(Level.OVERWORLD.location().toString())) {
                    markers.add(new Marker("threat", view.screenX(pin.gateX()), view.screenY(pin.gateZ()), true,
                            List.of(Component.literal("Seed threat"), muted("A Seed at " + pin.x() + ", " + pin.z() + " in the Dream Realm"),
                                    muted("would open its Gate here, near " + pin.gateX() + ", " + pin.gateZ())), pin));
                }
            }
        }
        for (FieldGuideStatusPayload.Pin pin : status.pins()) {
            if (!pin.dimension().equals(dimension)) {
                continue;
            }
            boolean gateLayer = pin.kind().equals("gate") || pin.kind().equals("seed");
            if (!ClientFieldGuideState.shown(gateLayer ? ClientFieldGuideState.Layer.GATES : ClientFieldGuideState.Layer.PINS)) {
                continue;
            }
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(pin.label()));
            lines.add(muted(pin.x() + ", " + pin.z()));
            if (!pin.detail().isEmpty()) {
                lines.add(muted(pin.detail()));
            }
            if (pin.named()) {
                lines.add(muted("Right-click to remove"));
            }
            markers.add(new Marker(pin.kind(), view.screenX(pin.x()), view.screenY(pin.z()), pin.active(), lines, pin));
        }
        return markers;
    }

    private static Component muted(String text) {
        return Component.literal(text).withStyle(net.minecraft.ChatFormatting.GRAY);
    }

    private Marker markerAt(double mouseX, double mouseY) {
        FieldGuideMapRenderer.View view = view();
        if (!view.contains(mouseX, mouseY)) {
            return null;
        }
        Marker best = null;
        double bestDistance = 25;
        for (Marker marker : markers(mapDimension(), view)) {
            double dx = marker.x() - mouseX;
            double dy = marker.y() - mouseY;
            double distance = dx * dx + dy * dy;
            if (distance <= bestDistance) {
                best = marker;
                bestDistance = distance;
            }
        }
        return best;
    }

    private void drawMapTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        Marker marker = markerAt(mouseX, mouseY);
        if (marker != null) {
            graphics.renderComponentTooltip(font, marker.lines(), mouseX, mouseY);
            return;
        }
        FieldGuideMapRenderer.View view = view();
        if (view.contains(mouseX, mouseY) && ClientFieldGuideState.shown(ClientFieldGuideState.Layer.DANGER)) {
            FieldGuideStatusPayload status = ClientFieldGuideState.status();
            int cell = status.dangerCell();
            int bx = (int) Math.floor(view.blockX(mouseX));
            int bz = (int) Math.floor(view.blockZ(mouseY));
            String dimension = mapDimension();
            if (!ClientFieldGuideState.explored(dimension, bx >> 4, bz >> 4)) {
                return;
            }
            int rating = FieldGuideMapRenderer.rating(status, dimension, Math.floorDiv(bx, cell), Math.floorDiv(bz, cell));
            if (rating >= 0 && zoomButtonAt(view, mouseX, mouseY) < 0) {
                graphics.renderComponentTooltip(font, List.of(Component.literal(CreatureRank.byOrdinal(rating).displayName() + " danger"),
                        muted("Right-click to pin this spot")), mouseX, mouseY);
            }
        }
    }

    private void drawPinPrompt(GuiGraphics graphics, int mouseX, int mouseY) {
        FieldGuideMapRenderer.View view = view();
        int w = 230;
        int x = view.left() + (view.width() - w) / 2;
        int y = view.top() + view.height() - 26;
        graphics.fill(x, y, x + w, y + 22, 0xF0EADFC2);
        graphics.renderOutline(x, y, w, 22, INK_MUTED);
        pinName.setX(x + 4);
        pinName.setY(y + 4);
        String[] labels = {"Pin", "Warn", "x"};
        int bx = x + 130;
        for (String label : labels) {
            int bw = font.width(label) + 10;
            boolean hover = mouseX >= bx && mouseX < bx + bw && mouseY >= y + 4 && mouseY < y + 18;
            graphics.fill(bx, y + 4, bx + bw, y + 18, hover ? 0xFFF3EAD3 : PAGE_SHADE);
            graphics.renderOutline(bx, y + 4, bw, 14, INK_MUTED);
            graphics.drawString(font, label, bx + 5, y + 7, label.equals("Warn") ? ACCENT : INK, false);
            bx += bw + 4;
        }
    }

    private String promptButtonAt(double mouseX, double mouseY) {
        FieldGuideMapRenderer.View view = view();
        int w = 230;
        int x = view.left() + (view.width() - w) / 2;
        int y = view.top() + view.height() - 26;
        int bx = x + 130;
        for (String label : new String[]{"Pin", "Warn", "x"}) {
            int bw = font.width(label) + 10;
            if (mouseX >= bx && mouseX < bx + bw && mouseY >= y + 4 && mouseY < y + 18) {
                return label;
            }
            bx += bw + 4;
        }
        return null;
    }

    private void startPin(int x, int z) {
        pending = true;
        pendingX = x;
        pendingZ = z;
        pinName.setValue("");
        pinName.setVisible(true);
        setFocused(pinName);
        pinName.setFocused(true);
    }

    private void finishPin(String action) {
        if (action != null) {
            PacketDistributor.sendToServer(new FieldGuidePinRequestPayload(action, mapDimension(), pendingX, pendingZ, pinName.getValue()));
            click();
        }
        pending = false;
        pinName.setVisible(false);
        pinName.setFocused(false);
    }

    private void click() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }

    // Input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Tab clickedTab = tabAt(mouseX, mouseY);
        if (clickedTab != null) {
            if (clickedTab != tab) {
                tab = clickedTab;
                finishPin(null);
                click();
            }
            return true;
        }
        if (tab == Tab.CREATURES) {
            int top = top() + 26;
            if (mouseX >= left() + 6 && mouseX < left() + 104) {
                int index = (int) Math.floor((mouseY - top + 2) / 14);
                if (index >= 0 && index < creatures().size() && mouseY >= top - 2) {
                    if (index != selected) {
                        selected = index;
                        pageScroll = 0;
                        click();
                    }
                    return true;
                }
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (tab == Tab.CALLINGS) {
            if (overFrequency(mouseX, mouseY)) {
                SpellClientConfig.setFrequency(SpellClientConfig.frequency().next());
                click();
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (pending) {
            String choice = promptButtonAt(mouseX, mouseY);
            if (choice != null) {
                finishPin(switch (choice) {
                    case "Pin" -> FieldGuidePinRequestPayload.PLACE;
                    case "Warn" -> FieldGuidePinRequestPayload.WARNING;
                    default -> null;
                });
                return true;
            }
            if (pinName.isMouseOver(mouseX, mouseY)) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
        }
        ClientFieldGuideState.Layer layer = layerAt(mouseX, mouseY);
        if (layer != null) {
            ClientFieldGuideState.toggle(layer);
            click();
            return true;
        }
        FieldGuideMapRenderer.View view = view();
        int zoomButton = zoomButtonAt(view, mouseX, mouseY);
        if (zoomButton >= 0) {
            switch (zoomButton) {
                case 0 -> changeZoom(1);
                case 1 -> changeZoom(-1);
                default -> centreOnPlayer();
            }
            click();
            return true;
        }
        if (view.contains(mouseX, mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                Marker marker = markerAt(mouseX, mouseY);
                if (marker != null && marker.pin() != null && marker.pin().named()) {
                    PacketDistributor.sendToServer(new FieldGuidePinRequestPayload(FieldGuidePinRequestPayload.REMOVE,
                            mapDimension(), marker.pin().x(), marker.pin().z(), ""));
                    click();
                } else {
                    startPin((int) Math.floor(view.blockX(mouseX)), (int) Math.floor(view.blockZ(mouseY)));
                }
                return true;
            }
            if (pending) {
                finishPin(null);
            }
            dragging = button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && isMap()) {
            int i = tab.ordinal();
            float scale = ZOOMS[zoom[i]];
            centreX[i] -= dragX / scale;
            centreZ[i] -= dragY / scale;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (tab == Tab.CALLINGS) {
            callingsScroll = Math.max(0, callingsScroll - (int) (scrollY * 10));
            return true;
        }
        if (tab == Tab.CREATURES) {
            pageScroll = Math.max(0, pageScroll - (int) (scrollY * 10));
            return true;
        }
        changeZoom(scrollY > 0 ? 1 : -1);
        return true;
    }

    private void changeZoom(int step) {
        int i = tab.ordinal();
        zoom[i] = Math.max(0, Math.min(ZOOMS.length - 1, zoom[i] + step));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (pending) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                finishPin(FieldGuidePinRequestPayload.PLACE);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                finishPin(null);
                return true;
            }
            return pinName.keyPressed(keyCode, scanCode, modifiers) || pinName.canConsumeInput();
        }
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
