@echo off
rem ==============================================================================
rem AlgoAdda Local Development Launcher
rem
rem Assumptions:
rem  - Maven dependencies resolved in core-api
rem  - Python virtual environment created at backtest-service\venv
rem  - npm dependencies installed in web
rem  - PostgreSQL running on port 5432 if using production mode (default uses local H2 profile)
rem ==============================================================================

echo Starting AlgoAdda...
echo.

echo Launching Core API Backend (Spring Boot)...
start "AlgoAdda - Core API" cmd /k "cd /d "%~dp0core-api" && .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local,seed-demo""

echo Launching Backtest Microservice (Python FastAPI)...
start "AlgoAdda - Backtest Service" cmd /k "cd /d "%~dp0backtest-service" && .\venv\Scripts\python.exe -m uvicorn main:app --port 8000"

echo Launching Web Frontend (React + Vite)...
start "AlgoAdda - Web Frontend" cmd /k "cd /d "%~dp0web" && npm run dev"

echo.
echo AlgoAdda services initialized!
echo Note: Closing any individual window will stop that service.
