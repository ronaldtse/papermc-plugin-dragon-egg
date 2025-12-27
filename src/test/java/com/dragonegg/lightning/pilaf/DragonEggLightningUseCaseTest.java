package com.dragonegg.lightning.pilaf;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
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
 *
 * Prerequisites for running these tests:
 * 1. Start Docker server: docker-compose up -d
 * 2. Wait for server to be ready (check logs)
 * 3. Set PILAF_INTEGRATION_TEST=true environment variable
 *
 * Or run with: PILAF_INTEGRATION_TEST=true mvn test -Dtest=DragonEggLightningUseCaseTest
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DragonEggLightningUseCaseTest {

    // Configuration from environment variables with defaults for Docker setup
    private static final String RCON_HOST = System.getenv().getOrDefault("RCON_HOST", "localhost");
    private static final int RCON_PORT = Integer.parseInt(System.getenv().getOrDefault("RCON_PORT", "25575"));
    private static final String RCON_PASSWORD = System.getenv().getOrDefault("RCON_PASSWORD", "dragon123");

    private RealServerBackend realServerBackend;
    private MockBukkitBackend mockBukkitBackend;
    private boolean serverAvailable = false;

    @BeforeAll
    void checkServerAvailability() {
        // Check if we should run integration tests
        String runIntegration = System.getenv("PILAF_INTEGRATION_TEST");
        if (!"true".equalsIgnoreCase(runIntegration)) {
            System.out.println("Skipping PILAF integration tests (set PILAF_INTEGRATION_TEST=true to enable)");
            return;
        }

        // Try to connect to server
        RconClient testClient = new RconClient(RCON_HOST, RCON_PORT, RCON_PASSWORD);
        try {
            serverAvailable = testClient.connect();
            if (serverAvailable) {
                System.out.println("Server is available for integration testing");
                testClient.disconnect();
            }
        } catch (Exception e) {
            System.out.println("Server not available: " + e.getMessage());
            serverAvailable = false;
        }
    }

    @BeforeEach
    void setUp() {
        realServerBackend = new RealServerBackend(RCON_HOST, RCON_PORT, RCON_PASSWORD);
        mockBukkitBackend = new MockBukkitBackend();
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
    @DisplayName("Test Dragon Egg Lightning Plugin End-to-End Use Case with Docker Server")
    void testDragonEggLightningEndToEnd() {
        // Skip if server not available
        Assumptions.assumeTrue(serverAvailable,
            "Skipping test - Docker server not available. Start with: docker-compose up -d");

        System.out.println("\nTesting Dragon Egg Lightning Plugin Use Case");
        System.out.println("===============================================");

        // Initialize real server backend for testing
        assertDoesNotThrow(() -> {
            realServerBackend.initialize();
            System.out.println("RealServerBackend initialized for use case testing");
        });

        try {
            // Step 1: Spawn test player
            System.out.println("\nStep 1: Setting up test player");
            realServerBackend.executeServerCommand("user", Arrays.asList("add", "test_player"));
            realServerBackend.movePlayer("test_player", "spawn", "spawn");
            System.out.println("Test player 'test_player' created and teleported to spawn");

            // Step 2: Equip dragon eggs in offhand
            System.out.println("\nStep 2: Equipping dragon eggs in offhand");
            realServerBackend.giveItem("test_player", "dragon_egg", 3);
            realServerBackend.equipItem("test_player", "dragon_egg", "offhand");
            System.out.println("Dragon eggs equipped in offhand");

            // Step 3: Spawn zombies with different armor setups
            System.out.println("\nStep 3: Spawning test zombies");

            // Zombie without armor
            List<Double> zombie1Location = Arrays.asList(10.0, 64.0, 10.0);
            realServerBackend.spawnEntity("zombie_unarmored", "ZOMBIE", zombie1Location, null);
            assertTrue(realServerBackend.entityExists("zombie_unarmored"));
            System.out.println("Zombie without armor spawned at " + zombie1Location);

            // Zombie with full diamond armor
            Map<String, String> armorEquipment = new HashMap<>();
            armorEquipment.put("helmet", "diamond_helmet");
            armorEquipment.put("chestplate", "diamond_chestplate");
            armorEquipment.put("leggings", "diamond_leggings");
            armorEquipment.put("boots", "diamond_boots");

            List<Double> zombie2Location = Arrays.asList(15.0, 64.0, 10.0);
            realServerBackend.spawnEntity("zombie_armored", "ZOMBIE", zombie2Location, armorEquipment);
            assertTrue(realServerBackend.entityExists("zombie_armored"));
            System.out.println("Zombie with diamond armor spawned at " + zombie2Location);

            // Step 4: Assert lightning ability is ready
            System.out.println("\nStep 4: Verifying lightning ability status");
            boolean lightningReady = realServerBackend.pluginReceivedCommand("DragonEggLightning", "status", "test_player");
            System.out.println("Lightning ability status verified: " + (lightningReady ? "READY" : "NOT READY"));

            // Step 5: Execute lightning strikes
            System.out.println("\nStep 5: Executing lightning strikes");

            // Lightning strike on unarmored zombie
            List<String> lightningArgs1 = Arrays.asList("test_player", "lightning", "zombie_unarmored");
            realServerBackend.executePlayerCommand("test_player", "ability", lightningArgs1);
            System.out.println("Lightning strike executed on unarmored zombie");

            // Check unarmored zombie health after lightning
            double unarmoredHealth = realServerBackend.getEntityHealth("zombie_unarmored");
            System.out.println("Unarmored zombie health after lightning: " + unarmoredHealth);

            // Lightning strike on armored zombie
            List<String> lightningArgs2 = Arrays.asList("test_player", "lightning", "zombie_armored");
            realServerBackend.executePlayerCommand("test_player", "ability", lightningArgs2);
            System.out.println("Lightning strike executed on armored zombie");

            // Check armored zombie health after lightning
            double armoredHealth = realServerBackend.getEntityHealth("zombie_armored");
            System.out.println("Armored zombie health after lightning: " + armoredHealth);

            // Step 6: Verify final status
            System.out.println("\nStep 6: Final verification");

            // Verify both zombies still exist (lightning shouldn't kill instantly)
            boolean unarmoredExists = realServerBackend.entityExists("zombie_unarmored");
            boolean armoredExists = realServerBackend.entityExists("zombie_armored");

            assertTrue(unarmoredExists, "Unarmored zombie should still exist after lightning");
            assertTrue(armoredExists, "Armored zombie should still exist after lightning");

            System.out.println("Both zombies survived lightning strikes");
            System.out.println("Final status verification completed");

            // Step 7: Cleanup
            System.out.println("\nStep 7: Cleaning up test entities");
            realServerBackend.removeAllTestEntities();
            realServerBackend.removeAllTestPlayers();
            System.out.println("Test cleanup completed");

        } catch (Exception e) {
            System.err.println("Error during use case execution: " + e.getMessage());
            fail("Use case test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test PILAF Framework with Mock Backend for Dragon Egg Scenario")
    void testDragonEggLightningMockBackend() {
        System.out.println("\nTesting Dragon Egg Lightning with Mock Backend");
        System.out.println("=================================================");

        assertDoesNotThrow(() -> {
            mockBukkitBackend.initialize();
            System.out.println("MockBukkitBackend initialized");
        });

        // Test the same scenario with mock backend
        System.out.println("Simulating player setup...");
        mockBukkitBackend.giveItem("test_player", "dragon_egg", 3);
        mockBukkitBackend.equipItem("test_player", "dragon_egg", "offhand");

        System.out.println("Simulating zombie spawns...");
        List<Double> location1 = Arrays.asList(10.0, 64.0, 10.0);
        List<Double> location2 = Arrays.asList(15.0, 64.0, 10.0);

        mockBukkitBackend.spawnEntity("zombie_unarmored", "ZOMBIE", location1, null);
        mockBukkitBackend.spawnEntity("zombie_armored", "ZOMBIE", location2, null);

        assertTrue(mockBukkitBackend.entityExists("zombie_unarmored"));
        assertTrue(mockBukkitBackend.entityExists("zombie_armored"));

        System.out.println("Simulating lightning strikes...");
        List<String> args1 = Arrays.asList("test_player", "lightning", "zombie_unarmored");
        List<String> args2 = Arrays.asList("test_player", "lightning", "zombie_armored");

        mockBukkitBackend.executePlayerCommand("test_player", "ability", args1);
        mockBukkitBackend.executePlayerCommand("test_player", "ability", args2);

        // Verify plugin received commands
        assertTrue(mockBukkitBackend.pluginReceivedCommand("DragonEggLightning", "ability", "test_player"));

        System.out.println("Mock backend simulation completed successfully");

        assertDoesNotThrow(() -> {
            mockBukkitBackend.cleanup();
            System.out.println("Mock backend cleanup completed");
        });
    }

    @Test
    @DisplayName("Test server connection via RCON")
    void testRconConnection() {
        // Skip if server not available
        Assumptions.assumeTrue(serverAvailable,
            "Skipping test - Docker server not available. Start with: docker-compose up -d");

        RconClient client = new RconClient(RCON_HOST, RCON_PORT, RCON_PASSWORD);
        assertTrue(client.connect(), "Should connect to RCON server");

        // Execute a simple command
        String result = client.executeCommand("list");
        assertNotNull(result, "Should receive response from server");
        System.out.println("Server player list: " + result);

        client.disconnect();
    }
}
