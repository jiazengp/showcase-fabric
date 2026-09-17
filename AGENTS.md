# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Build and Development Commands

### Single Version Building and Testing
- `./gradlew build` - Build the mod for the default Minecraft version, generates JAR in `build/libs/`
- `./gradlew build -PmcVer=26.3` - Build for the supported Minecraft version explicitly
- `./gradlew runServer` - Run development server for testing
- `./gradlew runGameTest` - Run the Fabric GameTest suite for the default Minecraft version
- `./gradlew runGameTest -PmcVer=26.3` - Run GameTests for a specific supported version
- `./gradlew test` - Run unit tests
- `./gradlew clean` - Clean build artifacts

### Version Targeting
- `./buildAll.sh` (Linux/Mac) or `buildAll.bat` (Windows) - Build for every version listed in `version_properties/`
- `./gradlew listVersions` - List all available Minecraft versions for building
- Output JARs are collected in `build/buildAllJars/` directory
- Current supported baseline: 26.3
- Version-specific dependencies are loaded from `version_properties/{version}.properties`

### Development Server
- Server files are in `run/` directory
- Configuration: `run/config/showcase/config.yml`
- LuckPerms integration available in development environment
- Server runs on default port with test world

### Documentation
- `cd docs && pnpm install && pnpm dev` - Run documentation site locally
- Documentation uses Nuxt.js with multi-language support
- Version info auto-generated from `gradle.properties`

### CI/CD Workflows
- **build.yml**: Matrix builds for all supported MC versions on PR/push
- **test.yml**: Multi-version GameTest and unit test execution
- **release.yml**: Automated multi-version JAR creation and publishing to GitHub, Modrinth, and CurseForge
- **multi-version-compatibility.yml**: Comprehensive compatibility testing across all versions

### Publishing Setup (Modrinth & CurseForge)

The release workflow uses a matrix strategy to publish separate releases for each Minecraft version, utilizing mc-publish action for platform distribution.

**Required GitHub Secrets:**
1. **MODRINTH_TOKEN** - Modrinth API token with write permissions
   - Generate at: https://modrinth.com/settings/account → API tokens
2. **CURSEFORGE_TOKEN** - CurseForge API token
   - Generate at: https://www.curseforge.com/account/api-tokens

**Setup Instructions:**
1. Go to repository Settings → Secrets and variables → Actions
2. Add the two secrets listed above with your platform-specific values
3. Create initial project pages on both Modrinth and CurseForge manually
4. Tag a release (e.g., `git tag v2.3.1 && git push --tags`) to trigger automatic publishing

**Publishing Features:**
- **Separate releases per Minecraft version** (currently 26.3 on the mainline branch)
- **Dynamic version configuration** from `version_properties/` files
- **Version-specific compatibility detection** using `compatible_minecraft_versions` field
- **Flexible release types** (release/beta/alpha) based on `release_type` property  
- **Integrated changelog linking** to website changelog
- **Individual JAR publishing** instead of bundled multi-version packages
- **Automated version metadata** extraction and validation

**Matrix Strategy Benefits:**
- Each MC version gets its own release entry on platforms
- Users can easily find and download the correct version
- Platform-specific compatibility information is accurate
- Release notes link back to the main repository changelog

## Architecture Overview

### Core Components

**Command System**: Multi-layered command architecture in `src/main/java/com/showcase/command/`
- `ShowcaseCommand` - Main command registration hub
- `PlayerShowcaseCommand` - Player-facing commands (`/showcase`)
- `AdminShowcaseCommand` - Admin commands (`/admin-showcase`) 
- `ShowcaseManageCommand` - Management commands (`/showcase-manage`)
- `ShareCommandExecutor` - Command execution logic with fine-grained permission support
- `ShareCommandUtils` - Shared utilities for command processing

**Permission System**: Type-safe permission management
- `Permissions` class - Constants with enum-based `CommandType` for hierarchical permissions
- `PermissionChecker` - LuckPerms integration with fallback to operator levels
- Supports fine-grained permissions like `showcase.command.item.receivers`

**Data Management**:
- `ShowcaseManager` - Core sharing logic and temporary storage
- `ShareRepository` - Persistent data storage using Fabric's global data system
- `GlobalDataManager` - JSON codec-based serialization for cross-session persistence
- `JsonCodecDataStorage` - Enhanced data storage with error recovery, automatic backup of corrupted files
- Share entries expire automatically based on configuration

**GUI System**: Custom inventory-based interfaces
- `BaseWorldGui` - Foundation for all GUI implementations
- `ContainerGui` - Display shared containers with pagination
- `MerchantContext`/`ReadonlyMerchantGui` - Villager trade display
- `MapViewerGui` - Interactive map viewing with zoom/pan

### Key Integrations

**Chat & Placeholders**:
- `ChatMessageListener` - Processes chat keywords like `[item]` → clickable links
- `Placeholders` - PlaceholderAPI integration for other mods (`%showcase:item%`)
- Both systems respect permission hierarchies

