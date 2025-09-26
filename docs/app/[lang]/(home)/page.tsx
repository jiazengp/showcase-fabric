import Link from 'next/link';

export default async function HomePage({
  params,
}: {
  params: Promise<{ lang: string }>;
}) {
  const { lang } = await params;
  const localizedPrefix = lang === 'en' ? '' : `/${lang}`;

  return (
    <main className="flex flex-1 flex-col justify-center text-center">
      <h1 className="mb-4 text-2xl font-bold">Showcase Mod Documentation</h1>
      <p className="text-gray-600 dark:text-gray-400">
        A server-side Minecraft mod for sharing items, inventories, and more through interactive links.
      </p>
      <p className="mt-4 text-gray-600 dark:text-gray-400">
        You can open{' '}
        <Link
          href={`${localizedPrefix}/docs`}
          className="text-blue-600 font-semibold underline dark:text-blue-400"
        >
          /docs
        </Link>{' '}
        and see the documentation.
      </p>
    </main>
  );
}
