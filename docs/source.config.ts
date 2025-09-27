import {
  defineConfig,
  defineDocs,
  frontmatterSchema,
  metaSchema,
} from 'fumadocs-mdx/config';
import { z } from 'zod';

// You can customise Zod schemas for frontmatter and `meta.json` here
// see https://fumadocs.dev/docs/mdx/collections#define-docs
export const docs = defineDocs({
  docs: {
    schema: frontmatterSchema.extend({
      // Add custom frontmatter fields for mod documentation
      version: z.string().optional(),
      category: z.string().optional(),
      tags: z.array(z.string()).optional(),
    }),
  },
  meta: {
    schema: metaSchema.extend({
      // Add custom meta fields for enhanced navigation
      icon: z.string().optional(),
      description: z.string().optional(),
    }),
  },
});

export default defineConfig({
  mdxOptions: {
    // Provider import for custom components
    providerImportSource: '@/mdx-components',
  },
});
