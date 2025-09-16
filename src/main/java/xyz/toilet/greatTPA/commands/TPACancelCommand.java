package xyz.toilet.greatTPA.commands;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.toilet.greatTPA.GreatTPA;
import xyz.toilet.greatTPA.RequestManager;
import xyz.toilet.greatTPA.TeleportRequest;

public class TPACancelCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public TPACancelCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.plugin.getMessage("player-only-command"));
         return true;
      } else {
         Player player = (Player)sender;
         this.plugin.getCastManager().startCast(player, "tpacancel", () -> {
            RequestManager requestManager = this.plugin.getRequestManager();
            if (args.length == 0) {
               TeleportRequest lastSent = this.findLastSentRequest(player.getUniqueId());
               if (lastSent == null) {
                  player.sendMessage(this.plugin.getMessage("no-sent-request"));
                  return;
               }

               Player targetx = Bukkit.getPlayer(lastSent.getReceiver());
               if (targetx != null) {
                  targetx.sendMessage(this.plugin.getMessage("request-cancelled-by", player.getName()));
               }

               player.sendMessage(this.plugin.getMessage("request-cancelled", targetx != null ? targetx.getName() : "未知玩家"));
               this.plugin.getRequestManager().removeRequest(lastSent);
            } else if (args.length == 1) {
               Player target = Bukkit.getPlayer(args[0]);
               if (target == null) {
                  player.sendMessage(this.plugin.getMessage("player-offline"));
                  return;
               }

               TeleportRequest request = requestManager.getSpecificRequest(target.getUniqueId(), player.getUniqueId());
               if (request == null) {
                  player.sendMessage(this.plugin.getMessage("no-request-to-player", target.getName()));
                  return;
               }

               target.sendMessage(this.plugin.getMessage("request-cancelled-by", player.getName()));
               player.sendMessage(this.plugin.getMessage("request-cancelled", target.getName()));
               this.plugin.getRequestManager().removeRequest(request);
            } else {
               player.sendMessage(this.plugin.getMessage("tpacancel-usage"));
            }

         });
         return true;
      }
   }

   private TeleportRequest findLastSentRequest(UUID senderId) {
      Iterator var2 = this.plugin.getRequestManager().getRequests().values().iterator();

      while(var2.hasNext()) {
         List<TeleportRequest> reqList = (List)var2.next();
         Iterator var4 = reqList.iterator();

         while(var4.hasNext()) {
            TeleportRequest req = (TeleportRequest)var4.next();
            if (req.getSender().equals(senderId)) {
               return req;
            }
         }
      }

      return null;
   }
}
