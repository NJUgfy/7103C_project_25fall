from flask import current_app
import pandas as pd
import re
from ..apis import news_api
from datetime import datetime, timedelta

def search(keyword, from_param, to_param, limits, sources, region):
    try:
        if limits > 100:
            limits = 100
        if limits < 0:
            limits = 1
        if region == "" or region == None or region == "global":
            region = None
        if sources == "" or source == None:
            sources = ""
        if from_param == "" or from_param == None or to_param == "" or to_param == None:
            current_date = datetime.now()
            thirty_days_ago = current_date - timedelta(days=30)
            to_param = current_date.strftime("%Y-%m-%d")
            from_param = thirty_days_ago.strftime("%Y-%m-%d")
        results = None
        api = news_api.NewsApi()
        return True, api.get_everything(keyword, from_param, to_param, limits, sources, region), "success"
    except Exception as e:
        return False, None, f"查询失败: {str(e)}"