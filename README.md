# Showcase Mod

Showcase Mod allows players to easily share their inventory, held items, hotbar, ender chest, or any container with others via clickable share links and Readonly GUI.

## Features ✨

- Share your **held item**, **inventory**, **hotbar**, **ender chest**, or any **container**
- Generate clickable share links that expire after a configurable time
- Share publicly or privately to specific players
- **Multi-language support** (English, Chinese, Japanese, Korean, French)
- **LuckPerms support** for permission management
- **Placeholder support** for chat messages
- Admin commands to share on behalf of others and manage shares

## Commands & Usage ⌨️

### Player Commands

- `/showcase item [player (optional)] [description (optional)]`  
  Share the item you are holding
- `/showcase inventory [player (optional)] [description (optional)]`  
  Share your inventory
- `/showcase hotbar [player (optional)] [description (optional)]`  
  Share your hotbar
- `/showcase enderchest [player (optional)] [description (optional)]`  
  Share your ender chest
- `/showcase container [player (optional)] [description (optional)]`  
  Share a container you open within 10 seconds
- `/showcase-view <id>`  
  View shared content by share link ID

### Admin Commands

- `/adminshare <type> <targetPlayer> [receiver (optional)] [description (optional)]`  
  Share on behalf of another player (types: item, inventory, hotbar, enderchest)
- `/showcase-manage reload`  
  Reload the mod configuration
- `/showcase-manage list [page (optional)]`  
  List active shares with pagination
- `/showcase-manage cancel <shareId|player>`  
  Cancel a specific share or all shares from a player

### Placeholders

Use these placeholders in chat:

- `%showcase:item%` - Share held item
- `%showcase:inventory%` - Share inventory
- `%showcase:hotbar%` - Share hotbar
- `%showcase:ender_chest%` - Share ender chest

## Permissions 🔒

With LuckPerms installed, these permissions are available:

| Permission Node | Default | Description |
|-----------------|---------|-------------|
| `showcase.admin` | OP 4 | Full access to all admin commands |
| `showcase.manage.list` | OP 4 | View active shares with `/showcase-manage list` |
| `showcase.manage.cancel` | OP 4 | Cancel shares with `/showcase-manage cancel` |
| `showcase.manage.reload` | OP 4 | Reload config with `/showcase-manage reload` |
| `showcase.bypass.cooldown` | OP 2 | Bypass command cooldowns |
| `showcase.share.item` | true | Allow using `/share item` |
| `showcase.share.inventory` | true | Allow using `/share inventory` |
| `showcase.share.hotbar` | true | Allow using `/share hotbar` |
| `showcase.share.enderchest` | true | Allow using `/share enderchest` |
| `showcase.share.container` | true | Allow using `/share container` |

Without LuckPerms, OP levels are used as fallback (shown in Default column).

## Configuration ⚙️

```jsonc
{
  "shareLinkExpiryTime": 300,  // Share link expiry in seconds
  "shareCommandCooldown": 10,  // Cooldown between commands
  "containerListeningDuration": 10,  // Time to open container
  "locale": "en_us"  // Default language (en_us, zh_cn, ja_jp, ko_kr, fr_fr)
}
```

## License

[MIT](./LICENSE)
