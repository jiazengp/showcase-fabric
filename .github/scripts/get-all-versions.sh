#!/bin/bash

# Script to get all supported Minecraft versions from version_properties directory.
# This branch only accepts the 26.x+ mainline.
# Outputs JSON array of version strings

set -e

VERSIONS_DIR="version_properties"

if [ ! -d "$VERSIONS_DIR" ]; then
    echo "Error: $VERSIONS_DIR directory not found"
    exit 1
fi

# Get all .properties files, extract version numbers, and keep only 26.x+.
VERSIONS=$(ls "$VERSIONS_DIR"/*.properties 2>/dev/null | sed 's|.*/||; s|\.properties$||' | awk -F. '$1+0 >= 26' | sort -V)

if [ -z "$VERSIONS" ]; then
    echo "Error: No version files found in $VERSIONS_DIR"
    exit 1
fi

# Convert to JSON array format
echo -n "["
FIRST=true
for VERSION in $VERSIONS; do
    if [ "$FIRST" = true ]; then
        FIRST=false
    else
        echo -n ","
    fi
    echo -n "\"$VERSION\""
done
echo "]"
