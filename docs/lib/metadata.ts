import type { Metadata } from 'next';

export const siteConfig = {
  name: 'Showcase Mod',
  description: 'A server-side Minecraft mod for sharing items, inventories, and more through interactive links',
  url: 'https://showcase-fabric.vercel.app',
  ogImage: 'https://showcase-fabric.vercel.app/og-image.png',
  links: {
    github: 'https://github.com/jiazengp/showcase-fabric',
    modrinth: 'https://modrinth.com/mod/showcase',
    curseforge: 'https://www.curseforge.com/minecraft/mc-mods/showcasemod',
  },
};

export function createMetadata(override: Metadata = {}): Metadata {
  return {
    metadataBase: new URL(siteConfig.url),
    title: {
      default: siteConfig.name,
      template: `%s | ${siteConfig.name}`,
    },
    description: siteConfig.description,
    keywords: [
      'Minecraft',
      'Fabric',
      'Mod',
      'Server',
      'Items',
      'Inventory',
      'Sharing',
      'PlaceholderAPI',
      'Commands',
    ],
    authors: [
      {
        name: 'jiazengp',
        url: 'https://github.com/jiazengp',
      },
    ],
    creator: 'jiazengp',
    openGraph: {
      type: 'website',
      locale: 'en_US',
      url: siteConfig.url,
      title: siteConfig.name,
      description: siteConfig.description,
      siteName: siteConfig.name,
      images: [
        {
          url: siteConfig.ogImage,
          width: 1200,
          height: 630,
          alt: siteConfig.name,
        },
      ],
    },
    twitter: {
      card: 'summary_large_image',
      title: siteConfig.name,
      description: siteConfig.description,
      images: [siteConfig.ogImage],
      creator: '@jiazengp',
    },
    icons: {
      icon: '/favicon.ico',
      shortcut: '/favicon-16x16.png',
      apple: '/apple-touch-icon.png',
    },
    manifest: '/site.webmanifest',
    robots: {
      index: true,
      follow: true,
      googleBot: {
        index: true,
        follow: true,
        'max-video-preview': -1,
        'max-image-preview': 'large',
        'max-snippet': -1,
      },
    },
    verification: {
      google: 'your-google-verification-code',
      // Add other verification codes as needed
    },
    alternates: {
      canonical: siteConfig.url,
      languages: {
        'en-US': `${siteConfig.url}/en`,
        'zh-CN': `${siteConfig.url}/cn`,
      },
    },
    ...override,
  };
}