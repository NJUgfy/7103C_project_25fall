from flask import current_app
from ..apis import okx_api
import pandas as pd
import re

def get_market() -> (bool, pd.DataFrame, str):
    try:
        results = None
        api = okx_api.OkxApi()
        return True, api.get_market_info("BTC-USDT").to_json(), "success"
    except Exception as e:
        return False, None, f"查询失败: {str(e)}"

def get_klines(platform, symbol, market_type, interval, start_time, end_time, limit):
    try:
        results = None
        api = None
        if platform == "OKX":
            api = okx_api.OkxApi()
        return True, api.fetch_klines(symbol, market_type, interval, start_time, end_time, limit), "successs"
    except Exception as e:
        return False, None, f"查询失败: {str(e)}"


def get_ticker(platform, symbol):
    try:
        results = None
        api = None
        if platform == "OKX":
            api = okx_api.OkxApi()
        return True, api.get_ticker(symbol), "successs"
    except Exception as e:
        return False, None, f"查询失败: {str(e)}"


def get_depth(platform, symbol):
    try:
        results = None
        api = None
        if platform == "OKX":
            api = okx_api.OkxApi()
        return True, api.get_books(symbol), "success"
    except Exception as e:
        return False, None, f"查询失败: {str(e)}"