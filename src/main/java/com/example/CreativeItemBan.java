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

import java.util.HashSet;
import java.util.Set;

/**
 * 主插件类：负责加载配置、注册命令与监听器，以及基础的创造/give拦截（保留）。
 * 新增：创模加强模式配置与消息、权限读取，注册 CreativeProtectionListener。
 */
public class CreativeItemBan extends JavaPlugin implements Listener {

    // 原有
    private final Set<Material> creativeBannedItems = new HashSet<>();
    private final Set<String> giveBannedItems = new HashSet<>();
    private boolean debugMode = false;

    // 原有 bypass
    private String creativeBypassPerm;
    private String giveBypassPerm;

    // 原有消息
    private String creativeBlockedMsg;
    private String giveBlockedMsg;
    private String bypassMsg;
    private String reloadSuccessMsg;

    // 新增：创模加强模式开关
    private boolean cpEnabled;
    private boolean placeBan;
    private boolean shulkerCheckOnPlace;
    private String shulkerAction; // cancel | sanitize | log-only
    private boolean blockMoveToContainers;
    private boolean blockMiddleClone;
    private boolean clearOnCreativeSwitch;

    // 新增：违规监控配置
    private boolean violationMonitoringEnabled;
    private int violationTimeWindow; // 时间窗口（秒）
    private int violationWarningThreshold; // 警告阈值
    private int violationClearThreshold; // 清空物品阈值
    private String violationBypassPerm; // 绕过违规监控的权限

    // 新增：消息
    private String msgCreativePlaceBlocked;
    private String msgShulkerBlocked;
    private String msgShulkerSanitized;
    private String msgCloneBlocked;
    private String msgContainerMoveBlocked;
    private String msgCleanedOnSwitch;
    private String msgViolationWarning;
    private String msgViolationClearItems;

    // 新增：细分bypass
    private String placeBypassPerm = "creativeitemban.bypass.place";
    private String shulkerBypassPerm = "creativeitemban.bypass.shulker";
    private String cloneBypassPerm = "creativeitemban.bypass.clone";
    private String containerBypassPerm = "creativeitemban.bypass.container";
    private String lifecycleBypassPerm = "creativeitemban.bypass.lifecycle";

    @Override
    public void onEnable() {
        getLogger().info("§aCreativeItemBan 插件 v2.1 已启用！");
        saveDefaultConfig();
        loadConfig();
        // 注册原有监听（保留用于Creative物品获取与give命令）
        getServer().getPluginManager().registerEvents(this, this);
        // 注册创模加强模式监听
        getServer().getPluginManager().registerEvents(new CreativeProtectionListener(this), this);

        // 注册命令
        getCommand("creativeitemban").setExecutor(new CreativeItemBanCommand(this));

        logConfigInfo();
    }

    @Override
    public void onDisable() {
        getLogger().info("§cCreativeItemBan 插件已禁用");
    }

    void loadConfig() {
        reloadConfig();

        // 清空以前的配置
        creativeBannedItems.clear();
        giveBannedItems.clear();

        // 基础配置
        debugMode = getConfig().getBoolean("debug", false);

        // 创造模式禁止物品
        for (String itemName : getConfig().getStringList("creative-banned.items")) {
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                creativeBannedItems.add(material);
                if (debugMode) getLogger().info("§e[DEBUG] 添加创造禁止物品: " + material);
            } catch (IllegalArgumentException e) {
                getLogger().warning("无效的创造模式物品名: " + itemName);
            }
        }

        // give命令禁止物品（支持带命名空间和不带命名空间）
        for (String itemName : getConfig().getStringList("give-banned.items")) {
            String normalizedItem = itemName.toLowerCase().trim();

            if (normalizedItem.startsWith("minecraft:")) {
                giveBannedItems.add(normalizedItem);
                if (debugMode) getLogger().info("§e[DEBUG] 添加give禁止物品(带命名空间): " + normalizedItem);
            } else {
                try {
                    Material.valueOf(normalizedItem.toUpperCase());
                    giveBannedItems.add("minecraft:" + normalizedItem);
                    giveBannedItems.add(normalizedItem);
                    if (debugMode) getLogger().info("§e[DEBUG] 添加give禁止物品: " + normalizedItem);
                } catch (IllegalArgumentException e) {
                    getLogger().warning("无效的give命令物品名: " + itemName);
                }
            }
        }

        // 权限
        creativeBypassPerm = getConfig().getString("permissions.creative-bypass", "creativeitemban.bypass.creative");
        giveBypassPerm = getConfig().getString("permissions.give-bypass", "creativeitemban.bypass.give");

