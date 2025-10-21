import pandas as pd
import numpy as np
import time
from . import config
import datetime
import json
from newsapi import NewsApiClient

class NewsApi:
    def __init__(self):
        self.key = config.get_news_api_key()
        self.api = NewsApiClient(api_key=self.key)
    
    def get_everything(self, keyword, from_param, to_param, limits, sources, region):
        all_articles = self.api.get_everything(q=keyword,
                                      sources=sources,
                                      from_param=from_param,
                                      to=to_param,
                                      language=region,
                                      sort_by='relevancy',
                                      page_size=limits,
                                      page=1)
        # print(all_articles)
        results = []
        for item in all_articles["articles"]:
            result = {}
            result["title"] = item["title"]
            result["source"] = item["source"]["name"]
            result["published_at"] = item["publishedAt"]
            result["url"] = item["url"]
            result["summary"] = item["description"]
            results.append(result)
        return results

if __name__ == "__main__":
    bot = NewsApi()
    print(bot.get_everything("bitcoin", "2025-10-19", "2025-10-20", 10, "", "en"))
