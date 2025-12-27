package com.dragonegg.lightning.pilaf;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * OOP-based RCON client for server management
 * Handles connection, authentication, and command execution
 */
public class RconClient {

    private static final int PACKET_SIZE_HEADER = 14;
    private static final int RESPONSE_PACKET_ID = 0x12345678;
    private static final int AUTH_PACKET_ID = 0x12345678;

    private final String host;
    private final int port;
    private final String password;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private boolean authenticated;

    public RconClient(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.authenticated = false;
    }

    /**
     * Connect to RCON server
     */
    public boolean connect() {
        try {
            socket = new Socket(host, port);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            // Authenticate with server
            if (authenticate()) {
                System.out.println("✅ RCON client connected successfully");
                return true;
            } else {
                disconnect();
                return false;
            }

        } catch (IOException e) {
            System.out.println("❌ Failed to connect to RCON server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticate with RCON server
     */
    private boolean authenticate() {
        try {
            // Send auth packet
            int requestId = AUTH_PACKET_ID;
            String command = password;
            int packetSize = 4 + command.length() + 2;

            ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE_HEADER + command.length());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(packetSize);
            buffer.putInt(requestId);
            buffer.putInt(2); // AUTH command
            buffer.put(command.getBytes());
            buffer.put((byte) 0); // Null terminator

            outputStream.write(buffer.array());
            outputStream.flush();

            // Read response
            RconPacket response = readPacket();
            if (response != null && response.getRequestId() == AUTH_PACKET_ID) {
                authenticated = true;
                return true;
            }

        } catch (IOException e) {
            System.out.println("❌ Authentication failed: " + e.getMessage());
        }

        return false;
    }

    /**
     * Execute RCON command
     */
    public String executeCommand(String command) {
        if (!authenticated) {
            throw new IllegalStateException("RCON client not authenticated");
        }

        try {
            int requestId = RESPONSE_PACKET_ID;
            int packetSize = 4 + command.length() + 2;

            ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE_HEADER + command.length());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(packetSize);
            buffer.putInt(requestId);
            buffer.putInt(2); // EXEC command
            buffer.put(command.getBytes());
            buffer.put((byte) 0); // Null terminator

            outputStream.write(buffer.array());
            outputStream.flush();

            // Read response
            RconPacket response = readPacket();
            if (response != null && response.getRequestId() == requestId) {
                return response.getBody();
            }

        } catch (IOException e) {
            System.out.println("❌ Command execution failed: " + e.getMessage());
        }

        return null;
    }

    /**
     * Read RCON packet from server
     */
    private RconPacket readPacket() throws IOException {
        int size = inputStream.readInt();
        if (size < 14) return null;

        int requestId = inputStream.readInt();
        int type = inputStream.readInt();

        byte[] bodyBytes = new byte[size - 8];
        inputStream.readFully(bodyBytes);
        inputStream.readByte(); // Null terminator

        String body = new String(bodyBytes);
        return new RconPacket(requestId, type, body);
    }

    /**
     * Disconnect from RCON server
     */
    public void disconnect() {
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();

            authenticated = false;
            System.out.println("🧹 RCON client disconnected");

        } catch (IOException e) {
            System.out.println("❌ Error during disconnection: " + e.getMessage());
        }
    }

    /**
     * Check if client is authenticated
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * RCON packet data structure
     */
    private static class RconPacket {
        private final int requestId;
        private final int type;
        private final String body;

        public RconPacket(int requestId, int type, String body) {
            this.requestId = requestId;
            this.type = type;
            this.body = body;
        }

        public int getRequestId() { return requestId; }
        public int getType() { return type; }
        public String getBody() { return body; }
    }
}
