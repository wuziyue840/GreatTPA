package xyz.toilet.greatTPA.commands;

import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.toilet.greatTPA.GreatTPA;
import xyz.toilet.greatTPA.RequestManager;
import xyz.toilet.greatTPA.RequestType;
import xyz.toilet.greatTPA.TeleportRequest;

public class TPAListCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public TPAListCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.plugin.getMessage("player-only-command"));
         return true;
      } else {
         Player player = (Player)sender;
         RequestManager requestManager = this.plugin.getRequestManager();
         List<TeleportRequest> requests = requestManager.getRequestsForReceiver(player.getUniqueId());
         if (requests != null && !requests.isEmpty()) {
            player.sendMessage(this.plugin.getMessage("tpalist-title"));
            int index = 1;

            for(Iterator var9 = requests.iterator(); var9.hasNext(); ++index) {
               TeleportRequest request = (TeleportRequest)var9.next();
               Player senderPlayer = Bukkit.getPlayer(request.getSender());
               String senderName = senderPlayer != null ? senderPlayer.getName() : "未知玩家";
               String requestType = this.plugin.getMessage(request.getType() == RequestType.TPA_TO ? "tpalist-type-to" : "tpalist-type-here");
               player.sendMessage(this.plugin.getMessage("tpalist-entry", index, senderName, requestType));
            }

            return true;
         } else {
            player.sendMessage(this.plugin.getMessage("tpalist-empty"));
            return true;
         }
      }
   }
}
