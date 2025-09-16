package xyz.toilet.greatTPA.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.toilet.greatTPA.GreatTPA;
import xyz.toilet.greatTPA.PlayerData;

public class TPAUnblockCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public TPAUnblockCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.plugin.getMessage("player-only-command"));
         return true;
      } else {
         Player player = (Player)sender;
         if (args.length != 1) {
            player.sendMessage(this.plugin.getMessage("tpunblock-usage"));
            return true;
         } else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
               player.sendMessage(this.plugin.getMessage("player-offline"));
               return true;
            } else {
               PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
               if (!playerData.isBlocking(target.getUniqueId())) {
                  player.sendMessage(this.plugin.getMessage("not-blocked", target.getName()));
                  return true;
               } else {
                  playerData.unblockPlayer(target.getUniqueId());
                  player.sendMessage(this.plugin.getMessage("player-unblocked-success", target.getName()));
                  return true;
               }
            }
         }
      }
   }
}
