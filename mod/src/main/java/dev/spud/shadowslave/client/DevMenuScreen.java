package dev.spud.shadowslave.client;

import com.mojang.brigadier.ParseResults;
import dev.spud.shadowslave.awakening.AbilityVariant;
import dev.spud.shadowslave.awakening.AspectKit;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.awakening.KitLadder;
import dev.spud.shadowslave.devtools.DevTools;
import dev.spud.shadowslave.network.payload.CoreStatusPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshot;
import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.core.ShardRank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The dev menu (operators, or cheats on): swap your Nature or the ability on
 * either key on the spot, set your rank and core, top up shards and essence,
 * and start or end a Nightmare. Every button sends a {@code /shadowslave}
 * command, so the server checks permission and does the work; the screen only
 * shows the soul the server last synced.
 */
public class DevMenuScreen extends Screen {
    private static final int PANEL_WIDTH = 440;
    private static final int PANEL_HEIGHT = 226;
    private static final int GRID_TOP = 54;
    private static final int COLUMNS = 4;
    private static final int ROW = 28;
    private static final String[] PROGRESS_LABELS = {"Soul rank", "Core saturation", "Essence and health", "Soul shards", "Start over"};
    private static final String[] WORLD_LABELS = {"Dream Realm and time", "Gates and weather", "Inside a Nightmare, and game mode", "Start a Nightmare"};
    private static final String[] TABS = {"Nature", "First key", "Second key", "Progress", "World"};

    private static int tab;
    private static int page;
    private List<String> nightmares = List.of();
    private SoulSnapshot shown;
    private CoreStatusPayload shownCore;

    public DevMenuScreen() {
        super(Component.translatable("screen.shadowslave.dev_menu"));
    }

    private int left() {
        return (this.width - panelWidth()) / 2;
    }

    private int top() {
        return Math.max(4, (this.height - PANEL_HEIGHT) / 2);
    }

    private int panelWidth() {
        return Math.min(this.width - 8, PANEL_WIDTH);
    }

    @Override
    protected void init() {
        shown = ClientSoulState.latest().orElse(null);
        shownCore = ClientCoreState.status();
        int x = left();
        int y = top();
        int tabWidth = (panelWidth() - 16) / TABS.length;
        for (int i = 0; i < TABS.length; i++) {
            int index = i;
            Button button = Button.builder(Component.literal(TABS[i]), pressed -> {
                tab = index;
                rebuildWidgets();
            }).bounds(x + 8 + i * tabWidth, y + 32, tabWidth - 2, 16).build();
            button.active = tab != i;
            addRenderableWidget(button);
        }
        switch (tab) {
            case 0 -> natureTab(x, y);
            case 1 -> firstKeyTab(x, y);
            case 2 -> secondKeyTab(x, y);
            case 3 -> progressTab(x, y);
            default -> worldTab(x, y);
        }
    }

    private void natureTab(int x, int y) {
        Optional<AspectKit> current = currentKit();
        List<AspectKit> kits = AspectKitCatalog.all();
        for (int i = 0; i < kits.size(); i++) {
            AspectKit kit = kits.get(i);
            Button button = gridButton(x, y, i, 16, kit.natureName(),
                    "nature " + kit.natureName().toLowerCase(Locale.ROOT),
                    kit.abilityName() + ": " + kit.abilityDescription() + "\nFlaw, " + kit.flawName() + ": " + kit.flawDescription());
            button.active = current.map(held -> !held.equals(kit)).orElse(true);
        }
    }

    private void firstKeyTab(int x, int y) {
        List<AspectKit> kits = AspectKitCatalog.all();
        for (int i = 0; i < kits.size(); i++) {
            AspectKit kit = kits.get(i);
            Button button = gridButton(x, y, i, 16, kit.abilityName(),
                    "ability dormant " + DevTools.abilityKey(kit.abilityId()),
                    kit.natureName() + ". " + kit.abilityDescription());
            button.active = !holds(kit.abilityId());
        }
    }

