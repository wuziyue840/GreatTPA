package xyz.toilet.greatTPA;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import xyz.toilet.greatTPA.commands.BackCommand;
import xyz.toilet.greatTPA.commands.TPABlockCommand;
import xyz.toilet.greatTPA.commands.TPACancelCommand;
import xyz.toilet.greatTPA.commands.TPACommand;
import xyz.toilet.greatTPA.commands.TPADenyCommand;
import xyz.toilet.greatTPA.commands.TPAHelpCommand;
import xyz.toilet.greatTPA.commands.TPAHereCommand;
import xyz.toilet.greatTPA.commands.TPAListCommand;
import xyz.toilet.greatTPA.commands.TPARandomTeleportCommand;
import xyz.toilet.greatTPA.commands.TPAReloadCommand;
import xyz.toilet.greatTPA.commands.TPAToggleCommand;
import xyz.toilet.greatTPA.commands.TPAUnblockCommand;
import xyz.toilet.greatTPA.commands.TPAcceptCommand;

public class CommandAliasManager {
   private final GreatTPA plugin;

   public CommandAliasManager(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public void registerCommands() {
      ConfigurationSection aliases = this.plugin.getConfig().getConfigurationSection("command-aliases");
      if (aliases != null) {
         this.registerCommand(aliases.getString("tpa"), new TPACommand(this.plugin));
         this.registerCommand(aliases.getString("tpahere"), new TPAHereCommand(this.plugin));
         this.registerCommand(aliases.getString("tpaaccept"), new TPAcceptCommand(this.plugin));
         this.registerCommand(aliases.getString("tpadeny"), new TPADenyCommand(this.plugin));
         this.registerCommand(aliases.getString("tpacancel"), new TPACancelCommand(this.plugin));
         this.registerCommand(aliases.getString("back"), new BackCommand(this.plugin));
         this.registerCommand(aliases.getString("tpatoggle"), new TPAToggleCommand(this.plugin));
         this.registerCommand(aliases.getString("tpablock"), new TPABlockCommand(this.plugin));
         this.registerCommand(aliases.getString("tpaunblock"), new TPAUnblockCommand(this.plugin));
         this.registerCommand(aliases.getString("tpahelp"), new TPAHelpCommand(this.plugin));
         this.registerCommand(aliases.getString("tpareload"), new TPAReloadCommand(this.plugin));
         this.registerCommand(aliases.getString("tpalist"), new TPAListCommand(this.plugin));
         this.registerCommand(aliases.getString("tpartp"), new TPARandomTeleportCommand(this.plugin));
      }
   }

   private void registerCommand(String alias, Object executor) {
      if (alias != null && !alias.isEmpty()) {
         this.plugin.getCommand(alias).setExecutor((CommandExecutor)executor);
      }

   }
}
