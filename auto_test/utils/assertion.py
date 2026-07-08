"""
通用断言模块（Assertion）

在接口自动化里，「断言」就是检查「实际结果」是否符合「预期」。
以前在每个 test_xxx.py 里手写:
    assert resp.status_code == 200
    assert resp.json()["code"] == 200

B 档方案把断言规则写在 YAML 的 validate 里:
    validate:
      - eq: [status_code, 200]
      - eq: [body.code, 200]
      - contains: [body.message, 成功]

本模块负责读取这些规则，自动执行断言。
失败时抛出 AssertionError，pytest 会把用例标红并显示错误信息。
"""
from __future__ import annotations

from collections.abc import Callable
from typing import Any

from utils.response_accessor import get_response_value

# type_match 规则里，YAML 写的类型名 → Python 真正的类型
# 例如 YAML 写 str，这里映射到 Python 的 str
TYPE_NAME_MAP: dict[str, type] = {
    "str": str,
    "int": int,
    "float": float,
    "bool": bool,
    "list": list,
    "dict": dict,
}


class AssertionErrorDetail(AssertionError):
    """
  自定义断言异常（继承自 AssertionError）。

  继承说明（给初学者）:
    - class Child(Parent) 表示 Child 是 Parent 的子类
    - pytest 识别 AssertionError 为「测试失败」
    - 我们继承它，可以附带更详细的报错信息，同时 pytest 仍能正常捕获

  属性:
    rule    : 哪条规则失败了，如 "eq"
    path    : 从响应的哪个路径取值
    expected: 期望值
    actual  : 实际值
    """

    def __init__(
        self,
        message: str,
        *,
        rule: str,
        path: str,
        expected: Any,
        actual: Any,
    ) -> None:
        super().__init__(message)
        self.rule = rule
        self.path = path
        self.expected = expected
        self.actual = actual


def run_validations(response: Any, rules: list[dict] | None) -> None:
    """
    执行一条 step 里的全部断言规则。

    参数:
        response: requests.Response 对象（发完 HTTP 请求后的返回值）
        rules   : YAML 里 validate 字段的列表；None 或 [] 表示不断言

    示例 rules:
        [
          {"eq": ["status_code", 200]},
          {"eq": ["body.code", 200]},
          {"contains": ["body.message", "成功"]},
        ]

    抛出:
        AssertionErrorDetail: 任一规则不满足时
        ValueError          : 规则格式写错了
    """
    if not rules:
        # 没有 validate 字段，直接跳过
        return

    for index, rule in enumerate(rules):
        _run_single_rule(
            lambda path: get_response_value(response, path),
            rule,
            index,
            label="validate",
        )


def run_validations_on_mapping(
    data: dict[str, Any],
    rules: list[dict] | None,
    *,
    label: str = "expect",
) -> None:
    """对 dict 数据执行断言（用于 DB 查询结果等）。"""
    if not rules:
        return

    for index, rule in enumerate(rules):
        _run_single_rule(
            lambda path, mapping=data: _get_mapping_value(mapping, path),
            rule,
            index,
            label=label,
        )


def _get_mapping_value(data: dict[str, Any], path: str) -> Any:
    path = str(path).strip()
    if path not in data:
        raise KeyError(f"字段不存在: {path!r}，可用字段={list(data.keys())}")
    return data[path]


