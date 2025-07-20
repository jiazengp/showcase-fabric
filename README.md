# Showcase Mod

> The current description is for V2. For V1, please [click here](https://modrinth.com/mod/showcase/version/1.1.0+mc1.21.6).

Allows players to easily showcase their various items, any container, or even villager trades to others via clickable share links and a read-only GUI.

## Features ✨

- Supports sharing any item, container, and even villager trade interfaces
- Fully server-side mod: all features are provided independently by the server
- Auto-unpack preview: when sharing shulker boxes or backpacks, users can click to view contents whether shared as items or containers
- Supports preview interaction such as clicking to open book GUIs and view maps
- Localization support with multiple languages available
- Persistent data storage: all shares are saved automatically and deleted only after their configured expiration time
- LuckPerms integration: all features and commands have corresponding permission nodes registered
- Placeholder support: use specified placeholders directly in chat messages for dynamic content display


## Commands & Usage ⌨️

### Player Commands

- `/showcase item [player (optional)] [valid duration (optional)] [description (optional)]`
- `/showcase inventory [player (optional)] [valid duration (optional)] [description (optional)]`
- `/showcase hotbar [player (optional)] [valid duration (optional)] [description (optional)]`
- `/showcase enderchest [player (optional)] [valid duration (optional)] [description (optional)]`
- `/showcase container [player (optional)] [valid duration (optional)] [description (optional)]`  
  Share a container you open within 10 seconds
- `/showcase merchant [player (optional)] [valid duration (optional)] [description (optional)]`  
  Share the trade gui you open within 10 seconds
- `/showcase cancel <id>`  
  Cancel your share by share link ID
- `/showcase-view <id>`  
  View shared content by share link ID

### Admin Commands

- `/admin-showcase <type> <targetPlayer> [receiver (optional)] [valid duration (optional)] [description (optional)]`  
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

## Configuration ⚙️

```jsonc
{
  "shareLinkExpiryTime": 300,  // Maximum selectable expiry time for share links
  "shareLinkMinimumExpiryTime": 60 // Minimum selectable expiry time for share links
  "shareCommandCooldown": 10,  // Cooldown between commands
  "containerListeningDuration": 10  // Time to open container
}
```

## License

[MIT](./LICENSE)
