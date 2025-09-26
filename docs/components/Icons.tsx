import React from 'react';

// Icon components using SVG for better compatibility
export const Icons = {
  Command: () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M8 19V16H16V19M8 5V8H16V5M18 3H6C5.45 3 5 3.45 5 4V7C5 7.55 5.45 8 6 8H8V16H6C5.45 16 5 16.45 5 17V20C5 20.55 5.45 21 6 21H18C18.55 21 19 20.55 19 20V17C19 16.45 18.55 16 18 16H16V8H18C18.55 8 19 7.55 19 7V4C19 3.45 18.55 3 18 3Z"
        stroke="currentColor"
        strokeWidth="1.5"
        fill="none"
      />
    </svg>
  ),
  Settings: () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z"
        stroke="currentColor"
        strokeWidth="1.5"
      />
      <path
        d="M19.4 15A1.65 1.65 0 0 0 18.33 17.3L19.9 19A1.65 1.65 0 0 1 17.3 19.9L15.6 18.33A1.65 1.65 0 0 0 13.23 19.4L13 21.74A1.65 1.65 0 0 1 10.26 21.74L10 19.4A1.65 1.65 0 0 0 7.7 18.33L6.1 19.9A1.65 1.65 0 0 1 4.1 17.3L5.67 15.6A1.65 1.65 0 0 0 4.6 13.23L2.26 13A1.65 1.65 0 0 1 2.26 10.26L4.6 10A1.65 1.65 0 0 0 5.67 7.7L4.1 6.1A1.65 1.65 0 0 1 6.1 4.1L7.7 5.67A1.65 1.65 0 0 0 10 4.6L10.26 2.26A1.65 1.65 0 0 1 13 2.26L13.23 4.6A1.65 1.65 0 0 0 15.6 5.67L17.3 4.1A1.65 1.65 0 0 1 19.9 6.1L18.33 7.7A1.65 1.65 0 0 0 19.4 10L21.74 10.26A1.65 1.65 0 0 1 21.74 13L19.4 13.23V15Z"
        stroke="currentColor"
        strokeWidth="1.5"
      />
    </svg>
  ),
  Shield: () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M12 22S2 17 2 12L12 2L22 12C22 17 12 22 12 22Z"
        stroke="currentColor"
        strokeWidth="1.5"
        fill="none"
      />
    </svg>
  ),
  Code: () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M16 18L22 12L16 6M8 6L2 12L8 18"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  ),
  Database: () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <ellipse cx="12" cy="5" rx="9" ry="3" stroke="currentColor" strokeWidth="1.5" fill="none"/>
      <path d="M3 5V19C3 20.66 7.03 22 12 22S21 20.66 21 19V5" stroke="currentColor" strokeWidth="1.5" fill="none"/>
      <path d="M3 12C3 13.66 7.03 15 12 15S21 13.66 21 12" stroke="currentColor" strokeWidth="1.5" fill="none"/>
    </svg>
  ),
  BookOpen: () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path
        d="M2 3H8C9.06087 3 10.0783 3.42143 10.8284 4.17157C11.5786 4.92172 12 5.93913 12 7V21C12 20.2044 11.6839 19.4413 11.1213 18.8787C10.5587 18.3161 9.79565 18 9 18H2V3Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
      <path
        d="M22 3H16C14.9391 3 13.9217 3.42143 13.1716 4.17157C12.4214 4.92172 12 5.93913 12 7V21C12 20.2044 12.3161 19.4413 12.8787 18.8787C13.4413 18.3161 14.2044 18 15 18H22V3Z"
        stroke="currentColor"
        strokeWidth="1.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  ),
  HelpCircle: () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="1.5" fill="none"/>
      <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
      <path d="M12 17H12.01" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  ),
  Activity: () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <polyline points="22,12 18,12 15,21 9,3 6,12 2,12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none"/>
    </svg>
  ),
  Layers: () => (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <polygon points="12,2 2,7 12,12 22,7 12,2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none"/>
      <polyline points="2,17 12,22 22,17" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none"/>
      <polyline points="2,12 12,17 22,12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" fill="none"/>
    </svg>
  ),
};

// Create individual icon components for MDX usage
export const Command = Icons.Command;
export const Settings = Icons.Settings;
export const Shield = Icons.Shield;
export const Code = Icons.Code;
export const Database = Icons.Database;
export const BookOpen = Icons.BookOpen;
export const HelpCircle = Icons.HelpCircle;
export const Activity = Icons.Activity;
export const Layers = Icons.Layers;