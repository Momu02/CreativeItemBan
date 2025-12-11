# CreativeItemBan - Spigot/Bukkit 物品禁止插件

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-Spigot/Bukkit%20API-yellow.svg)](https://spigotmc.org/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.x-success.svg)](https://www.minecraft.net/)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com/)

一个功能强大的Spigot/Bukkit插件，用于在Minecraft服务器上禁止特定物品的获取。支持创造模式和/give命令分离配置，集成LuckyPerms权限系统。

##  🎯 版本兼容性

| Minecraft 版本 | 服务器类型 | 兼容性状态 | 测试认证 |
|----------------|------------|------------|----------|
| 1.20.1 - 1.20.6 | Spigot/Paper/Bukkit系列 | ✅ 完全兼容 | 已测试 |
| 1.20.1 - 1.20.6 | Mohist系列 | ✅ 完全兼容 | 已测试 (Mohist 47.4.13) |
| 1.21.x | Spigot/Paper/Bukkit系列 |  ⚠️ 理论兼容 | 需要版本适配 |
| 1.21.x | Mohist系列 |  ⚠️ 理论兼容 | 需要版本适配 |

> **技术说明**：本插件使用标准Spigot/Bukkit API，不依赖任何服务端特定实现，理论上兼容所有Spigot系服务器（Spigot、Paper、Bukkit、Mohist等）。

##  🎮 适配服务器
- ✅ **Spigot系列**：Spigot、Paper、Bukkit等
- ✅ **Forge修改版**：Mohist (1.20.1已测试)
-  ⚠️ **其他服务器**：理论上兼容，需要自行验证

## ✨ 功能特性

-  🔥 **双模式禁止**：分别配置创造模式和/give命令的禁止物品
-   🔒 **权限集成**：全面支持LuckyPerms权限系统
-   **可定制消息**：支持彩色消息和自定义提示文本（支持 & 颜色代码）
-  ️ **管理命令**：内置完整的命令系统，便于管理
-   📊 **调试模式**：详细的日志输出，便于问题排查
-   **高性能**：使用HashSet实现，快速查找物品
-   🔄 **自动颜色转换**：配置文件中的 & 颜色代码自动转换到游戏中

## 📦 安装方法

1. 下载最新版本的 `CreativeItemBan-1.0.jar`
2. 将插件文件放入服务器的 `plugins` 目录
3. 重启服务器或使用 `reload` 命令
4. 根据需要编辑 `plugins/CreativeItemBan/config.yml`

##  ⚙️ 配置文件

配置文件位于 `plugins/CreativeItemBan/config.yml`

### 主要部分：
```yaml
# 创造模式物品禁止配置
creative-banned:
  items:
    - TNT
    - BEDROCK
    - COMMAND_BLOCK
    # ... 更多物品

# Give命令物品禁止配置
give-banned:
  items:
    - minecraft:tnt
    - minecraft:bedrock
    - minecraft:command_block
    # ... 更多物品

# 权限配置
permissions:
  bypass-permissions:
    - creativeitemban.bypass
    - creativeitemban.bypass.creative
    - creativeitemban.bypass.give
  creative-bypass: "creativeitemban.bypass.creative"
  give-bypass: "creativeitemban.bypass.give"

# 消息配置（支持颜色代码 &）
messages:
  creative-blocked: "&c这个物品在创造模式中被禁止获取"
  give-blocked: "&c这个物品无法通过give命令获取"
  bypass-enabled: "&a你拥有权限，可以获取禁止物品"
  reload-success: "&a配置文件已重新加载"

# 调试模式
debug: false
```

##  🔧 可用命令

| 命令 | 权限节点 | 描述 |
|------|---------|------|
| `/creativeitemban reload` | `creativeitemban.admin` | 重新加载配置文件 |
| `/creativeitemban status` | `creativeitemban.admin` 或 `creativeitemban.view` | 查看插件状态 |
| `/creativeitemban list` | `creativeitemban.admin` 或 `creativeitemban.view` | 查看禁止物品列表 |
| `/creativeitemban debug <on/off>` | `creativeitemban.admin` | 开启/关闭调试模式 |
| `/creativeitemban version` | 无 | 查看插件版本 |
| `/creativeitemban help` | 无 | 显示帮助信息 |

##  🔐 权限节点

### 命令权限
- `creativeitemban.admin` - 管理员权限（所有命令）
- `creativeitemban.view` - 查看权限（status、list命令）

### 绕过权限
- `creativeitemban.bypass` - 通用绕过权限（所有场景）
- `creativeitemban.bypass.creative` - 仅创造模式绕过
- `creativeitemban.bypass.give` - 仅/give命令绕过

### 默认权限设置
OP玩家默认**不会**绕过限制，除非明确授予`creativeitemban.bypass`权限。

##  🛠️ 开发者信息

### 技术栈
- **Java版本**: Java 17+
- **API**: Spigot/Bukkit API（标准API，无服务端依赖）
- **构建工具**: Apache Maven 3.8+
- **测试版本**: Minecraft 1.20.1 Mohist 47.4.13

### 项目结构
```
CreativeItemBan/
├── src/main/java/com/example/
│   ├── CreativeItemBan.java      # 插件主类
│    └── CreativeItemBanCommand.java  # 命令处理类
├── src/main/resources/
│   ├── config.yml                # 默认配置文件
│     └── plugin.yml                # 插件描述文件
├── pom.xml                       # Maven构建配置
└── README.md                     # 项目说明文档
```

### 构建方法
```bash
mvn clean package -DskipTests
```

### 版本适配说明
如需适配其他Minecraft版本，请在 `pom.xml` 中修改 `<spigot.version>` 为对应的API版本号。

##  ❓ 常见问题

### Q: 为什么OP玩家无法获取TNT？
A: 这是设计特性。OP玩家默认**不会**绕过物品禁止，除非被授予`creativeitemban.bypass`权限。

### Q: 如何允许特定权限组绕过限制？
A: 使用LuckyPerms为权限组添加对应的绕过权限节点。

### Q: 支持哪些物品格式？
A: 
- 创造模式：物品ID（大写），如 `TNT`、`BEDROCK`
- Give命令：物品ID（大写）或带命名空间的ID，如 `minecraft:tnt`

### Q: 如何添加自定义提示消息？
A: 编辑config.yml中的messages部分，支持标准颜色代码（&符号），插件会自动转换为游戏内颜色。

### Q: 是否需要在不同MC版本上重新编译？
A: 插件已在1.20.x系列中完全兼容。理论上支持1.20.x全系列，无需重新编译。1.21.x可能需要调整API版本号。

## 📝 更新日志

### v2.0 (当前版本)
- ✅ 实现创造模式和/give命令物品禁止分离配置
- ✅ 集成LuckyPerms权限系统
- ✅ 完善的管理命令系统
- ✅ 可配置的消息和颜色支持（自动转换机制）
- ✅ 修复OP玩家默认绕过的问题
- ✅ 添加调试模式和详细日志
- ✅ 版本兼容性明确，定位为Spigot/Bukkit通用插件

### v1.0 (初始版本)
- ✅ 基础物品禁止功能

##  🤝 贡献

欢迎提交Issue和Pull Request！

## 📄 许可证

本项目基于 MIT 许可证开源。详见 [LICENSE](LICENSE) 文件。

## 📬 联系方式

如有问题或建议，请通过GitHub Issues提交。

---

**核心要点**：
1.  🎯 **插件类型**：Spigot/Bukkit标准插件，非Mohist专属
2.  🔄 **兼容范围**：Minecraft 1.20.x全系列已兼容，1.21.x理论上支持
3.  ⚙️ **技术要求**：Java 17+，标准Spigot/Bukkit API
4.  **应用场景**：所有基于Spigot/Paper/Bukkit的服务器均可使用