# CreativeItemBan - Spigot/Bukkit Item Ban Plugin

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-Spigot/Bukkit%20API-yellow.svg)](https://spigotmc.org/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.x-success.svg)](https://www.minecraft.net/)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com/)

A powerful Spigot/Bukkit plugin for banning specific items on Minecraft servers. Supports separate configurations for creative mode and /give commands, integrates with LuckyPerms permission system.

##  Version Compatibility

| Minecraft Version | Server Type | Compatibility Status | Tested |
|-------------------|-------------|---------------------|---------|
| 1.20.1 - 1.20.6 | Spigot/Paper/Bukkit series | ✅ Fully Compatible | Tested |
| 1.20.1 - 1.20.6 | Mohist series | ✅ Fully Compatible | Tested (Mohist 47.4.13) |
| 1.21.x | Spigot/Paper/Bukkit series |  ⚠️ Theoretical Compatible | Version adaptation required |
| 1.21.x | Mohist series | ️ Theoretical Compatible | Version adaptation required |

> **Technical Note**: This plugin uses standard Spigot/Bukkit API, does not depend on any server-specific implementation, theoretically compatible with all Spigot-based servers (Spigot, Paper, Bukkit, Mohist, etc.).

##  🎮 Supported Servers
- ✅ **Spigot series**: Spigot, Paper, Bukkit, etc.
- ✅ **Forge-modified**: Mohist (tested with 1.20.1)
-  ⚠️ **Other servers**: Theoretically compatible, requires verification

##  ✨ Features

-  🔥 **Dual Ban Modes**: Separate banned items for creative mode and /give commands
-  🔒 **Permission Integration**: Full support for LuckyPerms permission system
-  🎨 **Customizable Messages**: Support colored messages and custom prompts (supports & color codes)
-  ⚙️ **Management Commands**: Complete command system for easy administration
-  📊 **Debug Mode**: Detailed logging for troubleshooting
-  ⚡ **High Performance**: Uses HashSet for fast item lookup
-  🔄 **Auto Color Conversion**: Automatically converts & color codes from config to in-game colors

## 📦 Installation

1. Download the latest version of `CreativeItemBan-1.0.jar`
2. Place the plugin file in your server's `plugins` directory
3. Restart the server or use the `reload` command
4. Edit `plugins/CreativeItemBan/config.yml` as needed

##  ⚙️ Configuration

Configuration file is located at `plugins/CreativeItemBan/config.yml`

### Main Sections:
```yaml
# Creative mode item ban configuration
creative-banned:
  items:
    - TNT
    - BEDROCK
    - COMMAND_BLOCK
    # ... more items

# Give command item ban configuration
give-banned:
  items:
    - minecraft:tnt
    - minecraft:bedrock
    - minecraft:command_block
    # ... more items

# Permission configuration
permissions:
  bypass-permissions:
    - creativeitemban.bypass
    - creativeitemban.bypass.creative
    - creativeitemban.bypass.give
  creative-bypass: "creativeitemban.bypass.creative"
  give-bypass: "creativeitemban.bypass.give"

# Message configuration (supports color codes &)
messages:
  creative-blocked: "&cThis item is banned in creative mode"
  give-blocked: "&cThis item cannot be obtained via give command"
  bypass-enabled: "&aYou have permission to get banned items"
  reload-success: "&aConfiguration reloaded successfully"

# Debug mode
debug: false
```

##  🔧 Available Commands

| Command | Permission Node | Description |
|---------|----------------|-------------|
| `/creativeitemban reload` | `creativeitemban.admin` | Reload configuration file |
| `/creativeitemban status` | `creativeitemban.admin` or `creativeitemban.view` | View plugin status |
| `/creativeitemban list` | `creativeitemban.admin` or `creativeitemban.view` | View banned items list |
| `/creativeitemban debug <on/off>` | `creativeitemban.admin` | Enable/disable debug mode |
| `/creativeitemban version` | None | View plugin version |
| `/creativeitemban help` | None | Show help information |

## 🔐 Permission Nodes

### Command Permissions
- `creativeitemban.admin` - Administrator permission (all commands)
- `creativeitemban.view` - View permission (status, list commands)

### Bypass Permissions
- `creativeitemban.bypass` - General bypass permission (all scenarios)
- `creativeitemban.bypass.creative` - Creative mode only bypass
- `creativeitemban.bypass.give` - /give command only bypass

### Default Permission Settings
OP players do **not** bypass restrictions by default, unless explicitly granted the `creativeitemban.bypass` permission.

## ️ Developer Information

### Tech Stack
- **Java Version**: Java 17+
- **API**: Spigot/Bukkit API (standard API, no server dependencies)
- **Build Tool**: Apache Maven 3.8+
- **Tested Version**: Minecraft 1.20.1 Mohist 47.4.13

### Project Structure
```
CreativeItemBan/
├── src/main/java/com/example/
│   ├── CreativeItemBan.java      # Main plugin class
│   └── CreativeItemBanCommand.java  # Command handler class
├── src/main/resources/
│   ├── config.yml                # Default configuration file
│   └── plugin.yml                # Plugin description file
├── pom.xml                       # Maven build configuration
└── README.md                     # Project documentation
```

### Build Method
```bash
mvn clean package -DskipTests
```

### Version Adaptation Instructions
To adapt to other Minecraft versions, modify `<spigot.version>` in `pom.xml` to the corresponding API version number.

##  ❓ Frequently Asked Questions

### Q: Why can't OP players get TNT?
A: This is by design. OP players do **not** bypass item bans by default, unless granted the `creativeitemban.bypass` permission.

### Q: How to allow specific permission groups to bypass restrictions?
A: Use LuckyPerms to add corresponding bypass permission nodes to the permission group.

### Q: What item formats are supported?
A: 
- Creative mode: Item ID (uppercase), like `TNT`, `BEDROCK`
- Give command: Item ID (uppercase) or namespaced ID, like `minecraft:tnt`

### Q: How to add custom prompt messages?
A: Edit the messages section in config.yml, supports standard color codes (& symbol), plugin automatically converts to in-game colors.

### Q: Does it need recompilation for different Minecraft versions?
A: The plugin is fully compatible with the 1.20.x series. Theoretically supports all 1.20.x versions without recompilation. 1.21.x may require API version number adjustment.

## 📝 Changelog

### v2.0 (Current Version)
- ✅ Implemented separate banned items for creative mode and /give commands
- ✅ Integrated LuckyPerms permission system
- ✅ Complete management command system
- ✅ Configurable messages with color support (auto-conversion mechanism)
- ✅ Fixed OP players bypassing by default issue
- ✅ Added debug mode with detailed logging
- ✅ Clarified version compatibility, positioned as Spigot/Bukkit universal plugin

### v1.0 (Initial Version)
- ✅ Basic item ban functionality

##  🤝 Contributing

Issues and Pull Requests are welcome!

## 📄 License

This project is open source under the MIT License. See [LICENSE](LICENSE) file for details.

##  📬 Contact

For issues or suggestions, please submit via GitHub Issues.

---

**Key Points**:
1.  🎯 **Plugin Type**: Spigot/Bukkit standard plugin, not Mohist-specific
2.  🔄 **Compatibility**: Minecraft 1.20.x series fully compatible, 1.21.x theoretically supported
3. ️ **Requirements**: Java 17+, standard Spigot/Bukkit API
4.  **Application**: All Spigot/Paper/Bukkit-based servers can use it