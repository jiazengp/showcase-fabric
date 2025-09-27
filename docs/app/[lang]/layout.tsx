import { RootProvider } from 'fumadocs-ui/provider';
import '../globals.css';

const translations = {
  zh: {
    search: '搜索文档...',
    toc: '目录',
    lastUpdate: '最后更新',
    searchNoResult: '未找到结果',
    editOnGithub: '在 GitHub 上编辑',
    previousPage: '上一页',
    nextPage: '下一页',
  },
} as const;

const locales = [
  { name: 'English', locale: 'en' },
  { name: '中文', locale: 'zh' },
];

export default async function LanguageLayout({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;

  return (
    <RootProvider
      i18n={{
        locale: lang,
        locales,
        translations: translations[lang as keyof typeof translations],
      }}
      search={{
        enabled: true,
        hotKey: [
          { display: 'K', key: 'k' },
          { display: '⌘', key: 'meta' },
        ],
      }}
      theme={{
        attribute: 'class',
        defaultTheme: 'system',
        enableSystem: true,
        disableTransitionOnChange: false,
      }}
    >
      {children}
    </RootProvider>
  );
}