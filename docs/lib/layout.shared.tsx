import type { BaseLayoutProps } from 'fumadocs-ui/layouts/shared';
import { i18n } from '@/lib/i18n';

/**
 * Shared layout configurations
 *
 * you can customise layouts individually from:
 * Home Layout: app/[lang]/(home)/layout.tsx
 * Docs Layout: app/[lang]/docs/layout.tsx
 */
export function baseOptions(locale?: string): BaseLayoutProps {
  const currentLocale = locale || i18n.defaultLanguage;
  const localizedPrefix = currentLocale === i18n.defaultLanguage ? '' : `/${currentLocale}`;

  return {
    i18n,
    nav: {
      title: 'Showcase Mod',
      url: `${localizedPrefix}/`,
      transparentMode: 'top',
    },
    // Enhanced navigation links
    links: [
      {
        text: 'Documentation',
        url: `${localizedPrefix}/docs`,
        active: 'nested-url',
      },
      {
        type: 'menu',
        text: 'Download',
        items: [
          {
            text: 'Modrinth',
            url: 'https://modrinth.com/mod/showcase',
            description: 'Recommended platform',
            external: true,
          },
          {
            text: 'CurseForge',
            url: 'https://www.curseforge.com/minecraft/mc-mods/showcasemod',
            description: 'Alternative platform',
            external: true,
          },
          {
            text: 'GitHub Releases',
            url: 'https://github.com/jiazengp/showcase-fabric/releases',
            description: 'Source and development builds',
            external: true,
          },
        ],
      },
      {
        text: 'GitHub',
        url: 'https://github.com/jiazengp/showcase-fabric',
        external: true,
      },
    ],
    // Enhanced footer configuration
    githubUrl: 'https://github.com/jiazengp/showcase-fabric',
  };
}
