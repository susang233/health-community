"""
响应取值工具（Response Accessor）

assertion.py（断言）和 extract.py（提取）都需要从 HTTP 响应里按路径取值。
例如路径 "body.data.token" 表示：
  1. 先取响应 JSON 体（body）
  2. 再取 data 字段
  3. 再取 token 字段

把这个逻辑抽成公共函数，避免在两个文件里重复写一遍。
"""
from __future__ import annotations

from typing import Any

# requests 库的 Response 对象类型注解（仅用于提示，运行时不强制）
try:
    from requests import Response
except ImportError:  # pragma: no cover
    Response = Any  # type: ignore


def get_response_value(response: Response, path: str) -> Any:
    """
    按「点分路径」从 HTTP 响应中取值。

    支持的路径格式:
      - status_code     → response.status_code（HTTP 状态码，如 200）
      - body            → 整个 JSON 响应体（dict）
      - body.code       → 响应体里的 code 字段
      - body.data.token → 嵌套字段，用 . 一层层往下找

    参数:
        response: requests 库发请求后返回的 Response 对象
        path    : 点分路径字符串

    返回:
        取到的值，类型可能是 int、str、dict、list 等

    抛出:
        ValueError : 路径格式不支持
        KeyError   : JSON 里找不到对应字段
        RuntimeError: 响应不是合法 JSON

    示例:
        假设 response.json() 返回:
        {"code": 200, "message": "操作成功", "data": {"token": "abc", "userId": 1}}

        get_response_value(response, "status_code")     → 200
        get_response_value(response, "body.code")       → 200
        get_response_value(response, "body.data.token")   → "abc"
    """
    # strip() 去掉首尾空格，防止 YAML 里不小心多空格
    path = path.strip()

    # ---------- 情况 1：取 HTTP 状态码 ----------
    if path == "status_code":
        return response.status_code

    # ---------- 情况 2：取响应 JSON 体 ----------
    if path == "body" or path.startswith("body."):
        # response.json() 把响应正文解析成 Python dict
        # 如果正文不是 JSON（如 HTML 错误页），会抛异常
        try:
            body = response.json()
        except Exception as exc:
            raise RuntimeError(
                f"响应不是合法 JSON，无法读取路径 '{path}'。"
                f"HTTP 状态码={response.status_code}，"
                f"响应正文前 200 字符: {response.text[:200]}"
            ) from exc

        # 只要整个 body，不再往下拆
        if path == "body":
            return body

        # path 形如 "body.data.token" → 去掉前缀 "body." → "data.token"
        # split(".", 1) 只分割第一个点，避免 data 里还有点的情况
        _, remainder = path.split(".", 1)
        return _get_nested_value(body, remainder, full_path=path)

    raise ValueError(
        f"不支持的响应路径 '{path}'。"
        f"目前只支持 status_code、body、body.xxx 三种形式。"
    )


def _get_nested_value(data: Any, dotted_path: str, full_path: str) -> Any:
    """
    在 dict 里按点分路径向下取值（内部辅助函数）。

    参数:
        data        : 当前层级的数据，通常是 dict
        dotted_path : 剩余路径，如 "data.token"
        full_path   : 完整路径，仅用于报错信息

    示例:
        data = {"data": {"token": "abc"}}
        _get_nested_value(data, "data.token", "body.data.token") → "abc"
    """
    # 用点号拆成各级 key，例如 "data.token" → ["data", "token"]
    keys = dotted_path.split(".")
    current = data

    for key in keys:
        # 每一层都应该是 dict，才能用 key 继续往下取
        if not isinstance(current, dict):
            raise KeyError(
                f"路径 '{full_path}' 解析失败："
                f"在字段 '{key}' 处期望 dict，实际是 {type(current).__name__}"
            )
        if key not in current:
            raise KeyError(
                f"路径 '{full_path}' 解析失败：字段 '{key}' 不存在。"
                f"当前可用字段: {list(current.keys())}"
            )
        # 进入下一层
        current = current[key]

    return current
