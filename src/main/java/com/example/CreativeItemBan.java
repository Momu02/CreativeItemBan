package com.example;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreativeItemBan extends JavaPlugin implements Listener {
    private Set<Material> creativeBannedItems = new HashSet<>();
    private Set<String> giveBannedItems = new HashSet<>();
    private boolean debugMode = false;
    private String creativeBypassPerm;
    private String giveBypassPerm;
    private String creativeBlockedMsg;
    private String giveBlockedMsg;
    private String bypassMsg;
    private String reloadSuccessMsg;
    
    @Override
    public void onEnable() {
        getLogger().info("§aCreativeItemBan 插件 v2.0 已启用！");
        saveDefaultConfig();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        
        // 注册命令
        getCommand("creativeitemban").setExecutor(new CreativeItemBanCommand(this));
        
        logConfigInfo();
    }
    
    @Override
    public void onDisable() {
        getLogger().info("§cCreativeItemBan 插件已禁用");
    }
    
    private void loadConfig() {
        reloadConfig();
        
        // 清空以前的配置
        creativeBannedItems.clear();
        giveBannedItems.clear();
        
        // 加载配置
        debugMode = getConfig().getBoolean("debug", false);
        
        // 加载创造模式禁止物品
        for (String itemName : getConfig().getStringList("creative-banned.items")) {
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                creativeBannedItems.add(material);
                if (debugMode) getLogger().info("§e[DEBUG] 添加创造禁止物品: " + material);
            } catch (IllegalArgumentException e) {
                getLogger().warning("无效的创造模式物品名: " + itemName);
            }
        }
        
        // 加载give命令禁止物品（支持带命名空间和不带命名空间）
        for (String itemName : getConfig().getStringList("give-banned.items")) {
            String normalizedItem = itemName.toLowerCase().trim();
            
            // 如果已经是minecraft:格式，直接使用
            if (normalizedItem.startsWith("minecraft:")) {
                giveBannedItems.add(normalizedItem);
                if (debugMode) getLogger().info("§e[DEBUG] 添加give禁止物品(带命名空间): " + normalizedItem);
            } else {
                // 尝试转换为Material验证，同时添加两种格式
                try {
                    Material material = Material.valueOf(normalizedItem.toUpperCase());
                    giveBannedItems.add("minecraft:" + normalizedItem.toLowerCase());
                    giveBannedItems.add(normalizedItem); // 同时添加不带命名空间的版本
                    if (debugMode) getLogger().info("§e[DEBUG] 添加give禁止物品: " + normalizedItem + " (解析为: " + material + ")");
                } catch (IllegalArgumentException e) {
                    getLogger().warning("无效的give命令物品名: " + itemName);
                }
            }
        }
        
        // 加载权限设置
        creativeBypassPerm = getConfig().getString("permissions.creative-bypass", "creativeitemban.bypass.creative");
        giveBypassPerm = getConfig().getString("permissions.give-bypass", "creativeitemban.bypass.give");
        // 加载消息并进行颜色代码转换
        String rawCreativeMsg = getConfig().getString("messages.creative-blocked", "&c这个物品在创造模式中被禁止获取");
        String rawGiveMsg = getConfig().getString("messages.give-blocked", "&c这个物品无法通过give命令获取");
        String rawBypassMsg = getConfig().getString("messages.bypass-enabled", "&a你拥有权限，可以获取禁止物品");
        String rawReloadSuccessMsg = getConfig().getString("messages.reload-success", "&a配置文件已重新加载");
        
        creativeBlockedMsg = ChatColor.translateAlternateColorCodes('&', rawCreativeMsg);
        giveBlockedMsg = ChatColor.translateAlternateColorCodes('&', rawGiveMsg);
        bypassMsg = ChatColor.translateAlternateColorCodes('&', rawBypassMsg);
        reloadSuccessMsg = ChatColor.translateAlternateColorCodes('&', rawReloadSuccessMsg);
    }
    
    private void logConfigInfo() {
        getLogger().info("§a===== CreativeItemBan 配置信息 =====");
        getLogger().info("§a创造模式禁止物品: §e" + creativeBannedItems.size() + " 个");
        getLogger().info("§aGive命令禁止物品: §e" + giveBannedItems.size() + " 个");
        getLogger().info("§a创造绕过权限: §e" + creativeBypassPerm);
        getLogger().info("§aGive绕过权限: §e" + giveBypassPerm);
        getLogger().info("§a调试模式: §e" + (debugMode ? "开启" : "关闭"));
        getLogger().info("§a=================================");
    }
    
    /**
     * 检查玩家是否有权限绕过限制
     */
    private boolean hasBypassPermission(Player player, String permissionType) {
        boolean hasGeneralBypass = player.hasPermission("creativeitemban.bypass");
        boolean hasSpecificBypass = false;
        
        if ("creative".equals(permissionType)) {
            hasSpecificBypass = player.hasPermission(creativeBypassPerm);
        } else if ("give".equals(permissionType)) {
            hasSpecificBypass = player.hasPermission(giveBypassPerm);
        }
        
        return hasGeneralBypass || hasSpecificBypass;
    }
    
    /**
     * 处理创造模式物品获取事件
     */
    @EventHandler
    public void onCreativeInventory(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // 检查是否是创造模式
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) return;
        
        // 检查权限
        if (hasBypassPermission(player, "creative")) {
            if (debugMode) player.sendMessage(bypassMsg);
            return;
        }
        
        // 获取点击的物品
        ItemStack clickedItem = event.getCursor();
        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            Material itemType = clickedItem.getType();
            
            if (debugMode) {
                getLogger().info("[DEBUG] 玩家 " + player.getName() + " 尝试在创造模式获取: " + itemType);
            }
            
            if (creativeBannedItems.contains(itemType)) {
                event.setCancelled(true);
                event.setCursor(null); // 清空光标
                player.sendMessage(creativeBlockedMsg);
                
                if (debugMode) {
                    getLogger().info("[DEBUG] 阻止玩家 " + player.getName() + " 获取禁止物品: " + itemType);
                }
                return;
            }
        }
        
        // 也检查被替换的物品
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
            Material currentType = event.getCurrentItem().getType();
            
            if (debugMode) {
                getLogger().info("[DEBUG] 玩家 " + player.getName() + " 点击库存物品: " + currentType);
            }
            
            if (creativeBannedItems.contains(currentType)) {
                event.setCancelled(true);
                player.sendMessage(creativeBlockedMsg);
                
                if (debugMode) {
                    getLogger().info("[DEBUG] 阻止玩家 " + player.getName() + " 操作禁止物品: " + currentType);
                }
            }
        }
    }
    
    /**
     * 处理give命令事件
     */
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        
        if (debugMode) {
            getLogger().info("[DEBUG] 命令事件: " + command + " (玩家: " + player.getName() + ")");
        }
        
        // 只对创造模式玩家检查give命令
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) return;
        
        // 检查权限
        if (hasBypassPermission(player, "give")) {
            if (debugMode) player.sendMessage(bypassMsg);
            return;
        }
        
        String normalizedCmd = command.toLowerCase();
        
        // 检查是否是give命令
        if (normalizedCmd.startsWith("/give ") || normalizedCmd.startsWith("/minecraft:give ")) {
            // 移除命令前缀来获取实际的give部分
            String givePart;
            if (normalizedCmd.startsWith("/minecraft:give ")) {
                givePart = normalizedCmd.substring("/minecraft:give ".length());
            } else {
                givePart = normalizedCmd.substring("/give ".length());
            }
            
            String[] args = givePart.trim().split("\\s+");
            if (args.length < 2) return; // 无效的命令格式
            
            // 解析物品名
            String itemNameArg = null;
            
            // give命令格式：/give <player> <item> [amount] [data] [components]
            // 物品名通常是第二个参数（索引1），但如果有玩家选择器可能不同
            int itemArgIndex = 1;
            
            // 第一个参数通常是目标玩家（可以是选择器）
            // 我们假设第二个参数是物品名
            if (args.length > itemArgIndex) {
                String potentialItem = args[itemArgIndex];
                
                // 检查是否是数字（可能是数量参数）
                try {
                    Integer.parseInt(potentialItem);
                    // 如果是数字，那么物品名可能是下一个参数
                    itemArgIndex = 2;
                    if (args.length > itemArgIndex) {
                        potentialItem = args[itemArgIndex];
                    } else {
                        return; // 命令参数不足
                    }
                } catch (NumberFormatException e) {
                    // 不是数字，就是物品名
                }
                
                String itemToCheck = potentialItem.toLowerCase();
                
                // 检查是否在禁止列表中
                if (giveBannedItems.contains(itemToCheck) || 
                    giveBannedItems.contains("minecraft:" + itemToCheck)) {
                    
                    event.setCancelled(true);
                    player.sendMessage(giveBlockedMsg);
                    
                    if (debugMode) {
                        getLogger().info("[DEBUG] 阻止 " + player.getName() + " 通过give命令获取: " + itemToCheck);
                    }
                    return;
                }
                
                if (debugMode) {
                    getLogger().info("[DEBUG] give命令物品 " + itemToCheck + " 不在禁止列表中");
                }
            }
        }
    }
    
    /**
     * 重新加载配置
     */
    public void reloadConfiguration() {
        loadConfig();
        logConfigInfo();
        // 使用配置中的reload-success消息，但也保持向后兼容
        String message = (reloadSuccessMsg != null) ? reloadSuccessMsg : "配置文件已重新加载";
        getLogger().info(message);
    }
    
    public Set<Material> getCreativeBannedItems() {
        return creativeBannedItems;
    }
    
    public Set<String> getGiveBannedItems() {
        return giveBannedItems;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
}