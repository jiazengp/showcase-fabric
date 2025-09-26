import { DocsLayout } from 'fumadocs-ui/layouts/docs';
import { baseOptions } from '@/lib/layout.shared';
import { source } from '@/lib/source';
import type { ReactNode } from 'react';

export default async function Layout({
  children,
  params,
}: {
  children: ReactNode;
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const localizedPrefix = lang === 'en' ? '' : `/${lang}`;

  return (
    <DocsLayout
      tree={source.pageTree[lang]}
      {...baseOptions(lang)}
      sidebar={{
        enabled: true,
        collapsible: true,
        tabs: [
          {
            title: 'Getting Started',
            url: `${localizedPrefix}/docs/getting-started`,
            description: 'Installation and setup guide',
          },
          {
            title: 'Features',
            url: `${localizedPrefix}/docs/features`,
            description: 'Core functionality and capabilities',
          },
          {
            title: 'Commands',
            url: `${localizedPrefix}/docs/commands`,
            description: 'Complete command reference',
          },
          {
            title: 'Configuration',
            url: `${localizedPrefix}/docs/configuration`,
            description: 'Setup and customization',
          },
          {
            title: 'Developers',
            url: `${localizedPrefix}/docs/developers`,
            description: 'API and development resources',
          },
          {
            title: 'Support',
            url: `${localizedPrefix}/docs/support`,
            description: 'Help and troubleshooting',
          },
        ],
      }}
    >
      {children}
    </DocsLayout>
  );
}
