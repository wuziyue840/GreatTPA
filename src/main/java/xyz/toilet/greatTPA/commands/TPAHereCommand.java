package xyz.toilet.greatTPA.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.toilet.greatTPA.GreatTPA;
import xyz.toilet.greatTPA.PlayerData;
import xyz.toilet.greatTPA.RequestType;
import xyz.toilet.greatTPA.TeleportRequest;

public class TPAHereCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public TPAHereCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.plugin.getMessage("player-only-command"));
         return true;
      } else {
         Player player = (Player)sender;
         if (args.length != 1) {
            player.sendMessage(this.plugin.getMessage("tpahere-usage"));
            return true;
         } else {
            PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (playerData.isOnCooldown("tpahere")) {
               long remaining = playerData.getRemainingCooldown("tpahere");
               player.sendMessage(this.plugin.getMessage("cooldown-message", remaining));
               return true;
            } else {
               this.plugin.getCastManager().startCast(player, "tpahere", () -> {
                  Player target = Bukkit.getPlayer(args[0]);
                  if (target == null) {
                     player.sendMessage(this.plugin.getMessage("player-offline"));
                  } else if (player.getUniqueId().equals(target.getUniqueId())) {
                     player.sendMessage(this.plugin.getMessage("cannot-tpa-self"));
                  } else if (this.plugin.getConfig().getBoolean("dimension-check", true) && !player.getWorld().getUID().equals(target.getWorld().getUID())) {
                     player.sendMessage(this.plugin.getMessage("dimension-mismatch"));
                  } else {
                     PlayerData targetData = this.plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
                     if (targetData.isDndMode()) {
                        player.sendMessage(this.plugin.getMessage("dnd-active"));
                     } else if (targetData.isBlocking(player.getUniqueId())) {
                        player.sendMessage(this.plugin.getMessage("player-blocked"));
                     } else {
                        TeleportRequest request = new TeleportRequest(player.getUniqueId(), target.getUniqueId(), System.currentTimeMillis(), RequestType.TPA_HERE);
                        this.plugin.getRequestManager().addRequest(request);
                        player.sendMessage(this.plugin.getMessage("tpahere-sent", target.getName()));
                        target.sendMessage(this.plugin.getMessage("tpahere-received", player.getName()));
                        playerData.setCooldown("tpahere");
                     }
                  }
               });
               return true;
            }
         }
      }
   }
}
