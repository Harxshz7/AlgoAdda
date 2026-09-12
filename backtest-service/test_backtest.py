from fastapi.testclient import TestClient
from main import app
from datetime import datetime, timezone, timedelta

client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert data["service"] == "backtest-service"


def test_backtest_successful_run():
    end_date = datetime.now(timezone.utc)
    start_date = end_date - timedelta(days=90)

    payload = {
        "strategy_config": {
            "symbol": "^NSEI",
            "timeframe": "1d",
            "strategy_name": "SMA_CROSSOVER",
            "parameters": {
                "fast_period": 10,
                "slow_period": 25
            }
        },
        "date_range_start": start_date.isoformat(),
        "date_range_end": end_date.isoformat()
    }

    response = client.post("/backtest", json=payload)
    assert response.status_code == 200
    data = response.json()

    assert data["status"] == "COMPLETED"
    assert "metrics" in data
    metrics = data["metrics"]
    assert "win_rate" in metrics
    assert "profit_factor" in metrics
    assert "max_drawdown" in metrics
    assert "sharpe_ratio" in metrics
    assert "total_trades" in metrics
    assert "total_return_pct" in metrics
    assert "annualized_return_pct" in metrics
    assert len(data["equity_curve"]) > 0
    assert "report_summary" in data


def test_backtest_invalid_date_range():
    end_date = datetime.now(timezone.utc)
    start_date = end_date + timedelta(days=10)  # start after end

    payload = {
        "strategy_config": {
            "symbol": "^NSEI",
            "timeframe": "1d",
            "strategy_name": "SMA_CROSSOVER"
        },
        "date_range_start": start_date.isoformat(),
        "date_range_end": end_date.isoformat()
    }

    response = client.post("/backtest", json=payload)
    assert response.status_code in [400, 422]


def test_backtest_missing_required_fields():
    payload = {
        "strategy_config": {
            "symbol": "^NSEI"
        }
        # missing dates
    }

    response = client.post("/backtest", json=payload)
    assert response.status_code == 422
