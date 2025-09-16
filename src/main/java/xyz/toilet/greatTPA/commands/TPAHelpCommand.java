package xyz.toilet.greatTPA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.toilet.greatTPA.GreatTPA;

public class TPAHelpCommand implements CommandExecutor {
   private final GreatTPA plugin;

   public TPAHelpCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      sender.sendMessage(this.plugin.getMessage("tpa-help"));
      return true;
   }
}
