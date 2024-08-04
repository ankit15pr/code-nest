@echo off

REM Start Backend
cd C:\Users\parma\Desktop\code-nest-main\code-nest-main\backend
start mvnw spring-boot:run

REM Wait for backend to fully start (adjust timeout as needed)
timeout /t 10

REM Start Frontend using Live Server
cd C:\Users\parma\Desktop\code-nest-main\code-nest-main\frontend
start live-server --port=5500
