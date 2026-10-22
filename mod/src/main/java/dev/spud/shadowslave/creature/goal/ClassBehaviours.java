package dev.spud.shadowslave.creature.goal;

import dev.spud.shadowslave.creature.CreatureClass;
import dev.spud.shadowslave.creature.NightmareCreature;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.PathType;

/**
 * The behaviour package each Class adds (DESIGN, from ch. 4 and 51):
 *
 * <ul>
 *   <li>Beast: instinct. Charges, hunts in packs (see
 *       {@link NightmareCreature#setTarget}) and flees fire.</li>
 *   <li>Monster: uses terrain. Ambushes prey that is not looking, retreats
 *       when badly hurt and comes back.</li>
 *   <li>Demon: real tactics. Feints, picks the weakest player, opens doors and
 *       paths around fire, lava and other obvious traps.</li>
 *   <li>Devil: one signature power with a clear counter, through
 *       {@link NightmareCreature#useSignaturePower}.</li>
 *   <li>Tyrant: spawns lesser copies of itself and commands the creatures
 *       around it.</li>
 * </ul>
 *
 * Every goal checks the creature's current Class when it starts, so a copy
 * moved to another Class switches package. Terror and Titan have no package
 * yet.
 */
public final class ClassBehaviours {
    private ClassBehaviours() {
    }

    public static void registerGoals(NightmareCreature creature, GoalSelector goals, GoalSelector targets) {
        goals.addGoal(1, new FleeFireGoal(creature));
        goals.addGoal(1, new RetreatToRecoverGoal(creature));
        goals.addGoal(2, new AmbushLungeGoal(creature));
        goals.addGoal(2, new SignaturePowerGoal(creature));
        goals.addGoal(2, new SpawnLesserCopiesGoal(creature));
        goals.addGoal(3, new CommandNearbyGoal(creature));
        goals.addGoal(3, new OpenDoorGoal(creature, true) {
            @Override
            public boolean canUse() {
                return creature.creatureClass() == CreatureClass.DEMON && super.canUse();
            }
        });
        goals.addGoal(4, new CreatureMeleeGoal(creature));

        targets.addGoal(2, new TargetWeakestPlayerGoal(creature));
        targets.addGoal(3, new NearestAttackableTargetGoal<>(creature, Player.class, true) {
            @Override
            public boolean canUse() {
                return creature.creatureClass() != CreatureClass.DEMON && creature.usesSightTargeting() && super.canUse();
            }
        });
    }

    /** Navigation settings that follow the Class; called whenever Rank or Class changes. */
    public static void configure(NightmareCreature creature) {
        boolean demon = creature.creatureClass() == CreatureClass.DEMON;
        if (creature.getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(demon);
        }
        // Demons see obvious traps and path around them; the rest walk into them like vanilla mobs.
        creature.setPathfindingMalus(PathType.DANGER_FIRE, demon ? 16.0F : 8.0F);
        creature.setPathfindingMalus(PathType.DAMAGE_FIRE, demon ? -1.0F : 16.0F);
        creature.setPathfindingMalus(PathType.DANGER_OTHER, demon ? 16.0F : 8.0F);
        creature.setPathfindingMalus(PathType.DAMAGE_OTHER, demon ? -1.0F : 8.0F);
        creature.setPathfindingMalus(PathType.POWDER_SNOW, demon ? -1.0F : 0.0F);
        creature.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, demon ? 16.0F : 0.0F);
        // Beasts fear fire outright.
        if (creature.creatureClass() == CreatureClass.BEAST) {
            creature.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        }
    }
}
