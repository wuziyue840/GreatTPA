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

   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(this.plugin.getMessage("player-only-command"));
         return true;
      } else if (!this.plugin.getConfig().getBoolean("random-teleport.enabled", true)) {
         sender.sendMessage(this.plugin.getMessage("tpartp-disabled"));
         return true;
      } else {
         Player player = (Player)sender;
         PlayerData playerData = this.plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
         if (playerData.isOnCooldown("tpartp")) {
            long remaining = playerData.getRemainingCooldown("tpartp");
            player.sendMessage(this.plugin.getMessage("tpartp-cooldown", remaining));
            return true;
         } else {
            // 在施法前获取玩家当前位置
            Location originalLocation = player.getLocation();
            int castTime = this.plugin.getConfig().getInt("random-teleport.cast-time", 5);

            // 通知玩家施法开始
            player.sendMessage(this.plugin.getMessage("tpartp-casting", castTime));

            new BukkitRunnable(){
               @Override
               public void run(){
                  // 施法完成后检查功能是否仍启用
                  if (!plugin.getConfig().getBoolean("random-teleport.enabled", true)) {
                     player.sendMessage(plugin.getMessage("tpartp-disabled"));
                     return;
                  }

                  // 检查玩家是否仍在游戏中
                  if (!player.isOnline()) {
                     return;
                  }

                  // 重新获取玩家数据（可能已过期）
                  PlayerData currentPlayerData = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                  if (currentPlayerData == null) {
                     player.sendMessage(plugin.getMessage("error-occurred"));
                     return;
                  }

                  // 检查冷却状态（防止施法期间其他人设置冷却）
                  if (currentPlayerData.isOnCooldown("tpartp")) {
                     long rem = currentPlayerData.getRemainingCooldown("tpartp");
                     player.sendMessage(plugin.getMessage("tpartp-cooldown", rem));
                     return;
                  }

                  // 获取配置参数
                  int minDistance = plugin.getConfig().getInt("random-teleport.min-distance", 100);
                  int maxDistance = plugin.getConfig().getInt("random-teleport.max-distance", 500);

                  // 生成随机位置
                  double angle = random.nextDouble() * 2.0D * Math.PI;
                  int distance = minDistance + random.nextInt(maxDistance - minDistance + 1);

                  // 使用施法开始时的位置作为起点
                  Location targetLoc = originalLocation.clone();
                  targetLoc.add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance);

                  // 寻找安全位置
                  Location safeLocation = findSafeLocation(targetLoc);
                  if (safeLocation == null) {
                     player.sendMessage(plugin.getMessage("teleport-failed"));
                  } else {
                     // 执行传送
                     player.teleport(safeLocation.add(0.5, 0, 0.5)); // 传送到方块中心
                     player.sendMessage(plugin.getMessage("tpartp-teleported"));

                     // 设置冷却时间（使用当前玩家数据）
                     currentPlayerData.setCooldown("tpartp");
                  }
               }
            }.runTaskLater(plugin, castTime * 20L);

            return true;
         }
      }
   }

   private Location findSafeLocation(Location location) {
      World world = location.getWorld();
      int x = location.getBlockX();
      int z = location.getBlockZ();

      // 尝试从世界最高点向下搜索
      for (int y = world.getMaxHeight() - 1; y > world.getMinHeight(); y--) {
         Location testLoc = new Location(world, x, y, z);
         if (this.isSafeLocation(testLoc)) {
            return testLoc;
         }
      }

      // 扩展搜索范围（3x3区域）
      for (int y = world.getMaxHeight() - 1; y > world.getMinHeight(); y--) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               Location testLoc = new Location(world, x + dx, y, z + dz);
               if (this.isSafeLocation(testLoc)) {
                  return testLoc;
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

      // 检查危险方块
      if (isDangerous(ground.getType())) return false;

      // 检查位置是否安全
      return ground.getType().isSolid() &&
              isPassable(feet.getType()) &&
              isPassable(head.getType());
   }

   private boolean isDangerous(Material material) {
      String name = material.name();
      return name.contains("LAVA") ||
              name.contains("FIRE") ||
              name.contains("CACTUS") ||
              name.contains("MAGMA") ||
              material == Material.SWEET_BERRY_BUSH;
   }

   private boolean isPassable(Material material) {
      return !material.isSolid() &&
              material != Material.WATER &&
              material != Material.LAVA;
   }
}