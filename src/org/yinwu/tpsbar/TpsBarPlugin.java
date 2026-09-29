package org.yinwu.tpsbar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * TpsBarAlias —— 把 /tpsbar 变成 Canvas 内置命令 /regionbar tps_bar 的别名。
 *
 * 设计要点：
 *  1) 用 dispatchCommand 转发，而不是自己实现 BossBar —— Canvas 的 RegionizedTpsBar
 *     是按“区域(region)”统计的，自己实现拿不到同样的数据。
 *  2) 转发时保持“以发送者身份执行”，所以 Canvas 的权限节点 canvas.command.regionbar
 *     仍然会被正常检查：没有该权限的玩家用 /tpsbar 依然会被拦下（不会提权）。
 *  3) 本服是 Canvas（Folia 系，区域化线程）：
 *     - 无参数时，玩家命令本身就跑在该玩家的 region 线程上，直接转发即可；
 *     - 带参数时，转发会在“目标玩家”的 region 线程上执行，避免跨区域访问玩家。
 *  4) /tpsbar <玩家> 需要 tpsbar.others（默认仅 OP）。
 */
public class TpsBarPlugin extends JavaPlugin {

    private static final String TARGET_COMMAND = "regionbar tps_bar";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("tpsbar")) {
            return false;
        }

        // 不带参数：玩家切换自己的 TPS 条（已在自身 region 线程上）；控制台则提示用法
        if (args.length == 0) {
            if (sender instanceof Player) {
                return getServer().dispatchCommand(sender, TARGET_COMMAND);
            }
            sender.sendMessage("§e用法: /tpsbar <玩家名>");
            return true;
        }

        // 带参数：切换指定玩家的 TPS 条，需要 tpsbar.others
        if (!sender.hasPermission("tpsbar.others")) {
            sender.sendMessage("§c你没有权限切换其他玩家的 TPS 条。");
            return true;
        }

        final Player target = getServer().getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§c找不到在线玩家: " + args[0]);
            return true;
        }

        // Folia/Canvas：在目标玩家自己的 region 线程上转发，避免跨区域线程访问
        final CommandSender from = sender;
        target.getScheduler().run(this,
                task -> getServer().dispatchCommand(from, TARGET_COMMAND + " " + target.getName()),
                null);
        return true;
    }
}
