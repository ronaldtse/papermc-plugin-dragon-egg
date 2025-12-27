package com.dragonegg.lightning.pilaf;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * RCON client for Minecraft server management
 * Implements the Source RCON protocol used by Minecraft servers
 */
public class RconClient {

    private static final int SERVERDATA_AUTH = 3;
    private static final int SERVERDATA_AUTH_RESPONSE = 2;
    private static final int SERVERDATA_EXECCOMMAND = 2;
    private static final int SERVERDATA_RESPONSE_VALUE = 0;

    private final String host;
    private final int port;
    private final String password;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean authenticated;
    private int requestId = 1;

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
            socket.setSoTimeout(5000);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            // Authenticate with server
            if (authenticate()) {
                System.out.println("RCON client connected successfully");
                return true;
            } else {
                disconnect();
                return false;
            }

        } catch (IOException e) {
            System.out.println("Failed to connect to RCON server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authenticate with RCON server
     */
    private boolean authenticate() {
        try {
            int authId = requestId++;
            sendPacket(authId, SERVERDATA_AUTH, password);

            // Read response
            RconPacket response = readPacket();
            if (response == null) {
                System.out.println("Authentication failed: no response");
                return false;
            }

            // Authentication successful if request ID matches (not -1)
            if (response.getRequestId() == authId || response.getRequestId() != -1) {
                authenticated = true;
                return true;
            } else {
                System.out.println("Authentication failed: invalid password");
                return false;
            }

        } catch (IOException e) {
            System.out.println("Authentication failed: " + e.getMessage());
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
            int cmdId = requestId++;
            sendPacket(cmdId, SERVERDATA_EXECCOMMAND, command);

            // Read response
            RconPacket response = readPacket();
            if (response != null) {
                return response.getBody();
            }

        } catch (IOException e) {
            System.out.println("Command execution failed: " + e.getMessage());
        }

        return null;
    }

    /**
     * Send RCON packet
     */
    private void sendPacket(int id, int type, String body) throws IOException {
        byte[] bodyBytes = body.getBytes("UTF-8");
        int size = 4 + 4 + bodyBytes.length + 2; // id + type + body + 2 null terminators

        ByteBuffer buffer = ByteBuffer.allocate(4 + size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(size);
        buffer.putInt(id);
        buffer.putInt(type);
        buffer.put(bodyBytes);
        buffer.put((byte) 0);
        buffer.put((byte) 0);

        outputStream.write(buffer.array());
        outputStream.flush();
    }

    /**
     * Read RCON packet from server
     */
    private RconPacket readPacket() throws IOException {
        // Read size (4 bytes, little-endian)
        byte[] sizeBytes = new byte[4];
        if (inputStream.read(sizeBytes) != 4) {
            return null;
        }
        int size = ByteBuffer.wrap(sizeBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

        if (size < 10) {
            return null;
        }

        // Read rest of packet
        byte[] packetBytes = new byte[size];
        int totalRead = 0;
        while (totalRead < size) {
            int read = inputStream.read(packetBytes, totalRead, size - totalRead);
            if (read == -1) break;
            totalRead += read;
        }

        ByteBuffer packet = ByteBuffer.wrap(packetBytes).order(ByteOrder.LITTLE_ENDIAN);
        int requestId = packet.getInt();
        int type = packet.getInt();

        // Body is remaining bytes minus 2 null terminators
        int bodyLength = size - 10;
        byte[] bodyBytes = new byte[bodyLength];
        packet.get(bodyBytes);

        String body = new String(bodyBytes, "UTF-8").trim();
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
            System.out.println("RCON client disconnected");

        } catch (IOException e) {
            System.out.println("Error during disconnection: " + e.getMessage());
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
