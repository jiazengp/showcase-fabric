import { getLatestMinecraftVersion, getVersionInfo } from '@/lib/versions';

interface VersionedCodeBlockProps {
  language?: string;
  template: string;
  version?: string;
}

/**
 * Display a code block with version placeholders automatically replaced
 *
 * Placeholders:
 * - {MC_VERSION} - Minecraft version (e.g., "1.21.6")
 * - {FABRIC_VERSION} - Fabric API version
 * - {LOADER_VERSION} - Fabric Loader version
 * - {PLACEHOLDER_API_VERSION} - PlaceholderAPI version
 * - {MOD_VERSION} - Example mod version based on MC version
 */
export function VersionedCodeBlock({ language = "groovy", template, version }: VersionedCodeBlockProps) {
  const targetVersion = version || getLatestMinecraftVersion();
  const versionInfo = getVersionInfo(targetVersion);

  if (!versionInfo) {
    return (
      <pre className="rounded-lg bg-red-50 p-4 text-red-700 dark:bg-red-900 dark:text-red-300">
        Error: Version information not available for {targetVersion}
      </pre>
    );
  }

  // Replace version placeholders
  const processedCode = template
    .replace(/{MC_VERSION}/g, versionInfo.minecraftVersion)
    .replace(/{FABRIC_VERSION}/g, versionInfo.fabricVersion)
    .replace(/{LOADER_VERSION}/g, versionInfo.fabricLoaderVersion)
    .replace(/{PLACEHOLDER_API_VERSION}/g, versionInfo.placeholderApiVersion)
    .replace(/{MOD_VERSION}/g, `2.3.1+mc${versionInfo.minecraftVersion}`);

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <span className="text-sm text-gray-600 dark:text-gray-400">
          For Minecraft {targetVersion}
        </span>
        <span className="rounded bg-blue-100 px-2 py-1 text-xs text-blue-800 dark:bg-blue-900 dark:text-blue-200">
          Auto-generated
        </span>
      </div>
      <pre className="overflow-x-auto rounded-lg bg-gray-100 p-4 dark:bg-gray-800">
        <code className={`language-${language}`}>
          {processedCode}
        </code>
      </pre>
    </div>
  );
}

/**
 * Maven dependency with current versions
 */
export function MavenDependency({ version }: { version?: string }) {
  const template = `<dependency>
    <groupId>maven.modrinth</groupId>
    <artifactId>showcase</artifactId>
    <version>{MOD_VERSION}</version>
    <scope>provided</scope>
</dependency>`;

  return <VersionedCodeBlock language="xml" template={template} version={version} />;
}

/**
 * Gradle dependency with current versions
 */
export function GradleDependency({ version }: { version?: string }) {
  const template = `dependencies {
    minecraft "{MC_VERSION}"
    mappings "net.fabricmc:yarn:{MC_VERSION}+build.1:v2"
    modImplementation "net.fabricmc:fabric-loader:{LOADER_VERSION}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:{FABRIC_VERSION}"

    // Showcase mod
    modImplementation "maven.modrinth:showcase:{MOD_VERSION}"

    // Optional dependencies
    modImplementation "eu.pb4:placeholder-api:{PLACEHOLDER_API_VERSION}"
}`;

  return <VersionedCodeBlock language="groovy" template={template} version={version} />;
}