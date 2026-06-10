#!/bin/bash

# 球馆预约管理系统部署脚本

set -e

echo "========================================="
echo "球馆预约管理系统 - 部署脚本"
echo "========================================="

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装，请先安装 Docker"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "错误: Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

# 显示菜单
echo ""
echo "请选择操作:"
echo "1. 启动所有服务"
echo "2. 停止所有服务"
echo "3. 重启所有服务"
echo "4. 查看服务状态"
echo "5. 查看日志"
echo "6. 清理所有数据（危险操作）"
echo "0. 退出"
echo ""

read -p "请输入选项 [0-6]: " choice

case $choice in
    1)
        echo "正在启动服务..."
        docker-compose up -d
        echo ""
        echo "服务启动成功！"
        echo "前端地址: http://localhost"
        echo "后端地址: http://localhost:8080/api"
        echo "MySQL 端口: 3306"
        ;;
    2)
        echo "正在停止服务..."
        docker-compose down
        echo "服务已停止"
        ;;
    3)
        echo "正在重启服务..."
        docker-compose restart
        echo "服务已重启"
        ;;
    4)
        echo "服务状态:"
        docker-compose ps
        ;;
    5)
        echo "查看日志 (Ctrl+C 退出):"
        docker-compose logs -f
        ;;
    6)
        read -p "确认要清理所有数据吗？这将删除数据库数据！(yes/no): " confirm
        if [ "$confirm" = "yes" ]; then
            echo "正在清理..."
            docker-compose down -v
            echo "数据已清理"
        else
            echo "操作已取消"
        fi
        ;;
    0)
        echo "退出"
        exit 0
        ;;
    *)
        echo "无效选项"
        exit 1
        ;;
esac
