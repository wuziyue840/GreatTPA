package xyz.toilet.greatTPA.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.toilet.greatTPA.GreatTPA;
import xyz.toilet.greatTPA.PlayerData;
import xyz.toilet.greatTPA.RequestManager;
import xyz.toilet.greatTPA.RequestType;
import xyz.toilet.greatTPA.TeleportRequest;

public class TPAcceptCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public TPAcceptCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.plugin.getMessage("player-only-command"));
         return true;
      } else {
         Player player = (Player)sender;
         PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
         if (playerData.isOnCooldown("tpaaccept")) {
            long remaining = playerData.getRemainingCooldown("tpaaccept");
            player.sendMessage(this.plugin.getMessage("cooldown-message", remaining));
            return true;
         } else {
            this.plugin.getCastManager().startCast(player, "tpaaccept", () -> {
               RequestManager requestManager = this.plugin.getRequestManager();
               TeleportRequest request = null;
               if (args.length == 0) {
                  request = requestManager.getLatestRequest(player.getUniqueId());
               } else {
                  if (args.length != 1) {
                     player.sendMessage(this.plugin.getMessage("tpaaccept-usage"));
                     return;
                  }

                  Player senderPlayer = Bukkit.getPlayer(args[0]);
                  if (senderPlayer == null) {
                     player.sendMessage(this.plugin.getMessage("player-offline"));
                     return;
                  }

                  request = requestManager.getSpecificRequest(player.getUniqueId(), senderPlayer.getUniqueId());
               }

               if (request == null) {
                  player.sendMessage(this.plugin.getMessage("no-pending-request"));
               } else {
                  long timeout = this.plugin.getConfig().getLong("request-timeout", 30L) * 1000L;
                  if (request.isExpired(timeout)) {
                     this.plugin.getRequestManager().removeRequest(request);
                     player.sendMessage(this.plugin.getMessage("request-expired"));
                  } else {
                     Player senderPlayerx = Bukkit.getPlayer(request.getSender());
                     if (senderPlayerx == null) {
                        player.sendMessage(this.plugin.getMessage("player-offline"));
                        this.plugin.getRequestManager().removeRequest(request);
                     } else if (this.plugin.getConfig().getBoolean("dimension-check", true) && !player.getWorld().getUID().equals(senderPlayerx.getWorld().getUID())) {
                        player.sendMessage(this.plugin.getMessage("dimension-mismatch"));
                     } else {
                        if (request.getType() == RequestType.TPA_TO) {
                           senderPlayerx.teleport(player.getLocation());
                           senderPlayerx.sendMessage(this.plugin.getMessage("teleport-success"));
                        } else if (request.getType() == RequestType.TPA_HERE) {
                           player.teleport(senderPlayerx.getLocation());
                           player.sendMessage(this.plugin.getMessage("teleport-success"));
                        }

                        player.sendMessage(this.plugin.getMessage("request-accepted", senderPlayerx.getName()));
                        senderPlayerx.sendMessage(this.plugin.getMessage("request-accepted-by", player.getName()));
                        this.plugin.getRequestManager().removeRequest(request);
                        playerData.setCooldown("tpaaccept");
                     }
                  }
               }
            });
            return true;
         }
      }
   }
}
