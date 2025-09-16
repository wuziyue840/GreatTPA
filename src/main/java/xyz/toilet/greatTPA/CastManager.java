package xyz.toilet.greatTPA;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class CastManager {
   private final Map<UUID, CastManager.CastSession> activeCasts = new ConcurrentHashMap();
   private final GreatTPA plugin;

   public CastManager(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public void startCast(Player player, String command, Runnable onSuccess) {
      int castTime = this.plugin.getConfig().getInt("command-settings." + command + ".cast-time", 0);
      double maxMove = this.plugin.getConfig().getDouble("command-settings." + command + ".max-move-distance", 0.0D);
      if (castTime <= 0) {
         onSuccess.run();
      } else {
         Location startLoc = player.getLocation().clone();
         CastManager.CastSession session = new CastManager.CastSession(player, command, startLoc, castTime, maxMove, onSuccess);
         this.activeCasts.put(player.getUniqueId(), session);
         player.sendMessage(this.plugin.getMessage("casting-start", castTime));
         session.start();
      }
   }

   public void cancelCast(UUID playerId) {
      CastManager.CastSession session = (CastManager.CastSession)this.activeCasts.remove(playerId);
      if (session != null) {
         session.cancel();
      }

   }

   public void checkMovement(Player player) {
      CastManager.CastSession session = (CastManager.CastSession)this.activeCasts.get(player.getUniqueId());
      if (session != null) {
         session.checkDistance(player.getLocation());
      }

   }

   private class CastSession {
      private final Player player;
      private final String command;
      private final Location startLocation;
      private final int castTime;
      private final double maxMoveDistance;
      private final Runnable onSuccess;
      private BukkitTask task;

      public CastSession(Player player, String command, Location startLocation,
                         int castTime, double maxMoveDistance, Runnable onSuccess) {
         this.player = player;
         this.command = command;
         this.startLocation = startLocation;
         this.castTime = castTime;
         this.maxMoveDistance = maxMoveDistance;
         this.onSuccess = onSuccess;
      }

      public void start() {
         this.task = Bukkit.getScheduler().runTaskLater(CastManager.this.plugin, () -> {
            if (CastManager.this.activeCasts.remove(this.player.getUniqueId()) != null) {
               this.onSuccess.run();
            }

         }, (long)this.castTime * 20L);
      }

      public void checkDistance(Location currentLoc) {
         if (this.startLocation.getWorld().equals(currentLoc.getWorld())) {
            double distance = this.startLocation.distanceSquared(currentLoc);
            if (distance > this.maxMoveDistance * this.maxMoveDistance) {
               this.cancel();
               this.player.sendMessage(CastManager.this.plugin.getMessage("casting-cancelled-move"));
            }
         }

      }

      public void cancel() {
         if (this.task != null) {
            this.task.cancel();
            CastManager.this.activeCasts.remove(this.player.getUniqueId());
            this.player.sendMessage(CastManager.this.plugin.getMessage("casting-cancelled"));
         }

      }
   }
}
