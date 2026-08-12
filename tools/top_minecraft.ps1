# 等 Minecraft 窗口出现，置顶 1 秒后还原，便于立刻看到启动画面喵
Add-Type @"
using System;
using System.Runtime.InteropServices;
public class BTop {
  [DllImport("user32.dll")] public static extern bool SetWindowPos(IntPtr hWnd, IntPtr hWndInsertAfter, int X, int Y, int cx, int cy, uint uFlags);
}
"@

$proc = $null
for ($i = 0; $i -lt 180; $i++) {
  $proc = Get-Process | Where-Object { $_.MainWindowTitle -like '*Minecraft*' } | Select-Object -First 1
  if ($proc -and $proc.MainWindowHandle -ne 0) { break }
  Start-Sleep -Seconds 2
}
if ($proc -and $proc.MainWindowHandle -ne 0) {
  [BTop]::SetWindowPos($proc.MainWindowHandle, [IntPtr]-1, 0, 0, 0, 0, 0x0003)  # HWND_TOPMOST
  Start-Sleep -Seconds 1
  [BTop]::SetWindowPos($proc.MainWindowHandle, [IntPtr]-2, 0, 0, 0, 0, 0x0003)  # HWND_NOTOPMOST
}
