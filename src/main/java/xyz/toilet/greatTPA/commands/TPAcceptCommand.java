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

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(plugin.getMessage("player-only-command"));
         return true;
      }

      Player player = (Player) sender;
      PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

      // 检查冷却时间
      if (playerData.isOnCooldown("tpaaccept")) {
         long remaining = playerData.getRemainingCooldown("tpaaccept");
         player.sendMessage(plugin.getMessage("cooldown-message", remaining));
         return true;
      }

      // 开始施法过程
      plugin.getCastManager().startCast(player, "tpaaccept", () -> {
         RequestManager requestManager = plugin.getRequestManager();
         TeleportRequest request;

         if (args.length == 0) {
            // 接受最新请求
            request = requestManager.getLatestRequest(player.getUniqueId());
         } else if (args.length == 1) {
            // 接受特定玩家的请求
            Player senderPlayer = Bukkit.getPlayer(args[0]);
            if (senderPlayer == null) {
               player.sendMessage(plugin.getMessage("player-offline"));
               return;
            }
            request = requestManager.getSpecificRequest(player.getUniqueId(), senderPlayer.getUniqueId());
         } else {
             request = null;
             player.sendMessage(plugin.getMessage("tpaaccept-usage"));
            return;
         }

         if (request == null) {
            player.sendMessage(plugin.getMessage("no-pending-request"));
            return;
         }

         // 检查请求是否过期
         long timeout = plugin.getConfig().getLong("request-timeout", 30) * 1000;
         if (request.isExpired(timeout)) {
            plugin.getRequestManager().removeRequest(request);
            player.sendMessage(plugin.getMessage("request-expired"));
            return;
         }

         Player senderPlayer = Bukkit.getPlayer(request.getSender());
         if (senderPlayer == null) {
            player.sendMessage(plugin.getMessage("player-offline"));
            plugin.getRequestManager().removeRequest(request);
            return;
         }

         // 检查维度
         if (plugin.getConfig().getBoolean("dimension-check", true) &&
                 !player.getWorld().getUID().equals(senderPlayer.getWorld().getUID())) {
            player.sendMessage(plugin.getMessage("dimension-mismatch"));
            return;
         }

         // 开始传送等待
         Runnable teleportRunnable = () -> {
            // 执行传送
            if (request.getType() == RequestType.TPA_TO) {
               // 发送方传送到接收方
               senderPlayer.teleport(player.getLocation());
               senderPlayer.sendMessage(plugin.getMessage("teleport-success"));
            } else if (request.getType() == RequestType.TPA_HERE) {
               // 接收方传送到发送方
               player.teleport(senderPlayer.getLocation());
               player.sendMessage(plugin.getMessage("teleport-success"));
            }

            player.sendMessage(plugin.getMessage("request-accepted", senderPlayer.getName()));
            senderPlayer.sendMessage(plugin.getMessage("request-accepted-by", player.getName()));

            // 移除请求
            plugin.getRequestManager().removeRequest(request);

            // 设置冷却时间
            playerData.setCooldown("tpaaccept");
         };

         // 开始传送等待
         plugin.getCastManager().startWait(player, "tpaaccept", teleportRunnable);
      });

      return true;
   }
}