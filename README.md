# Showcase Mod

> The current description is for V2. For V1, please [click here](https://modrinth.com/mod/showcase/version/1.1.0+mc1.21.6).

A server-side Minecraft mod that allows players to share links in chat to showcase items, inventories, containers, villager trades, and statistics through commands or chat keywords.

## ✨ Features

- **Comprehensive Sharing**: Items, inventories, containers, and villager trades
- **Server-Side**: No client installation required
- **Interactive Previews**: Auto-unpack containers, view books and maps
- **Multi-Language**: Support for 15+ languages
- **Persistent Storage**: Shares saved until expiration
- **Permission System**: LuckPerms integration
- **Configurable Chat Keywords**: Customizable trigger words for quick sharing
- **Statistics Tracking**: Player statistics with detailed categorization
- **Developer Placeholders**: PlaceholderAPI integration for other plugins
- **Chat Item Icons**: Customizable item icons in chat messages (Experimental)

<details>
<summary><strong>About Chat Item Icons</strong> (Experimental)</summary>

Displays item icons in chat messages for shared items.

**Why Resource Pack?** As a server-side mod, we use Minecraft's custom font system to render icons.

**Setup**:
1. Generate resource pack at [IconifyCraft](https://iconifycraft.vercel.app/)
2. Host the pack on a web server
3. Enable in `config/showcase/config.yml`: `itemIcons.enabled = true`
4. Add to `server.properties`:
   ```properties
   resource-pack=<your-hosted-pack-url>
   resource-pack-sha1=<pack-sha1-hash>
   require-resource-pack=true
   ```

**Config Options**:
```yaml
itemIcons:
  enabled: false                    # Enable/disable icons
  fontNamespace: "iconifycraft"     # Resource pack namespace
  includeInItemNames: true          # Show icons in item names
```

⚠Warning: Experimental feature. Current support for displaying blocks, including chests and ender chests, is incomplete.
</details>

## ⌨️ Commands

### Player Commands

```
/showcase <type> [player] [duration] [description]
```
**Types**: `item`, `inventory`, `hotbar`, `enderchest`, `container`, `merchant`, `stats`

- `container` - Share a container opened within 10 seconds
- `merchant` - Share a villager trade interface opened within 10 seconds
- `stats` - Share player statistics with categorized data

**Other Commands**:
- `/showcase cancel <id>` - Cancel your share
- `/showcase-view <id>` - View shared content

### Admin Commands
```
/admin-showcase <type> <player> [receiver] [duration] [description]
/showcase-manage <action> [args]
```

**Actions**: `reload`, `list [page]`, `cancel <id|player>`

### Chat Keywords

Players can type keywords in square brackets to quickly share content:

**Default Keywords** (configurable in `config.yml`):
- `[item]` or `[i]` - Share held item
- `[inventory]` or `[inv]` - Share inventory
- `[hotbar]` or `[hb]` - Share hotbar
- `[ender_chest]` or `[ec]` - Share ender chest
- `[stats]` or `[stat]` - Share statistics

Example: `Check out my [item]!` → automatically replaced with clickable share link

## 🔌 Developer API

### PlaceholdersAPI Integration

```
%showcase:item%        - Share held item
%showcase:inventory%   - Share inventory  
%showcase:hotbar%      - Share hotbar
%showcase:ender_chest% - Share ender chest
%showcase:stats%       - Share statistics
```

## ⚙️ Configuration

Main configuration file: `config/showcase/config.yml` (YAML format)

Customize share settings, keywords, cooldowns, and statistics display options. Language files are built into the mod for automatic localization.

## 📝 License

[MIT](./LICENSE)
