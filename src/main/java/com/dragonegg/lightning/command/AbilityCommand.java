package com.dragonegg.lightning.command;

import com.dragonegg.lightning.DragonEggLightningPlugin;
import com.dragonegg.lightning.ability.Ability;
import com.dragonegg.lightning.ability.AbilityManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command handler for /ability command.
 */
public class AbilityCommand implements CommandExecutor {

  private final DragonEggLightningPlugin plugin;
  private final AbilityManager abilityManager;

  public AbilityCommand(
    DragonEggLightningPlugin plugin,
    AbilityManager abilityManager
  ) {
    this.plugin = plugin;
    this.abilityManager = abilityManager;
  }

  @Override
  public boolean onCommand(
    CommandSender sender,
    Command command,
    String label,
    String[] args
  ) {
    // Check if sender is a player
    if (!(sender instanceof Player)) {
      sender.sendMessage(
        Component.text("This command can only be used by players!",
          NamedTextColor.RED)
      );
      return true;
    }

    Player player = (Player) sender;

    // Handle version/info command
    if (args.length == 1 && (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("info"))) {
      String pluginVersion = plugin.getDescription().getVersion();
      String pluginName = plugin.getDescription().getName();

      player.sendMessage(
        Component.text("=== " + pluginName + " Plugin Info ===", NamedTextColor.GOLD)
      );
      player.sendMessage(
        Component.text("Version: " + pluginVersion, NamedTextColor.GREEN)
      );
      player.sendMessage(
        Component.text("Author: " + String.join(", ", plugin.getDescription().getAuthors()), NamedTextColor.BLUE)
      );
      player.sendMessage(
        Component.text("Description: " + plugin.getDescription().getDescription(), NamedTextColor.GRAY)
      );
      player.sendMessage(
        Component.text("Website: " + plugin.getDescription().getWebsite(), NamedTextColor.BLUE)
      );
      return true;
    }

    // Check arguments for ability use
    if (args.length != 1) {
      player.sendMessage(
        Component.text("Usage: /ability <number> | /ability version", NamedTextColor.RED)
      );
      return true;
    }

    // Parse ability ID
    int abilityId;
    try {
      abilityId = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      player.sendMessage(
        Component.text("Invalid ability number!", NamedTextColor.RED)
      );
      return true;
    }

    // Get ability
    Ability ability = abilityManager.getAbility(abilityId);
    if (ability == null) {
      player.sendMessage(
        Component.text("Ability not found!", NamedTextColor.RED)
      );
      return true;
    }

    // Check if player has required item
    if (!ability.hasRequiredItem(player)) {
      player.sendMessage(
        Component.text(
          "You must hold a Dragon Egg in your offhand to use this ability!",
          NamedTextColor.RED
        )
      );
      return true;
    }

    // Check cooldown
    if (abilityManager.isOnCooldown(player)) {
      int remaining = abilityManager.getRemainingCooldown(player);
      player.sendMessage(
        Component.text(
          "Ability on cooldown! " + remaining + " seconds remaining.",
          NamedTextColor.RED
        )
      );
      return true;
    }

    // Use ability
    boolean success = abilityManager.useAbility(player, ability);
    if (!success) {
      player.sendMessage(
        Component.text(
          "Failed to use ability!",
          NamedTextColor.RED
        )
      );
    }

    return true;
  }
}
