#!/usr/bin/env node

const mineflayer = require('mineflayer');
const { pathfinder, Movements, GoalBlock } = require('mineflayer-pathfinder');

/**
 * Mineflayer client for PILAF integration
 * Handles real Minecraft player actions and command execution
 */
class PilafMineflayerClient {
    constructor(options) {
        this.host = options.host || 'localhost';
        this.port = options.port || 25565;
        this.username = options.username || 'pilaf_test_player';
        this.password = options.password || '';
        this.version = options.version || false;

        this.bot = null;
        this.connected = false;
        this.commandQueue = [];
        this.listeners = [];

        // Initialize bot
        this.createBot();
    }

    /**
     * Create and configure Mineflayer bot
     */
    createBot() {
        this.bot = mineflayer.createBot({
            host: this.host,
            port: this.port,
            username: this.username,
            password: this.password,
            version: this.version,
            checkTimeoutInterval: 30000
        });

        // Load pathfinder plugin
        this.bot.loadPlugin(pathfinder);

        // Set up event listeners
        this.setupEventListeners();
    }

    /**
     * Set up bot event listeners
     */
    setupEventListeners() {
        // Login event
        this.bot.on('login', () => {
            console.log(`[Mineflayer] ${this.username} logged in to ${this.host}:${this.port}`);
            this.connected = true;

            // Initialize pathfinder
            const defaultMove = new Movements(this.bot);
            defaultMove.canDig = false;
            defaultMove.placeCost = 8000;
            this.bot.pathfinder.setMovements(defaultMove);

            // Notify listeners
            this.notifyListeners('connected');

            // Process command queue
            this.processCommandQueue();
        });

        // Chat message handling
        this.bot.on('chat', (username, message) => {
            console.log(`[Mineflayer] Chat: ${username}: ${message}`);
            this.notifyListeners('chat', { username, message });
        });

        // Health change monitoring
        this.bot.on('health', () => {
            this.notifyListeners('health', {
                health: this.bot.health,
                food: this.bot.food,
                saturation: this.bot.saturation
            });
        });

        // Death handling
        this.bot.on('death', () => {
            console.log(`[Mineflayer] ${this.username} died`);
            this.notifyListeners('death');
        });

        // End/kick handling
        this.bot.on('end', (reason) => {
            console.log(`[Mineflayer] Connection ended: ${reason}`);
            this.connected = false;
            this.notifyListeners('end', { reason });
        });

        // Error handling
        this.bot.on('error', (error) => {
            console.log(`[Mineflayer] Error: ${error.message}`);
            this.notifyListeners('error', { error });
        });
    }

    /**
     * Connect to Minecraft server
     */
    connect() {
        return new Promise((resolve, reject) => {
            const connectTimeout = setTimeout(() => {
                reject(new Error('Connection timeout'));
            }, 30000);

            const checkConnection = () => {
                if (this.connected) {
                    clearTimeout(connectTimeout);
                    resolve();
                }
            };

            // Listen for connection
            this.addListener('connected', checkConnection);
            this.addListener('error', (error) => {
                clearTimeout(connectTimeout);
                reject(error);
            });
        });
    }

    /**
     * Execute command as player
     */
    async executeCommand(command) {
        if (!this.connected) {
            throw new Error('Bot not connected');
        }

        console.log(`[Mineflayer] Executing command: ${command}`);

        return new Promise((resolve, reject) => {
            const timeout = setTimeout(() => {
                reject(new Error('Command timeout'));
            }, 10000);

            // Send command to chat
            this.bot.chat(`/${command}`);

            // Set up response listener
            const responseListener = (username, message) => {
                if (message.includes('ability') || message.includes('Lightning')) {
                    clearTimeout(timeout);
                    this.bot.removeListener('chat', responseListener);
                    resolve(message);
                }
            };

            this.bot.on('chat', responseListener);
        });
    }

    /**
     * Give item to player
     */
    giveItem(itemType, count) {
        if (!this.connected) {
            throw new Error('Bot not connected');
        }

        console.log(`[Mineflayer] Giving ${count} ${itemType} to ${this.username}`);
        this.executeCommand(`give @s ${itemType} ${count}`);
    }

