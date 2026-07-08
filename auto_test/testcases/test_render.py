"""
第二步验收测试：变量池 + 渲染

运行:
    pytest testcases/test_render.py -v
"""
import os

import pytest

from utils.loader import load_case
from utils.render import render, render_step_request, render_string
from utils.variable_pool import VariablePool


class TestVariablePool:
    """测试变量池的基本存取功能。"""

    def test_set_and_get(self):
        pool = VariablePool()
        pool.set("token", "abc123")
        assert pool.get("token") == "abc123"

    def test_get_missing_key_returns_default(self):
        pool = VariablePool()
        # 不存在的 key 应返回 default，而不是报错
        assert pool.get("not_exist", "fallback") == "fallback"

    def test_update_batch(self):
        pool = VariablePool()
        pool.update({"token": "t1", "user_id": 100})
        assert pool.get("token") == "t1"
        assert pool.get("user_id") == 100

    def test_clear(self):
        pool = VariablePool()
        pool.set("token", "abc")
        pool.clear()
        assert pool.has("token") is False

    def test_pool_priority_over_env(self, monkeypatch):
        """
        monkeypatch 是 pytest 提供的工具，可以在测试里临时改环境变量，
        测试结束后自动恢复，不影响你的真实 .env。
        """
        monkeypatch.setenv("MY_VAR", "from_env")
        pool = VariablePool()
        pool.set("MY_VAR", "from_pool")
        # 变量池里的值应优先于环境变量
        assert pool.resolve("MY_VAR") == "from_pool"


class TestRender:
    """测试占位符渲染。"""

    def test_render_env_variable_in_string(self, monkeypatch):
        monkeypatch.setenv("USER_USERNAME", "testuser")
        pool = VariablePool()
        pool.load_env("USER_USERNAME")
        result = render_string("${USER_USERNAME}", pool)
        assert result == "testuser"

    def test_render_bearer_token_mixed_string(self):
        pool = VariablePool()
        pool.set("token", "jwt_token_xyz")
        result = render_string("Bearer ${token}", pool)
        assert result == "Bearer jwt_token_xyz"

    def test_render_whole_placeholder_keeps_int_type(self):
        """整个字符串是 ${user_id} 时，应保持 int 类型（不是字符串 "1001"）。"""
        pool = VariablePool()
        pool.set("user_id", 1001)
        result = render_string("${user_id}", pool)
        assert result == 1001
        assert isinstance(result, int)

    def test_render_nested_request_dict(self, monkeypatch):
        monkeypatch.setenv("USER_USERNAME", "alice")
        monkeypatch.setenv("USER_PASSWORD", "Password1")
        pool = VariablePool()
        pool.load_env("USER_USERNAME", "USER_PASSWORD")

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
        assert rendered["json"]["username"] == "alice"
        assert rendered["json"]["password"] == "Password1"
        # bool 类型不应被改动
        assert rendered["json"]["rememberMe"] is True

    def test_render_missing_variable_raises_clear_error(self):
        pool = VariablePool()
        with pytest.raises(KeyError, match="变量 'token' 未定义"):
            render_string("${token}", pool)

    def test_render_step_request_from_yaml_case(self, monkeypatch):
        """用真实的 login_success.yaml 验证整条渲染链路。"""
        monkeypatch.setenv("USER_USERNAME", "yaml_user")
        monkeypatch.setenv("USER_PASSWORD", "yaml_pass")

        case = load_case("cases/user/login_success.yaml")
        pool = VariablePool()
        pool.load_env()

        step = case["steps"][0]
        rendered_request = render_step_request(step, pool)

        assert rendered_request["json"]["username"] == "yaml_user"
        assert rendered_request["json"]["password"] == "yaml_pass"
        assert rendered_request["url"] == "/user/login"

    def test_chained_variables_login_then_api_call(self):
        """
        模拟「登录 extract token → 下一步带 token 调接口」的串联场景。
        这是第二步要支撑的核心能力（第三步 extract 会自动写入，这里手动 set 模拟）。
        """
        pool = VariablePool()
        pool.set("token", "mock_jwt_token")

        headers = render({"Authorization": "Bearer ${token}"}, pool)
        assert headers["Authorization"] == "Bearer mock_jwt_token"
