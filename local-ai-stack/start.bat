@echo off
echo Starting Local AI Stack...
docker compose up -d
echo.
echo Services starting:
echo   Open WebUI  -^> http://localhost:3000
echo   Ollama API  -^> http://localhost:11434
echo   ChromaDB    -^> http://localhost:8000
echo   n8n         -^> http://localhost:5678
echo.
echo Run 'docker compose logs -f' to watch logs
pause
