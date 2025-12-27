package com.dragonegg.lightning.pilaf;

/**
 * Factory for creating DITF backends
 *
 * Supports creating different backend implementations based on configuration:
 * - MockBukkit: Fast, in-memory testing
 * - real-server: Integration testing with actual servers
 */
public class PilafBackendFactory {

    /**
     * Create a backend implementation
     *
     * @param backendType The type of backend to create
     * @return Backend implementation
     * @throws IllegalArgumentException if backend type is not supported
     */
    public static PilafBackend createBackend(String backendType) {
        switch (backendType.toLowerCase()) {
            case "mockbukkit":
            case "MockBukkit":
                return new MockBukkitBackend();
            case "real-server":
            case "realserver":
                return new RealServerBackend();
            default:
                throw new IllegalArgumentException("Unsupported backend type: " + backendType +
                    ". Supported types: mockbukkit, real-server");
        }
    }

    /**
     * Create a backend from system property or environment variable
     *
     * Checks DITF_BACKEND environment variable or ditf.backend system property
     * Falls back to "mockbukkit" if not set
     *
     * @return Backend implementation
     */
    public static PilafBackend createDefaultBackend() {
        String backendType = System.getProperty("ditf.backend",
            System.getenv("DITF_BACKEND"));

        if (backendType == null) {
            backendType = "mockbukkit"; // Default to fast testing
        }

        return createBackend(backendType);
    }
}