def _run_single_rule(
    getter: Callable[[str], Any],
    rule: dict,
    index: int,
    *,
    label: str,
) -> None:
    """
    执行单条断言规则（内部函数）。

    YAML 里每条规则是一个只有一个 key 的 dict，例如:
        {"eq": [path, expected]}

    不能写成:
        {"eq": [...], "contains": [...]}  # 一条规则只能有一种断言类型
    """
    if not isinstance(rule, dict):
        raise ValueError(f"{label}[{index}] 必须是 dict，实际是 {type(rule).__name__}")

    # len(rule) != 1 保证每条规则只有一个断言类型
    if len(rule) != 1:
        raise ValueError(
            f"{label}[{index}] 格式错误：每条规则只能有一种断言，"
            f"例如 {{eq: [body.code, 200]}}，当前 keys={list(rule.keys())}"
        )

    # dict 转 list 取第一个（也是唯一一个）键值对
    # 例如 {"eq": ["body.code", 200]} → rule_name="eq", params=["body.code", 200]
    rule_name, params = next(iter(rule.items()))

    if not isinstance(params, list) or len(params) != 2:
        raise ValueError(
            f"{label}[{index}] 的 {rule_name} 需要两个参数 [路径, 期望值]，"
            f"当前是 {params!r}"
        )

    path, expected = params
    path = str(path).strip()

    actual = getter(path)

    # 根据规则类型分发到具体断言函数
    if rule_name == "eq":
        _assert_eq(rule_name, path, actual, expected)
    elif rule_name == "ne":
        _assert_ne(rule_name, path, actual, expected)
    elif rule_name == "contains":
        _assert_contains(rule_name, path, actual, expected)
    elif rule_name == "type_match":
        _assert_type_match(rule_name, path, actual, expected)
    else:
        raise ValueError(
            f"{label}[{index}] 不支持的断言类型 '{rule_name}'。"
            f"目前支持: eq, ne, contains, type_match"
        )


def _assert_eq(rule: str, path: str, actual: Any, expected: Any) -> None:
    """相等断言：actual == expected"""
    if actual != expected:
        raise AssertionErrorDetail(
            f"断言失败 [{rule}] 路径={path}：期望 {expected!r}，实际 {actual!r}",
            rule=rule,
            path=path,
            expected=expected,
            actual=actual,
        )


def _assert_ne(rule: str, path: str, actual: Any, expected: Any) -> None:
    """不等断言：actual != expected"""
    if actual == expected:
        raise AssertionErrorDetail(
            f"断言失败 [{rule}] 路径={path}：期望不等于 {expected!r}，实际却是 {actual!r}",
            rule=rule,
            path=path,
            expected=expected,
            actual=actual,
        )


def _assert_contains(rule: str, path: str, actual: Any, expected: Any) -> None:
    """
    包含断言：expected 应出现在 actual 里。

    支持:
      - 两个都是 str → 子串包含，如 "账号或密码错误" in message
      - actual 是 list → expected 是列表元素
    """
    ok = False
    if isinstance(actual, str) and isinstance(expected, str):
        ok = expected in actual
    elif isinstance(actual, list):
        ok = expected in actual
    else:
        raise AssertionErrorDetail(
            f"断言失败 [{rule}] 路径={path}：contains 不支持 "
            f"actual={type(actual).__name__} 与 expected={type(expected).__name__} 的组合",
            rule=rule,
            path=path,
            expected=expected,
            actual=actual,
        )

    if not ok:
        raise AssertionErrorDetail(
            f"断言失败 [{rule}] 路径={path}：期望包含 {expected!r}，实际 {actual!r}",
            rule=rule,
            path=path,
            expected=expected,
            actual=actual,
        )


def _assert_type_match(rule: str, path: str, actual: Any, expected_type_name: Any) -> None:
    """
    类型断言：检查 actual 的类型是否匹配。

    YAML 示例:
        type_match: [body.data.token, str]
        type_match: [body.data.userId, int]
    """
    if not isinstance(expected_type_name, str):
        raise ValueError(
            f"type_match 的第二个参数必须是类型名字符串（如 str、int），"
            f"当前是 {expected_type_name!r}"
        )

    expected_type = TYPE_NAME_MAP.get(expected_type_name)
    if expected_type is None:
        raise ValueError(
            f"type_match 不支持的类型名 '{expected_type_name}'。"
            f"支持: {', '.join(TYPE_NAME_MAP.keys())}"
        )

    if not isinstance(actual, expected_type):
        raise AssertionErrorDetail(
            f"断言失败 [{rule}] 路径={path}：期望类型 {expected_type_name}，"
            f"实际类型 {type(actual).__name__}，值={actual!r}",
            rule=rule,
            path=path,
            expected=expected_type_name,
            actual=actual,
        )
