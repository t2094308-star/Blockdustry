#!/bin/bash
# 一键启动游戏：关闭旧 Minecraft 窗口 → 启动 runClient → 等窗口出现置顶 1 秒喵
DIR="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$DIR/.." && pwd)"

# 1. 关闭旧的 Minecraft 窗口（若有），避免多开冲突喵
powershell.exe -NoProfile -Command "Get-Process | Where-Object {\$_.MainWindowTitle -like '*Minecraft*'} | Stop-Process -Force" 2>/dev/null || true
sleep 1

# 2. 启动游戏（后台，日志不阻塞）喵
cd "$REPO"
./gradlew runClient >"$DIR/.game_log.txt" 2>&1 &

# 3. 等窗口出现并置顶 1 秒喵
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "$DIR/top_minecraft.ps1"
