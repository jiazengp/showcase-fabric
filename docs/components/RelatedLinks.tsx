import React from 'react';
import { Icons } from './Icons';

interface RelatedLink {
  title: string;
  href: string;
  description: string;
  icon: React.ReactNode;
}

interface RelatedLinksProps {
  title?: string;
  links: RelatedLink[];
}


export function RelatedLinks({ title = "Related Topics", links }: RelatedLinksProps) {
  if (!links || links.length === 0) return null;

  return (
    <div className="mt-12 rounded-lg border border-gray-200 bg-gray-50/50 p-6 dark:border-gray-700 dark:bg-gray-900/50">
      <h3 className="mb-4 text-lg font-semibold text-gray-900 dark:text-gray-100">
        {title}
      </h3>
      <div className="grid gap-3">
        {links.map((link, index) => (
          <a
            key={index}
            href={link.href}
            className="group flex items-start gap-3 rounded-md p-3 transition-colors hover:bg-gray-100 dark:hover:bg-gray-800"
          >
            <div className="flex-shrink-0 text-gray-500 transition-colors group-hover:text-blue-600 dark:text-gray-400 dark:group-hover:text-blue-400">
              {link.icon}
            </div>
            <div className="min-w-0 flex-1">
              <div className="font-medium text-gray-900 group-hover:text-blue-600 dark:text-gray-100 dark:group-hover:text-blue-400">
                {link.title}
              </div>
              <div className="text-sm text-gray-600 dark:text-gray-400">
                {link.description}
              </div>
            </div>
          </a>
        ))}
      </div>
    </div>
  );
}

// Preset link collections for common scenarios
export const createRelatedLinks = {
  commands: (currentPage?: string) => {
    const allLinks = [
      {
        title: "Player Commands",
        href: "/commands/player-commands",
        description: "Standard player command reference",
        icon: <Icons.Command />,
      },
      {
        title: "Admin Commands",
        href: "/commands/admin-commands",
        description: "Administrative commands for server management",
        icon: <Icons.Shield />,
      },
    ];
    return allLinks.filter(link => !currentPage || !link.href.includes(currentPage));
  },

  configuration: (currentPage?: string) => {
    const allLinks = [
      {
        title: "Configuration File",
        href: "/configuration/config-file",
        description: "Complete reference for config.yml settings",
        icon: <Icons.Settings />,
      },
      {
        title: "Permissions",
        href: "/configuration/permissions",
        description: "Permission system configuration with LuckPerms",
        icon: <Icons.Shield />,
      },
    ];
    return allLinks.filter(link => !currentPage || !link.href.includes(currentPage));
  },

  developers: (currentPage?: string) => {
    const allLinks = [
      {
        title: "API Overview",
        href: "/developers/api-overview",
        description: "Complete developer guide and API reference",
        icon: <Icons.Code />,
      },
      {
        title: "Java API",
        href: "/developers/java-api",
        description: "Direct API integration for Fabric mods",
        icon: <Icons.Code />,
      },
      {
        title: "Events",
        href: "/developers/events",
        description: "Event system for custom integrations",
        icon: <Icons.Activity />,
      },
      {
        title: "PlaceholderAPI",
        href: "/developers/placeholderapi",
        description: "Placeholder integration with other mods",
        icon: <Icons.Layers />,
      },
      {
        title: "Data API",
        href: "/developers/data-api",
        description: "Data storage and retrieval systems",
        icon: <Icons.Database />,
      },
    ];
    return allLinks.filter(link => !currentPage || !link.href.includes(currentPage));
  },

  support: (currentPage?: string) => {
    const allLinks = [
      {
        title: "FAQ",
        href: "/support/faq",
        description: "Frequently asked questions and answers",
        icon: <Icons.HelpCircle />,
      },
      {
        title: "Troubleshooting",
        href: "/support/troubleshooting",
        description: "Common issues and solutions",
        icon: <Icons.Activity />,
      },
    ];
    return allLinks.filter(link => !currentPage || !link.href.includes(currentPage));
  },

  general: () => [
    {
      title: "Getting Started",
      href: "/getting-started/installation",
      description: "Installation and initial setup guide",
      icon: <Icons.BookOpen />,
    },
    {
      title: "Configuration",
      href: "/configuration/config-file",
      description: "Configure mod behavior and settings",
      icon: <Icons.Settings />,
    },
    {
      title: "Commands",
      href: "/commands/player-commands",
      description: "Command reference and usage",
      icon: <Icons.Command />,
    },
    {
      title: "Troubleshooting",
      href: "/support/troubleshooting",
      description: "Get help with common issues",
      icon: <Icons.HelpCircle />,
    },
  ],
};