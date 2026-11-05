@echo off
echo ==========================================
echo  Pulling AI models into Ollama
echo  This runs ONCE and may take 20-40 mins
echo  depending on your internet speed
echo ==========================================

REM Qwen2.5:32b — best quality for 32GB RAM (quantized ~19GB)
REM Great at: reasoning, math, finance, multilingual (German)
docker exec ollama ollama pull qwen2.5:32b

REM nomic-embed-text — small embedding model for RAG document search
REM Required for Open WebUI RAG to work
docker exec ollama ollama pull nomic-embed-text

echo.
echo ==========================================
echo  Done! Open http://localhost:3000 in your browser
echo  Create your account, then go to:
echo  Settings > Models > set qwen2.5:32b as default
echo ==========================================
pause
