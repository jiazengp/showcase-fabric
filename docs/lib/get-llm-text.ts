import { source } from '@/lib/source';
import type { InferPageType } from 'fumadocs-core/source';

export async function getLLMText(page: InferPageType<typeof source>) {
  const processed = page.data.body;

  return `# ${page.data.title}
URL: ${page.url}

${processed}`;
}