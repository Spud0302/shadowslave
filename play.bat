@echo off
rem Build the mod from this checkout and launch a Minecraft 1.21.1 client with it
rem loaded. NeoForge, GeckoLib, SmartBrainLib and PlayerAnimationLib come along
rem automatically through Gradle's runClient. Double-click it, or run
rem   play.bat [--branch NAME] [--pull] [--world NAME]
rem See PLAYTEST.md.
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "BRANCH="
set "PULL=0"
set "WORLD="

:parse
if "%~1"=="" goto parsed
if /i "%~1"=="--branch" (
    if "%~2"=="" (echo --branch needs a name& goto usage_fail)
    set "BRANCH=%~2"
    shift
    shift
    goto parse
)
if /i "%~1"=="--pull" (set "PULL=1" & shift & goto parse)
if /i "%~1"=="--world" (
    if "%~2"=="" (echo --world needs a name& goto usage_fail)
    set "WORLD=%~2"
    shift
    shift
    goto parse
)
if /i "%~1"=="--help" goto usage
if /i "%~1"=="-h" goto usage
echo Unknown option: %~1
goto usage_fail

:parsed
rem Gradle needs a JDK (17 or newer) to start; it downloads JDK 21 for the build
rem itself if it is missing.
set "JAVA_CMD=java"
if defined JAVA_HOME set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
"%JAVA_CMD%" -version >nul 2>&1
if errorlevel 1 (
    echo No Java found. Install JDK 21 from https://adoptium.net/temurin/releases/?version=21 and run this again.
    goto fail
)

if defined BRANCH set "PULL=1"
if "%PULL%"=="1" (
    for /f %%s in ('git status --porcelain --untracked-files^=no') do (
        echo You have uncommitted changes, so I won't switch or pull. Commit or stash them first.
        goto fail
    )
    if defined BRANCH (
        git fetch origin "!BRANCH!" || goto fail
        git checkout "!BRANCH!" 2>nul || git checkout -b "!BRANCH!" --track "origin/!BRANCH!" || goto fail
    )
    git pull --ff-only || goto fail
)

for /f %%b in ('git rev-parse --abbrev-ref HEAD') do set "CUR_BRANCH=%%b"
for /f %%c in ('git rev-parse --short HEAD') do set "CUR_COMMIT=%%c"
echo Launching !CUR_BRANCH! @ !CUR_COMMIT!

if defined WORLD (
    call mod\gradlew.bat -p mod runClient "-PquickPlayWorld=!WORLD!"
) else (
    call mod\gradlew.bat -p mod runClient
)
if errorlevel 1 goto fail
exit /b 0

:usage
echo Usage: play.bat [--branch NAME] [--pull] [--world NAME]
echo.
echo   --branch NAME  switch to NAME (fetched from GitHub) before building
echo   --pull         pull the latest commits for the current branch first
echo   --world NAME   skip the menus and load the singleplayer world NAME
echo                  (the world must already exist in mod\run\saves)
exit /b 0

:usage_fail
echo Usage: play.bat [--branch NAME] [--pull] [--world NAME]
:fail
rem Keep the window open when double-clicked so the error stays readable.
echo.
pause
exit /b 1
