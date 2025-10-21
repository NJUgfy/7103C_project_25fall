import os

class Config:
    # 基础配置
    SECRET_KEY = os.getenv('SECRET_KEY', 'fallback_secret_key')
    
    # 数据库配置（通过环境变量获取）
    SQLALCHEMY_DATABASE_URI = (
        f"mysql+pymysql://{os.getenv('DB_USER')}:{os.getenv('DB_PASS')}"
        f"@{os.getenv('DB_HOST')}/{os.getenv('DB_NAME')}?charset=utf8mb4"
    )
    SQLALCHEMY_ENGINE_OPTIONS = {
        'pool_size': 10,
        'pool_recycle': 300,
        'pool_pre_ping': True,
        'connect_args': {
            "init_command": "SET time_zone='+08:00'"
        }
    }
    
    # JWT配置
    JWT_SECRET_KEY = os.getenv('JWT_SECRET_KEY')
    JWT_ACCESS_TOKEN_EXPIRES = int(os.getenv('JWT_EXPIRES', 3600))
    
    # 生产环境专用配置
    PROPAGATE_EXCEPTIONS = True  # 允许错误冒泡到Nginx日志