**Statistics System**:
- `ShowcaseStatistics` - Core statistics tracking and caching system with comprehensive integration
- `StatCollector` - Gathers player statistics with categorization
- `StatFormatter` - Converts raw stats into readable books
- `StatCategories` - Configurable grouping of statistics
- `StatisticsCache` - Thread-safe caching with expiration and performance metrics
- `PlayerStatisticsData`/`ServerStatisticsData` - Persistent statistics data structures

### Data Flow

1. **Command Execution**: Commands → `ShareCommandExecutor` → `ShowcaseManager`
2. **Permission Validation**: Multi-level checks (base command → sub-permissions)  
3. **Share Creation**: Content serialization → temporary storage → expiration handling
4. **View Generation**: Share ID → GUI rendering → interactive display
5. **Persistence**: Cross-session data via `GlobalDataManager` + JSON codecs

### Testing Infrastructure

**GameTest Suite** (`src/gametest/`):
- Own `gametest` source set, kept out of the published JAR and compiled as part of `check`
- `GameTestPlayerUtils` - Shared helper for constructing `ServerPlayer` instances inside tests
- `BlockCategoriesGameTest` - Block categorization and translation-key coverage
- `ContainerContentsGameTest` - Bundle and shulker-box unpacking, including slot ordering
- `EventSystemGameTest` - `ShowcaseEvents` / `ShowcaseAPI` listener semantics
- `DataPersistenceGameTest` - `ShareRepository` CRUD, bulk operations and load edge cases
- Classes are registered through the `fabric-gametest` entrypoint in `src/gametest/resources/fabric.mod.json`
- Run with `./gradlew runGameTest -PmcVer=<version>`; the task sets `-Dfabric-api.gametest=true`,
  writes `build/gametest-report.xml`, and fails the build when a required test does not pass
- Tests must call `GameTestHelper#succeed()` (or `fail(...)`) — returning normally is not enough

### Translation Files

Translations live in `data/showcase/lang/*.json` and are delivered by server-translations-api.
Every locale's set is the **union of that locale with `en_us`**, which drives two failure modes:

- a key missing from `en_us` is sent for **no** locale, so every client renders the raw key;
- a key missing from one locale falls back to the English text, so it renders untranslated.

`./gradlew build` cannot see either, so run the static check before pushing:

```
python3 .github/scripts/check-translations.py
```

It is wired into `.github/workflows/i18n.yml` and fails on: a locale whose key set differs from
`en_us`, placeholder count/type drift, a key used by `translatable(...)` but absent from `en_us`,
a call site whose argument count does not match `en_us`, and broadcast copy that regressed to the
first person or (for Chinese) lost the reflexive 自己. It also warns about keys nothing references.

When adding or changing a message:

- Add the key to **`en_us` first** — that is the fallback every other locale inherits.
- Keep the placeholder count, order and `%s`/`%d` types identical to `en_us`; the order must match
  the argument order at the call site, which is not always the order the target language prefers.
- `*_shared` / `other_*_shared` / `default.*` are **broadcast** to every player, including the one
  who acted, so they name players in the third person. Messages addressed to a single viewer or
  actor may use 你.
- Within those broadcasts, content the acting player genuinely owns — item, inventory, hotbar,
  ender chest, statistics — is described as "their own" (Chinese: 自己的). **Containers and
  merchants are the exception**: a container may be a chest standing in the world and a merchant
  belongs to a villager, so those use the indefinite form (English "a container", Chinese 一个容器)
  and must not claim ownership.

### Configuration

**Version Targeting**:
- Version-specific properties in `version_properties/` (currently 26.3 on the mainline branch)
- Automatic version detection and dependency resolution via `build.gradle:8-22`
- Preprocessor definitions generated in `build.properties` for conditional compilation
- Each version pins its own Fabric API, Fabric Loader and mod dependency coordinates
- Multi-version build scripts: `buildAll.sh`/`buildAll.bat` for cross-platform support
- CI/CD matrix builds test all versions in parallel

**Runtime Configuration**:
- YAML-based config with automatic reload capability
- Per-share-type permission defaults and cooldown settings
- Chat keyword customization and statistics display options

### Permission Hierarchy Design

The permission system uses a tree structure where higher-level permissions grant access to base functionality, and sub-permissions control specific features:

```
showcase.command.item              # Base item sharing
├── showcase.command.item.receivers    # Can specify receivers  
├── showcase.command.item.duration     # Can set custom duration
└── showcase.command.item.description  # Can add descriptions
```

This allows granular control where users might share items but not specify recipients, or set durations but not descriptions.

## Development Environment

**Environment Detection**:
- `DevUtils` - Utility class for development environment checks
- Test commands and features are automatically restricted to development environment only
- Use `DevUtils.isDevelopment()` to check environment status
- Development-specific features include test commands, configuration validation tests, and placeholder testing

**Version Compatibility**:
- Mixin method signatures vary between versions (e.g., `damage` method in `MixinServerPlayerEntity`)
- Use preprocessor directives (`#if MC_VER >= 1212`) for version-specific code
- Version compatibility layers in `utils/compat/` handle API differences between versions
- Cross-version data format compatibility handled by `JsonCodecDataStorage` with automatic error recovery

**Data Storage Error Handling**:
- Corrupted data files are automatically backed up with timestamps
- Failed codec decoding creates detailed error logs with specific failure information
- Original corrupted files are safely removed to prevent repeated loading failures
