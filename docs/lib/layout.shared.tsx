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
    },
    // Enhanced navigation links
    links: [
      {
        text: 'Documentation',
        url: `${localizedPrefix}/docs`,
        active: 'nested-url',
      },
      {
        text: 'GitHub',
        url: 'https://github.com/jiazengp/showcase-fabric',
        external: true,
      },
      {
        text: 'Download',
        url: 'https://modrinth.com/mod/showcase',
        external: true,
      },
    ],
  };
}
