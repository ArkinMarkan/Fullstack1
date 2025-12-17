@echo off
REM Maven Dependencies Fix Script for Movie Booking Application
REM This script resolves common Maven dependency and cache issues

echo 🔧 Fixing Maven Dependencies for Movie Booking Application
echo.

REM Navigate to backend directory
if exist "backend" (
    cd backend
    echo 📂 Moved to backend directory
) else (
    echo ❌ Backend directory not found! Make sure you're in the project root.
    pause
    exit /b 1
)

echo.
echo 1️⃣ Cleaning Maven cache and local repository...

REM Clean the project
echo 🧹 Cleaning Maven project...
mvn clean

REM Remove corrupted dependencies from local repository
echo 🗑️ Removing potentially corrupted dependencies...
if exist "%USERPROFILE%\.m2\repository\com\mysql" (
    rmdir /s /q "%USERPROFILE%\.m2\repository\com\mysql"
    echo ✅ Removed MySQL connector cache
)

if exist "%USERPROFILE%\.m2\repository\io\jsonwebtoken" (
    rmdir /s /q "%USERPROFILE%\.m2\repository\io\jsonwebtoken"
    echo ✅ Removed JWT cache
)

if exist "%USERPROFILE%\.m2\repository\org\springframework" (
    echo ⚠️ Spring cache is large, skipping automatic removal
    echo    You can manually clear it if needed: %USERPROFILE%\.m2\repository\org\springframework
)

echo.
echo 2️⃣ Refreshing dependencies...

REM Force refresh dependencies
echo 📦 Downloading fresh dependencies...
mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false
mvn dependency:resolve -U

echo.
echo 3️⃣ Validating project structure...

REM Validate dependencies
echo 🔍 Validating dependencies...
mvn dependency:resolve-sources
mvn dependency:tree

echo.
echo 4️⃣ Compiling project...

REM Compile the project
echo ⚡ Compiling project...
mvn compile

if %errorlevel% == 0 (
    echo.
    echo ✅ SUCCESS: Project compiled successfully!
    echo.
    echo 5️⃣ Running tests...
    mvn test -DskipTests=false
    
    if %errorlevel__ == 0 (
        echo ✅ Tests passed successfully!
    ) else (
        echo ⚠️ Some tests failed, but compilation is working
    )
    
    echo.
    echo 🎉 Maven dependencies fixed successfully!
    echo.
    echo Next steps:
    echo 1. Import the project into your IDE
    echo 2. Refresh/reload the project
    echo 3. Run: mvn spring-boot:run
    
) else (
    echo.
    echo ❌ FAILED: Compilation still failing
    echo.
    echo Additional troubleshooting steps:
    echo 1. Check Java version: java -version
    echo 2. Check Maven version: mvn -version  
    echo 3. Clear entire .m2 cache: rmdir /s /q "%USERPROFILE%\.m2\repository"
    echo 4. Re-import project in IDE
    echo.
)

echo.
pause
