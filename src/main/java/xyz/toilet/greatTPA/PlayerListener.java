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
      // 检测玩家是否在施法或等待过程中移动
      plugin.getCastManager().checkMovement(event.getPlayer());
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player player = event.getEntity();
      if (plugin.getConfig().getBoolean("back-enabled", true)) {
         plugin.getPlayerDataManager().getPlayerData(player.getUniqueId())
                 .setLastDeathLocation(player.getLocation());
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      plugin.getCastManager().cancelCast(player.getUniqueId());
      plugin.getCastManager().cancelWait(player.getUniqueId());
      plugin.getRequestManager().clearRequests(player.getUniqueId());
   }
}