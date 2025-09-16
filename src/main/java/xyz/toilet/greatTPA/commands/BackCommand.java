package xyz.toilet.greatTPA.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.toilet.greatTPA.GreatTPA;
import xyz.toilet.greatTPA.PlayerData;
import xyz.toilet.greatTPA.*;


public class BackCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public BackCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(plugin.getMessage("player-only-command"));
         return true;
      }

      Player player = (Player) sender;

      // 检查功能是否启用
      if (!plugin.getConfig().getBoolean("back-enabled", true)) {
         player.sendMessage(plugin.getMessage("back-disabled"));
         return true;
      }

      PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

      // 检查冷却时间
      if (playerData.isOnCooldown("back")) {
         long remaining = playerData.getRemainingCooldown("back");
         player.sendMessage(plugin.getMessage("cooldown-message", remaining));
         return true;
      }

      // 开始施法过程
      plugin.getCastManager().startCast(player, "back", () -> {
         Location deathLoc = playerData.getLastDeathLocation();
         if (deathLoc == null) {
            player.sendMessage(plugin.getMessage("no-death-location"));
            return;
         }

         // 开始传送等待
         Runnable teleportRunnable = () -> {
            player.teleport(deathLoc);
            player.sendMessage(plugin.getMessage("back-success"));

            // 设置冷却时间
            playerData.setCooldown("back");
         };

         // 开始传送等待
         plugin.getCastManager().startWait(player, "back", teleportRunnable);
      });

      return true;
   }
}