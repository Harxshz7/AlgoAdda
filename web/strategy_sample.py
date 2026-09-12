"""
Nifty 50 Volatility Breakout Quantitative Strategy
AlgoAdda White-Box Compliance Standard
"""
import pandas as pd
import numpy as np

def generate_signals(df: pd.DataFrame, fast_period: int = 20, slow_period: int = 50) -> pd.Series:
    close = df['Close']
    fast_ma = close.rolling(fast_period).mean()
    slow_ma = close.rolling(slow_period).mean()
    signals = (fast_ma > slow_ma).astype(int)
    return signals