        // 消息
        creativeBlockedMsg = cc(getConfig().getString("messages.creative-blocked", "&c这个物品在创造模式中被禁止获取"));
        giveBlockedMsg = cc(getConfig().getString("messages.give-blocked", "&c这个物品无法通过give命令获取"));
        bypassMsg = cc(getConfig().getString("messages.bypass-enabled", "&a你拥有权限，可以获取禁止物品"));
        reloadSuccessMsg = cc(getConfig().getString("messages.reload-success", "&a配置文件已重新加载"));

        // 加强模式配置
        cpEnabled = getConfig().getBoolean("creative-protection.enabled", true);
        placeBan = getConfig().getBoolean("creative-protection.place-ban", true);
        shulkerCheckOnPlace = getConfig().getBoolean("creative-protection.shulker.check-on-place", true);
        shulkerAction = getConfig().getString("creative-protection.shulker.action", "cancel");
        blockMoveToContainers = getConfig().getBoolean("creative-protection.inventory.block-move-to-containers", true);
        blockMiddleClone = getConfig().getBoolean("creative-protection.inventory.block-middle-clone", true);
        clearOnCreativeSwitch = getConfig().getBoolean("creative-protection.lifecycle.clear-on-creative-switch", false);

        // 违规监控配置
        violationMonitoringEnabled = getConfig().getBoolean("creative-protection.violation-monitoring.enabled", true);
        violationTimeWindow = getConfig().getInt("creative-protection.violation-monitoring.time-window", 300);
        violationWarningThreshold = getConfig().getInt("creative-protection.violation-monitoring.warning-threshold", 10);
        violationClearThreshold = getConfig().getInt("creative-protection.violation-monitoring.clear-threshold", 20);
        violationBypassPerm = getConfig().getString("creative-protection.violation-monitoring.bypass-permission", "creativeitemban.bypass.violation");

