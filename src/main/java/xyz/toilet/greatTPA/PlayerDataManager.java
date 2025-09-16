package xyz.toilet.greatTPA;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
   private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap();
   private final GreatTPA plugin;

   public PlayerDataManager(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public PlayerData getPlayerData(UUID uuid) {
      return (PlayerData)this.playerDataMap.computeIfAbsent(uuid, (k) -> {
         return new PlayerData(this.plugin);
      });
   }

   public void saveAllData() {
   }
}
