package xyz.toilet.greatTPA;

import java.util.UUID;

public class TeleportRequest {
   private final UUID sender;
   private final UUID receiver;
   private final long timestamp;
   private final RequestType type;

   public TeleportRequest(UUID sender, UUID receiver, long timestamp, RequestType type) {
      this.sender = sender;
      this.receiver = receiver;
      this.timestamp = timestamp;
      this.type = type;
   }

   public UUID getSender() {
      return this.sender;
   }

   public UUID getReceiver() {
      return this.receiver;
   }

   public long getTimestamp() {
      return this.timestamp;
   }

   public RequestType getType() {
      return this.type;
   }

   public boolean isExpired(long timeout) {
      return System.currentTimeMillis() - this.timestamp > timeout;
   }
}
