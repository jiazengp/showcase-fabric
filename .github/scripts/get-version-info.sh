#!/bin/bash

# Script to extract version information from version_properties files
# Usage: ./get-version-info.sh [version] [property]
# Example: ./get-version-info.sh 1.21.4 compatible_minecraft_versions

set -e

VERSION=$1
PROPERTY=$2

if [ -z "$VERSION" ] || [ -z "$PROPERTY" ]; then
    echo "Usage: $0 <version> <property>"
    echo "Available properties:"
    echo "  - minecraft_version"
    echo "  - compatible_minecraft_versions"  
    echo "  - release_type"
    echo "  - fabric_loader_version"
    echo "  - fabric_version"
    exit 1
fi

VERSION_FILE="version_properties/${VERSION}.properties"

if [ ! -f "$VERSION_FILE" ]; then
    echo "Error: Version file $VERSION_FILE not found"
    exit 1
fi

# Extract the property value
VALUE=$(grep "^${PROPERTY}=" "$VERSION_FILE" | cut -d'=' -f2-)

if [ -z "$VALUE" ]; then
    echo "Error: Property $PROPERTY not found in $VERSION_FILE"
    exit 1
fi

echo "$VALUE"