# config/setting.py
import os
from pathlib import Path
from typing import Any

import yaml
from dotenv import load_dotenv

ROOT_DIR = Path(__file__).resolve().parent.parent


class DatabaseConfig:
    """MySQL 连接配置（与 backend test profile 共用同一库）。"""

    def __init__(self, conf: dict[str, Any] | None) -> None:
        conf = conf or {}
        self.enabled = bool(conf.get("enabled", False))
        self.host = os.getenv("DB_HOST", conf.get("host", "localhost"))
        self.port = int(os.getenv("DB_PORT", conf.get("port", 3306)))
        self.name = os.getenv("DB_NAME", conf.get("name", "community"))
        self.user = os.getenv("DB_USERNAME", conf.get("user", "root"))
        self.password = os.getenv("DB_PASSWORD", conf.get("password", ""))
        self.charset = conf.get("charset", "utf8mb4")


class Config:
    def __init__(self):
        load_dotenv(ROOT_DIR / ".env")
        env = os.getenv("TEST_ENV", "dev")
        with open(ROOT_DIR / "config" / f"{env}.yaml", "r", encoding="utf-8") as f:
            self.conf = yaml.safe_load(f)

        # 默认 token 为空
        self.token = None

        self.user_username = os.getenv("USER_USERNAME")
        self.user_password = os.getenv("USER_PASSWORD")
        self.admin_username = os.getenv("ADMIN_USERNAME")
        self.admin_password = os.getenv("ADMIN_PASSWORD")
        self.superadmin_username = os.getenv("SUPERADMIN_USERNAME")
        self.superadmin_password = os.getenv("SUPERADMIN_PASSWORD")
        self.database = DatabaseConfig(self.conf.get("database"))

    @property
    def base_url(self):
        return self.conf["base_url"]

    @property
    def default_headers(self):
        return self.conf.get("headers", {})

    @property
    def timeout(self):
        return self.conf.get("timeout", 10)

    @property
    def database_enabled(self) -> bool:
        return self.database.enabled

    # 登录后设置 token
    def set_token(self, token):
        self.token = token


# 全局唯一配置
config = Config()