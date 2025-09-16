package xyz.toilet.greatTPA.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.toilet.greatTPA.GreatTPA;
import xyz.toilet.greatTPA.RequestManager;
import xyz.toilet.greatTPA.TeleportRequest;

public class TPADenyCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public TPADenyCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.plugin.getMessage("player-only-command"));
         return true;
      } else {
         Player player = (Player)sender;
         this.plugin.getCastManager().startCast(player, "tpadeny", () -> {
            RequestManager requestManager = this.plugin.getRequestManager();
            TeleportRequest request = null;
            Player senderPlayer;
            if (args.length == 0) {
               request = requestManager.getLatestRequest(player.getUniqueId());
            } else {
               if (args.length != 1) {
                  player.sendMessage(this.plugin.getMessage("tpadeny-usage"));
                  return;
               }

               senderPlayer = Bukkit.getPlayer(args[0]);
               if (senderPlayer == null) {
                  player.sendMessage(this.plugin.getMessage("player-offline"));
                  return;
               }

               request = requestManager.getSpecificRequest(player.getUniqueId(), senderPlayer.getUniqueId());
            }

            if (request == null) {
               player.sendMessage(this.plugin.getMessage("no-pending-request"));
            } else {
               senderPlayer = Bukkit.getPlayer(request.getSender());
               if (senderPlayer != null) {
                  senderPlayer.sendMessage(this.plugin.getMessage("request-denied-by", player.getName()));
               }

               player.sendMessage(this.plugin.getMessage("request-denied", senderPlayer != null ? senderPlayer.getName() : "未知玩家"));
               this.plugin.getRequestManager().removeRequest(request);
            }
         });
         return true;
      }
   }
}
