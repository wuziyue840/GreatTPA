package xyz.toilet.greatTPA.commands;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.toilet.greatTPA.GreatTPA;
import xyz.toilet.greatTPA.PlayerData;

public class TPARandomTeleportCommand implements CommandExecutor {
   private final GreatTPA plugin;
   private final Random random = new Random();

   public TPARandomTeleportCommand(GreatTPA plugin) {
      this.plugin = plugin;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(plugin.getMessage("player-only-command"));
         return true;
      }

      Player player = (Player) sender;

      if (!plugin.getConfig().getBoolean("tpartp.enabled", true)) {
         player.sendMessage(plugin.getMessage("tpartp-disabled"));
         return true;
      }

      PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());

      if (playerData.isOnCooldown("tpartp_cooldown")) {
         long remaining = playerData.getRemainingCooldown("tpartp_cooldown");
         player.sendMessage(plugin.getMessage("tpartp-cooldown", remaining));
         return true;
      }

      int tpartpCastTime = plugin.getConfig().getInt("tpartp.cast-time", 5);
      double tpartpMaxMove = plugin.getConfig().getDouble("tpartp.max-move-distance", 3.0);

      plugin.getCastManager().startCast(player, "tpartp_spell", () -> {
         if (!plugin.getConfig().getBoolean("tpartp.enabled", true)) {
            player.sendMessage(plugin.getMessage("tpartp-disabled"));
            return;
         }

         // 开始传送等待
         Runnable teleportRunnable = () -> {
            performRandomTeleport(player);
            playerData.setCooldown("tpartp_cooldown");
         };

         // 开始TPARTP传送等待
         plugin.getCastManager().startTpartpWait(player, teleportRunnable);
      }, tpartpCastTime, tpartpMaxMove);

      player.sendMessage(plugin.getMessage("tpartp-start", tpartpCastTime, tpartpMaxMove));
      return true;
   }

   private void performRandomTeleport(Player player) {
      // 获取配置参数
      int tpartpMinDist = plugin.getConfig().getInt("tpartp.min-distance", 100);
      int tpartpMaxDist = plugin.getConfig().getInt("tpartp.max-distance", 500);

      // 随机生成角度和距离
      double angle = random.nextDouble() * 2 * Math.PI;
      int distance = tpartpMinDist + random.nextInt(tpartpMaxDist - tpartpMinDist + 1);

      // 计算目标位置
      Location currentLoc = player.getLocation();
      Location targetLoc = currentLoc.clone();
      targetLoc.add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);

      // 寻找安全位置:cite[2]
      Location safeLocation = findSafeLocation(targetLoc);
      if (safeLocation == null) {
         player.sendMessage(plugin.getMessage("teleport-failed"));
         return;
      }

      // 执行传送:cite[5]:cite[9]
      player.teleport(safeLocation);
      player.sendMessage(plugin.getMessage("tpartp-success"));
   }

   private Location findSafeLocation(Location origin) {
      World w = origin.getWorld();
      int x = origin.getBlockX(), z = origin.getBlockZ();

      // 先单柱扫描（快速路径）
      for (int y = w.getMaxHeight() - 1; y > w.getMinHeight(); y--) {
         Location loc = new Location(w, x, y, z);
         if (isSafeLocation(loc)) {
            return loc.add(0.5, 1, 0.5); // 居中、站在方块上
         }
      }

      // 再 3×3 扩展（兜底）
      for (int y = w.getMaxHeight() - 1; y > w.getMinHeight(); y--) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (dx == 0 && dz == 0) continue; // 跳过已扫描中心柱
               Location loc = new Location(w, x + dx, y, z + dz);
               if (isSafeLocation(loc)) {
                  return loc.add(0.5, 1, 0.5);
               }
            }
         }
      }
      return null;
   }

   private boolean isSafeLocation(Location loc) {
      Block ground = loc.getBlock();
      Block feet = loc.clone().add(0, 1, 0).getBlock();
      Block head = loc.clone().add(0, 2, 0).getBlock();

      return ground.getType().isSolid() &&
              !isDangerous(ground.getType()) &&
              feet.isEmpty() &&
              head.isEmpty();
   }

   private boolean isDangerous(Material m) {
      return m == Material.LAVA || m == Material.MAGMA_BLOCK ||
              m == Material.CACTUS || m == Material.FIRE ||
              m == Material.WATER; // 根据需要增删
   }

   private boolean isPassable(Material material) {
      return !material.isSolid() &&
              material != Material.WATER &&
              material != Material.LAVA;
   }
}