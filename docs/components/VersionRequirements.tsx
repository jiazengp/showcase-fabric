import { getLatestMinecraftVersion, getVersionInfo, getAvailableMinecraftVersions } from '@/lib/versions';

interface VersionRequirementsProps {
  version?: string;
  showAllVersions?: boolean;
}

/**
 * Display version requirements automatically read from version_properties
 */
export function VersionRequirements({ version, showAllVersions = false }: VersionRequirementsProps) {
  const targetVersion = version || getLatestMinecraftVersion();
  const versionInfo = getVersionInfo(targetVersion);

  if (!versionInfo) {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-4 dark:border-red-700 dark:bg-red-900">
        <p className="text-red-700 dark:text-red-300">Version information not available for {targetVersion}</p>
      </div>
    );
  }

  const availableVersions = getAvailableMinecraftVersions();

  return (
    <div className="rounded-lg border border-blue-200 bg-blue-50 p-4 dark:border-blue-700 dark:bg-blue-900">
      <h4 className="font-semibold text-blue-900 dark:text-blue-100">Server Requirements</h4>
      <ul className="mt-2 space-y-1 text-sm text-blue-700 dark:text-blue-300">
        <li>
          <strong>Minecraft Server:</strong>{' '}
          {showAllVersions ? availableVersions.join(', ') : targetVersion}
          {versionInfo.compatibleVersions.length > 1 && (
            <span className="ml-1 text-xs">
              (Compatible: {versionInfo.compatibleVersions.join(', ')})
            </span>
          )}
        </li>
        <li>
          <strong>Fabric Loader:</strong> {versionInfo.fabricLoaderVersion}
        </li>
        <li>
          <strong>Fabric API:</strong> {versionInfo.fabricVersion}
        </li>
        {versionInfo.placeholderApiVersion && (
          <li>
            <strong>PlaceholderAPI:</strong> {versionInfo.placeholderApiVersion}
          </li>
        )}
      </ul>
    </div>
  );
}

/**
 * Display dependency versions for developers
 */
export function DependencyVersions({ version }: { version?: string }) {
  const targetVersion = version || getLatestMinecraftVersion();
  const versionInfo = getVersionInfo(targetVersion);

  if (!versionInfo) {
    return null;
  }

  return (
    <div className="space-y-4">
      <h4 className="font-semibold">Dependency Versions for MC {targetVersion}</h4>

      <div className="rounded-md bg-gray-100 p-4 dark:bg-gray-800">
        <pre className="text-sm">
{`dependencies {
    minecraft "${versionInfo.minecraftVersion}"
    mappings "net.fabricmc:yarn:${versionInfo.minecraftVersion}+build.1:v2"
    modImplementation "net.fabricmc:fabric-loader:${versionInfo.fabricLoaderVersion}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${versionInfo.fabricVersion}"

    // Optional dependencies
    modImplementation "eu.pb4:placeholder-api:${versionInfo.placeholderApiVersion}"
}`}
        </pre>
      </div>
    </div>
  );
}