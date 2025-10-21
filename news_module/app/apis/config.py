import os
from typing import Dict, Any
from configparser import ConfigParser

# 获取当前 Python 脚本所在的绝对路径
script_dir = os.path.dirname(os.path.abspath(__file__))

# 组合出配置文件的绝对路径
config_path = os.path.join(script_dir, "config.ini")

config = ConfigParser()
config.read(config_path)
config_dict = {
    'news': {
        "api_key": config.get('news', 'API_KEY'),
    }
}

def get_news_api_key() -> str:
    return config_dict['news']['api_key']