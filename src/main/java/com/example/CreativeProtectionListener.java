package com.example;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CreativeProtectionListener implements Listener {

    private final CreativeItemBan plugin;
    
    // 违规监控数据结构：存储每个玩家的违规时间记录
    private final Map<UUID, List<Long>> playerViolations = new ConcurrentHashMap<>();

    public CreativeProtectionListener(CreativeItemBan plugin) {
        this.plugin = plugin;
    }

    // Helper: check if a material is banned in creative
    private boolean isCreativeBanned(Material type) {
        return type != null && plugin.getCreativeBannedItems().contains(type);
    }

    // Helper: check if an item is a shulker box (any color)
    private boolean isShulkerBoxItem(ItemStack stack) {
        if (stack == null) return false;
        Material t = stack.getType();
        return t != null && (t == Material.SHULKER_BOX
                || t == Material.WHITE_SHULKER_BOX
                || t == Material.ORANGE_SHULKER_BOX
                || t == Material.MAGENTA_SHULKER_BOX
                || t == Material.LIGHT_BLUE_SHULKER_BOX
                || t == Material.YELLOW_SHULKER_BOX
                || t == Material.LIME_SHULKER_BOX
                || t == Material.PINK_SHULKER_BOX
                || t == Material.GRAY_SHULKER_BOX
                || t == Material.LIGHT_GRAY_SHULKER_BOX
                || t == Material.CYAN_SHULKER_BOX
                || t == Material.PURPLE_SHULKER_BOX
                || t == Material.BLUE_SHULKER_BOX
                || t == Material.BROWN_SHULKER_BOX
                || t == Material.GREEN_SHULKER_BOX
                || t == Material.RED_SHULKER_BOX
                || t == Material.BLACK_SHULKER_BOX);
    }

    // Helper: scan shulker contents for banned materials
    private boolean shulkerContainsBanned(ItemStack shulkerItem) {
        if (shulkerItem == null || !isShulkerBoxItem(shulkerItem)) return false;
        if (!(shulkerItem.getItemMeta() instanceof BlockStateMeta meta)) return false;
        if (!(meta.getBlockState() instanceof ShulkerBox shulker)) return false;
        for (ItemStack content : shulker.getInventory().getContents()) {
            if (content == null) continue;
            if (isCreativeBanned(content.getType())) return true;
        }
        return false;
    }

    // Helper: sanitize shulker contents (remove banned items)
    private boolean sanitizeShulker(ItemStack shulkerItem) {
        if (shulkerItem == null || !isShulkerBoxItem(shulkerItem)) return false;
        if (!(shulkerItem.getItemMeta() instanceof BlockStateMeta meta)) return false;
        if (!(meta.getBlockState() instanceof ShulkerBox shulker)) return false;
        boolean changed = false;
        Inventory inv = shulker.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack it = inv.getItem(i);
            if (it != null && isCreativeBanned(it.getType())) {
                inv.setItem(i, null);
                changed = true;
            }
        }
        if (changed) {
            meta.setBlockState(shulker);
            shulkerItem.setItemMeta(meta);
        }
        return changed;
    }

    private boolean isCreative(Player p) {
        return p != null && p.getGameMode() == GameMode.CREATIVE;
    }

    private boolean hasBypass(Player p, String type) {
        return plugin.hasBypass(p, type);
    }

    // 违规监控相关方法
    private void addViolation(Player player) {
        if (!plugin.isViolationMonitoringEnabled()) return;
        if (hasBypass(player, "violation")) return;
        
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis() / 1000; // 转换为秒
        
        // 获取或创建该玩家的违规记录列表
        List<Long> violations = playerViolations.computeIfAbsent(uuid, k -> new ArrayList<>());
        
        // 添加当前时间
        violations.add(currentTime);
        
        // 清理过期记录（超出时间窗口的记录）
        long windowSize = plugin.getViolationTimeWindow();
        violations.removeIf(time -> (currentTime - time) > windowSize);
        
        // 处理违规事件
        handleViolation(player, violations.size());
    }
    
    private void handleViolation(Player player, int violationCount) {
        if (violationCount >= plugin.getViolationWarningThreshold() && 
            violationCount < plugin.getViolationClearThreshold()) {
            // 达到警告阈值，发送警告
            String message = plugin.getMsgViolationWarning().replace("%violations%", String.valueOf(violationCount));
            player.sendMessage(message);
            if (plugin.isDebugMode()) {
                plugin.getLogger().info("[DEBUG] 玩家 " + player.getName() + " 违规次数警告: " + violationCount + " 次");
            }
        } else if (violationCount >= plugin.getViolationClearThreshold()) {
            // 达到清空物品阈值，执行清空操作
            clearPlayerBannedItems(player);
            String message = plugin.getMsgViolationClearItems().replace("%total_violations%", String.valueOf(violationCount));
            player.sendMessage(message);
            if (plugin.isDebugMode()) {
                plugin.getLogger().info("[DEBUG] 玩家 " + player.getName() + " 违规次数达到" + violationCount + "次，清空违禁物品");
            }
        }
    }
    
    private void clearPlayerBannedItems(Player player) {
        Inventory inv = player.getInventory();
        boolean cleaned = false;
        
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack it = inv.getItem(i);
            if (it == null) continue;

            // 如果是禁品，清空
            if (isCreativeBanned(it.getType())) {
                inv.setItem(i, null);
                cleaned = true;
                continue;
            }
            
            // 如果是潜影盒，清理内部禁品
            if (isShulkerBoxItem(it)) {
                boolean changed = sanitizeShulker(it);
                if (changed) cleaned = true;
            }
        }
        
        if (cleaned && plugin.isDebugMode()) {
            plugin.getLogger().info("[DEBUG] 违规监控：已清空玩家 " + player.getName() + " 的违禁物品");
        }
    }

    // 1) 创模禁止放置被禁物品；2) 禁止放置含禁品的潜影盒（或清理）
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.isCpEnabled()) return;

        Player player = event.getPlayer();
        if (!isCreative(player)) return;

        ItemStack inHand = event.getItemInHand();
        if (inHand == null) return;

        // 放置禁品
        if (plugin.isPlaceBan() && isCreativeBanned(inHand.getType())) {
            if (!hasBypass(player, "place")) {
                event.setCancelled(true);
                if (plugin.isDebugMode()) plugin.getLogger().info("[DEBUG] 取消放置禁品: " + inHand.getType());
                msg(player, plugin.getMsgCreativePlaceBlocked());
                // 记录违规
                addViolation(player);
                return;
            }
        }

        // 放置潜影盒含禁品
        if (plugin.isShulkerCheckOnPlace() && isShulkerBoxItem(inHand)) {
            if (shulkerContainsBanned(inHand) && !hasBypass(player, "shulker")) {
                String action = plugin.getShulkerAction();
                if ("cancel".equalsIgnoreCase(action)) {
                    event.setCancelled(true);
                    msg(player, plugin.getMsgShulkerBlocked());
                    if (plugin.isDebugMode()) plugin.getLogger().info("[DEBUG] 取消放置含禁品潜影盒");
                    // 记录违规
                    addViolation(player);
                } else if ("sanitize".equalsIgnoreCase(action)) {
                    boolean changed = sanitizeShulker(inHand);
                    if (changed) {
                        // 替换手中物品的Meta已完成，允许放置
                        msg(player, plugin.getMsgShulkerSanitized());
                        if (plugin.isDebugMode()) plugin.getLogger().info("[DEBUG] 清空潜影盒中的禁品并允许放置");
                    }
                } else if ("log-only".equalsIgnoreCase(action)) {
                    // 仅记录
                    if (plugin.isDebugMode()) plugin.getLogger().info("[DEBUG] 发现含禁品潜影盒，log-only放行");
                }
            }
        }
    }

    // 创模禁止中键克隆禁品；禁止向容器搬运禁品；禁止Q键丢弃禁品
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.isCpEnabled()) return;

        HumanEntity who = event.getWhoClicked();
        if (!(who instanceof Player player)) return;
        if (!isCreative(player)) return;

        ClickType click = event.getClick();
        InventoryView view = event.getView();
        Inventory top = view.getTopInventory();
        
        // 中键克隆（在创模中常见）- 在所有界面都检测
        if (plugin.isBlockMiddleClone() && click == ClickType.MIDDLE) {
            // 中键克隆可能涉及多个物品源：当前槽位或光标物品
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();
            
            // 创建一个更全面的中键克隆检查
            if ((currentItem != null && currentItem.getType() != Material.AIR && isBannedOrContainingBanned(currentItem)) ||
                (cursorItem != null && cursorItem.getType() != Material.AIR && isBannedOrContainingBanned(cursorItem))) {
                
                if (!hasBypass(player, "clone")) {
                    event.setCancelled(true);
                    msg(player, plugin.getMsgCloneBlocked());
                    if (plugin.isDebugMode()) {
                        plugin.getLogger().info("[DEBUG] 取消中键克隆禁品/含禁品潜影盒");
                        if (currentItem != null && currentItem.getType() != Material.AIR) {
                            plugin.getLogger().info("[DEBUG] 当前槽位物品: " + currentItem.getType());
                        }
                        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                            plugin.getLogger().info("[DEBUG] 光标物品: " + cursorItem.getType());
                        }
                    }
                    // 记录违规
                    addViolation(player);
                    return; // 重要：立即返回，不执行后续逻辑
                }
            }
            // 即使物品不是禁品，中键克隆事件也需要处理完毕
            return;
        }

        // 修复Q键丢弃漏洞 - 在任何界面都检测Q键丢弃
        if (plugin.isBlockMoveToContainers() && (click == ClickType.DROP || click == ClickType.CONTROL_DROP)) {
            ItemStack itemToCheck = event.getCurrentItem();
            if (itemToCheck != null && itemToCheck.getType() != Material.AIR) {
                if (isCreativeBanned(itemToCheck.getType()) || 
                    (isShulkerBoxItem(itemToCheck) && shulkerContainsBanned(itemToCheck))) {
                    
                    if (!hasBypass(player, "container")) {
                        event.setCancelled(true);
                        msg(player, plugin.getMsgContainerMoveBlocked());
                        if (plugin.isDebugMode()) {
                            plugin.getLogger().info("[DEBUG] 取消Q键丢弃禁品/含禁品潜影盒 (click=" + click + 
                                                  "), slot=" + event.getSlot() + ", rawSlot=" + event.getRawSlot());
                            plugin.getLogger().info("[DEBUG] 被丢弃物品: " + itemToCheck.getType());
                        }
                        // 记录违规
                        addViolation(player);
                        return;
                    }
                }
            }
        }

        // 禁止把禁品塞进容器（仅限创模）
        if (plugin.isBlockMoveToContainers() && top != null && !(top instanceof PlayerInventory)) {
            // 获取所有可能的物品来进行检测
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();
            
            // 检测所有可能的操作类型
            boolean blockOperation = false;
            String debugReason = "";
            
            // 1. 检测当前槽位的物品（如Shift点击时）
            if (currentItem != null && currentItem.getType() != Material.AIR) {
                if (isCreativeBanned(currentItem.getType())) {
                    blockOperation = true;
                    debugReason = "当前槽位有禁物品";
                } else if (isShulkerBoxItem(currentItem) && shulkerContainsBanned(currentItem)) {
                    blockOperation = true;
                    debugReason = "当前槽位有含禁品潜影盒";
                }
            }
            
            // 2. 检测光标上的物品（如拖拽放置时）
            if (!blockOperation && cursorItem != null && cursorItem.getType() != Material.AIR) {
                if (isCreativeBanned(cursorItem.getType())) {
                    blockOperation = true;
                    debugReason = "光标上有禁物品";
                } else if (isShulkerBoxItem(cursorItem) && shulkerContainsBanned(cursorItem)) {
                    blockOperation = true;
                    debugReason = "光标上有含禁品潜影盒";
                }
            }
            
            // 如果检测到需要阻止的操作
            if (blockOperation && !hasBypass(player, "container")) {
                event.setCancelled(true);
                msg(player, plugin.getMsgContainerMoveBlocked());
                if (plugin.isDebugMode()) {
                    plugin.getLogger().info("[DEBUG] 取消容器操作: " + debugReason + ", slot=" + event.getSlot() + 
                                          ", rawSlot=" + event.getRawSlot() + ", topSize=" + top.getSize());
                    if (currentItem != null) {
                        plugin.getLogger().info("[DEBUG] 当前物品: " + currentItem.getType());
                    }
                    if (cursorItem != null) {
                        plugin.getLogger().info("[DEBUG] 光标物品: " + cursorItem.getType());
                    }
                }
                // 记录违规
                addViolation(player);
            }
        }
    }

    // 辅助方法：检查物品是否是禁品或含禁品潜影盒
    private boolean isBannedOrContainingBanned(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        return isCreativeBanned(item.getType()) || 
               (isShulkerBoxItem(item) && shulkerContainsBanned(item));
    }

    // 切换为CREATIVE时清理禁品（可选）
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if (!plugin.isCpEnabled()) return;
        if (event.getNewGameMode() != GameMode.CREATIVE) return;

        Player player = event.getPlayer();
        if (hasBypass(player, "lifecycle")) return;
        if (!plugin.isClearOnCreativeSwitch()) return;

        Inventory inv = player.getInventory();
        boolean cleaned = false;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack it = inv.getItem(i);
            if (it == null) continue;

            if (isCreativeBanned(it.getType())) {
                inv.setItem(i, null);
                cleaned = true;
                continue;
            }
            if (isShulkerBoxItem(it)) {
                boolean changed = sanitizeShulker(it);
                if (changed) cleaned = true;
            }
        }
        if (cleaned) {
            msg(player, plugin.getMsgCleanedOnSwitch());
            if (plugin.isDebugMode()) plugin.getLogger().info("[DEBUG] 切换CREATIVE时清理了禁品/潜影盒内容");
        }
    }
    private void msg(Player p, String message) {
        if (message != null && !message.isEmpty()) p.sendMessage(message);
    }
}