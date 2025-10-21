#!/bin/bash

# Gunicorn 终止脚本 - 基于 PID 文件
# 用法: ./stop_gunicorn.sh [端口号]

# 默认端口
PORT=${1:-9106}
PID_FILE="/tmp/gunicorn_$PORT.pid"

echo "正在停止端口 $PORT 上的 Gunicorn 服务..."

# 检查 PID 文件是否存在
if [ ! -f "$PID_FILE" ]; then
    echo "错误: 找不到 PID 文件: $PID_FILE"
    echo "尝试通过端口查找进程..."
    
    # 尝试通过端口查找进程
    PORT_PIDS=$(lsof -ti :$PORT)
    if [ ! -z "$PORT_PIDS" ]; then
        echo "找到端口 $PORT 上的进程: $PORT_PIDS"
        read -p "是否要终止这些进程? (y/N): " confirm
        if [[ $confirm == [yY] || $confirm == [yY][eE][sS] ]]; then
            kill -TERM $PORT_PIDS
            echo "已发送终止信号"
        else
            echo "操作已取消"
            exit 0
        fi
    else
        echo "未找到运行在端口 $PORT 的进程"
        exit 1
    fi
else
    # 读取 PID
    PID=$(cat "$PID_FILE")
    echo "从 PID 文件读取到进程 ID: $PID"
    
    # 检查进程是否存在
    if ps -p "$PID" > /dev/null; then
        echo "正在终止进程 $PID..."
        
        # 发送终止信号
        kill -TERM "$PID"
        
        # 等待进程结束（最多10秒）
        for i in {1..10}; do
            if ! ps -p "$PID" > /dev/null; then
                break
            fi
            sleep 1
        done
        
        # 检查是否成功终止
        if ps -p "$PID" > /dev/null; then
            echo "进程未正常终止，发送强制终止信号..."
            kill -9 "$PID"
            sleep 1
        fi
        
        # 再次确认进程已终止
        if ! ps -p "$PID" > /dev/null; then
            echo "进程 $PID 已成功终止"
            # 删除 PID 文件
            rm -f "$PID_FILE"
            echo "已删除 PID 文件: $PID_FILE"
        else
            echo "警告: 无法终止进程 $PID"
            exit 1
        fi
    else
        echo "进程 $PID 不存在，删除无效的 PID 文件"
        rm -f "$PID_FILE"
        exit 1
    fi
fi

echo "Gunicorn 服务已停止"