"""YAML 用例加载器 — B 档第一步：统一用例格式与读取入口。"""
from __future__ import annotations

from pathlib import Path
from typing import Any

import yaml

ROOT_DIR = Path(__file__).resolve().parent.parent


def resolve_path(path: str | Path) -> Path:
    p = Path(path)
    return p if p.is_absolute() else ROOT_DIR / p


def load_yaml(path: str | Path) -> Any:
    with open(resolve_path(path), encoding="utf-8") as f:
        return yaml.safe_load(f)


def validate_case(case: dict) -> dict:
    if not isinstance(case, dict):
        raise ValueError("用例必须是 dict")
    if not case.get("steps"):
        label = case.get("name") or case.get("case_id") or "unknown"
        raise ValueError(f"用例缺少 steps: {label}")
    for i, step in enumerate(case["steps"]):
        if "request" not in step:
            raise ValueError(f"步骤 {i} 缺少 request")
        req = step["request"]
        if not req.get("method") or not req.get("url"):
            raise ValueError(f"步骤 {i} 的 request 缺少 method 或 url")
    return case


def load_case(path: str | Path) -> dict:
    """加载单条用例（YAML 根节点为 dict）。"""
    data = load_yaml(path)
    if isinstance(data, list):
        raise ValueError(f"{path} 含多条用例，请使用 load_cases()")
    return validate_case(data)


def load_cases(path: str | Path) -> list[dict]:
    """加载用例；YAML 根节点为 list 时返回多条，为 dict 时返回单条列表。"""
    data = load_yaml(path)
    if isinstance(data, list):
        return [validate_case(c) for c in data]
    return [validate_case(data)]


def list_case_files(directory: str | Path = "cases") -> list[Path]:
    """递归列出 cases 目录下所有 .yaml 用例文件（跳过 template 示例）。"""
    root = resolve_path(directory)
    files = sorted(root.rglob("*.yaml"))
    return [f for f in files if "template" not in f.name]
