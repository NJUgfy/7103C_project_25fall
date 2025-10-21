from flask import Flask
from flask_jwt_extended import JWTManager
from dotenv import load_dotenv
import logging

load_dotenv()  # 加载环境变量

jwt = JWTManager()

def create_app():

    app = Flask(__name__)
    app.config.from_object('app.config.Config')
    jwt.init_app(app)
    # 配置
    # 初始化插件
    handler = logging.FileHandler('flask.log')
    handler.setLevel(logging.INFO)
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    handler.setFormatter(formatter)
    app.logger.addHandler(handler)
    app.logger.setLevel(logging.INFO)
    
    # 注册蓝图

    from .routers.default_routers import default_bp
    app.register_blueprint(default_bp, url_prefix="/irls/news")
    
    return app