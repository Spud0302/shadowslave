package dev.spud.shadowslave.client;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import dev.spud.shadowslave.ShadowSlaveMod;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Casting poses played through the Player Animation Library. Each kit ability
 * has a short animation in {@code player_animations/ability_casts.json} named
 * {@code cast_<ability>}; it overrides only the bones it keys.
 */
public final class AbilityCastAnimations {
    static final ResourceLocation LAYER = ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "ability_cast");
    /** Above PAL's default layers so a cast reads over idle and walking poses. */
    private static final int PRIORITY = 1500;

    private AbilityCastAnimations() {
    }

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, PRIORITY,
                player -> new PlayerAnimationController(player, (controller, state, setter) -> PlayState.STOP));
    }

    /** The animation id for an ability id such as {@code shadowslave:generation/ability/kindle}. */
    static ResourceLocation animationFor(ResourceLocation abilityId) {
        String path = abilityId.getPath();
        return ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "cast_" + path.substring(path.lastIndexOf('/') + 1));
    }

    public static void play(AbstractClientPlayer player, ResourceLocation abilityId) {
        if (PlayerAnimationAccess.getPlayerAnimationLayer(player, LAYER) instanceof PlayerAnimationController controller) {
            controller.triggerAnimation(animationFor(abilityId));
        }
    }
}
