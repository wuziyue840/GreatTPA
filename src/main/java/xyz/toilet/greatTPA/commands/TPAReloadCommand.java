package xyz.toilet.greatTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.toilet.greatTPA.GreatTPA;

public class TPAReloadCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public TPAReloadCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!sender.hasPermission("greattpa.reload")) {
         sender.sendMessage(this.plugin.getMessage("no-permission"));
         return true;
      } else {
         this.plugin.reloadConfig();
         this.plugin.saveDefaultConfig();
         this.plugin.getMessageManager().reloadMessages();
         boolean rtpEnabled = this.plugin.getConfig().getBoolean("random-teleport.enabled", true);
         String var10001 = this.plugin.getMessage("config-reloaded");
         sender.sendMessage(var10001 + " §7(随机传送: " + (rtpEnabled ? "§a启用" : "§c禁用") + "§7)");
         return true;
      }
   }
}
