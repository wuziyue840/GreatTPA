package xyz.toilet.greatTPA;

import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MessageManager {
   private FileConfiguration messages;
   private final File messagesFile;
   private final GreatTPA plugin;

   public MessageManager(GreatTPA plugin) {
      this.plugin = plugin;
      this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
      this.reloadMessages();
   }

   public void reloadMessages() {
      if (!this.messagesFile.exists()) {
         this.plugin.saveResource("messages.yml", false);
      }

      this.messages = YamlConfiguration.loadConfiguration(this.messagesFile);
   }

   public String getMessage(String key, Object... args) {
      String message = this.messages.getString(key, "&c消息未配置: " + key);
      message = ChatColor.translateAlternateColorCodes('&', message);

      for(int i = 0; i < args.length; ++i) {
         message = message.replace("{" + i + "}", String.valueOf(args[i]));
      }

      return message;
   }
}
