import '@/app/global.css';
import { Inter } from 'next/font/google';
import { createMetadata } from '@/lib/metadata';

const inter = Inter({
  subsets: ['latin'],
});

export const metadata = createMetadata();

export default function Layout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className={inter.className} suppressHydrationWarning>
      <body className="flex flex-col min-h-screen">
        {children}
      </body>
    </html>
  );
}
