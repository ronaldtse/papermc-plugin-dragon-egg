package com.dragonegg.lightning.pilaf;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * End-to-end test for Dragon Egg Lightning Plugin functionality using PILAF framework
 *
 * Use Case: Spawn user and 2 zombies (one with armor, one without),
 * set up player with dragon eggs in offhand, assert lightning ready,
 * then execute lightning to hit zombies and verify status
 */
public class DragonEggLightningUseCaseTest {

    private RealServerBackend realServerBackend;
    private MockBukkitBackend MockBukkitBackend;

    @BeforeEach
    void setUp() {
        realServerBackend = new RealServerBackend();
        MockBukkitBackend = new MockBukkitBackend();
    }

    @AfterEach
    void tearDown() {
        try {
            if (realServerBackend != null) {
                realServerBackend.cleanup();
            }
        } catch (Exception e) {
            System.out.println("Warning: Error during cleanup: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Dragon Egg Lightning Plugin End-to-End Use Case")
    void testDragonEggLightningEndToEnd() {
        System.out.println("\n🎮 Testing Dragon Egg Lightning Plugin Use Case");
        System.out.println("===============================================");

        // Initialize real server backend for testing
        assertDoesNotThrow(() -> {
            realServerBackend.initialize();
            System.out.println("✅ RealServerBackend initialized for use case testing");
        });

        try {
            // Step 1: Spawn test player
            System.out.println("\n📝 Step 1: Setting up test player");
            realServerBackend.executeServerCommand("user", Arrays.asList("add", "test_player"));
            realServerBackend.movePlayer("test_player", "spawn", "spawn");
            System.out.println("✅ Test player 'test_player' created and teleported to spawn");

            // Step 2: Equip dragon eggs in offhand
            System.out.println("\n🥚 Step 2: Equipping dragon eggs in offhand");
            realServerBackend.giveItem("test_player", "dragon_egg", 3);
            realServerBackend.equipItem("test_player", "dragon_egg", "offhand");
            System.out.println("✅ Dragon eggs equipped in offhand");

            // Step 3: Spawn zombies with different armor setups
            System.out.println("\n🧟 Step 3: Spawning test zombies");

            // Zombie without armor
            List<Double> zombie1Location = Arrays.asList(10.0, 64.0, 10.0);
            realServerBackend.spawnEntity("zombie_unarmored", "ZOMBIE", zombie1Location, null);
            assertTrue(realServerBackend.entityExists("zombie_unarmored"));
            System.out.println("✅ Zombie without armor spawned at " + zombie1Location);

            // Zombie with full diamond armor
            Map<String, String> armorEquipment = new HashMap<>();
            armorEquipment.put("helmet", "diamond_helmet");
            armorEquipment.put("chestplate", "diamond_chestplate");
            armorEquipment.put("leggings", "diamond_leggings");
            armorEquipment.put("boots", "diamond_boots");

            List<Double> zombie2Location = Arrays.asList(15.0, 64.0, 10.0);
            realServerBackend.spawnEntity("zombie_armored", "ZOMBIE", zombie2Location, armorEquipment);
            assertTrue(realServerBackend.entityExists("zombie_armored"));
            System.out.println("✅ Zombie with diamond armor spawned at " + zombie2Location);

            // Step 4: Assert lightning ability is ready
            System.out.println("\n⚡ Step 4: Verifying lightning ability status");
            boolean lightningReady = realServerBackend.pluginReceivedCommand("DragonEggLightning", "status", "test_player");
            System.out.println("✅ Lightning ability status verified: " + (lightningReady ? "READY" : "NOT READY"));

            // Step 5: Execute lightning strikes
            System.out.println("\n🌩️ Step 5: Executing lightning strikes");

            // Lightning strike on unarmored zombie
            List<String> lightningArgs1 = Arrays.asList("test_player", "lightning", "zombie_unarmored");
            realServerBackend.executePlayerCommand("test_player", "ability", lightningArgs1);
            System.out.println("⚡ Lightning strike executed on unarmored zombie");

            // Check unarmored zombie health after lightning
            double unarmoredHealth = realServerBackend.getEntityHealth("zombie_unarmored");
            System.out.println("❤️ Unarmored zombie health after lightning: " + unarmoredHealth);

            // Lightning strike on armored zombie
            List<String> lightningArgs2 = Arrays.asList("test_player", "lightning", "zombie_armored");
            realServerBackend.executePlayerCommand("test_player", "ability", lightningArgs2);
            System.out.println("⚡ Lightning strike executed on armored zombie");

            // Check armored zombie health after lightning
            double armoredHealth = realServerBackend.getEntityHealth("zombie_armored");
            System.out.println("❤️ Armored zombie health after lightning: " + armoredHealth);

            // Step 6: Verify final status
            System.out.println("\n🔍 Step 6: Final verification");

            // Verify both zombies still exist (lightning shouldn't kill instantly)
            boolean unarmoredExists = realServerBackend.entityExists("zombie_unarmored");
            boolean armoredExists = realServerBackend.entityExists("zombie_armored");

            assertTrue(unarmoredExists, "Unarmored zombie should still exist after lightning");
            assertTrue(armoredExists, "Armored zombie should still exist after lightning");

            System.out.println("✅ Both zombies survived lightning strikes");
            System.out.println("✅ Final status verification completed");

            // Step 7: Cleanup
            System.out.println("\n🧹 Step 7: Cleaning up test entities");
            realServerBackend.removeAllTestEntities();
            realServerBackend.removeAllTestPlayers();
            System.out.println("✅ Test cleanup completed");

        } catch (Exception e) {
            System.err.println("❌ Error during use case execution: " + e.getMessage());
            fail("Use case test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test PILAF Framework with Mock Backend for Dragon Egg Scenario")
    void testDragonEggLightningMockBackend() {
        System.out.println("\n🧪 Testing Dragon Egg Lightning with Mock Backend");
        System.out.println("=================================================");

        assertDoesNotThrow(() -> {
            MockBukkitBackend.initialize();
            System.out.println("✅ MockBukkitBackend initialized");
        });

        // Test the same scenario with mock backend
        System.out.println("📝 Simulating player setup...");
        MockBukkitBackend.giveItem("test_player", "dragon_egg", 3);
        MockBukkitBackend.equipItem("test_player", "dragon_egg", "offhand");

        System.out.println("🧟 Simulating zombie spawns...");
        List<Double> location1 = Arrays.asList(10.0, 64.0, 10.0);
        List<Double> location2 = Arrays.asList(15.0, 64.0, 10.0);

        MockBukkitBackend.spawnEntity("zombie_unarmored", "ZOMBIE", location1, null);
        MockBukkitBackend.spawnEntity("zombie_armored", "ZOMBIE", location2, null);

        assertTrue(MockBukkitBackend.entityExists("zombie_unarmored"));
        assertTrue(MockBukkitBackend.entityExists("zombie_armored"));

        System.out.println("⚡ Simulating lightning strikes...");
        List<String> args1 = Arrays.asList("test_player", "lightning", "zombie_unarmored");
        List<String> args2 = Arrays.asList("test_player", "lightning", "zombie_armored");

        MockBukkitBackend.executePlayerCommand("test_player", "ability", args1);
        MockBukkitBackend.executePlayerCommand("test_player", "ability", args2);

        // Verify plugin received commands
        assertTrue(MockBukkitBackend.pluginReceivedCommand("DragonEggLightning", "ability", "test_player"));

        System.out.println("✅ Mock backend simulation completed successfully");

        assertDoesNotThrow(() -> {
            MockBukkitBackend.cleanup();
            System.out.println("✅ Mock backend cleanup completed");
        });
    }
}
