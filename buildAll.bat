@echo off
setlocal enabledelayedexpansion
REM Multi-version build script for Showcase mod
REM Inspired by Fuji's buildAll script

echo Starting multi-version build for Showcase mod...
echo Build started at: %DATE% %TIME%

REM Create build output directory
if not exist "build\buildAllJars" mkdir build\buildAllJars

REM Initialize counters
set total_versions=0
set successful_builds=0
set failed_builds=0

REM Verify version properties files exist
echo Verifying version properties files...
if not exist "version_properties\*.properties" (
    echo ERROR: No version properties files found in version_properties\ directory
    exit /b 1
)

REM Count total versions
for %%f in (version_properties\*.properties) do (
    set /a total_versions+=1
)
echo Found %total_versions% Minecraft versions to build

REM Build for all available versions
echo.
echo Starting builds...
for %%f in (version_properties\*.properties) do (
    set "version=%%~nf"
    call :build_version !version!
    if !ERRORLEVEL! EQU 0 (
        set /a successful_builds+=1
    ) else (
        set /a failed_builds+=1
        echo × Failed to build for Minecraft !version!
    )
)

REM Final summary
echo.
echo ========================================
echo Multi-version build summary:
echo ========================================
echo Total versions: %total_versions%
echo Successful builds: %successful_builds%
echo Failed builds: %failed_builds%
echo Build completed at: %DATE% %TIME%

if %failed_builds% GTR 0 (
    echo.
    echo ⚠️ Some builds failed. Check the output above for details.
    exit /b 1
)

echo.
echo ✓ All builds completed successfully!
echo JAR files are available in build/buildAllJars directory:
dir build\buildAllJars\*.jar /B

REM Verify all expected JARs exist
echo.
echo Verifying build artifacts...
set missing_jars=0
for %%f in (version_properties\*.properties) do (
    set "version=%%~nf"
    if not exist "build\buildAllJars\showcase-*+!version!.jar" (
        echo WARNING: Missing JAR for Minecraft !version!
        set /a missing_jars+=1
    )
)

if %missing_jars% GTR 0 (
    echo ⚠️ %missing_jars% JAR files are missing
    exit /b 1
) else (
    echo ✓ All expected JAR files are present
)

goto :eof

:build_version
set "mcver=%~1"
set /a build_num=%successful_builds% + %failed_builds% + 1
echo.
echo ========================================
echo Building for Minecraft %mcver% (%build_num%/%total_versions%)
echo ========================================

REM Verify properties file exists
if not exist "version_properties\%mcver%.properties" (
    echo ERROR: Properties file not found: version_properties\%mcver%.properties
    exit /b 1
)

REM Skip clean to preserve previous version JARs
echo Building without cleaning (preserving previous versions)...

REM Build for specific version (inspired by Fuji)
echo Building mod for Minecraft %mcver%...
call gradlew build -PmcVer=%mcver%
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed for Minecraft %mcver%
    exit /b 1
)

REM Verify and copy JAR files
set jar_found=0
for %%j in (build\libs\showcase-*+%mcver%.jar) do (
    if exist "%%j" (
        set jar_found=1
        copy "%%j" "build\buildAllJars\%%~nxj" >nul
        echo ✓ Copied %%~nxj to build/buildAllJars
    )
)

if %jar_found% EQU 0 (
    echo ERROR: No JAR file found for Minecraft %mcver% in build\libs\
    exit /b 1
)

echo ✓ Build completed for Minecraft %mcver%
exit /b 0