    /**
     * Equip item to slot
     */
    equipItem(itemType, slot) {
        if (!this.connected) {
            throw new Error('Bot not connected');
        }

        console.log(`[Mineflayer] Equipping ${itemType} to ${slot}`);
        this.executeCommand(`replaceitem entity @s ${slot} ${itemType}`);
    }

    /**
     * Move to position
     */
    moveTo(x, y, z) {
        if (!this.connected) {
            throw new Error('Bot not connected');
        }

        console.log(`[Mineflayer] Moving to [${x}, ${y}, ${z}]`);

        const goal = new GoalBlock(x, y, z);
        this.bot.pathfinder.goto(goal);
    }

    /**
     * Get current position
     */
    getPosition() {
        if (!this.connected) {
            throw new Error('Bot not connected');
        }

        const pos = this.bot.entity.position;
        return {
            x: Math.floor(pos.x),
            y: Math.floor(pos.y),
            z: Math.floor(pos.z)
        };
    }

    /**
     * Get inventory contents
     */
    getInventory() {
        if (!this.connected) {
            throw new Error('Bot not connected');
        }

        const items = [];
        for (const slot of this.bot.inventory.slots) {
            if (slot) {
                items.push({
                    type: slot.name,
                    count: slot.count,
                    displayName: slot.displayName
                });
            }
        }
        return items;
    }

    /**
     * Check if item exists in inventory
     */
    hasItem(itemType, slot = 'any') {
        const inventory = this.getInventory();

        if (slot === 'any') {
            return inventory.some(item => item.type === itemType);
        }

        // Check specific slot
        const slotIndex = this.getSlotIndex(slot);
        const item = this.bot.inventory.slots[slotIndex];

        return item && item.name === itemType;
    }

    /**
     * Get slot index from slot name
     */
    getSlotIndex(slot) {
        const slotMap = {
            'mainhand': 36,
            'offhand': 45,
            'helmet': 5,
            'chestplate': 6,
            'leggings': 7,
            'boots': 8
        };

        return slotMap[slot] || 36;
    }

    /**
     * Disconnect from server
     */
    disconnect() {
        if (this.bot) {
            this.bot.end('PILAF test completed');
            this.connected = false;
            console.log(`[Mineflayer] Disconnected ${this.username}`);
        }
    }

    /**
     * Add event listener
     */
    addListener(event, callback) {
        this.listeners.push({ event, callback });
    }

    /**
     * Remove event listener
     */
    removeListener(event, callback) {
        const index = this.listeners.findIndex(l => l.event === event && l.callback === callback);
        if (index !== -1) {
            this.listeners.splice(index, 1);
        }
    }

    /**
     * Notify all listeners of an event
     */
    notifyListeners(event, data) {
        this.listeners
            .filter(listener => listener.event === event)
            .forEach(listener => {
                try {
                    listener.callback(data);
                } catch (error) {
                    console.error(`[Mineflayer] Listener error: ${error.message}`);
                }
            });
    }

    /**
     * Process queued commands
     */
    processCommandQueue() {
        while (this.commandQueue.length > 0 && this.connected) {
            const command = this.commandQueue.shift();
            this.executeCommand(command).catch(console.error);
        }
    }

    /**
     * Queue command for execution
     */
    queueCommand(command) {
        this.commandQueue.push(command);
        if (this.connected) {
            this.processCommandQueue();
        }
    }
}

// CLI interface for testing
if (require.main === module) {
    const args = process.argv.slice(2);
    const host = args[0] || 'localhost';
    const port = parseInt(args[1]) || 25565;
    const username = args[2] || 'pilaf_test_player';

    const client = new PilafMineflayerClient({ host, port, username });

    client.connect()
        .then(() => {
            console.log('[Mineflayer] Connected successfully');

            // Test commands
            setTimeout(() => {
                client.giveItem('dragon_egg', 3);
            }, 2000);

            setTimeout(() => {
                client.equipItem('dragon_egg', 'offhand');
            }, 4000);

            setTimeout(() => {
                client.executeCommand('ability 1');
            }, 6000);

            setTimeout(() => {
                client.disconnect();
                process.exit(0);
            }, 10000);
        })
        .catch(error => {
            console.error('[Mineflayer] Connection failed:', error.message);
            process.exit(1);
        });
}

module.exports = PilafMineflayerClient;
