package tw.kid7.BannerMaker.command;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class CommandComponent implements CommandExecutor, TabCompleter {
    //名稱
    private String name;
    //介紹
    private String description;
    //權限
    private String permission;
    //使用方法
    private String usage;
    //僅能由玩家執行
    private boolean onlyFromPlayer;

    private Map<String, CommandComponent> subCommands = Maps.newHashMap();

    public CommandComponent(String name, String description, String permission, String usage, boolean onlyFromPlayer) {
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.usage = usage;
        this.onlyFromPlayer = onlyFromPlayer;
    }

    public final boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        //有參數
        if (args.length > 0) {
            //試著找出子指令
            CommandComponent subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                //若有子指令，執行子指令
                return subCommand.onCommand(commandSender, command, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
            }
        }
        //無參數或無該子指令，執行本身
        //檢查權限
        if (!hasPermission(commandSender)) {
            commandSender.sendMessage(ChatColor.RED + "Lacking permission " + permission);
        }
        //限玩家執行
        if (onlyFromPlayer && !(commandSender instanceof Player)) {
            commandSender.sendMessage(ChatColor.RED + "This command can only be used by players in game");
        }
        //執行指令
        return executeCommand(commandSender, command, label, args);
    }

    public final List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            //本身的建議
            List<String> suggestions = getSuggestions(commandSender, command, label, args);
            //根據權限產生子指令清單
            for (Map.Entry<String, CommandComponent> subCommandEntry : subCommands.entrySet()) {
                if (subCommandEntry.getValue().hasPermission(commandSender)) {
                    suggestions.add(subCommandEntry.getKey());
                }
            }
            //取得部分指令
            String partialCommand = args[0];
            //匹配部分指令
            StringUtil.copyPartialMatches(partialCommand, suggestions, completions);
        } else if (args.length > 1) {
            //試著找出子指令
            CommandComponent subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                //若有子指令，交給子指令處理
                return subCommand.onTabComplete(commandSender, command, label + " " + args[0], Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return completions;
    }

    /**
     * 註冊子指令
     *
     * @param label            標籤
     * @param commandComponent 指令實體
     */
    public final void registerSubCommand(String label, CommandComponent commandComponent) {
        Preconditions.checkArgument(label.length() > 0, "Label cannot be empty");
        subCommands.put(label.toLowerCase(), commandComponent);
    }

    public final boolean hasPermission(CommandSender commandSender) {
        return permission == null || permission.isEmpty() || commandSender.hasPermission(permission) || commandSender instanceof ConsoleCommandSender;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final String getUsage() {
        return usage;
    }

    /**
     * 執行指令
     *
     * @param commandSender 指令發送者
     * @param command       指令
     * @param label         標籤
     * @param args          參數
     * @return 是否執行成功
     */
    public boolean executeCommand(CommandSender commandSender, Command command, String label, String[] args) {
        //未實作指令，可能只是群組，顯示子指令清單
        //根據權限產生子指令清單
        for (Map.Entry<String, CommandComponent> subCommandEntry : subCommands.entrySet()) {
            if (subCommandEntry.getValue().hasPermission(commandSender)) {
                CommandComponent subCommand = subCommandEntry.getValue();
                commandSender.sendMessage(subCommand.getUsage() + ChatColor.GRAY + " - " + subCommand.getDescription());
            }
        }
        return true;
    }

    /**
     * 自動補全建議
     *
     * @param commandSender 指令發送者
     * @param command       指令
     * @param label         標籤
     * @param args          參數
     * @return 自動補全建議
     */
    public List<String> getSuggestions(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
}