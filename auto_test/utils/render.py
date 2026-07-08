"""
变量渲染（Render）

「渲染」在测试框架里的意思是：
  把 YAML 里带占位符的字符串，替换成真实值，再拿去发 HTTP 请求。

示例:
  渲染前: {"username": "${USER_USERNAME}", "Authorization": "Bearer ${token}"}
  渲染后: {"username": "testuser", "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9..."}

占位符格式固定为: ${变量名}
  - ${USER_USERNAME}  → 从变量池 / 环境变量读取
  - ${token}          → 通常来自上一步接口 extract 的结果

本模块依赖 variable_pool.py 提供的 resolve() 方法查找变量值。
"""
from __future__ import annotations

import re
from typing import Any

from utils.variable_pool import VariablePool

# 正则表达式：匹配 ${...} 格式的占位符
# 说明:
#   r"..." 表示原始字符串，反斜杠不需要转义
#   \$     匹配字面量字符 $
#   \{     匹配字面量字符 {
#   (      开始捕获组（把括号里匹配到的内容单独取出来）
#   [^}]+  匹配除了 } 以外的一个或多个字符（即变量名）
#   )      结束捕获组
#   \}     匹配字面量字符 }
PLACEHOLDER_PATTERN = re.compile(r"\$\{([^}]+)\}")


def render_string(text: str, pool: VariablePool) -> Any:
    """
    渲染单个字符串中的 ${变量名} 占位符。

    有两种情况:

    情况 A — 整个字符串就是一个占位符，例如 "${user_id}":
      返回变量的「原始类型」（可能是 int、bool 等）。
      这样 YAML 里的 json 字段可以保持数字类型，而不是变成字符串 "1001"。

    情况 B — 占位符嵌在文本中间，例如 "Bearer ${token}":
      只能返回 str（字符串），因为要把变量拼进一段话里。

    参数:
        text: 待渲染的字符串
        pool: 变量池对象

    返回:
        渲染后的值（可能是 str、int 等）

    示例:
        render_string("${USER_USERNAME}", pool)  → "testuser"
        render_string("Bearer ${token}", pool) → "Bearer eyJhbG..."
    """
    # re.fullmatch 要求「整个字符串」从头到尾完全匹配模式
    # 例如 fullmatch("${token}") 能匹配；fullmatch("Bearer ${token}") 不能匹配
    whole_placeholder = PLACEHOLDER_PATTERN.fullmatch(text)
    if whole_placeholder:
        # group(1) 取第一个捕获组，即 ${ 和 } 之间的变量名
        var_name = whole_placeholder.group(1).strip()
        return pool.resolve(var_name)

    # 不是「整个字符串都是占位符」→ 做字符串内替换
    def _replace(match: re.Match[str]) -> str:
        """
        这是传给 re.sub 的回调函数。
        每找到一个 ${xxx}，sub 会调用一次这个函数，用返回值替换匹配到的文本。

        re.Match 是正则匹配结果对象；match.group(1) 同样是变量名。
        这里必须 str(value)，因为拼接结果只能是字符串。
        """
        var_name = match.group(1).strip()
        value = pool.resolve(var_name)
        return str(value)

    # re.sub(模式, 替换函数, 原字符串) 把所有匹配项替换掉
    return PLACEHOLDER_PATTERN.sub(_replace, text)


def render(data: Any, pool: VariablePool) -> Any:
    """
    递归渲染任意数据结构中的占位符。

    「递归」意思：函数会调用自己，处理嵌套结构。
      - 遇到 dict  → 遍历每个 value 继续 render
      - 遇到 list  → 遍历每个元素继续 render
      - 遇到 str   → 调用 render_string 替换占位符
      - 其他类型   → 原样返回（int、bool、None 不需要渲染）

    参数:
        data: 通常是 step["request"]，可能是 dict / list / str
        pool: 变量池

    返回:
        渲染后的新对象（不会修改传入的 data，而是构造新对象返回）

    示例:
        request = {
            "method": "POST",
            "url": "/user/login",
            "json": {
                "username": "${USER_USERNAME}",
                "password": "${USER_PASSWORD}",
                "rememberMe": True,
            },
        }
        rendered = render(request, pool)
    """
    # isinstance(对象, 类型) 判断对象是不是某种类型
    if isinstance(data, str):
        return render_string(data, pool)

    if isinstance(data, dict):
        # 字典推导式：对 dict 的每个键值对，key 不变，value 递归渲染
        # 等价于新建一个 dict，比直接改原 dict 更安全
        return {key: render(value, pool) for key, value in data.items()}

    if isinstance(data, list):
        # 列表推导式：对 list 的每个元素递归渲染
        return [render(item, pool) for item in data]

    # int、float、bool、None 等，没有占位符概念，直接返回
    return data


def render_step_request(step: dict, pool: VariablePool) -> dict:
    """
    专门渲染用例「某一个 step」里的 request 块。

    这是给 Runner（第四步）准备的便捷函数：
      传入 step 字典和变量池，返回渲染好的 request 字典。

    参数:
        step: YAML 里 steps 列表中的单个元素，例如 step = case["steps"][0]
        pool: 变量池

    返回:
        渲染后的 request 字典，可直接用于发 HTTP 请求

    抛出:
        KeyError: step 里没有 request 字段时
    """
    if "request" not in step:
        raise KeyError("step 缺少 request 字段，无法渲染")
    # 先取出 request，再对整个 request 做递归渲染
    return render(step["request"], pool)
