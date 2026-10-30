package dev.spud.shadowslave.client;

import dev.spud.shadowslave.item.SoulShardItem;
import dev.spud.shadowslave.network.payload.WeaveTracePayload;
import dev.spud.shadowslave.network.payload.WeavingStatusPayload;
import dev.spud.shadowslave.weaving.MemoryWeave;
import dev.spud.shadowslave.weaving.StrandRestriction;
import dev.spud.shadowslave.weaving.StrandRules;
import dev.spud.shadowslave.weaving.StrandText;
import dev.spud.shadowslave.weaving.WeavingMenu;
import dev.spud.shadowslave.weaving.WeavingRegistry;
import dev.spud.shadowslave.weaving.WeavingRules;
import dev.spud.shadowslave.weaving.WovenStrands;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Weaving Table in the Spell look (DESIGN, after Sunny's Weaving). Left:
 * the piece, its soul-shard anchor, the materials, and the Weave view, where
 * the chosen strand's pattern shows as points around the anchor; hold the
 * mouse and trace through them in order. Right: the branch's level and
 * points, what the piece holds or teaches (study by holding, or unravel),
 * the strands you know, and what the last action did. Everything shown comes
 * from the server's Weaving reading; the server judges every trace.
 */
public final class WeavingScreen extends AbstractContainerScreen<WeavingMenu> {
    private static final int LEFT_WIDTH = 176;
    private static final int RIGHT_LEFT = 180;
    private static final int BUTTON_HEIGHT = 14;
    private static final int VIEW_X = 108;
    private static final int VIEW_Y = 62;
    private static final int VIEW_RADIUS = 38;
    private static final int[] TIER_BOX = {48, 102, 92, 102 + BUTTON_HEIGHT};
    private static final int[] TUNE_BOX = {96, 102, 168, 102 + BUTTON_HEIGHT};
    private static final int[] LAY_BOX = {60, 102, 156, 102 + BUTTON_HEIGHT};
    private static final int LIST_TOP = 94;
    private static final int ROW = 11;
    private static final int MESSAGE_TOP = 196;

    private int branch = -1;
    private String selected = "";
    private int tier = 1;
    private int tune = -1;
    private final List<float[]> trace = new ArrayList<>();
    private boolean tracing;
    private int traceFade;
    private int studying = -1;
    private long studyStart;
    /** Fusing two known strands into one of your own (Patterns, high levels): the two picked, in order. */
    private boolean fusing;
    private String fuseFirst = "";
    private String fuseSecond = "";
    private net.minecraft.client.gui.components.EditBox fuseName;
    private static final int[] FUSE_BOX = {60, 102, 156, 102 + BUTTON_HEIGHT};

    public WeavingScreen(WeavingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320;
        this.imageHeight = WeavingMenu.INVENTORY_Y + 82;
        this.inventoryLabelY = WeavingMenu.INVENTORY_Y - 11;
    }

    private WeavingStatusPayload status() {
        return ClientWeavingState.status();
    }

    private WeavingStatusPayload.Branch currentBranch() {
        List<WeavingStatusPayload.Branch> branches = status().branches();
        if (branches.isEmpty()) {
            return null;
        }
        if (branch < 0) {
            // Open on Patterns, the branch the table is for.
            branch = 0;
            for (int i = 0; i < branches.size(); i++) {
                if (branches.get(i).name().equals("Patterns")) {
                    branch = i;
                }
            }
        }
        branch = Math.min(branch, branches.size() - 1);
        return branches.get(branch);
    }

    @Override
    protected void init() {
        super.init();
        fuseName = new net.minecraft.client.gui.components.EditBox(this.font, this.leftPos + 50, this.topPos + 80, 118, 14,
                Component.literal("Name"));
        fuseName.setMaxLength(dev.spud.shadowslave.weaving.StrandFusion.NAME_LENGTH);
        fuseName.setHint(Component.literal("Name it (optional)").withStyle(ChatFormatting.DARK_GRAY));
        fuseName.visible = fusing;
        addRenderableWidget(fuseName);
    }

    private Optional<WeavingStatusPayload.Strand> known(String id) {
        return status().strands().stream().filter(strand -> strand.id().equals(id)).findFirst();
    }

    private int[] fuseToggleBox() {
        int x = RIGHT_LEFT + 8;
        int width = this.imageWidth - RIGHT_LEFT - 16;
        int y = knownTop() - 2;
        return new int[]{x + width - 34, y, x + width, y + 11};
    }

    private void setFusing(boolean on) {
        fusing = on;
        fuseFirst = "";
        fuseSecond = "";
        if (fuseName != null) {
            fuseName.visible = on;
            fuseName.setValue("");
            fuseName.setFocused(false);
        }
        selected = "";
        trace.clear();
    }

    private Optional<WeavingStatusPayload.Strand> chosen() {
        return status().strands().stream().filter(strand -> strand.id().equals(selected)).findFirst();
    }

    /** The highest Tier the chosen strand can be laid at here, counting the anchor in the slot. */
    private int maxTier(WeavingStatusPayload.Strand strand) {
        ItemStack anchor = this.menu.anchor();
        int anchorCap = anchor.getItem() instanceof SoulShardItem shard ? StrandRules.anchorCap(Optional.of(shard.rank())) : 1;
        return Math.max(1, Math.min(strand.maxTier(), Math.min(status().tierCap(), anchorCap)));
    }

    private Optional<StrandRestriction> restriction() {
        return tune < 0 ? Optional.empty() : Optional.of(StrandRestriction.values()[tune]);
    }

    private boolean canTune(WeavingStatusPayload.Strand strand) {
        return strand.tunable() && status().patternsLevel() >= 2;
    }

    private boolean holdsThread() {
        for (int i = WeavingMenu.FIRST_MATERIAL; i < WeavingMenu.OUTPUT_SLOT; i++) {
            if (this.menu.table().getItem(i).is(WeavingRegistry.WOVEN_THREAD.get())) {
                return true;
            }
        }
        return false;
    }

    private WovenStrands centreStrands() {
        WovenStrands strands = this.menu.centre().get(WeavingRegistry.WOVEN_STRANDS.get());
        return strands == null ? WovenStrands.EMPTY : strands;
    }

    /** What the right-hand "In this piece" list shows: the piece's strands, then what it teaches that you don't know. */
    private record PieceRow(int strandIndex, int lessonIndex, Component label) {
    }

    private List<PieceRow> pieceRows() {
        List<PieceRow> rows = new ArrayList<>();
        WovenStrands strands = centreStrands();
        for (int i = 0; i < strands.size() && rows.size() < WeavingMenu.MAX_LISTED; i++) {
            WovenStrands.Laid laid = strands.strands().get(i);
            rows.add(new PieceRow(i, -1, StrandText.name(laid).copy().append(" " + WeavingRules.tierName(laid.tier()))));
        }
        List<WeavingStatusPayload.Lesson> table = status().table();
        for (int i = 0; i < table.size() && rows.size() < WeavingMenu.MAX_LISTED; i++) {
            WeavingStatusPayload.Lesson lesson = table.get(i);
            if (!lesson.known()) {
                rows.add(new PieceRow(-1, i, Component.literal("◇ " + lesson.name()).withStyle(ChatFormatting.GRAY)));
            }
        }
        return rows;
    }

    private int knownTop() {
        List<PieceRow> rows = pieceRows();
        return rows.isEmpty() ? LIST_TOP : LIST_TOP + 12 + rows.size() * ROW + 4;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (traceFade > 0 && !tracing && --traceFade == 0) {
            trace.clear();
        }
        if (studying >= 0 && this.minecraft.level != null
                && this.minecraft.level.getGameTime() - studyStart >= StrandRules.STUDY_TICKS) {
            press(WeavingMenu.BUTTON_STUDIED + studying);
            studying = -1;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderListTooltip(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        SpellStyle.panel(graphics, x, y, x + LEFT_WIDTH, y + this.imageHeight, SpellStyle.RULE);
        SpellStyle.panel(graphics, x + RIGHT_LEFT, y, x + this.imageWidth, y + this.imageHeight, SpellStyle.RULE);
        for (Slot slot : this.menu.slots) {
            if (slot.index >= WeavingMenu.TABLE_SLOTS && slot.container != this.menu.table()) {
                graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, 0xE0080610);
                graphics.renderOutline(x + slot.x - 2, y + slot.y - 2, 20, 20, SpellStyle.RULE);
                continue;
            }
            int colour = slot.index == WeavingMenu.ANCHOR_SLOT ? SpellStyle.ASPECT
                    : slot.index == WeavingMenu.CENTRE_SLOT ? SpellStyle.ABILITY
                    : slot.index == WeavingMenu.OUTPUT_SLOT ? SpellStyle.withAlpha(SpellStyle.ASPECT, 0.6F) : SpellStyle.RULE;
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, 0xE0080610);
            graphics.renderOutline(x + slot.x - 2, y + slot.y - 2, 20, 20, colour);
        }
        graphics.drawString(this.font, "Piece", x + WeavingMenu.CENTRE_X - 1, y + WeavingMenu.CENTRE_Y + 20, SpellStyle.HINT, false);
        graphics.drawString(this.font, "Anchor", x + WeavingMenu.ANCHOR_X - 4, y + WeavingMenu.ANCHOR_Y + 20, SpellStyle.HINT, false);
        renderView(graphics, mouseX, mouseY);
        graphics.fill(x + 8, y + WeavingMenu.INVENTORY_Y - 14, x + LEFT_WIDTH - 8, y + WeavingMenu.INVENTORY_Y - 13, SpellStyle.RULE);
        renderBranch(graphics, mouseX, mouseY);
    }

    /** The anchor ring, the chosen strand's points, the tolerance around the next one, and the trace so far. */
    private void renderView(GuiGraphics graphics, int mouseX, int mouseY) {
        int cx = this.leftPos + VIEW_X;
        int cy = this.topPos + VIEW_Y;
        long time = this.minecraft.level == null ? 0 : this.minecraft.level.getGameTime();
        if (fusing) {
            renderFuse(graphics, mouseX, mouseY);
            return;
        }
        if (bindingRemnant()) {
            renderBinding(graphics, mouseX, mouseY, time);
            return;
        }
        ring(graphics, cx, cy, VIEW_RADIUS, SpellStyle.withAlpha(SpellStyle.RULE, 0.9F));
        ring(graphics, cx, cy, VIEW_RADIUS - 1, SpellStyle.withAlpha(SpellStyle.BORDER, 0.35F));
        boolean anchored = !this.menu.anchor().isEmpty();
        int glow = anchored ? SpellStyle.ASPECT : SpellStyle.HINT;
        float pulse = 0.35F + 0.25F * (float) Math.sin(time * 0.2F);
        graphics.fill(cx - 3, cy - 3, cx + 3, cy + 3, SpellStyle.withAlpha(glow, anchored ? 0.4F + pulse : 0.3F));
        graphics.fill(cx - 1, cy - 1, cx + 1, cy + 1, SpellStyle.withAlpha(glow, 0.95F));

        if (holdsThread()) {
            renderButton(graphics, mouseX, mouseY, LAY_BOX, "Lay the thread", true);
        }
        Optional<WeavingStatusPayload.Strand> chosen = chosen();
        if (chosen.isEmpty()) {
            if (!holdsThread()) {
                graphics.drawCenteredString(this.font, "Choose a strand", cx, this.topPos + 106, SpellStyle.HINT);
            }
            renderTrace(graphics);
            return;
        }
        WeavingStatusPayload.Strand strand = chosen.get();
        tier = Mth.clamp(tier, 1, maxTier(strand));
        if (!canTune(strand)) {
            tune = -1;
        }
        List<Float> points = strand.points();
        int count = points.size() / 2;
        int reached = reached(strand);
        float tolerance = StrandRules.tolerance(status().patternsLevel(), tier) * VIEW_RADIUS;
        for (int i = 0; i < count; i++) {
            int px = cx + Math.round(points.get(i * 2) * VIEW_RADIUS);
            int py = cy + Math.round(points.get(i * 2 + 1) * VIEW_RADIUS);
            if (i > 0) {
                int lx = cx + Math.round(points.get(i * 2 - 2) * VIEW_RADIUS);
                int ly = cy + Math.round(points.get(i * 2 - 1) * VIEW_RADIUS);
                line(graphics, lx, ly, px, py, SpellStyle.withAlpha(SpellStyle.ABILITY, i <= reached ? 0.55F : 0.18F), 1);
            }
            boolean next = i == reached;
            if (next) {
                ring(graphics, px, py, Math.max(2, Math.round(tolerance)), SpellStyle.withAlpha(SpellStyle.ASPECT, 0.45F + pulse * 0.5F));
            }
            int colour = i < reached ? SpellStyle.ASPECT : next ? SpellStyle.TEXT : SpellStyle.ABILITY;
            graphics.fill(px - 2, py - 2, px + 2, py + 2, colour);
            graphics.drawString(this.font, Integer.toString(i + 1), px + 3, py - 9, SpellStyle.withAlpha(colour, 0.8F), false);
        }
        renderTrace(graphics);
        if (!holdsThread()) {
            renderButton(graphics, mouseX, mouseY, TIER_BOX, "Tier " + WeavingRules.tierName(tier), maxTier(strand) > 1);
            String tuneLabel = restriction().map(value -> value.getSerializedName().substring(0, 1).toUpperCase()
                    + value.getSerializedName().substring(1)).orElse("Untuned");
            renderButton(graphics, mouseX, mouseY, TUNE_BOX, tuneLabel, canTune(strand));
            String cost = costText(strand, tier);
            int line = 0;
            for (FormattedCharSequence text : this.font.split(Component.literal(cost), 96)) {
                if (line++ < 2) {
                    graphics.drawString(this.font, text, this.leftPos + 50, this.topPos + 119 + (line - 1) * 9, SpellStyle.MUTED, false);
                }
            }
        }
    }

    private static final int[] BIND_BOX = {60, 102, 156, 102 + BUTTON_HEIGHT};

    /** An echo remnant in the piece slot turns the view into the Binding view. */
    private boolean bindingRemnant() {
        return this.menu.centre().has(dev.spud.shadowslave.echoes.EchoesRegistry.REMNANT_OF.get());
    }

    /**
     * The Binding view: the creature's echo circling the anchor, what binding
     * it takes, and the button that binds it. The server words the needs.
     */
    private void renderBinding(GuiGraphics graphics, int mouseX, int mouseY, long time) {
        int cx = this.leftPos + VIEW_X;
        int cy = this.topPos + 42;
        Optional<dev.spud.shadowslave.network.payload.EchoStatusPayload.Binding> binding = ClientEchoState.status().binding();
        boolean ready = binding.map(value -> value.lock().isEmpty()).orElse(false);
        float pulse = 0.5F + 0.5F * (float) Math.sin(time * 0.15F);
        ring(graphics, cx, cy, 24, SpellStyle.withAlpha(SpellStyle.ASPECT, 0.35F + 0.3F * pulse));
        ring(graphics, cx, cy, 17, SpellStyle.withAlpha(SpellStyle.ASPECT, 0.2F));
        for (int i = 0; i < 6; i++) {
            double angle = time * 0.06 + Math.PI * 2 * i / 6;
            int px = cx + (int) Math.round(Math.cos(angle) * 21);
            int py = cy + (int) Math.round(Math.sin(angle) * 21);
            graphics.fill(px - 1, py - 1, px + 2, py + 2, SpellStyle.withAlpha(SpellStyle.ASPECT, 0.85F));
        }
        graphics.renderItem(this.menu.centre(), cx - 8, cy - 8);
        if (binding.isEmpty()) {
            graphics.drawCenteredString(this.font, "Reading the echo…", cx, this.topPos + 80, SpellStyle.HINT);
            return;
        }
        var value = binding.get();
        int x = this.leftPos + 50;
        int right = this.leftPos + 168;
        graphics.drawCenteredString(this.font, fit(value.name(), 118), cx, this.topPos + 70, SpellStyle.TITLE);
        graphics.drawString(this.font, "Binding " + value.held() + "/" + value.level(), x, this.topPos + 81,
                value.held() >= value.level() ? SpellStyle.ASPECT : SpellStyle.FLAW, false);
        String essence = value.essence() + " essence";
        graphics.drawString(this.font, essence, right - this.font.width(essence), this.topPos + 81, SpellStyle.MUTED, false);
        graphics.drawString(this.font, fit("Anchor: " + value.anchor() + " shard", 118), x, this.topPos + 91, SpellStyle.MUTED, false);
        renderButton(graphics, mouseX, mouseY, BIND_BOX, "Bind the Echo", ready);
        int line = 0;
        for (FormattedCharSequence text : this.font.split(Component.literal(ready ? value.rank() + " Echo. It never grows."
                : value.lock()), 96)) {
            if (line++ < 3) {
                graphics.drawString(this.font, text, x, this.topPos + 119 + (line - 1) * 9, ready ? SpellStyle.HINT : SpellStyle.FLAW, false);
            }
        }
    }

    /** "30 essence · 1 × Chainback Link, 1 × Mimic Husk" for one Tier. */
    private static String costText(WeavingStatusPayload.Strand strand, int tier) {
        if (strand.costs().size() < tier) {
            return strand.essence().get(tier - 1) + " essence · " + strand.counts().get(tier - 1) + " × " + SoulScreen.itemName(strand.material());
        }
        WeavingStatusPayload.Cost cost = strand.costs().get(tier - 1);
        return cost.essence() + " essence · " + String.join(", ", cost.materials().stream()
                .map(material -> material.count() + " × " + SoulScreen.itemName(material.item())).toList());
    }

    /** The fusing view: the two strands picked from the list, a name, and the button that fuses them. */
    private void renderFuse(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + 50;
        int y = this.topPos + 22;
        graphics.drawString(this.font, "Fuse two strands", x, y, SpellStyle.TITLE, false);
        y += 12;
        String first = known(fuseFirst).map(WeavingStatusPayload.Strand::name).orElse("Pick a strand you know");
        String second = known(fuseSecond).map(WeavingStatusPayload.Strand::name).orElse(fuseFirst.isEmpty() ? "…then another" : "Pick a second strand");
        graphics.drawString(this.font, fit("1  " + first, 118), x, y, fuseFirst.isEmpty() ? SpellStyle.HINT : SpellStyle.ABILITY, false);
        y += 11;
        graphics.drawString(this.font, fit("2  " + second, 118), x, y, fuseSecond.isEmpty() ? SpellStyle.HINT : SpellStyle.ABILITY, false);
        y += 13;
        graphics.drawString(this.font, "Each part at " + status().createShare() + "%", x, y, SpellStyle.MUTED, false);
        boolean ready = !fuseFirst.isEmpty() && !fuseSecond.isEmpty();
        renderButton(graphics, mouseX, mouseY, FUSE_BOX, "Fuse", ready);
        graphics.drawString(this.font, "Costs " + dev.spud.shadowslave.weaving.StrandFusion.CREATE_ESSENCE + " essence,",
                this.leftPos + 50, this.topPos + 119, SpellStyle.MUTED, false);
        graphics.drawString(this.font, "then trace it", this.leftPos + 50, this.topPos + 128, SpellStyle.HINT, false);
    }

    /** How many of the chosen strand's points the trace has passed in order, as the server will count them. */
    private int reached(WeavingStatusPayload.Strand strand) {
        if (trace.isEmpty()) {
            return 0;
        }
        List<Float> points = strand.points();
        float tolerance = StrandRules.tolerance(status().patternsLevel(), tier) * 1.5F;
        int reached = 0;
        for (float[] sample : trace) {
            if (reached * 2 >= points.size()) {
                break;
            }
            if (Math.hypot(sample[0] - points.get(reached * 2), sample[1] - points.get(reached * 2 + 1)) <= tolerance) {
                reached++;
            }
        }
        return reached;
    }

    private void renderTrace(GuiGraphics graphics) {
        int cx = this.leftPos + VIEW_X;
        int cy = this.topPos + VIEW_Y;
        float alpha = tracing ? 0.95F : Math.min(0.95F, traceFade / 20.0F);
        for (int i = 1; i < trace.size(); i++) {
            float[] a = trace.get(i - 1);
            float[] b = trace.get(i);
            line(graphics, cx + Math.round(a[0] * VIEW_RADIUS), cy + Math.round(a[1] * VIEW_RADIUS),
                    cx + Math.round(b[0] * VIEW_RADIUS), cy + Math.round(b[1] * VIEW_RADIUS), SpellStyle.withAlpha(SpellStyle.ASPECT, alpha), 2);
        }
    }

    private static void ring(GuiGraphics graphics, int cx, int cy, int radius, int colour) {
        int steps = Math.max(24, radius * 6);
        for (int i = 0; i < steps; i++) {
            double angle = Math.PI * 2 * i / steps;
            int px = cx + (int) Math.round(Math.cos(angle) * radius);
            int py = cy + (int) Math.round(Math.sin(angle) * radius);
            graphics.fill(px, py, px + 1, py + 1, colour);
        }
    }

    private static void line(GuiGraphics graphics, int x0, int y0, int x1, int y1, int colour, int width) {
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        for (int i = 0; i <= steps; i++) {
            float t = steps == 0 ? 0.0F : i / (float) steps;
            int px = Math.round(x0 + (x1 - x0) * t);
            int py = Math.round(y0 + (y1 - y0) * t);
            graphics.fill(px, py, px + width, py + width, colour);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 7, SpellStyle.TITLE, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, SpellStyle.MUTED, false);
        int x = RIGHT_LEFT + 8;
        int width = this.imageWidth - RIGHT_LEFT - 16;
        String last = status().lastResult();
        int y = MESSAGE_TOP;
        graphics.fill(x, y - 4, x + width, y - 3, SpellStyle.RULE);
        if (last.isEmpty()) {
            last = this.menu.centre().isEmpty()
                    ? "Lay a piece in the centre and a soul shard as its anchor. Study what a weave holds; trace what you know."
                    : "Hold Study to learn from its weave, or trace a strand you know.";
        }
        for (FormattedCharSequence line : this.font.split(Component.literal(last), width)) {
            if (y > this.imageHeight - 10) {
                break;
            }
            graphics.drawString(this.font, line, x, y, status().lastResult().isEmpty() ? SpellStyle.HINT : SpellStyle.VOICE, false);
            y += 9;
        }
    }

    private void renderBranch(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + RIGHT_LEFT + 8;
        int y = this.topPos + 7;
        int width = this.imageWidth - RIGHT_LEFT - 16;
        WeavingStatusPayload status = status();
        WeavingStatusPayload.Branch current = currentBranch();
        if (current == null) {
            graphics.drawString(this.font, "No Weaving branches are loaded.", x, y, SpellStyle.MUTED, false);
            return;
        }
        graphics.drawString(this.font, status.branches().size() > 1 ? "‹ " + current.name() + " ›" : current.name(), x, y, SpellStyle.TITLE, false);
        String level = "Level " + current.level() + "/" + current.maxLevel();
        graphics.drawString(this.font, level, x + width - this.font.width(level), y, SpellStyle.ASPECT, false);
        y += 12;
        graphics.drawString(this.font, current.xp() + " XP  ·  " + status.points() + (status.points() == 1 ? " point" : " points"),
                x, y, SpellStyle.TEXT, false);
        y += 10;
        graphics.fill(x, y, x + width, y + 4, SpellStyle.PANEL);
        graphics.renderOutline(x, y, width, 4, SpellStyle.RULE);
        int filled = Math.round((width - 2) * status.xpTowardPoint() / (float) Math.max(1, status.xpPerPoint()));
        if (filled > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + filled, y + 3, SpellStyle.ABILITY);
        }
        y += 7;
        int lines = 0;
        for (FormattedCharSequence line : this.font.split(Component.literal(current.next()), width)) {
            if (lines++ < 3) {
                graphics.drawString(this.font, line, x, y, SpellStyle.MUTED, false);
                y += 9;
            }
        }
        renderButton(graphics, mouseX, mouseY, raiseBox(), "Raise", current.canRaise());
        renderButton(graphics, mouseX, mouseY, respecBox(), "Respec", status.canRespec());

        int listY = this.topPos + LIST_TOP;
        List<PieceRow> rows = pieceRows();
        if (!rows.isEmpty()) {
            graphics.drawString(this.font, "In this piece", x, listY, SpellStyle.ASPECT, false);
            for (int i = 0; i < rows.size(); i++) {
                PieceRow row = rows.get(i);
                int rowY = listY + 12 + i * ROW;
                boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= rowY - 1 && mouseY < rowY + ROW - 1;
                if (hovered) {
                    graphics.fill(x - 2, rowY - 2, x + width + 2, rowY + ROW - 2, SpellStyle.withAlpha(SpellStyle.BORDER, 0.25F));
                }
                if (row.lessonIndex() >= 0 && studying == row.lessonIndex() && this.minecraft.level != null) {
                    float progress = Math.min(1.0F, (this.minecraft.level.getGameTime() - studyStart + 0.0F) / StrandRules.STUDY_TICKS);
                    graphics.fill(x - 2, rowY + ROW - 3, x - 2 + Math.round((width + 4) * progress), rowY + ROW - 2, SpellStyle.ASPECT);
                }
                String action = row.strandIndex() < 0 ? "Study" : unravelLabel(row.strandIndex());
                graphics.drawString(this.font, fit(row.label().getString(), width - this.font.width(action) - 6), x, rowY,
                        row.strandIndex() >= 0 ? SpellStyle.ABILITY : SpellStyle.MUTED, false);
                graphics.drawString(this.font, action, x + width - this.font.width(action), rowY,
                        hovered ? SpellStyle.TEXT : SpellStyle.HINT, false);
            }
        }
        int knownY = this.topPos + knownTop();
        graphics.drawString(this.font, "Strands you know", x, knownY, SpellStyle.ASPECT, false);
        if (status.createShare() > 0) {
            int[] box = fuseToggleBox();
            boolean over = inside(mouseX, mouseY, box);
            String label = fusing ? "Done" : "Fuse";
            graphics.drawString(this.font, label, this.leftPos + box[2] - this.font.width(label), this.topPos + box[1] + 2,
                    over || fusing ? SpellStyle.TEXT : SpellStyle.HINT, false);
        }
        List<WeavingStatusPayload.Strand> known = status.strands();
        for (int i = 0; i < known.size(); i++) {
            WeavingStatusPayload.Strand strand = known.get(i);
            int rowY = knownY + 12 + i * ROW;
            if (rowY > this.topPos + MESSAGE_TOP - 14) {
                break;
            }
            boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= rowY - 1 && mouseY < rowY + ROW - 1;
            boolean picked = fusing ? strand.id().equals(fuseFirst) || strand.id().equals(fuseSecond) : strand.id().equals(selected);
            if (hovered || picked) {
                graphics.fill(x - 2, rowY - 2, x + width + 2, rowY + ROW - 2,
                        SpellStyle.withAlpha(picked ? SpellStyle.ASPECT : SpellStyle.BORDER, picked ? 0.2F : 0.25F));
            }
            boolean dim = !strand.lock().isEmpty() || (fusing && strand.own());
            graphics.drawString(this.font, fit((strand.own() ? "✦ " : "") + strand.name(), width - 24), x, rowY,
                    dim ? SpellStyle.HINT : strand.own() ? SpellStyle.ASPECT : SpellStyle.TEXT, false);
            int reach = Math.max(1, Math.min(strand.maxTier(), status.tierCap()));
            String tiers = reach == 1 ? "I" : "I-" + WeavingRules.tierName(reach);
            graphics.drawString(this.font, tiers, x + width - this.font.width(tiers), rowY, SpellStyle.ABILITY, false);
        }
        if (known.isEmpty()) {
            graphics.drawString(this.font, "None yet. Study a weave.", x, knownY + 12, SpellStyle.HINT, false);
        }
    }

    /** The Memory weave is undone and Bound is cut; every other strand unravels into a thread. */
    private String unravelLabel(int strandIndex) {
        ResourceLocation id = centreStrands().strands().get(strandIndex).id();
        return id.equals(MemoryWeave.BOUND) ? "Unbind" : id.equals(MemoryWeave.MEMORY_WEAVE) ? "Undo" : "Unravel";
    }

    /** {@code text} cut to {@code width} pixels, with an ellipsis if it had to be cut. */
    private String fit(String text, int width) {
        if (this.font.width(text) <= width) {
            return text;
        }
        return this.font.plainSubstrByWidth(text, width - this.font.width("…")) + "…";
    }

    private void renderListTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + RIGHT_LEFT + 8;
        int width = this.imageWidth - RIGHT_LEFT - 16;
        if (mouseX < x || mouseX >= x + width) {
            return;
        }
        int knownY = this.topPos + knownTop();
        List<WeavingStatusPayload.Strand> known = status().strands();
        for (int i = 0; i < known.size(); i++) {
            int rowY = knownY + 12 + i * ROW;
            if (mouseY >= rowY - 1 && mouseY < rowY + ROW - 1 && rowY <= this.topPos + MESSAGE_TOP - 14) {
                WeavingStatusPayload.Strand strand = known.get(i);
                List<Component> lines = new ArrayList<>();
                lines.add(Component.literal(strand.name()).withStyle(ChatFormatting.LIGHT_PURPLE));
                lines.add(Component.literal(strand.description()).withStyle(ChatFormatting.GRAY));
                lines.add(Component.literal("Suits " + strand.fits()).withStyle(ChatFormatting.AQUA));
                if (strand.own()) {
                    lines.add(Component.literal("Fused: " + strand.fused()).withStyle(ChatFormatting.DARK_PURPLE));
                }
                for (int tierIndex = 0; tierIndex < strand.maxTier(); tierIndex++) {
                    lines.add(Component.literal("Tier " + WeavingRules.tierName(tierIndex + 1) + ": " + costText(strand, tierIndex + 1))
                            .withStyle(ChatFormatting.DARK_AQUA));
                }
                if (!strand.lock().isEmpty()) {
                    lines.add(Component.literal(strand.lock()).withStyle(ChatFormatting.RED));
                }
                if (strand.laid() > 0) {
                    lines.add(Component.literal("Laid " + strand.laid() + (strand.laid() == 1 ? " time" : " times")).withStyle(ChatFormatting.DARK_GRAY));
                }
                graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
                return;
            }
        }
    }

    private int[] raiseBox() {
        int x = RIGHT_LEFT + 8;
        int width = (this.imageWidth - RIGHT_LEFT - 16 - 6) / 2;
        return new int[]{x, 74, x + width, 74 + BUTTON_HEIGHT};
    }

    private int[] respecBox() {
        int[] raise = raiseBox();
        int width = raise[2] - raise[0];
        return new int[]{raise[2] + 6, raise[1], raise[2] + 6 + width, raise[3]};
    }

    private void renderButton(GuiGraphics graphics, int mouseX, int mouseY, int[] box, String label, boolean enabled) {
        int x0 = this.leftPos + box[0];
        int y0 = this.topPos + box[1];
        int x1 = this.leftPos + box[2];
        int y1 = this.topPos + box[3];
        boolean hovered = enabled && mouseX >= x0 && mouseX < x1 && mouseY >= y0 && mouseY < y1;
        graphics.fill(x0, y0, x1, y1, hovered ? SpellStyle.withAlpha(SpellStyle.BORDER, 0.5F) : SpellStyle.PANEL);
        graphics.renderOutline(x0, y0, x1 - x0, y1 - y0, enabled ? SpellStyle.BORDER : SpellStyle.RULE);
        graphics.drawCenteredString(this.font, label, (x0 + x1) / 2, y0 + 3, enabled ? SpellStyle.TEXT : SpellStyle.HINT);
    }

    private boolean inside(double mouseX, double mouseY, int[] box) {
        return mouseX >= this.leftPos + box[0] && mouseX < this.leftPos + box[2]
                && mouseY >= this.topPos + box[1] && mouseY < this.topPos + box[3];
    }

    private float[] toAnchor(double mouseX, double mouseY) {
        return new float[]{(float) ((mouseX - this.leftPos - VIEW_X) / VIEW_RADIUS), (float) ((mouseY - this.topPos - VIEW_Y) / VIEW_RADIUS)};
    }

    private boolean inView(double mouseX, double mouseY) {
        float[] point = toAnchor(mouseX, mouseY);
        return Math.hypot(point[0], point[1]) <= 1.15;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && status().createShare() > 0 && inside(mouseX, mouseY, fuseToggleBox())) {
            setFusing(!fusing);
            click();
            return true;
        }
        if (fusing && button == 0) {
            if (inside(mouseX, mouseY, FUSE_BOX) && !fuseFirst.isEmpty() && !fuseSecond.isEmpty()) {
                click();
                PacketDistributor.sendToServer(new dev.spud.shadowslave.network.payload.StrandCreatePayload(fuseFirst, fuseSecond,
                        fuseName.getValue()));
                setFusing(false);
                return true;
            }
            int knownY = this.topPos + knownTop();
            List<WeavingStatusPayload.Strand> known = status().strands();
            int x = this.leftPos + RIGHT_LEFT + 8;
            int width = this.imageWidth - RIGHT_LEFT - 16;
            for (int i = 0; i < known.size() && mouseX >= x - 2 && mouseX < x + width + 2; i++) {
                int rowY = knownY + 12 + i * ROW;
                if (mouseY >= rowY - 1 && mouseY < rowY + ROW - 1) {
                    pickForFuse(known.get(i));
                    return true;
                }
            }
            if (fuseName.isMouseOver(mouseX, mouseY)) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            fuseName.setFocused(false);
        }
        if (button == 0 && !fusing && bindingRemnant() && inside(mouseX, mouseY, BIND_BOX)) {
            if (ClientEchoState.status().binding().map(value -> value.lock().isEmpty()).orElse(false)) {
                press(WeavingMenu.BUTTON_BIND);
            }
            return true;
        }
        WeavingStatusPayload.Branch current = currentBranch();
        Optional<WeavingStatusPayload.Strand> chosen = chosen();
        if (button == 0 && holdsThread() && inside(mouseX, mouseY, LAY_BOX)) {
            press(WeavingMenu.BUTTON_LAY);
            return true;
        }
        if (button == 0 && chosen.isPresent() && !holdsThread()) {
            if (inside(mouseX, mouseY, TIER_BOX)) {
                int max = maxTier(chosen.get());
                tier = tier >= max ? 1 : tier + 1;
                click();
                return true;
            }
            if (canTune(chosen.get()) && inside(mouseX, mouseY, TUNE_BOX)) {
                tune = tune + 1 >= StrandRestriction.values().length ? -1 : tune + 1;
                click();
                return true;
            }
        }
        if (button == 0 && chosen.isPresent() && inView(mouseX, mouseY)) {
            tracing = true;
            trace.clear();
            trace.add(toAnchor(mouseX, mouseY));
            return true;
        }
        if (current != null && current.canRaise() && inside(mouseX, mouseY, raiseBox())) {
            press(WeavingMenu.BUTTON_RAISE + branch);
            return true;
        }
        if (status().canRespec() && inside(mouseX, mouseY, respecBox())) {
            press(WeavingMenu.BUTTON_RESPEC);
            return true;
        }
        int x = this.leftPos + RIGHT_LEFT + 8;
        int width = this.imageWidth - RIGHT_LEFT - 16;
        if (mouseX >= x - 2 && mouseX < x + width + 2) {
            List<PieceRow> rows = pieceRows();
            for (int i = 0; i < rows.size(); i++) {
                int rowY = this.topPos + LIST_TOP + 12 + i * ROW;
                if (mouseY >= rowY - 1 && mouseY < rowY + ROW - 1) {
                    PieceRow row = rows.get(i);
                    if (row.strandIndex() >= 0) {
                        press(WeavingMenu.BUTTON_UNRAVEL + row.strandIndex());
                    } else {
                        studying = row.lessonIndex();
                        studyStart = this.minecraft.level == null ? 0 : this.minecraft.level.getGameTime();
                        press(WeavingMenu.BUTTON_STUDY + row.lessonIndex());
                    }
                    return true;
                }
            }
            int knownY = this.topPos + knownTop();
            List<WeavingStatusPayload.Strand> known = status().strands();
            for (int i = 0; i < known.size(); i++) {
                int rowY = knownY + 12 + i * ROW;
                if (mouseY >= rowY - 1 && mouseY < rowY + ROW - 1) {
                    selected = known.get(i).id().equals(selected) ? "" : known.get(i).id();
                    trace.clear();
                    click();
                    return true;
                }
            }
        }
        int branches = status().branches().size();
        if (branches > 1 && mouseX >= this.leftPos + RIGHT_LEFT && mouseY >= this.topPos + 4 && mouseY < this.topPos + 18) {
            branch = (branch + 1) % branches;
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (tracing) {
            float[] point = toAnchor(mouseX, mouseY);
            float[] last = trace.getLast();
            if (Math.hypot(point[0] - last[0], point[1] - last[1]) * VIEW_RADIUS >= 1.5 && trace.size() < StrandRules.TRACE_LIMIT) {
                trace.add(point);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (studying >= 0) {
            // Letting go before the pattern is picked out ends the study.
            studying = -1;
        }
        if (tracing && button == 0) {
            tracing = false;
            traceFade = 30;
            Optional<WeavingStatusPayload.Strand> chosen = chosen();
            if (chosen.isPresent() && trace.size() >= 3) {
                List<Float> samples = new ArrayList<>();
                for (float[] point : trace) {
                    samples.add(point[0]);
                    samples.add(point[1]);
                }
                PacketDistributor.sendToServer(new WeaveTracePayload(chosen.get().id(), tier,
                        restriction().map(StrandRestriction::getSerializedName).orElse(""), samples));
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /** Picks a strand for the fusion: the first click fills the first part, the next the second; clicking a picked one drops it. */
    private void pickForFuse(WeavingStatusPayload.Strand strand) {
        if (strand.own()) {
            return;
        }
        if (strand.id().equals(fuseFirst)) {
            fuseFirst = fuseSecond;
            fuseSecond = "";
        } else if (strand.id().equals(fuseSecond)) {
            fuseSecond = "";
        } else if (fuseFirst.isEmpty()) {
            fuseFirst = strand.id();
        } else {
            fuseSecond = strand.id();
        }
        click();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (fusing && fuseName.isFocused()) {
            if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                fuseName.setFocused(false);
                return true;
            }
            return fuseName.keyPressed(keyCode, scanCode, modifiers) || fuseName.canConsumeInput() || super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** Lets the screenshot rig open the fusing view with two strands picked and a name typed. */
    void fuseForTest(String first, String second, String name) {
        setFusing(true);
        fuseFirst = first;
        fuseSecond = second;
        fuseName.setValue(name);
    }

    /** Lets the screenshot rig and tests draw a trace without a mouse. */
    void traceForTest(List<float[]> points) {
        trace.clear();
        trace.addAll(points);
        tracing = false;
        traceFade = 400;
    }

    void selectForTest(String id, int tier, int tune) {
        setFusing(false);
        this.selected = id;
        this.tier = tier;
        this.tune = tune;
    }

    private void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void press(int id) {
        click();
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }
}
