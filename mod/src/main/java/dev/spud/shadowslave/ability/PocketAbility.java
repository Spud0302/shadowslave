package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;

/**
 * Threshold's Dormant ability: open the Hollow Pocket, nine slots of space
 * only you can reach, wherever you stand. Each opening is a door into
 * elsewhere, so Unmoored takes its toll.
 */
public final class PocketAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.THRESHOLD.abilityId();
    static final int ESSENCE_COST = 5;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.THRESHOLD.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        player.openMenu(new SimpleMenuProvider((id, inventory, who) -> new ChestMenu(MenuType.GENERIC_9x1, id, inventory,
                PocketSpace.pocket(player), 1), Component.literal("Hollow Pocket")));
        PocketSpace.doorOpened(player);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.4, 0.3, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8F, 0.6F);
        return Activation.USED;
    }
}
