package xyz.toilet.greatTPA;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GreatTPA extends JavaPlugin {
   private RequestManager requestManager;
   private PlayerDataManager playerDataManager;
   private CastManager castManager;
   private MessageManager messageManager;
   private CommandAliasManager aliasManager;

   public void onEnable() {
      this.saveDefaultConfig();
      this.createMessagesFile();
      this.requestManager = new RequestManager(this);
      this.playerDataManager = new PlayerDataManager(this);
      this.castManager = new CastManager(this);
      this.messageManager = new MessageManager(this);
      this.aliasManager = new CommandAliasManager(this);
      this.aliasManager.registerCommands();
      this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
      this.requestManager.startCleanupTask();
      this.getLogger().info("GreatTPA 插件已启用!");
   }

   private void createMessagesFile() {
      this.saveResource("messages.yml", false);
   }

   public String getMessage(String key, Object... args) {
      return this.messageManager.getMessage(key, args);
   }

   public boolean checkDimension(Player p1, Player p2) {
      return this.getConfig().getBoolean("dimension-check", true) ? p1.getWorld().getUID().equals(p2.getWorld().getUID()) : true;
   }

   public RequestManager getRequestManager() {
      return this.requestManager;
   }

   public PlayerDataManager getPlayerDataManager() {
      return this.playerDataManager;
   }

   public CastManager getCastManager() {
      return this.castManager;
   }

   public MessageManager getMessageManager() {
      return this.messageManager;
   }

   public void onDisable() {
      this.playerDataManager.saveAllData();
      this.getLogger().info("GreatTPA 插件已禁用!");
   }
}
