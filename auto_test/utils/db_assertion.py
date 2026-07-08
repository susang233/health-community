"""
数据库断言模块

YAML 示例:
    db_validate:
      - sql: "SELECT content, status FROM hc_post WHERE id = ${post_id}"
        expect:
          - eq: [content, "自动化测试发帖"]
          - eq: [status, "PENDING"]
"""
from __future__ import annotations

import json
from typing import Any

from utils.assertion import run_validations_on_mapping
from utils.db_client import get_db_client
from utils.render import render
from utils.variable_pool import VariablePool

try:
    import allure

    ALLURE_AVAILABLE = True
except ImportError:  # pragma: no cover
    ALLURE_AVAILABLE = False


def run_db_validations(rules: list[dict] | None, pool: VariablePool) -> None:
    if not rules:
        return

    client = get_db_client()
    for index, rule_block in enumerate(rules):
        if not isinstance(rule_block, dict):
            raise ValueError(f"db_validate[{index}] 必须是 dict")

        sql = render(rule_block.get("sql"), pool)
        if not isinstance(sql, str) or not sql.strip():
            raise ValueError(f"db_validate[{index}] 缺少有效 sql")

        row = client.query_one(sql)
        expect_rules = rule_block.get("expect") or []
        rendered_expect = render(expect_rules, pool)

        if ALLURE_AVAILABLE:
            allure.attach(
                json.dumps({"sql": sql, "row": _serialize_row(row)}, ensure_ascii=False, indent=2),
                name=f"db_validate[{index}]",
                attachment_type=allure.attachment_type.JSON,
            )

        run_validations_on_mapping(
            row,
            rendered_expect,
            label=f"db_validate[{index}].expect",
        )


def run_teardown(rules: list[dict] | None, pool: VariablePool) -> None:
    """用例结束后清理测试数据；变量缺失时跳过对应 SQL。"""
    if not rules:
        return

    client = get_db_client()
    for index, rule_block in enumerate(rules):
        if not isinstance(rule_block, dict):
            continue

        try:
            sql = render(rule_block.get("sql"), pool)
        except KeyError:
            continue

        if not isinstance(sql, str) or not sql.strip():
            continue

        client.execute(sql)


def _serialize_row(row: dict[str, Any]) -> dict[str, Any]:
    serialized: dict[str, Any] = {}
    for key, value in row.items():
        if hasattr(value, "isoformat"):
            serialized[key] = value.isoformat()
        else:
            serialized[key] = value
    return serialized
