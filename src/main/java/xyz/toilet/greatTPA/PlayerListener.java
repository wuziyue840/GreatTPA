package xyz.toilet.greatTPA;

import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
   private final GreatTPA plugin;

   public PlayerListener(GreatTPA plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
         this.plugin.getCastManager().checkMovement(event.getPlayer());
      }

   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      if (this.plugin.getConfig().getBoolean("back-enabled", true)) {
         this.plugin.getPlayerDataManager().getPlayerData(player.getUniqueId()).setLastDeathLocation(player.getLocation());
      }

   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.plugin.getCastManager().cancelCast(player.getUniqueId());
      this.plugin.getRequestManager().clearRequests(player.getUniqueId());
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      RequestManager requestManager = this.plugin.getRequestManager();
      List<TeleportRequest> requests = requestManager.getRequestsForReceiver(player.getUniqueId());
      if (requests != null) {
         long timeout = this.plugin.getConfig().getLong("request-timeout", 30L) * 1000L;
         long currentTime = System.currentTimeMillis();
         requests.removeIf((req) -> {
            return currentTime - req.getTimestamp() > timeout;
         });
      }

   }
}
