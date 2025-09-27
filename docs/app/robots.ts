import { MetadataRoute } from 'next';

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: '*',
      allow: '/',
      disallow: ['/api/', '/_next/', '/404'],
    },
    sitemap: 'https://showcase-fabric.vercel.app/sitemap.xml',
    host: 'https://showcase-fabric.vercel.app',
  };
}