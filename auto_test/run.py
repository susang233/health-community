#!/usr/bin/env python3
"""
接口自动化测试 — 统一运行入口

用法示例:
    # 跑全部测试
    python run.py

    # 只跑单元测试（不需要 backend）
    python run.py --unit

    # 只跑集成测试（需要 backend + .env）
    python run.py --integration

    # 生成 Allure 报告数据
    python run.py --integration --allure
    allure serve reports/allure-results

    # 生成 HTML 报告
    python run.py --html
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

# 保证无论从哪执行，都能找到 auto_test 下的模块
ROOT = Path(__file__).resolve().parent
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))


def build_pytest_args(args: argparse.Namespace) -> list[str]:
    """根据命令行参数组装 pytest 参数列表。"""
    pytest_args = ["testcases/", "-v"]

    if args.unit:
        pytest_args.extend(["-m", "not integration"])
    elif args.integration:
        pytest_args.extend(["-m", "integration"])

    if args.allure:
        allure_dir = ROOT / "reports" / "allure-results"
        allure_dir.mkdir(parents=True, exist_ok=True)
        pytest_args.extend([f"--alluredir={allure_dir}"])

    if args.html:
        html_path = ROOT / "reports" / "report.html"
        html_path.parent.mkdir(parents=True, exist_ok=True)
        pytest_args.extend([
            f"--html={html_path}",
            "--self-contained-html",
        ])

    # 允许把额外参数透传给 pytest，例如: python run.py -- -k login
    if args.pytest_args:
        pytest_args.extend(args.pytest_args)

    return pytest_args


def main() -> int:
    parser = argparse.ArgumentParser(description="health-community 接口自动化测试入口")
    group = parser.add_mutually_exclusive_group()
    group.add_argument(
        "--unit",
        action="store_true",
        help="只跑单元测试（不依赖 backend）",
    )
    group.add_argument(
        "--integration",
        action="store_true",
        help="只跑集成测试（需要 backend）",
    )
    parser.add_argument(
        "--allure",
        action="store_true",
        help="输出 Allure 报告数据到 reports/allure-results",
    )
    parser.add_argument(
        "--html",
        action="store_true",
        help="输出 HTML 报告到 reports/report.html",
    )
    parser.add_argument(
        "pytest_args",
        nargs="*",
        help="透传给 pytest 的额外参数（在 -- 之后）",
    )
    args = parser.parse_args()

    import pytest

    pytest_args = build_pytest_args(args)
    print("执行:", "pytest", " ".join(pytest_args))
    return pytest.main(pytest_args)


if __name__ == "__main__":
    raise SystemExit(main())
