package xyz.toilet.greatTPA;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class CastManager {
   private final Map<UUID, CastSession> activeCasts = new ConcurrentHashMap<>();
   private final Map<UUID, WaitSession> activeWaits = new ConcurrentHashMap<>();
   private final GreatTPA plugin;

   public CastManager(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public void startCast(Player player, String command, Runnable onSuccess) {
      int castTime = plugin.getConfig().getInt("command-settings." + command + ".cast-time", 0);
      double maxMove = plugin.getConfig().getDouble("command-settings." + command + ".max-move-distance", 0);

      startCast(player, command, onSuccess, castTime, maxMove);
   }

   public void startCast(Player player, String command, Runnable onSuccess, int castTime, double maxMove) {
      if (castTime <= 0) {
         onSuccess.run();
         return;
      }

      Location startLoc = player.getLocation().clone();
      CastSession session = new CastSession(player, command, startLoc, castTime, maxMove, onSuccess);
      activeCasts.put(player.getUniqueId(), session);

      player.sendMessage(plugin.getMessage("casting-start", castTime));
      session.start();
   }

   // 新增：开始传送等待
   public void startWait(Player player, String command, Runnable onSuccess) {
      int waitTime = plugin.getConfig().getInt("command-settings." + command + ".wait-time", 0);

      if (waitTime <= 0) {
         onSuccess.run();
         return;
      }

      Location startLoc = player.getLocation().clone();
      WaitSession session = new WaitSession(player, command, startLoc, waitTime, onSuccess);
      activeWaits.put(player.getUniqueId(), session);

      player.sendMessage(plugin.getMessage("teleport-waiting", waitTime));
      session.start();
   }

   // 新增：开始TPARTP等待
   public void startTpartpWait(Player player, Runnable onSuccess) {
      int waitTime = plugin.getConfig().getInt("tpartp.wait-time", 0);

      if (waitTime <= 0) {
         onSuccess.run();
         return;
      }

      Location startLoc = player.getLocation().clone();
      WaitSession session = new WaitSession(player, "tpartp", startLoc, waitTime, onSuccess);
      activeWaits.put(player.getUniqueId(), session);

      player.sendMessage(plugin.getMessage("teleport-waiting", waitTime));
      session.start();
   }

   public void cancelCast(UUID playerId) {
      CastSession session = activeCasts.remove(playerId);
      if (session != null) {
         session.cancel();
      }
   }

   public void cancelWait(UUID playerId) {
      WaitSession session = activeWaits.remove(playerId);
      if (session != null) {
         session.cancel();
      }
   }

   public void checkMovement(Player player) {
      // 检查施法移动
      CastSession castSession = activeCasts.get(player.getUniqueId());
      if (castSession != null) {
         castSession.checkDistance(player.getLocation());
      }

      // 检查等待移动
      WaitSession waitSession = activeWaits.get(player.getUniqueId());
      if (waitSession != null) {
         waitSession.checkDistance(player.getLocation());
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

   // 新增：等待会话类
   private class WaitSession {
      private final Player player;
      private final String command;
      private final Location startLocation;
      private final int waitTime;
      private final Runnable onSuccess;
      private BukkitTask task;

      public WaitSession(Player player, String command, Location startLocation,
                         int waitTime, Runnable onSuccess) {
         this.player = player;
         this.command = command;
         this.startLocation = startLocation;
         this.waitTime = waitTime;
         this.onSuccess = onSuccess;
      }

      public void start() {
         task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeWaits.remove(player.getUniqueId()) != null) {
               player.sendMessage(plugin.getMessage("teleport-waiting-complete"));
               onSuccess.run();
            }
         }, waitTime * 20L);
      }

      public void checkDistance(Location currentLoc) {
         if (startLocation.getWorld().equals(currentLoc.getWorld())) {
            double distance = startLocation.distanceSquared(currentLoc);
            if (distance > 1.0) { // 移动超过1格就取消
               cancel();
               player.sendMessage(plugin.getMessage("teleport-waiting-cancelled"));
            }
         }
      }

      public void cancel() {
         if (task != null) {
            task.cancel();
            activeWaits.remove(player.getUniqueId());
         }
      }
   }
}
