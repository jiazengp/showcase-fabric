import defaultMdxComponents from 'fumadocs-ui/mdx';
import { Tab, Tabs } from 'fumadocs-ui/components/tabs';
import { Callout } from 'fumadocs-ui/components/callout';
import { Card, Cards } from 'fumadocs-ui/components/card';
import { File, Folder, Files } from 'fumadocs-ui/components/files';
import { CodeBlock, Pre } from 'fumadocs-ui/components/codeblock';
import type { MDXComponents } from 'mdx/types';

// Import version-aware components
import { MinecraftVersion, VersionCompatibility, VersionTable } from '@/components/MinecraftVersion';
import { VersionRequirements, DependencyVersions } from '@/components/VersionRequirements';
import { VersionedCodeBlock, MavenDependency, GradleDependency } from '@/components/VersionedCodeBlock';
import { RelatedLinks } from '@/components/RelatedLinks';
import { Command, Settings, Shield, Code, Database, BookOpen, HelpCircle, Activity, Layers } from '@/components/Icons';

// Custom components for mod documentation
function CommandSyntax({ children, ...props }: { children: React.ReactNode }) {
  return (
    <span className="not-prose inline-block my-4 w-full rounded-lg border border-gray-200 bg-gray-50 p-4 dark:border-gray-700 dark:bg-gray-900">
      <span className="block font-mono text-sm text-gray-600 mb-2 dark:text-gray-400">Command Syntax:</span>
      <code className="text-sm font-bold text-gray-900 dark:text-gray-100">{children}</code>
    </span>
  );
}

function PermissionNode({ node, description }: { node: string; description: string }) {
  return (
    <div className="my-2 rounded border-l-4 border-blue-500 bg-blue-50 p-3 dark:bg-blue-950">
      <code className="font-mono text-sm font-bold text-blue-700 dark:text-blue-300">{node}</code>
      <p className="mt-1 text-sm text-blue-600 dark:text-blue-400">{description}</p>
    </div>
  );
}

// use this function to get MDX components, you will need it for rendering MDX
export function getMDXComponents(components?: MDXComponents): MDXComponents {
  return {
    ...defaultMdxComponents,
    // Enhanced code blocks
    pre: ({ ref: _ref, ...props }) => (
      <CodeBlock {...props}>
        <Pre>{props.children}</Pre>
      </CodeBlock>
    ),
    // Fumadocs UI components
    Tab,
    Tabs,
    Callout,
    Card,
    Cards,
    File,
    Folder,
    Files,
    // Custom mod-specific components
    CommandSyntax,
    PermissionNode,
    // Version-aware components (automatically read from version_properties)
    MinecraftVersion,
    VersionCompatibility,
    VersionTable,
    VersionRequirements,
    DependencyVersions,
    VersionedCodeBlock,
    MavenDependency,
    GradleDependency,
    // Related links components
    RelatedLinks,
    // Icon components
    Command,
    Settings,
    Shield,
    Code,
    Database,
    BookOpen,
    HelpCircle,
    Activity,
    Layers,
    ...components,
  };
}

// Export for MDX provider
export const useMDXComponents = getMDXComponents;
