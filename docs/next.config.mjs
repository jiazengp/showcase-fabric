import { createMDX } from 'fumadocs-mdx/next';

const withMDX = createMDX();

/** @type {import('next').NextConfig} */
const config = {
  reactStrictMode: true,
  // Basic image optimization
  images: {
    formats: ['image/webp'],
  },
  // Experimental features for better performance
  experimental: {
    optimizePackageImports: ['fumadocs-ui', 'fumadocs-core'],
  },
  // Compiler optimizations
  compiler: {
    removeConsole: process.env.NODE_ENV === 'production',
  },
};

export default withMDX(config);