    private void secondKeyTab(int x, int y) {
        List<AspectKit> kits = AspectKitCatalog.all();
        int perPage = 14;
        int pages = (kits.size() + perPage - 1) / perPage;
        page = Math.max(0, Math.min(page, pages - 1));
        int index = 0;
        for (AspectKit kit : kits.subList(page * perPage, Math.min(kits.size(), (page + 1) * perPage))) {
            for (AbilityVariant variant : KitLadder.variants(kit.natureId(), SoulRank.AWAKENED)) {
                Button button = gridButton(x, y, index++, 16, variant.name(),
                        "ability awakened " + DevTools.abilityKey(variant.abilityId()),
                        kit.natureName() + " · " + variant.role() + ". " + variant.description());
                button.active = !holds(variant.abilityId());
            }
        }
        int bottom = y + PANEL_HEIGHT - 40;
        Button clear = Button.builder(Component.literal("Clear second key"), pressed -> run("shadowslave dev ability awakened none"))
                .bounds(x + panelWidth() - 8 - 104, bottom, 104, 16).build();
        clear.active = KitLadder.all().stream().anyMatch(variant -> holds(variant.abilityId()));
        addRenderableWidget(clear);
        for (int i = 0; i < pages; i++) {
            int target = i;
            Button pageButton = Button.builder(Component.literal(kits.get(i * perPage).natureName() + " to "
                            + kits.get(Math.min(kits.size(), (i + 1) * perPage) - 1).natureName()), pressed -> {
                        page = target;
                        rebuildWidgets();
                    }).bounds(x + 8 + i * 124, bottom, 120, 16).build();
            pageButton.active = page != i;
            addRenderableWidget(pageButton);
        }
    }

    private void progressTab(int x, int y) {
        int column = (panelWidth() - 16 - 3 * 4) / 4;
        int row = y + GRID_TOP + 10;
        action(x + 8, row, column, "Dormant", "shadowslave dev rank dormant", "Back to a Dreamer: the second key empties and the core starts over.");
        action(x + 8 + (column + 4), row, column, "Awakened", "shadowslave dev rank awakened", "Rise to Awakened without the Soul Sea; your Nature's first Awakened ability goes on the second key.");
        action(x + 8 + 2 * (column + 4), row, column, "Master", "shadowslave dev rank master", "Rise to Master without a Nightmare; your Nature's first Master ability, if built, goes on the third key.");
        action(x + 8 + 3 * (column + 4), row, column, "Saint", "shadowslave dev rank saint", "Rise to Saint without a Nightmare; your Nature's Transformation, if built, goes on the last key.");
        row += ROW;
        int limit = shownCore.limit();
        action(x + 8, row, column, "Empty", "shadowslave core set 0", "");
        action(x + 8 + (column + 4), row, column, "Half", "shadowslave core set " + limit / 2, "");
        action(x + 8 + 2 * (column + 4), row, column, "Saturate", "shadowslave core fill", "A saturated core opens the next rank at the Soul Sea.");
        row += ROW;
        action(x + 8, row, column, "Restore all", "shadowslave dev restore", "Full essence, health and food; clears effects and fire.");
        action(x + 8 + (column + 4), row, column, "Free casting", "shadowslave dev free_casting", "Toggle: abilities cost no essence.");
        row += ROW;
        ShardRank[] ranks = ShardRank.values();
        for (int i = 0; i < ranks.length; i++) {
            String name = ranks[i].getSerializedName();
            action(x + 8 + i * (column + 4), row, column, "+16 " + titleCase(name), "shadowslave dev shards " + name, "Soul shards of " + name + " creatures.");
        }
        row += ROW;
        action(x + 8, row, column, "Reroll Aspect", "shadowslave aspect reroll", "A random Nature, rolled as the Dream Candle would.");
        action(x + 8 + (column + 4), row, column, "Reset soul", "shadowslave reset", "Back to uninfected: no Aspect, no rank.");
    }