        msgCreativePlaceBlocked = cc(getConfig().getString("messages.creative-place-blocked", "&c该物品在创造模式下禁止放置"));
        msgShulkerBlocked = cc(getConfig().getString("messages.shulker-blocked", "&c该潜影盒包含被禁止物品，无法放置"));
        msgShulkerSanitized = cc(getConfig().getString("messages.shulker-sanitized", "&e潜影盒中的被禁物品已清理，允许放置"));
        msgCloneBlocked = cc(getConfig().getString("messages.clone-blocked", "&c创造模式下禁止复制该物品"));
        msgContainerMoveBlocked = cc(getConfig().getString("messages.container-move-blocked", "&c不可将被禁止物品移动至容器"));
        msgCleanedOnSwitch = cc(getConfig().getString("messages.cleaned-on-switch", "&e已在切换至创造模式时清理被禁物品"));
        msgViolationWarning = cc(getConfig().getString("messages.violation-warning", "&e[警告] 你短时间内尝试了太多违禁操作！当前已违规&c%violations%&e次（5分钟内）。若继续，违禁物品将被清除！"));
        msgViolationClearItems = cc(getConfig().getString("messages.violation-clear-items", "&c[处罚] 你短时间内多次违规，已清空所有违禁物品！共违规%total_violations%次（5分钟内）。"));
    }

    private String cc(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    private void logConfigInfo() {
        getLogger().info("§a===== CreativeItemBan 配置信息 =====");
        getLogger().info("§a创造模式禁止物品: §e" + creativeBannedItems.size() + " 个");
        getLogger().info("§aGive命令禁止物品: §e" + giveBannedItems.size() + " 个");
        getLogger().info("§a创造绕过权限: §e" + creativeBypassPerm);
        getLogger().info("§aGive绕过权限: §e" + giveBypassPerm);
        getLogger().info("§a调试模式: §e" + (debugMode ? "开启" : "关闭"));
        getLogger().info("§a创模加强模式: §e" + (cpEnabled ? "启用" : "关闭"));
        getLogger().info("§a违规监控: §e" + (violationMonitoringEnabled ? "启用" : "关闭"));
        getLogger().info("§a=================================");
    }

    /**
     * 检查玩家是否有权限绕过限制（原有）
     */
    private boolean hasBypassPermission(Player player, String permissionType) {
        boolean hasGeneralBypass = player.hasPermission("creativeitemban.bypass");
        boolean hasSpecificBypass = false;

        if ("creative".equals(permissionType)) {
            hasSpecificBypass = player.hasPermission(creativeBypassPerm);
        } else if ("give".equals(permissionType)) {
            hasSpecificBypass = player.hasPermission(giveBypassPerm);
        } else if ("violation".equals(permissionType)) {
            hasSpecificBypass = player.hasPermission(violationBypassPerm);
        }

        return hasGeneralBypass || hasSpecificBypass;
    }

    /**
     * 处理创造模式物品获取事件（保留）
     */
    @EventHandler
    public void onCreativeInventory(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) return;

        if (hasBypassPermission(player, "creative")) {
            if (debugMode) player.sendMessage(bypassMsg);
            return;
        }

        ItemStack clickedItem = event.getCursor();
        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
            Material itemType = clickedItem.getType();

            if (debugMode) {
                getLogger().info("[DEBUG] 玩家 " + player.getName() + " 尝试在创造模式获取: " + itemType);
            }

            if (creativeBannedItems.contains(itemType)) {
                event.setCancelled(true);
                event.setCursor(null);
                player.sendMessage(creativeBlockedMsg);
                if (debugMode) {
                    getLogger().info("[DEBUG] 阻止玩家 " + player.getName() + " 获取禁止物品: " + itemType);
                }
                return;
            }
        }

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
     * 处理give命令事件（保留）
     */
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();

        if (debugMode) {
            getLogger().info("[DEBUG] 命令事件: " + command + " (玩家: " + player.getName() + ")");
        }

        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) return;

        if (hasBypassPermission(player, "give")) {
            if (debugMode) player.sendMessage(bypassMsg);
            return;
        }

        String normalizedCmd = command.toLowerCase();

        if (normalizedCmd.startsWith("/give ") || normalizedCmd.startsWith("/minecraft:give ")) {
            String givePart = normalizedCmd.startsWith("/minecraft:give ")
                    ? normalizedCmd.substring("/minecraft:give ".length())
                    : normalizedCmd.substring("/give ".length());

            String[] args = givePart.trim().split("\\s+");
            if (args.length < 2) return;

            int itemArgIndex = 1;
            if (args.length > itemArgIndex) {
                String potentialItem = args[itemArgIndex];
                try {
                    Integer.parseInt(potentialItem);
                    itemArgIndex = 2;
                    if (args.length > itemArgIndex) {
                        potentialItem = args[itemArgIndex];
                    } else {
                        return;
                    }
                } catch (NumberFormatException ignored) {}

                String itemToCheck = potentialItem.toLowerCase();

                if (giveBannedItems.contains(itemToCheck) ||
                        giveBannedItems.contains("minecraft:" + itemToCheck)) {

                    event.setCancelled(true);
                    player.sendMessage(giveBlockedMsg);
                    if (debugMode) {
                        getLogger().info("[DEBUG] 阻止 " + player.getName() + " 通过give命令获取: " + itemToCheck);
                    }
                }
            }
        }
    }

    public void reloadConfiguration() {
        loadConfig();
        logConfigInfo();
        String message = (reloadSuccessMsg != null) ? reloadSuccessMsg : "配置文件已重新加载";
        getLogger().info(message);
    }

    // === 对监听器暴露的 getter 与工具 ===

    public Set<Material> getCreativeBannedItems() {
        return creativeBannedItems;
    }

    public Set<String> getGiveBannedItems() {
        return giveBannedItems;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isCpEnabled() { return cpEnabled; }
    public boolean isPlaceBan() { return placeBan; }
    public boolean isShulkerCheckOnPlace() { return shulkerCheckOnPlace; }
    public String getShulkerAction() { return shulkerAction; }
    public boolean isBlockMoveToContainers() { return blockMoveToContainers; }
    public boolean isBlockMiddleClone() { return blockMiddleClone; }
    public boolean isClearOnCreativeSwitch() { return clearOnCreativeSwitch; }

    // 违规监控相关 getter
    public boolean isViolationMonitoringEnabled() { return violationMonitoringEnabled; }
    public int getViolationTimeWindow() { return violationTimeWindow; }
    public int getViolationWarningThreshold() { return violationWarningThreshold; }
    public int getViolationClearThreshold() { return violationClearThreshold; }
    public String getMsgViolationWarning() { return msgViolationWarning; }
    public String getMsgViolationClearItems() { return msgViolationClearItems; }

    public String getMsgCreativePlaceBlocked() { return msgCreativePlaceBlocked; }
    public String getMsgShulkerBlocked() { return msgShulkerBlocked; }
    public String getMsgShulkerSanitized() { return msgShulkerSanitized; }
    public String getMsgCloneBlocked() { return msgCloneBlocked; }
    public String getMsgContainerMoveBlocked() { return msgContainerMoveBlocked; }
    public String getMsgCleanedOnSwitch() { return msgCleanedOnSwitch; }

    // 统一的 bypass 判断给监听器用
    public boolean hasBypass(Player p, String type) {
        if (p == null) return false;
        if (p.hasPermission("creativeitemban.bypass")) return true;
        switch (type) {
            case "place": return p.hasPermission(placeBypassPerm);
            case "shulker": return p.hasPermission(shulkerBypassPerm);
            case "clone": return p.hasPermission(cloneBypassPerm);
            case "container": return p.hasPermission(containerBypassPerm);
            case "lifecycle": return p.hasPermission(lifecycleBypassPerm);
            case "creative": return p.hasPermission(creativeBypassPerm);
            case "give": return p.hasPermission(giveBypassPerm);
            case "violation": return p.hasPermission(violationBypassPerm);
            default: return false;
        }
    }
}