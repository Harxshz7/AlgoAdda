# AlgoAdda - Backtest Service (`backtest-service`)

Python microservice using FastAPI and `vectorbt` for automated algorithmic trading strategy backtesting, generating transparent performance metrics (Sharpe ratio, drawdown, win rate, equity curve).

## Tech Stack
- **Python 3.12+**
- **FastAPI**
- **Uvicorn**
- **vectorbt, pandas, numpy**

---

## Setup & Virtual Environment

### 1. Create and Activate Virtual Environment

**On Linux/macOS:**
```bash
cd backtest-service
python3 -m venv venv
source venv/bin/activate
```

**On Windows (PowerShell):**
```powershell
cd backtest-service
python -m venv venv
.\venv\Scripts\Activate.ps1
```

**On Windows (Command Prompt):**
```cmd
cd backtest-service
python -m venv venv
venv\Scripts\activate.bat
```

### 2. Install Dependencies
```bash
pip install -r requirements.txt
```

---

## Running the Service

Start the FastAPI development server:

```bash
uvicorn main:app --reload --port 8000
```

The service will be accessible at:
- **Base URL:** `http://localhost:8000`
- **Interactive Swagger Docs:** `http://localhost:8000/docs`

---

## Health Check Endpoint

To verify the service is running and responding:

```bash
curl http://localhost:8000/health
```

Expected Response:
```json
{"status": "ok"}
```