    private void worldTab(int x, int y) {
        int column = (panelWidth() - 16 - 3 * 4) / 4;
        int row = y + GRID_TOP + 10;
        action(x + 8, row, column, "Enter Dream", "shadowslave dream enter", "Cross into the Dream Realm.");
        action(x + 8 + (column + 4), row, column, "Wake", "shadowslave dream wake", "Back to the waking world.");
        action(x + 8 + 2 * (column + 4), row, column, "Day", "time set day", "");
        action(x + 8 + 3 * (column + 4), row, column, "Night", "time set midnight", "");
        row += ROW;
        action(x + 8, row, column, "Gate Cat 1", "shadowslave gate open 1", "Open a Category 1 Gate beside you.");
        action(x + 8 + (column + 4), row, column, "Gate Cat 2", "shadowslave gate open 2", "Open a Category 2 Gate beside you.");
        action(x + 8 + 2 * (column + 4), row, column, "Close Gate", "shadowslave gate close", "");
        action(x + 8 + 3 * (column + 4), row, column, "Clear weather", "weather clear", "");
        row += ROW;
        action(x + 8, row, column, "Skip 60 s", "shadowslave trial skip 60", "Inside a Nightmare: jump its clock ahead.");
        action(x + 8 + (column + 4), row, column, "Abort", "shadowslave trial abort", "Leave the Nightmare you are in, as if it never began.");
        action(x + 8 + 2 * (column + 4), row, column, "Creative", "gamemode creative", "");
        action(x + 8 + 3 * (column + 4), row, column, "Survival", "gamemode survival", "");
        row += ROW;
        for (int i = 0; i < nightmares.size() && i < 8; i++) {
            String id = nightmares.get(i);
            action(x + 8 + (i % 4) * (column + 4), row + (i / 4) * 18, column, prettify(id),
                    "shadowslave trial start " + id, "Start " + prettify(id) + " (Category 2) on your own.");
        }
        if (nightmares.isEmpty()) {
            requestNightmares();
        }
    }

