#!/bin/bash
# Multi-version build script for Showcase mod
# Inspired by Fuji's buildAll script

set -e  # Exit on any error

echo "Starting multi-version build for Showcase mod..."
echo "Build started at: $(date)"

# Create build output directory
mkdir -p build/buildAllJars

# Initialize counters
total_versions=0
successful_builds=0
failed_builds=0

# Verify version properties files exist
echo "Verifying version properties files..."
if [ ! -d "version_properties" ] || [ -z "$(ls -A version_properties/*.properties 2>/dev/null)" ]; then
    echo "ERROR: No version properties files found in version_properties/ directory"
    exit 1
fi

# Count total versions
total_versions=$(ls version_properties/*.properties | wc -l)
echo "Found $total_versions Minecraft versions to build"

# Function to build for a specific version
build_version() {
    local mcver=$1
    local start_time=$(date +%s)
    
    echo ""
    echo "========================================"
    echo "Building for Minecraft $mcver ($((successful_builds + failed_builds + 1))/$total_versions)"
    echo "========================================"
    
    # Verify properties file exists
    if [ ! -f "version_properties/$mcver.properties" ]; then
        echo "ERROR: Properties file not found: version_properties/$mcver.properties"
        return 1
    fi
    
    # Skip clean to preserve previous version JARs
    echo "Building without cleaning (preserving previous versions)..."
    
    # Build for specific version
    echo "Building mod for Minecraft $mcver..."
    ./gradlew build -PmcVer=$mcver
    if [ $? != 0 ]; then
        echo "ERROR: Build failed for Minecraft $mcver"
        return 1
    fi
    
    # Verify build artifacts exist
    local jar_found=false
    for jar in build/libs/showcase-*+$mcver.jar; do
        if [ -f "$jar" ]; then
            jar_found=true
            local jar_size=$(stat -f%z "$jar" 2>/dev/null || stat -c%s "$jar" 2>/dev/null || echo "unknown")
            cp "$jar" "build/buildAllJars/$(basename "$jar")"
            echo "✓ Copied $(basename "$jar") to build/buildAllJars (size: $jar_size bytes)"
        fi
    done
    
    if [ "$jar_found" = false ]; then
        echo "ERROR: No JAR file found for Minecraft $mcver in build/libs/"
        return 1
    fi
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    echo "✓ Build completed for Minecraft $mcver in ${duration}s"
    
    return 0
}

# Build for all available versions
echo ""
echo "Starting builds..."
for props_file in version_properties/*.properties; do
    if [ -f "$props_file" ]; then
        version=$(basename "$props_file" .properties)
        if build_version "$version"; then
            ((successful_builds++))
        else
            ((failed_builds++))
            echo "✗ Failed to build for Minecraft $version"
        fi
    fi
done

# Final summary
echo ""
echo "========================================"
echo "Multi-version build summary:"
echo "========================================"
echo "Total versions: $total_versions"
echo "Successful builds: $successful_builds"
echo "Failed builds: $failed_builds"
echo "Build completed at: $(date)"

if [ $failed_builds -gt 0 ]; then
    echo ""
    echo "⚠️  Some builds failed. Check the output above for details."
    exit 1
fi

echo ""
echo "✓ All builds completed successfully!"
echo "JAR files are available in build/buildAllJars directory:"
ls -la build/buildAllJars/*.jar 2>/dev/null || echo "No JAR files found"

# Verify all expected JARs exist
echo ""
echo "Verifying build artifacts..."
echo "Available JAR files:"
ls -la build/buildAllJars/*.jar 2>/dev/null || echo "No JAR files found"

missing_jars=0
for props_file in version_properties/*.properties; do
    version=$(basename "$props_file" .properties)
    echo "Checking for Minecraft $version JAR..."
    if ! ls build/buildAllJars/showcase-*+$version.jar >/dev/null 2>&1; then
        echo "WARNING: Missing JAR for Minecraft $version"
        ((missing_jars++))
    else
        echo "✓ Found JAR for Minecraft $version"
    fi
done

if [ $missing_jars -gt 0 ]; then
    echo "⚠️  $missing_jars JAR files are missing"
    exit 1
else
    echo "✓ All expected JAR files are present"
fi