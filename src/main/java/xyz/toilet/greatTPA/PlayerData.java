package xyz.toilet.greatTPA;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.bukkit.Location;

public class PlayerData {
   private boolean dndMode = false;
   private final Set<UUID> blockedPlayers = new ConcurrentSkipListSet();
   private Location lastDeathLocation;
   private final Map<String, Long> cooldowns = new ConcurrentHashMap();
   private final GreatTPA plugin;

   public PlayerData(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean isDndMode() {
      return this.dndMode;
   }

   public boolean toggleDndMode() {
      this.dndMode = !this.dndMode;
      return this.dndMode;
   }

   public Set<UUID> getBlockedPlayers() {
      return this.blockedPlayers;
   }

   public boolean blockPlayer(UUID playerId) {
      return this.blockedPlayers.add(playerId);
   }

   public boolean unblockPlayer(UUID playerId) {
      return this.blockedPlayers.remove(playerId);
   }

   public boolean isBlocking(UUID playerId) {
      return this.blockedPlayers.contains(playerId);
   }

   public Location getLastDeathLocation() {
      return this.lastDeathLocation;
   }

   public void setLastDeathLocation(Location location) {
      this.lastDeathLocation = location;
   }

   public boolean isOnCooldown(String command) {
      Long lastUsed = (Long)this.cooldowns.get(command);
      if (lastUsed == null) {
         return false;
      } else {
         int cooldownSec = this.plugin.getConfig().getInt("cooldowns." + command, 0);
         return System.currentTimeMillis() - lastUsed < (long)cooldownSec * 1000L;
      }
   }

   public void setCooldown(String command) {
      this.cooldowns.put(command, System.currentTimeMillis());
   }

   public long getRemainingCooldown(String command) {
      Long lastUsed = (Long)this.cooldowns.get(command);
      if (lastUsed == null) {
         return 0L;
      } else {
         int cooldownSec = this.plugin.getConfig().getInt("cooldowns." + command, 0);
         long remaining = lastUsed + (long)cooldownSec * 1000L - System.currentTimeMillis();
         return Math.max(0L, remaining) / 1000L;
      }
   }
}
