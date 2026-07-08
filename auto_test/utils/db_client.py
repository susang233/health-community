"""
MySQL 客户端（PyMySQL）

供 db_validate / teardown 使用，连接 backend 测试库。
"""
from __future__ import annotations

from typing import Any

import pymysql
from pymysql.cursors import DictCursor

from config.setting import config

_db_client: "DbClient | None" = None


class DbClient:
    def __init__(self, db_config) -> None:
        self._db_config = db_config

    def connect(self):
        return pymysql.connect(
            host=self._db_config.host,
            port=self._db_config.port,
            user=self._db_config.user,
            password=self._db_config.password,
            database=self._db_config.name,
            charset=self._db_config.charset,
            cursorclass=DictCursor,
            autocommit=True,
        )

    def ping(self) -> None:
        conn = self.connect()
        conn.close()

    def query_one(self, sql: str) -> dict[str, Any]:
        conn = self.connect()
        try:
            with conn.cursor() as cursor:
                cursor.execute(sql)
                row = cursor.fetchone()
                if row is None:
                    raise ValueError(f"SQL 未返回任何行:\n{sql}")
                return dict(row)
        finally:
            conn.close()

    def execute(self, sql: str) -> int:
        conn = self.connect()
        try:
            with conn.cursor() as cursor:
                return cursor.execute(sql)
        finally:
            conn.close()


def get_db_client() -> DbClient:
    global _db_client
    if not config.database_enabled:
        raise RuntimeError("数据库校验未启用，请在 config/{env}.yaml 中设置 database.enabled=true")
    if _db_client is None:
        _db_client = DbClient(config.database)
    return _db_client
