# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Build and Development Commands

### Single Version Building and Testing
- `./gradlew build` - Build the mod for the default Minecraft version, generates JAR in `build/libs/`
- `./gradlew build -PmcVer=26.2` - Build for the supported Minecraft version explicitly
- `./gradlew runServer` - Run development server for testing
- `./gradlew runShowcaseE2ETest` - Run E2E integration tests for the default version
- `./gradlew runShowcaseE2ETest -PmcVer=26.2` - Run E2E tests for the supported version explicitly
- `./gradlew test` - Run unit tests
- `./gradlew clean` - Clean build artifacts

### Version Targeting
- `./buildAll.sh` (Linux/Mac) or `buildAll.bat` (Windows) - Build for every version listed in `version_properties/`
- `./gradlew listVersions` - List all available Minecraft versions for building
- Output JARs are collected in `build/buildAllJars/` directory
- Current supported baseline: 26.2
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
- **Separate releases per Minecraft version** (currently 26.2 on the mainline branch)
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

**E2E Test Suite** (`src/main/java/com/showcase/test/`):
- `ShowcaseTestClient` - Main E2E test client with conditional execution
- Tests run in real Minecraft client environment with `runShowcaseE2ETest`
- Conditional execution controlled by `showcase.test.e2e` system property
- Automatic client initialization detection and basic framework validation
- Results output with proper exit codes for CI/CD integration

### Configuration

**Version Targeting**:
- Version-specific properties in `version_properties/` (currently 26.2 on the mainline branch)
- Automatic version detection and dependency resolution via `build.gradle:8-22`
- Preprocessor definitions generated in `build.properties` for conditional compilation
- Each version has isolated dependencies: Fabric API, Yarn mappings, Loader versions
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
