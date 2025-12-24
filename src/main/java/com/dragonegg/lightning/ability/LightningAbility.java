package com.dragonegg.lightning.ability;

import com.dragonegg.lightning.DragonEggLightningPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

/**
 * Lightning ability that strikes targets with purple lightning.
 */
public class LightningAbility implements Ability {

  private static final int STRIKE_COUNT = 3;
  private static final long STRIKE_INTERVAL_TICKS = 10L; // 0.5 seconds
  private static final double DAMAGE_PER_STRIKE = 3.0; // 1.5 hearts
  private static final long COOLDOWN_MILLIS = 60000L; // 60 seconds
  private static final double MAX_RANGE = 50.0;
  private static final String ABILITY_NAME = "Lightning Strike";

  private final DragonEggLightningPlugin plugin;

  public LightningAbility(DragonEggLightningPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean execute(Player player) {
    if (player == null) {
      return false;
    }

    // Find target entity
    LivingEntity target = findTargetEntity(player);

    if (target == null) {
      player.sendMessage(
        Component.text("No valid target found!", NamedTextColor.RED)
      );
      return false;
    }

    // Check if player still has dragon egg in offhand
    if (!hasRequiredItem(player)) {
      player.sendMessage(
        Component.text(
          "You must hold a Dragon Egg in your offhand!",
          NamedTextColor.RED
        )
      );
      return false;
    }

    // Execute lightning strikes with timing
    executeLightningStrikes(player, target);

    player.sendMessage(
      Component.text("Lightning ability activated!", NamedTextColor.LIGHT_PURPLE)
    );

    return true;
  }

  @Override
  public boolean hasRequiredItem(Player player) {
    if (player == null) {
      return false;
    }
    ItemStack offhand = player.getInventory().getItemInOffHand();
    return offhand != null && offhand.getType() == Material.DRAGON_EGG;
  }

  @Override
  public long getCooldownMillis() {
    return COOLDOWN_MILLIS;
  }

  @Override
  public String getName() {
    return ABILITY_NAME;
  }

  /**
   * Find the closest living entity in the direction the player is facing.
   *
   * @param player The player
   * @return The target entity or null if none found
   */
  private LivingEntity findTargetEntity(Player player) {
    if (player == null) {
      return null;
    }
    Location eyeLocation = player.getEyeLocation();
    Vector direction = eyeLocation.getDirection();

    // Use ray tracing to find entities
    RayTraceResult result = player.getWorld().rayTraceEntities(
      eyeLocation,
      direction,
      MAX_RANGE,
      entity -> entity instanceof LivingEntity &&
        entity != player &&
        !entity.isDead()
    );

    if (result != null && result.getHitEntity() instanceof LivingEntity) {
      return (LivingEntity) result.getHitEntity();
    }

    // Fallback: find nearest entity in viewing cone
    return findNearestEntityInCone(player, eyeLocation, direction);
  }

  /**
   * Find nearest entity within a viewing cone.
   *
   * @param player The player
   * @param eyeLocation The player's eye location
   * @param direction The player's look direction
   * @return The nearest entity or null
   */
  private LivingEntity findNearestEntityInCone(
    Player player,
    Location eyeLocation,
    Vector direction
  ) {
    LivingEntity nearest = null;
    double nearestDistance = MAX_RANGE;
    double minDotProduct = 0.9; // Roughly 25 degree cone

    for (Entity entity : player.getWorld().getNearbyEntities(
      eyeLocation,
      MAX_RANGE,
      MAX_RANGE,
      MAX_RANGE
    )) {
      if (!(entity instanceof LivingEntity) ||
          entity == player ||
          entity.isDead()) {
        continue;
      }

      Vector toEntity = entity.getLocation()
        .subtract(eyeLocation)
        .toVector()
        .normalize();
      double dot = direction.dot(toEntity);

      if (dot >= minDotProduct) {
        double distance = eyeLocation.distance(entity.getLocation());
        if (distance < nearestDistance) {
          nearest = (LivingEntity) entity;
          nearestDistance = distance;
        }
      }
    }

    return nearest;
  }

  /**
   * Execute sequential lightning strikes on target.
   *
   * @param player The player casting the ability
   * @param target The target entity
   */
  private void executeLightningStrikes(Player player, LivingEntity target) {
    // Store references to avoid final variable issues
    final LivingEntity finalTarget = target;
    final Player finalPlayer = player;

    new BukkitRunnable() {
      private int strikeCount = 0;

      @Override
      public void run() {
        // Check if target is still valid (use stored reference)
        if (finalTarget.isDead() || !finalTarget.isValid()) {
          player.sendMessage(
            Component.text("Target is no longer valid!", NamedTextColor.YELLOW)
          );
          cancel();
          return;
        }

        // Check if player still has dragon egg (can be switched mid-cast)
        if (!hasRequiredItem(finalPlayer)) {
          player.sendMessage(
            Component.text(
              "Ability cancelled! Dragon Egg removed from offhand.",
              NamedTextColor.RED
            )
          );
          cancel();
          return;
        }

        // Strike the target
        strikeLightning(finalTarget);
        strikeCount++;

        // Send strike message
        player.sendMessage(
          Component.text("Lightning strike " + strikeCount + "/" + STRIKE_COUNT + "!",
                        NamedTextColor.LIGHT_PURPLE)
        );

        // Check if all strikes are done
        if (strikeCount >= STRIKE_COUNT) {
          cancel();
        }
      }
    }.runTaskTimer(plugin, 0L, STRIKE_INTERVAL_TICKS);
  }

  /**
   * Strike a single lightning bolt on the target.
   *
   * @param target The target entity
   */
  private void strikeLightning(LivingEntity target) {
    Location targetLocation = target.getLocation();

    // Create actual lightning strike (use correct entity type)
    LightningStrike lightning = (LightningStrike) target.getWorld()
      .spawnEntity(targetLocation, EntityType.LIGHTNING_BOLT);

    // Make it visually purple with particles
    createPurpleLightningEffect(targetLocation);

    // Deal damage
    target.damage(DAMAGE_PER_STRIKE);

    // Play proper thunder sound
    target.getWorld().playSound(
      targetLocation,
      Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
      3.0f,
      1.0f
    );

    // Play explosion sound for impact
    target.getWorld().playSound(
      targetLocation,
      Sound.ENTITY_DRAGON_FIREBALL_EXPLODE,
      2.0f,
      1.0f
    );
  }

  /**
   * Create purple lightning visual effect.
   *
   * @param location The location to spawn the effect
   */
  private void createPurpleLightningEffect(Location location) {
    Location topLocation = location.clone().add(0, 10, 0);

    // Create vertical purple particle beam
    for (double y = 0; y <= 10; y += 0.3) {
      Location particleLocation = location.clone().add(0, y, 0);

      // Main purple beam
      particleLocation.getWorld().spawnParticle(
        Particle.DUST,
        particleLocation,
        5,
        0.1,
        0.1,
        0.1,
        0,
        new Particle.DustOptions(Color.fromRGB(128, 0, 128), 2.0f)
      );

      // Electric effect
      particleLocation.getWorld().spawnParticle(
        Particle.ELECTRIC_SPARK,
        particleLocation,
        3,
        0.2,
        0.2,
        0.2,
        0.05
      );
    }

    // Add explosion effect at impact
    location.getWorld().spawnParticle(
      Particle.DUST,
      location,
      50,
      0.5,
      0.5,
      0.5,
      0,
      new Particle.DustOptions(Color.fromRGB(255, 0, 255), 1.5f)
    );

    // Add flash effect
    location.getWorld().spawnParticle(
      Particle.FLASH,
      location,
      1,
      0,
      0,
      0,
      0
    );
  }
}
