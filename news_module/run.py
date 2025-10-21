from app import create_app

app = create_app()

if __name__ == '__main__':
    # 开发模式运行（生产环境应通过Gunicorn启动）
    app.run(host='127.0.0.1', port=9106)