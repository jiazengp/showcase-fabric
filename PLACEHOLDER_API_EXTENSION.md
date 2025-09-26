# Extended PlaceholderAPI Support for Showcase Mod

> 📚 **[Complete PlaceholderAPI Documentation](https://showcase-fabric.vercel.app/docs/developers/placeholderapi)** - Interactive examples and integration guides

This document describes the extended placeholder functionality implemented for the Showcase mod.

## Overview

The Showcase mod now includes comprehensive PlaceholderAPI integration, providing detailed information about shares, player statistics, permissions, and server metrics through standardized placeholders.

## Available Placeholders

### Player Share Information

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%showcase:shares_count%` | Number of active shares | `3` |
| `%showcase:shares_remaining%` | Shares until limit | `7` |
| `%showcase:last_share_type%` | Type of last share created | `item` |
| `%showcase:last_share_time%` | Time since last share | `5m ago` |
| `%showcase:next_share_expires%` | When next share expires | `25m` |

### Player Statistics

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%showcase:total_shares_created%` | Total shares ever created | `45` |
| `%showcase:total_shares_viewed%` | Total views received | `127` |
| `%showcase:most_shared_type%` | Most frequently shared type | `inventory` |
| `%showcase:shares_today%` | Shares created today | `5` |
| `%showcase:shares_this_week%` | Shares created this week | `18` |
| `%showcase:average_share_duration%` | Average duration of shares | `1h` |

### Permission Checks

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%showcase:can_share_item%` | Can share items | `true` |
| `%showcase:can_share_inventory%` | Can share inventory | `false` |
| `%showcase:can_share_stats%` | Can share statistics | `true` |
| `%showcase:can_use_chat_keywords%` | Can use chat keywords | `true` |
| `%showcase:has_admin_perms%` | Has admin permissions | `false` |

### Cooldown Information

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%showcase:item_cooldown%` | Seconds until can share item | `15` |
| `%showcase:inventory_cooldown%` | Seconds until can share inventory | `0` |
| `%showcase:chat_cooldown%` | Seconds until can use keywords | `0` |

### Server Statistics

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%showcase:server_total_shares%` | Total shares on server | `1247` |
| `%showcase:server_active_shares%` | Currently active shares | `157` |
| `%showcase:server_most_active_user%` | Player with most shares | `BuilderPro` |
| `%showcase:server_uptime%` | Showcase mod uptime | `4h` |

### Performance Metrics

| Placeholder | Description | Example Output |
|-------------|-------------|----------------|
| `%showcase:average_response_time%` | Average command response time | `15ms` |
| `%showcase:success_rate%` | Percentage of successful shares | `96.2%` |
| `%showcase:cache_hit_rate%` | Cache efficiency percentage | `87.3%` |

### Conditional Placeholders

| Placeholder | Description | Possible Outputs |
|-------------|-------------|------------------|
| `%showcase:if_can_share_item_yes_no%` | Item sharing permission | `yes` / `no` |
| `%showcase:if_admin_admin_player%` | Admin status | `admin` / `player` |
| `%showcase:if_cooldown_ready_waiting%` | Cooldown status | `ready` / `waiting` |
| `%showcase:if_has_shares_active_none%` | Share status | `active` / `none` |

## Configuration

The extended placeholder system can be configured in `config/showcase/config.yml`:

```yaml
placeholders:
  # Enable extended placeholder functionality
  enabled: true

  # Maximum number of active shares per player
  maxSharesPerPlayer: 10

  # Cache duration for placeholder results in seconds
  cacheDuration: 30

  # Enable player statistics tracking for placeholders
  enableStatisticsTracking: true

  # Enable server-wide statistics for placeholders
  enableServerStatistics: true

  # Enable performance metrics tracking
  enablePerformanceMetrics: true

  # How often to update statistics cache in seconds
  statisticsUpdateInterval: 60


  # Enable conditional placeholders (if_xxx_yes_no style)
  enableConditionalPlaceholders: true
```

## Usage Examples

### In Chat Messages

```
Player has %showcase:shares_count% active shares
Can create %showcase:shares_remaining% more shares
Server uptime: %showcase:server_uptime%
Admin status: %showcase:if_admin_admin_player%
```

### In GUI Components

```java
ItemStack infoItem = new ItemStack(Material.BOOK);
ItemMeta meta = infoItem.getItemMeta();
meta.setDisplayName("§6Share Information");
meta.setLore(Arrays.asList(
    "§7Active Shares: §f" + PlaceholderAPI.setPlaceholders(player, "%showcase:shares_count%"),
    "§7Remaining: §f" + PlaceholderAPI.setPlaceholders(player, "%showcase:shares_remaining%"),
    "§7Last Type: §f" + PlaceholderAPI.setPlaceholders(player, "%showcase:last_share_type%")
));
infoItem.setItemMeta(meta);
```

### In Scoreboard Integration

```java
Score shareScore = objective.getScore("§6Shares: §f" + PlaceholderAPI.setPlaceholders(player, "%showcase:shares_count%"));
shareScore.setScore(15);

Score typeScore = objective.getScore("§6Last Type: §f" + PlaceholderAPI.setPlaceholders(player, "%showcase:last_share_type%"));
typeScore.setScore(14);
```

## Testing Commands

### Admin Test Commands

> ⚠️ **Development Only**: These commands are only available in development environment and will not be registered in production builds.

The mod includes testing commands for administrators:

- `/showcase-test placeholders` - Test all extended placeholders
- `/showcase-test config` - Test configuration loading
- `/showcase-test simulate` - Simulate test data
- `/showcase-test validate` - Validate system functionality
- `/showcase-test performance` - Test performance metrics
- `/showcase-test benchmark <iterations>` - Benchmark placeholder speed

**Requirements**: Requires `showcase.admin.manage` permission.

### Testing Procedure

1. **Enable placeholders** in configuration
2. **Run configuration test**: `/showcase-test config`
3. **Simulate test data**: `/showcase-test simulate`
4. **Test all placeholders**: `/showcase-test placeholders`
5. **Validate system**: `/showcase-test validate`

## Performance Considerations

- **Caching**: Placeholder results are cached for performance
- **Configurable Features**: Disable unused features to improve performance
- **Async Operations**: Statistics updates happen asynchronously
- **Memory Usage**: Monitor memory usage on large servers

## Troubleshooting

### Common Issues

1. **Placeholders not working**: Check if `placeholders.enabled` is `true` in config
2. **Statistics not updating**: Verify `enableStatisticsTracking` is enabled
3. **Performance issues**: Increase `cacheDuration` or disable unused features
4. **Wrong values**: Check if `statisticsUpdateInterval` needs adjustment

### Debug Information

Enable debug logging for detailed information:

```yaml
debug:
  enabled: true
  logPlaceholders: true
  logPlaceholderPerformance: true
```

## Implementation Details

### Architecture

- **ExtendedPlaceholders**: Main placeholder registration class
- **ShowcaseStatistics**: Statistics tracking and management
- **PlaceholderTest**: Testing and validation utilities
- **PlaceholderTestCommand**: In-game testing commands

### Integration Points

- **ShowcaseMod**: Initialization and startup
- **ModConfig**: Configuration management
- **ShareRepository**: Data access layer
- **PermissionChecker**: Permission validation

## Future Enhancements

Potential future additions:

- **Custom placeholder arguments** (e.g., `%showcase:cooldown:item%`)
- **Time-based placeholders** (e.g., `%showcase:stats:today%`)
- **Advanced conditional logic**
- **Integration with other mods**
- **REST API placeholders**

## Compatibility

- **Minecraft Versions**: 1.21.2, 1.21.4, 1.21.5, 1.21.6
- **Placeholder API**: eu.pb4:placeholder-api
- **Dependencies**: Fabric API, LuckPerms (optional)

## Documentation & Resources

### 📖 Complete Documentation
- **[PlaceholderAPI Reference](https://showcase-fabric.vercel.app/docs/developers/placeholderapi)** - Complete placeholder list with examples
- **[Configuration Guide](https://showcase-fabric.vercel.app/docs/configuration)** - Setup and customization
- **[Developer Guide](https://showcase-fabric.vercel.app/docs/developers)** - Integration examples
- **[FAQ & Troubleshooting](https://showcase-fabric.vercel.app/docs/support/faq)** - Common issues

### 🔧 Testing Commands

> ⚠️ **Development Only**: These commands are only available when running in development environment.

```
/showcase-test placeholders  - Test all placeholders
/showcase-test performance   - Test performance metrics
/showcase-test benchmark 100 - Benchmark placeholder speed
/showcase-test config        - Test configuration loading
/showcase-test simulate      - Simulate test data
/showcase-test validate      - Validate system functionality
```

## Support

For issues or questions about the extended placeholder functionality:

1. **[Check Documentation](https://showcase-fabric.vercel.app/docs/developers/placeholderapi)** - Complete reference
2. **Run built-in test commands** - Use `/showcase-test` for diagnostics
3. **Review server logs** - Check for configuration errors
4. **[Report Issues](https://github.com/jiazengp/showcase-fabric/issues)** - GitHub Issues

The extended placeholder system provides comprehensive integration options for server administrators and plugin developers to create rich, dynamic content using Showcase mod data.