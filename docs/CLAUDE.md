# CLAUDE.md - Documentation Project

This file provides guidance to Claude Code (claude.ai/code) when working with the Showcase mod documentation project.

## Project Overview

This is a Next.js-based documentation site using Fumadocs for the Showcase Minecraft mod. The documentation automatically imports version information from the parent project's `version_properties/` directory.

## Build and Development Commands

### Development
- `pnpm dev` - Start development server on http://localhost:3000
- `pnpm build` - Build the documentation site for production
- `pnpm start` - Start production server
- `pnpm lint` - Run ESLint for code quality
- `pnpm type-check` - Run TypeScript type checking

### Content Management
- `pnpm content:validate` - Validate all MDX content and links
- `pnpm content:format` - Format MDX files consistently

## Architecture Overview

### Framework Stack
- **Next.js 14** - React framework with App Router
- **Fumadocs** - Documentation framework with built-in components
- **MDX** - Markdown with JSX components for rich content
- **TypeScript** - Type safety and better development experience
- **Tailwind CSS** - Utility-first CSS framework

### Key Directories

```
docs/
├── app/                    # Next.js App Router pages
│   ├── [lang]/            # Internationalization routes
│   │   ├── docs/          # Documentation pages
│   │   └── (home)/        # Homepage
├── components/            # Custom React components
│   ├── MinecraftVersion.tsx    # Version display components
│   ├── VersionRequirements.tsx # Requirements components
│   └── VersionedCodeBlock.tsx  # Dynamic code examples
├── content/docs/          # MDX documentation content
│   ├── getting-started/   # Installation and setup guides
│   ├── commands/          # Command reference
│   ├── features/          # Feature documentation
│   ├── configuration/     # Configuration guides
│   ├── developers/        # API and development docs
│   └── support/           # FAQ and troubleshooting
├── lib/                   # Utility libraries
│   ├── versions.ts        # Version data reader from parent project
│   └── layout.shared.tsx  # Shared layout configuration
├── mdx-components.tsx     # Global MDX component registry
└── source.config.ts      # Fumadocs configuration
```

### Version-Aware Components

The documentation uses custom components that automatically read version information from the parent project's `version_properties/` directory:

#### Core Components
- `<MinecraftVersion auto />` - Displays all supported MC versions
- `<VersionRequirements />` - Shows server requirements with current versions
- `<VersionTable />` - Interactive table of all supported versions
- `<DependencyVersions />` - Shows development dependencies for current MC version
- `<VersionedCodeBlock />` - Code examples with auto-populated version placeholders

#### Dynamic Placeholders
Code examples support these automatic replacements:
- `{MC_VERSION}` - Current Minecraft version (e.g., "1.21.6")
- `{FABRIC_VERSION}` - Fabric API version
- `{LOADER_VERSION}` - Fabric Loader version
- `{PLACEHOLDER_API_VERSION}` - PlaceholderAPI version
- `{MOD_VERSION}` - Showcase mod version (e.g., "2.3.1+mc1.21.6")

### Content Guidelines

#### MDX Files
- Use semantic headings (h1 for page title, h2 for sections, etc.)
- Include frontmatter with `title`, `description`, and `category`
- Use built-in Fumadocs components: `<Callout>`, `<Tabs>`, `<Card>`, etc.
- Prefer version-aware components over hardcoded versions

#### Component Usage Examples
```mdx
---
title: Installation Guide
description: Download and install Showcase mod
category: getting-started
---

# Installation Guide

<MinecraftVersion auto />

## Requirements

<VersionRequirements showAllVersions />

## Dependencies

<DependencyVersions />

## Code Example

<VersionedCodeBlock language="groovy" template={`
dependencies {
    modImplementation "maven.modrinth:showcase:{MOD_VERSION}"
}
`} />
```

### Internationalization (i18n)

The site supports multiple languages using Next.js dynamic routes:
- English (`/docs/...`) - Default language
- Route structure: `app/[lang]/docs/[[...slug]]/page.tsx`
- Language detection and routing handled automatically

### Custom MDX Components

Register all custom components in `mdx-components.tsx`:
```typescript
export function getMDXComponents(components?: MDXComponents): MDXComponents {
  return {
    ...defaultMdxComponents,
    // Custom components
    MinecraftVersion,
    VersionRequirements,
    // ... other components
    ...components,
  };
}
```

## Development Workflow

### Adding New Documentation
1. Create new `.mdx` file in appropriate `content/docs/` subdirectory
2. Add frontmatter with required fields
3. Update `meta.json` in the directory if needed for navigation
4. Use version-aware components instead of hardcoded versions
5. Test with `pnpm dev` to verify rendering

### Updating Version Information
Version information is automatically read from `../version_properties/` - no manual updates needed in documentation files.

### Content Validation
- Run `pnpm lint` to check code quality
- Use `pnpm type-check` for TypeScript validation
- Verify all links are working and components render correctly

## Important Notes

### Version Management
- **NEVER** hardcode Minecraft versions in MDX content
- Always use `<MinecraftVersion auto />` instead of specific version arrays
- Use `<VersionedCodeBlock>` for dependency examples
- Version information automatically syncs with parent project

### Link Management
- GitHub repository: `https://github.com/jiazengp/showcase-fabric`
- Modrinth: `https://modrinth.com/mod/showcase`
- CurseForge: `https://www.curseforge.com/minecraft/mc-mods/showcasemod`

### Performance Considerations
- Images should be optimized and use Next.js Image component
- Large code blocks should use syntax highlighting appropriately
- Consider lazy loading for heavy components

### Deployment
- Site builds statically and can be deployed to any static hosting
- Ensure all environment variables are properly configured
- Test build with `pnpm build` before deployment

## Common Tasks

### Adding a New Minecraft Version
1. Add new `.properties` file to `../version_properties/`
2. Documentation will automatically update to include the new version
3. No changes needed in MDX files or components

### Creating New Component Categories
1. Add new component file in `components/`
2. Export from `mdx-components.tsx`
3. Document usage in this file

### Updating External Links
1. Search for old URLs: `grep -r "old-url" content/docs/`
2. Replace systematically across all files
3. Test all updated links work correctly