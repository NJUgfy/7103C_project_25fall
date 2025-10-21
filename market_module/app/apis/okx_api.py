from okx.api import Account
from okx.app import OkxSPOT
from okx.app.utils import eprint
import pandas as pd
import numpy as np
import time
from . import config
import datetime
import json

class OkxApi:
    def __init__(self):

        self.key = config.get_okx_api_key()
        self.secret = config.get_okx_api_secret()
        self.passphrase = config.get_okx_api_pass()
        self.proxies = {}
        self.proxy_host = None
        self.flag = '0'
        self.timeframe = "1m"

        self.account = Account(
            key=self.key, secret=self.secret, passphrase=self.passphrase, flag=self.flag, proxies=self.proxies, proxy_host=self.proxy_host,
        )

        okx_spot = OkxSPOT(
            key=self.key, secret=self.secret, passphrase=self.passphrase, proxies=self.proxies, proxy_host=self.proxy_host,
        )
        self.spot_market = okx_spot.market
    
    def get_books(self, symbol):
        if symbol == "BTC_USDT":
            symbol = "BTC-USDT"
        
        market = self.spot_market
        
        get_books_result = market.get_books(instId=symbol)

        result = {}
        result["bids"] = []
        for item in get_books_result["data"]["bids"]:
            result["bids"].append([float(item[0]), float(item[1])])
        result["asks"] = []
        for item in get_books_result["data"]["asks"]:
            result["asks"].append([float(item[0]), float(item[1])])
        
        return result
    
    def get_ticker(self, symbol):
        if symbol == "BTC_USDT":
            symbol = "BTC-USDT"
        
        market = self.spot_market
        
        old_d = market.get_ticker(instId=symbol)["data"]
        new_d = {}

        new_d["symbol"] = symbol
        new_d["last_price"] = float(old_d["last"])
        new_d["high24h"] = float(old_d["high24h"])
        new_d["low24h"] = float(old_d["low24h"])
        new_d["volume24h"] = float(old_d["vol24h"])
        new_d["change_percent"] = (float(old_d["last"]) - float(old_d["open24h"])) / float(old_d["open24h"])
        return new_d


    def fetch_klines(self, symbol, market_type, interval, start_time, end_time, limit = 1000):
        if symbol == "BTC_USDT":
            symbol = "BTC-USDT"

        start_time_str = datetime.datetime.fromtimestamp(start_time).strftime("%Y-%m-%d %H:%M:%S")
        end_time_str = datetime.datetime.fromtimestamp(end_time).strftime("%Y-%m-%d %H:%M:%S")

        if market_type == "SPOT":
            market = self.spot_market
        else:
            market = None

        candle_result = market.get_history_candle(
            instId = symbol,
            start = start_time_str,
            end = end_time_str,
            bar= interval
        )
        candle = candle_result['data']
        df = market.candle_to_df(candle)
        kline_list = json.loads(df.to_json(orient='records', force_ascii=False))
        kline_list = kline_list[-1000:]

        result = []
        for d in kline_list:
            new_d = {}
            if "ts" in d:
                new_d["start_time"] = d["ts"]
            if "o" in d:
                new_d["open_price"] = d["o"]
            if "h" in d:
                new_d["high_price"] = d["h"]
            if "l" in d:
                new_d["low_price"] = d["l"]
            if "c" in d:
                new_d["close_price"] = d["c"]
            if "vol" in d:
                new_d["volume"] = d["vol"]
            if "volCcy" in d:
                new_d["volume_currency"] = d["volCcy"]
            result.append(new_d)

        return result

if __name__ == "__main__":
    bot = OkxApi()
    # print(bot.fetch_klines("BTC-USDT", "SPOT", "1m", 1760800000, 1760800180))
    # print(bot.get_ticker("BTC-USDT"))
    print(bot.get_books("BTC-USDT"))
