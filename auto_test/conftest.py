"""
pytest 全局 fixture 配置

fixture 是 pytest 的「测试前置准备」机制：
  测试函数的参数名如果和 fixture 函数名相同，
  pytest 会自动先执行 fixture，把返回值传给测试函数。
"""
from __future__ import annotations

import pytest
import requests

from config.setting import config
from core.runner import CaseRunner
from utils.db_client import get_db_client


@pytest.fixture(scope="session")
def case_runner() -> CaseRunner:
    """
    提供 CaseRunner 实例，整个测试会话共用一个。

    scope="session" 表示只创建一次，所有测试复用，省资源。
    """
    return CaseRunner()


@pytest.fixture(scope="session")
def backend_available() -> None:
    """
    检查 backend 是否已启动；未启动则跳过集成测试。

    单元测试（loader / render / assertion）不依赖这个 fixture。
    只有 test_api.py 的集成测试会用到。

    pytest.skip() 会跳过当前测试，不算失败（显示 SKIPPED）。
    """
    try:
        # 随便打一个轻量接口，能连上就说明 backend 在跑
        resp = requests.get(
            f"{config.base_url}/user/check-username",
            params={"username": "health_check"},
            timeout=3,
        )
        # 如果打到了别的服务（例如 Jenkins 本身）通常会是 403/302/404，这里直接跳过集成测试避免误判
        if resp.status_code != 200:
            pytest.skip(f"backend 响应异常 status_code={resp.status_code} base_url={config.base_url}")
    except requests.RequestException as exc:
        pytest.skip(f"backend 未启动或不可达（{config.base_url}），跳过集成测试: {exc}")


@pytest.fixture(scope="session")
def db_available() -> None:
    """
    检查 MySQL 测试库是否可连；未配置或连不上则跳过带 DB 校验的集成测试。
    """
    if not config.database_enabled:
        pytest.skip("database.enabled=false，跳过数据库校验相关测试")

    if not config.database.password:
        pytest.skip("未配置 DB_PASSWORD，跳过数据库校验相关测试")

    try:
        get_db_client().ping()
    except Exception as exc:
        pytest.skip(f"MySQL 不可达（{config.database.host}:{config.database.port}），跳过: {exc}")