    /** Asks the server for the Nightmare ids, through the same suggestions the chat box gets. */
    private void requestNightmares() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return;
        }
        ClientSuggestionProvider provider = connection.getSuggestionsProvider();
        ParseResults<net.minecraft.commands.SharedSuggestionProvider> parse =
                connection.getCommands().parse("shadowslave trial start ", provider);
        connection.getCommands().getCompletionSuggestions(parse).thenAccept(suggestions -> Minecraft.getInstance().execute(() -> {
            List<String> ids = suggestions.getList().stream().map(suggestion -> suggestion.getText()).sorted().toList();
            if (!ids.isEmpty() && !ids.equals(nightmares)) {
                nightmares = ids;
                if (Minecraft.getInstance().screen == this) {
                    rebuildWidgets();
                }
            }
        }));
    }

    private Button gridButton(int x, int y, int index, int rowHeight, String label, String command, String tooltip) {
        int width = (panelWidth() - 16 - (COLUMNS - 1) * 4) / COLUMNS;
        int column = index % COLUMNS;
        int row = index / COLUMNS;
        Button button = Button.builder(Component.literal(this.font.plainSubstrByWidth(label, width - 6)),
                        pressed -> run("shadowslave dev " + command))
                .bounds(x + 8 + column * (width + 4), y + GRID_TOP + row * (rowHeight + 2), width, rowHeight)
                .tooltip(Tooltip.create(Component.literal(tooltip)))
                .build();
        return addRenderableWidget(button);
    }

    private void action(int x, int y, int width, String label, String command, String tooltip) {
        Button.Builder builder = Button.builder(Component.literal(this.font.plainSubstrByWidth(label, width - 6)), pressed -> run(command))
                .bounds(x, y, width, 16);
        if (!tooltip.isEmpty()) {
            builder.tooltip(Tooltip.create(Component.literal(tooltip)));
        }
        addRenderableWidget(builder.build());
    }

    private static void run(String command) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            connection.sendCommand(command);
        }
    }

    @Override
    public void tick() {
        // The soul changes a moment after a button is pressed; redraw which buttons are held when the sync lands.
        if (ClientSoulState.latest().orElse(null) != shown || ClientCoreState.status() != shownCore) {
            rebuildWidgets();
        }
    }

    private boolean holds(ResourceLocation abilityId) {
        return shown != null && shown.abilityIds().contains(abilityId.toString());
    }

    /** The Nature whose innate rule the soul holds; the innate stays when the keys are swapped. */
    private Optional<AspectKit> currentKit() {
        return AspectKitCatalog.all().stream().filter(kit -> holds(kit.innateId())).findFirst();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        SpellStyle.veil(graphics, this.width, this.height);
        int x = left();
        int y = top();
        graphics.fill(x, y, x + panelWidth(), y + PANEL_HEIGHT, SpellStyle.PANEL);
        graphics.renderOutline(x, y, panelWidth(), PANEL_HEIGHT, SpellStyle.BORDER);
        graphics.fill(x + 6, y + 28, x + panelWidth() - 6, y + 29, SpellStyle.RULE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = left();
        int y = top();
        graphics.pose().pushPose();
        graphics.pose().translate(x + 8, y + 6, 0);
        graphics.pose().scale(1.5F, 1.5F, 1.0F);
        graphics.drawString(this.font, this.title, 0, 0, SpellStyle.TITLE, false);
        graphics.pose().popPose();
        String state = stateLine();
        graphics.drawString(this.font, state, x + panelWidth() - 8 - this.font.width(state), y + 10, SpellStyle.MUTED, false);
        String[] labels = tab == 3 ? PROGRESS_LABELS : tab == 4 ? WORLD_LABELS : new String[0];
        for (int i = 0; i < labels.length; i++) {
            String label = tab == 4 && i == 3 && nightmares.isEmpty() ? "Asking the server for Nightmares..." : labels[i];
            graphics.drawString(this.font, label, x + 8, y + GRID_TOP + i * ROW, SpellStyle.HINT, false);
        }
        graphics.fill(x + 6, y + PANEL_HEIGHT - 18, x + panelWidth() - 6, y + PANEL_HEIGHT - 17, SpellStyle.RULE);
        String hint = "Hover for details · Esc";
        int hintX = x + panelWidth() - 8 - this.font.width(hint);
        graphics.drawString(this.font, hint, hintX, y + PANEL_HEIGHT - 12, SpellStyle.HINT, false);
        String keys = this.font.plainSubstrByWidth(keysLine(), hintX - x - 16);
        graphics.drawString(this.font, keys, x + 8, y + PANEL_HEIGHT - 12, SpellStyle.ABILITY, false);
    }

    private String stateLine() {
        if (shown == null || shown.aspectId().isEmpty()) {
            return "No Aspect yet: pick a Nature";
        }
        String nature = currentKit().map(AspectKit::natureName).orElse("?");
        String stage = shown.spellState().equals(shown.soulRank()) ? titleCase(shown.spellState())
                : titleCase(shown.spellState()) + " (" + titleCase(shown.soulRank()) + ")";
        String core = shownCore.limit() > 0 ? " · core " + shownCore.saturation() + "/" + shownCore.limit() : "";
        return nature + " · " + stage + core;
    }

    private String keysLine() {
        if (shown == null) {
            return "";
        }
        List<String> keys = ClientKeyMappings.abilityKeyNames();
        List<String> names = new ArrayList<>();
        AspectKitCatalog.all().stream().filter(kit -> holds(kit.abilityId())).forEach(kit -> names.add(keys.get(0) + ": " + kit.abilityName()));
        KitLadder.all().stream().filter(variant -> holds(variant.abilityId())).forEach(variant -> names.add(keys.get(1) + ": " + variant.name()));
        return names.isEmpty() ? "" : String.join("  ·  ", names);
    }

    private static String prettify(String id) {
        String path = id.substring(id.indexOf(':') + 1);
        return titleCase(path.substring(path.lastIndexOf('/') + 1).replace('_', ' '));
    }

    private static String titleCase(String text) {
        StringBuilder out = new StringBuilder(text.length());
        boolean start = true;
        for (char c : text.toCharArray()) {
            out.append(start ? Character.toUpperCase(c) : c);
            start = c == ' ';
        }
        return out.toString();
    }
}
