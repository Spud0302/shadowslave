package dev.spud.shadowslave.world.entity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AshBurrowerSmartBrainIntegrationTest {
    private static final Path ENTITY = Path.of(
            "src/main/java/dev/spud/shadowslave/world/entity/AshBurrowerEntity.java");

    @Test
    void smartBrainOwnsGenericAiExecutionWhileVibrationPolicyStaysProjectOwned() throws IOException {
        String source = Files.readString(ENTITY);

        assertTrue(source.contains("implements GeoEntity, SmartBrainOwner<AshBurrowerEntity>"));
        assertTrue(source.contains("new SmartBrainProvider<>(this)"));
        assertTrue(source.contains("new NearbyPlayersSensor<>()"));
        assertTrue(source.contains("setScanRate(entity -> VIBRATION_SAMPLE_INTERVAL_TICKS)"));
        assertTrue(source.contains("new HurtBySensor<>()"));
        assertTrue(source.contains("BrainActivityGroup.coreTasks"));
        assertTrue(source.contains("BrainActivityGroup.idleTasks"));
        assertTrue(source.contains("BrainActivityGroup.fightTasks"));
        assertTrue(source.contains("new SetWalkTargetToAttackTarget"));
        assertTrue(source.contains("new AnimatableMeleeAttack"));
        assertTrue(source.contains("AshBurrowerVibrationBehavior.detects("));
        assertTrue(source.contains("AshBurrowerVibrationBehavior.shouldReleaseVibrationTarget("));
        assertTrue(source.contains("tickBrain(this)"));
    }

    @Test
    void migrationDoesNotRestoreVanillaSilverfishOrGenericPlayerTargetAuthority() throws IOException {
        String source = Files.readString(ENTITY);

        assertFalse(source.contains("super.registerGoals()"));
        assertFalse(source.contains("MeleeAttackGoal"));
        assertFalse(source.contains("RandomStrollGoal"));
        assertFalse(source.contains("HurtByTargetGoal"));
        assertFalse(source.contains("NearestAttackableTargetGoal"));
        assertFalse(source.contains("getNavigation().moveTo"));
        assertFalse(source.contains("public void tick()"));
        assertTrue(source.contains("new FloatGoal(this)"));
    }
}
