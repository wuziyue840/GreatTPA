package xyz.toilet.greatTPA;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RequestManager {
   private final Map<UUID, List<TeleportRequest>> requests = new ConcurrentHashMap();
   private final GreatTPA plugin;

   public RequestManager(GreatTPA plugin) {
      this.plugin = plugin;
   }

   public Map<UUID, List<TeleportRequest>> getRequests() {
      return this.requests;
   }

   public void addRequest(TeleportRequest request) {
      ((List)this.requests.computeIfAbsent(request.getReceiver(), (k) -> {
         return new CopyOnWriteArrayList();
      })).add(request);
   }

   public TeleportRequest getLatestRequest(UUID receiver) {
      List<TeleportRequest> receiverRequests = (List)this.requests.get(receiver);
      return receiverRequests != null && !receiverRequests.isEmpty() ? (TeleportRequest)receiverRequests.get(receiverRequests.size() - 1) : null;
   }

   public TeleportRequest getSpecificRequest(UUID receiver, UUID sender) {
      List<TeleportRequest> receiverRequests = (List)this.requests.get(receiver);
      if (receiverRequests != null) {
         Iterator var4 = receiverRequests.iterator();

         while(var4.hasNext()) {
            TeleportRequest req = (TeleportRequest)var4.next();
            if (req.getSender().equals(sender)) {
               return req;
            }
         }
      }

      return null;
   }

   public void removeRequest(TeleportRequest request) {
      List<TeleportRequest> receiverRequests = (List)this.requests.get(request.getReceiver());
      if (receiverRequests != null) {
         receiverRequests.remove(request);
      }

   }

   public void clearRequests(UUID playerId) {
      this.requests.remove(playerId);
      Iterator var2 = this.requests.values().iterator();

      while(var2.hasNext()) {
         List<TeleportRequest> reqList = (List)var2.next();
         reqList.removeIf((req) -> {
            return req.getSender().equals(playerId);
         });
      }

   }

   public void startCleanupTask() {
      (new BukkitRunnable() {
         public void run() {
            long currentTime = System.currentTimeMillis();
            long timeout = RequestManager.this.plugin.getConfig().getLong("request-timeout", 30L) * 1000L;
            Iterator var5 = RequestManager.this.requests.entrySet().iterator();

            while(var5.hasNext()) {
               Entry<UUID, List<TeleportRequest>> entry = (Entry)var5.next();
               List<TeleportRequest> reqList = (List)entry.getValue();
               Iterator iterator = reqList.iterator();

               while(iterator.hasNext()) {
                  TeleportRequest req = (TeleportRequest)iterator.next();
                  if (currentTime - req.getTimestamp() > timeout) {
                     Player senderPlayer = Bukkit.getPlayer(req.getSender());
                     Player receiverPlayer = Bukkit.getPlayer(req.getReceiver());
                     if (senderPlayer != null && receiverPlayer != null) {
                        senderPlayer.sendMessage(RequestManager.this.plugin.getMessage("request-expired-sender", receiverPlayer.getName()));
                     }

                     iterator.remove();
                  }
               }
            }

         }
      }).runTaskTimer(this.plugin, 6000L, 6000L);
   }

   public List<TeleportRequest> getRequestsForReceiver(UUID receiver) {
      return (List)this.requests.getOrDefault(receiver, Collections.emptyList());
   }
}
