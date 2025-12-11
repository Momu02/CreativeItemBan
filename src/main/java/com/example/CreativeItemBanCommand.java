package com.example;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CreativeItemBanCommand implements CommandExecutor, TabCompleter {
    
    private final CreativeItemBan plugin;
    
    public CreativeItemBanCommand(CreativeItemBan plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReload(sender);
            case "status":
                return handleStatus(sender);
            case "debug":
                return handleDebug(sender, args);
            case "list":
                return handleList(sender);
            case "help":
                sendUsage(sender);
                return true;
            case "version":
                sender.sendMessage(ChatColor.GREEN + "CreativeItemBan v2.0");
                sender.sendMessage(ChatColor.GRAY + "支持分别配置创造禁止和give禁止物品");
                sender.sendMessage(ChatColor.GRAY + "支持LuckyPerms权限系统");
                return true;
            default:
                sender.sendMessage(ChatColor.RED + "未知子命令！使用 /creativeitemban help 查看帮助");
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("creativeitemban.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }
        
        plugin.reloadConfiguration();
        sender.sendMessage(ChatColor.GREEN + "配置文件已重新加载！");
        return true;
    }
    
    private boolean handleStatus(CommandSender sender) {
        if (!sender.hasPermission("creativeitemban.admin") && !sender.hasPermission("creativeitemban.view")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "===== CreativeItemBan 状态 =====");
        sender.sendMessage(ChatColor.GREEN + "插件状态: " + ChatColor.YELLOW + "运行中");
        sender.sendMessage(ChatColor.GREEN + "创造禁止物品: " + ChatColor.YELLOW + plugin.getCreativeBannedItems().size() + " 个");
        sender.sendMessage(ChatColor.GREEN + "Give禁止物品: " + ChatColor.YELLOW + plugin.getGiveBannedItems().size() + " 个");
        sender.sendMessage(ChatColor.GREEN + "调试模式: " + ChatColor.YELLOW + (plugin.isDebugMode() ? "开启" : "关闭"));
        sender.sendMessage(ChatColor.GOLD + "=============================");
        return true;
    }
    
    private boolean handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("creativeitemban.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "用法: /creativeitemban debug <on|off>");
            return true;
        }
        
        String debugArg = args[1].toLowerCase();
        if ("on".equals(debugArg)) {
            // 保存到配置文件
            plugin.getConfig().set("debug", true);
            plugin.saveConfig();
            plugin.reloadConfiguration();
            sender.sendMessage(ChatColor.GREEN + "调试模式已开启！");
        } else if ("off".equals(debugArg)) {
            plugin.getConfig().set("debug", false);
            plugin.saveConfig();
            plugin.reloadConfiguration();
            sender.sendMessage(ChatColor.GREEN + "调试模式已关闭！");
        } else {
            sender.sendMessage(ChatColor.RED + "无效的参数！使用 on 或 off");
        }
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("creativeitemban.admin") && !sender.hasPermission("creativeitemban.view")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令！");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "===== 禁止物品列表 =====");
        
        Set<Material> creativeBanned = plugin.getCreativeBannedItems();
        Set<String> giveBanned = plugin.getGiveBannedItems();
        
        // 显示创造模式禁止物品
        sender.sendMessage(ChatColor.GREEN + "创造模式禁止物品 (" + creativeBanned.size() + "个):");
        if (creativeBanned.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "  无");
        } else {
            List<String> creativeItems = new ArrayList<>();
            for (Material material : creativeBanned) {
                creativeItems.add(ChatColor.YELLOW + material.toString());
            }
            sender.sendMessage(String.join(ChatColor.GRAY + ", ", creativeItems));
        }
        
        sender.sendMessage("");
        
        // 显示give命令禁止物品
        sender.sendMessage(ChatColor.GREEN + "Give命令禁止物品 (" + giveBanned.size() + "个):");
        if (giveBanned.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "  无");
        } else {
            List<String> giveItems = new ArrayList<>();
            for (String item : giveBanned) {
                giveItems.add(ChatColor.YELLOW + item);
            }
            sender.sendMessage(String.join(ChatColor.GRAY + ", ", giveItems));
        }
        
        sender.sendMessage(ChatColor.GOLD + "=========================");
        return true;
    }
    
    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== CreativeItemBan 命令帮助 =====");
        sender.sendMessage(ChatColor.GREEN + "/creativeitemban reload " + ChatColor.GRAY + "- 重新加载配置文件");
        sender.sendMessage(ChatColor.GREEN + "/creativeitemban status " + ChatColor.GRAY + "- 查看插件状态");
        sender.sendMessage(ChatColor.GREEN + "/creativeitemban list " + ChatColor.GRAY + "- 查看禁止物品列表");
        sender.sendMessage(ChatColor.GREEN + "/creativeitemban debug <on|off> " + ChatColor.GRAY + "- 开启/关闭调试模式");
        sender.sendMessage(ChatColor.GREEN + "/creativeitemban version " + ChatColor.GRAY + "- 查看插件版本");
        sender.sendMessage(ChatColor.GREEN + "/creativeitemban help " + ChatColor.GRAY + "- 显示此帮助");
        sender.sendMessage(ChatColor.GOLD + "=================================");
        sender.sendMessage(ChatColor.GRAY + "权限节点: creativeitemban.admin (管理员权限)");
        sender.sendMessage(ChatColor.GRAY + "权限节点: creativeitemban.view (查看权限)");
        sender.sendMessage(ChatColor.GRAY + "绕过权限: creativeitemban.bypass, creativeitemban.bypass.creative, creativeitemban.bypass.give");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "reload", "status", "list", "debug", "version");
            return filterTabComplete(subCommands, args[0]);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            List<String> debugOptions = Arrays.asList("on", "off");
            return filterTabComplete(debugOptions, args[1]);
        }
        return new ArrayList<>();
    }
    
    private List<String> filterTabComplete(List<String> options, String input) {
        List<String> matches = new ArrayList<>();
        String lowerInput = input.toLowerCase();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lowerInput)) {
                matches.add(option);
            }
        }
        return matches;
    }
}