import * as fs from 'fs';
import * as path from 'path';

export interface VersionInfo {
  minecraftVersion: string;
  compatibleVersions: string[];
  fabricLoaderVersion: string;
  fabricVersion: string;
  placeholderApiVersion: string;
  releaseType: 'release' | 'beta' | 'alpha';
}

/**
 * Read and parse a properties file
 */
function parsePropertiesFile(filePath: string): Record<string, string> {
  const content = fs.readFileSync(filePath, 'utf-8');
  const properties: Record<string, string> = {};

  content.split('\n').forEach(line => {
    line = line.trim();
    if (line && !line.startsWith('#')) {
      const [key, ...valueParts] = line.split('=');
      if (key && valueParts.length > 0) {
        properties[key.trim()] = valueParts.join('=').trim();
      }
    }
  });

  return properties;
}

/**
 * Get all available Minecraft versions from version_properties directory
 */
export function getAvailableMinecraftVersions(): string[] {
  const versionPropsDir = path.join(process.cwd(), '..', 'version_properties');

  if (!fs.existsSync(versionPropsDir)) {
    console.warn('version_properties directory not found, using fallback versions');
    return ['1.21.2', '1.21.4', '1.21.5', '1.21.6'];
  }

  const files = fs.readdirSync(versionPropsDir);
  const versions = files
    .filter(file => file.endsWith('.properties'))
    .map(file => file.replace('.properties', ''))
    .sort((a, b) => {
      // Sort versions properly (1.21.2 < 1.21.4 < 1.21.5 < 1.21.6)
      const parseVersion = (v: string) => v.split('.').map(n => parseInt(n, 10));
      const aParts = parseVersion(a);
      const bParts = parseVersion(b);

      for (let i = 0; i < Math.max(aParts.length, bParts.length); i++) {
        const aNum = aParts[i] || 0;
        const bNum = bParts[i] || 0;
        if (aNum !== bNum) {
          return aNum - bNum;
        }
      }
      return 0;
    });

  return versions;
}

/**
 * Get detailed version information for a specific Minecraft version
 */
export function getVersionInfo(minecraftVersion: string): VersionInfo | null {
  const versionPropsDir = path.join(process.cwd(), '..', 'version_properties');
  const filePath = path.join(versionPropsDir, `${minecraftVersion}.properties`);

  if (!fs.existsSync(filePath)) {
    return null;
  }

  const props = parsePropertiesFile(filePath);

  // Parse compatible versions array
  let compatibleVersions: string[] = [];
  if (props.compatible_minecraft_versions) {
    try {
      compatibleVersions = JSON.parse(props.compatible_minecraft_versions);
    } catch {
      compatibleVersions = [props.minecraft_version || minecraftVersion];
    }
  }

  return {
    minecraftVersion: props.minecraft_version || minecraftVersion,
    compatibleVersions,
    fabricLoaderVersion: props.fabric_loader_version || '',
    fabricVersion: props.fabric_version || '',
    placeholderApiVersion: props.placeholder_api_version || '',
    releaseType: (props.release_type as 'release' | 'beta' | 'alpha') || 'release',
  };
}

/**
 * Get all version information
 */
export function getAllVersionInfo(): VersionInfo[] {
  const versions = getAvailableMinecraftVersions();
  return versions
    .map(version => getVersionInfo(version))
    .filter((info): info is VersionInfo => info !== null);
}

/**
 * Get the latest available Minecraft version
 */
export function getLatestMinecraftVersion(): string {
  const versions = getAvailableMinecraftVersions();
  return versions[versions.length - 1] || '1.21.6';
}

/**
 * Check if a version is supported
 */
export function isSupportedVersion(version: string): boolean {
  return getAvailableMinecraftVersions().includes(version);
}