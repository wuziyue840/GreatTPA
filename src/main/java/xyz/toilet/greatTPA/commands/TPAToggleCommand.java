package xyz.toilet.greatTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.toilet.greatTPA.GreatTPA;
import xyz.toilet.greatTPA.PlayerData;

public class TPAToggleCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public TPAToggleCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.plugin.getMessage("player-only-command"));
         return true;
      } else {
         Player player = (Player)sender;
         PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
         boolean newState = playerData.toggleDndMode();
         if (newState) {
            player.sendMessage(this.plugin.getMessage("dnd-enabled"));
         } else {
            player.sendMessage(this.plugin.getMessage("dnd-disabled"));
         }

         return true;
      }
   }
}
