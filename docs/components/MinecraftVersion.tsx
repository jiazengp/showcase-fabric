import { getAvailableMinecraftVersions, getLatestMinecraftVersion } from '@/lib/versions';

interface MinecraftVersionProps {
  versions?: string[];
  latest?: boolean;
  auto?: boolean;
}

/**
 * Display Minecraft version badges
 *
 * @param versions - Array of specific versions to display
 * @param latest - Show only the latest version
 * @param auto - Automatically load all available versions from version_properties
 */
export function MinecraftVersion({ versions, latest = false, auto = false }: MinecraftVersionProps) {
  let displayVersions: string[] = [];

  if (auto) {
    // Automatically load all available versions
    displayVersions = getAvailableMinecraftVersions();
  } else if (latest) {
    // Show only the latest version
    displayVersions = [getLatestMinecraftVersion()];
  } else if (versions) {
    // Use provided versions
    displayVersions = versions;
  } else {
    // Fallback to auto if no props provided
    displayVersions = getAvailableMinecraftVersions();
  }

  return (
    <div className="inline-flex items-center gap-1">
      {displayVersions.map((version) => (
        <span
          key={version}
          className="rounded bg-green-100 px-2 py-1 text-xs font-medium text-green-800 dark:bg-green-900 dark:text-green-200"
        >
          MC {version}
        </span>
      ))}
    </div>
  );
}

/**
 * Display version compatibility information
 */
export function VersionCompatibility({ version }: { version?: string }) {
  const displayVersion = version || getLatestMinecraftVersion();

  return (
    <div className="rounded-lg border border-green-200 bg-green-50 p-4 dark:border-green-700 dark:bg-green-900">
      <h4 className="font-semibold text-green-900 dark:text-green-100">Version Compatibility</h4>
      <p className="mt-1 text-sm text-green-700 dark:text-green-300">
        This feature is available in Minecraft {displayVersion}
      </p>
    </div>
  );
}

/**
 * Display a table of all supported versions with details
 */
export function VersionTable() {
  const versions = getAvailableMinecraftVersions();

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
        <thead className="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
              Minecraft Version
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
              Status
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
              Download
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 bg-white dark:divide-gray-700 dark:bg-gray-900">
          {versions.map((version) => (
            <tr key={version}>
              <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900 dark:text-gray-100">
                {version}
              </td>
              <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                <span className="inline-flex rounded-full bg-green-100 px-2 text-xs font-semibold leading-5 text-green-800 dark:bg-green-900 dark:text-green-200">
                  Supported
                </span>
              </td>
              <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-500 dark:text-gray-400">
                <a
                  href={`https://modrinth.com/mod/showcase/version/latest?g=${version}`}
                  className="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  Download
                </a>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}