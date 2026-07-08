"""
变量提取模块（Extract）

接口串联时，上一步响应里的字段要给下一步用。
例如登录接口返回 token，发帖接口要在 Header 里带上它。

YAML 里用 extract 声明要提取什么:
    extract:
      token: body.data.token
      user_id: body.data.userId

本模块从 HTTP 响应中按路径取值，写入变量池（VariablePool）。
下一步 render 模块就能把 ${token} 替换成真实值。

流程:
  登录响应 → extract_vars() → pool.update({"token": "...", "user_id": 1})
  发帖请求 → render() 读取 ${token} → 带上 Authorization 头发请求
"""
from __future__ import annotations

from typing import Any

from utils.response_accessor import get_response_value
from utils.variable_pool import VariablePool


def extract_vars(
    response: Any,
    rules: dict[str, str] | None,
    pool: VariablePool,
) -> dict[str, Any]:
    """
    从响应中提取变量，并写入变量池。

    参数:
        response: requests.Response 对象
        rules   : YAML 里 extract 字段，格式为 {变量名: 响应路径}
                  例如 {"token": "body.data.token", "user_id": "body.data.userId"}
        pool    : 变量池对象，提取结果会 merge 进去

    返回:
        本次提取到的 dict 副本（方便日志或调试）

    抛出:
        KeyError / RuntimeError / ValueError: 路径不存在或响应非 JSON 时

    示例:
        rules = {"token": "body.data.token"}
        extracted = extract_vars(response, rules, pool)
        # pool 里现在有 token；render 可用 ${token}
    """
    if not rules:
        # 没有 extract 字段，什么都不做
        return {}

    extracted: dict[str, Any] = {}

    # rules.items() 遍历 dict 的每个键值对
    # var_name 是存到变量池里的名字，path 是响应里的取值路径
    for var_name, path in rules.items():
        if not isinstance(var_name, str) or not var_name.strip():
            raise ValueError(f"extract 的变量名无效: {var_name!r}")
        if not isinstance(path, str) or not path.strip():
            raise ValueError(f"extract['{var_name}'] 的路径无效: {path!r}")

        # 按路径从响应取值（逻辑与 assertion 共用 response_accessor）
        value = get_response_value(response, path.strip())
        extracted[var_name.strip()] = value

    # 批量写入变量池；下一步 render 即可使用 ${var_name}
    pool.update(extracted)
    return dict(extracted)
