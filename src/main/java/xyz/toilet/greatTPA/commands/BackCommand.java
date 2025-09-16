package xyz.toilet.greatTPA.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.toilet.greatTPA.GreatTPA;
import xyz.toilet.greatTPA.PlayerData;

public class BackCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public BackCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.plugin.getMessage("player-only-command"));
         return true;
      } else {
         Player player = (Player)sender;
         if (!this.plugin.getConfig().getBoolean("back-enabled", true)) {
            player.sendMessage(this.plugin.getMessage("back-disabled"));
            return true;
         } else {
            PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (playerData.isOnCooldown("back")) {
               long remaining = playerData.getRemainingCooldown("back");
               player.sendMessage(this.plugin.getMessage("cooldown-message", remaining));
               return true;
            } else {
               this.plugin.getCastManager().startCast(player, "back", () -> {
                  Location deathLoc = playerData.getLastDeathLocation();
                  if (deathLoc == null) {
                     player.sendMessage(this.plugin.getMessage("no-death-location"));
                  } else {
                     player.teleport(deathLoc);
                     player.sendMessage(this.plugin.getMessage("back-success"));
                     playerData.setCooldown("back");
                  }
               });
               return true;
            }
         }
      }
   }
}
