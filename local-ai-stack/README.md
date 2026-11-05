# Local AI Stack — Personal Life OS

A fully local, private AI setup running on your Windows 11 machine.
No data ever leaves your computer.

## Stack

| Service | URL | Purpose |
|---------|-----|---------|
| Open WebUI | http://localhost:3000 | Main chat interface |
| Ollama | http://localhost:11434 | Model runtime (API) |
| ChromaDB | http://localhost:8000 | Vector DB for document RAG |
| n8n | http://localhost:5678 | Automation & scheduled tasks |

**Model:** `qwen2.5:32b` (quantized ~19GB, fits in 32GB RAM)  
Strong at: reasoning, math/finance, German language, document analysis.

---

## First-Time Setup

### Prerequisites
- [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop/)
- In Docker Desktop → Settings → General → **Enable "Start Docker Desktop when you log in"**
- At least 25GB free disk space for the model

### Steps

1. **Start the stack**
   ```
   double-click start.bat
   ```
   Or from terminal:
   ```
   docker compose up -d
   ```

2. **Pull the AI model** (one time only, ~19GB download)
   ```
   double-click pull-models.bat
   ```
   This takes 20–40 minutes. Go make a coffee.

3. **Open the UI**
   - Go to http://localhost:3000
   - Create your account (first account = admin)
   - After creating your account, set `ENABLE_SIGNUP=false` in docker-compose.yml and restart

4. **Configure the model in Open WebUI**
   - Settings → Models → set `qwen2.5:32b` as default
   - Settings → RAG → set embedding model to `nomic-embed-text`

5. **Auto-start on Windows boot**
   - Docker Desktop starts automatically (set in step 1)
   - All containers have `restart: always` — they start with Docker automatically

---

## Your Use Cases — How to Use Each

### Work / Performance Reviews
- In Open WebUI, create a **Collection** called "Work Notes"
- Upload your meeting notes, task logs as `.txt` or `.md` files
- Ask: *"Draft my Q3 performance review based on my work notes"*

### Finance Tracking
- Keep a `finances.md` file in the `my-documents/` folder
- Format: monthly rent, expenses, stock portfolio, savings
- Upload to Open WebUI collection "Finances"
- Ask: *"What is my projected savings at end of December based on my current spending?"*

### German Documents & Contracts
- Scan/export contracts as PDF into `my-documents/contracts/`
- Upload to collection "Contracts"
- Ask: *"Which of my contracts expire in the next 3 months?"*
- For tax: upload your Lohnsteuerbescheinigung, upload receipts summary

### Language Learning
- Create a collection "Language Learning"
- Paste vocabulary lists, upload grammar notes
- Ask the model to quiz you, explain grammar, generate exercises

### Health Tracking
- Keep `health.md` with your goals, current weight, activities
- Ask: *"Create a 4-week diet plan for my goal of losing 5kg"*
- Ask: *"What blood tests should I get for a general health checkup in Germany?"*

### Shopping Recommendations
- Just ask directly: *"Compare the best value mechanical keyboards under 100 EUR in 2024"*
- No document needed — the model's training covers this

### Restaurant Recommendations
- Tell the model your city/district once in your system prompt
- Ask: *"Recommend Italian restaurants near [your area]"*

---

## Auto-scheduled Tasks with n8n

Go to http://localhost:5678 (login: admin / your password)

Example automations to set up:
- **Weekly finance summary** — every Sunday, read `finances.md`, ask Ollama for summary, send to your email
- **Document expiry check** — monthly, scan contracts folder, flag anything expiring in 60 days
- **Weekly schedule prep** — every Monday morning, summarize your week's appointments

n8n has an Ollama node built in — point it to `http://ollama:11434`.

---

## Document Folder Structure (my-documents/)

```
my-documents/
├── work/
│   ├── meeting-notes/
│   └── achievements/        ← log what you do weekly here
├── finances/
│   ├── monthly-expenses.md
│   ├── stocks.md
│   └── savings.md
├── contracts/
│   ├── rent-contract.pdf
│   ├── internet-contract.pdf
│   └── insurance/
├── health/
│   ├── goals.md
│   └── tracking.md
├── documents/
│   ├── passport.md          ← expiry date, renewal info
│   ├── visa.md
│   └── tax-2024/
└── language-learning/
    └── german-vocab.md      ← or whatever language you're learning
```

---

## Tips

- **System Prompt**: In Open WebUI → Settings → set a system prompt like:
  > "You are my personal assistant. I live in Germany, work at SAP, and my timezone is Europe/Berlin. Always be concise and practical."

- **Collections**: Group related documents into named collections so RAG is scoped — don't put everything in one collection or answers get noisy.

- **Model speed**: On CPU with 32GB RAM, `qwen2.5:32b` takes ~30-60 seconds per response. For quick questions, switch to `qwen2.5:14b` (faster, still very good).

- **Backup**: The named Docker volumes store all your data. Back them up periodically:
  ```
  docker run --rm -v ollama_data:/data -v %cd%:/backup alpine tar czf /backup/ollama-backup.tar.gz /data
  ```

---

## Updating

```bash
docker compose pull        # pull latest images
docker compose up -d       # restart with new images
```
