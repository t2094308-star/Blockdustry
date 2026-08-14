# -*- coding: utf-8 -*-
"""Blockbench MCP 最小 HTTP 客户端喵。
用法: python bb_mcp_client.py tools_list | tool_call <name> <json-args>
每次调用自动先 initialize 建立新会话（Blockbench 项目状态是应用级持久的）喵。
"""
import sys, json, urllib.request

URL = "http://127.0.0.1:3000/bb-mcp"

def post(payload, sid=None):
    data = json.dumps(payload).encode("utf-8")
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json, text/event-stream",
    }
    if sid:
        headers["Mcp-Session-Id"] = sid
    req = urllib.request.Request(URL, data=data, headers=headers, method="POST")
    try:
        resp = urllib.request.urlopen(req, timeout=60)
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", "replace")
        return None, {"_http_error": e.code, "_body": body}
    sid = resp.headers.get("Mcp-Session-Id") or sid
    body = resp.read().decode("utf-8", "replace")
    if body.startswith("data:"):
        lines = [l for l in body.splitlines() if l.startswith("data:")]
        if lines:
            body = lines[0][5:].strip()
    try:
        return sid, json.loads(body)
    except Exception:
        return sid, {"_raw": body}

def main():
    cmd = sys.argv[1] if len(sys.argv) > 1 else "tools_list"
    sid, r = post({"jsonrpc": "2.0", "id": 1, "method": "initialize",
                   "params": {"protocolVersion": "2024-11-05", "capabilities": {},
                              "clientInfo": {"name": "bb-curl-client", "version": "1.0"}}})
    post({"jsonrpc": "2.0", "method": "notifications/initialized", "params": {}}, sid)
    if cmd == "tools_list":
        sid, r = post({"jsonrpc": "2.0", "id": 2, "method": "tools/list", "params": {}}, sid)
        print(json.dumps(r, ensure_ascii=False, indent=2))
    elif cmd == "tool_call":
        name = sys.argv[2]
        args = json.loads(sys.argv[3]) if len(sys.argv) > 3 else {}
        sid, r = post({"jsonrpc": "2.0", "id": 3, "method": "tools/call",
                       "params": {"name": name, "arguments": args}}, sid)
        print(json.dumps(r, ensure_ascii=False, indent=2))

if __name__ == "__main__":
    main()
