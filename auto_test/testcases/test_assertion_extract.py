"""
第三步验收测试：通用断言 + 变量提取

运行:
    pytest testcases/test_assertion_extract.py -v
"""
from __future__ import annotations

from typing import Any

import pytest

from utils.assertion import AssertionErrorDetail, run_validations
from utils.extract import extract_vars
from utils.loader import load_case, load_cases
from utils.render import render
from utils.response_accessor import get_response_value
from utils.variable_pool import VariablePool


class MockResponse:
    """
    模拟 requests.Response，避免第三步测试依赖真实 backend。

    真实项目里 response 由 requests 库返回；单元测试用假对象即可。
    """

    def __init__(self, status_code: int, json_data: dict[str, Any], text: str = "") -> None:
        self.status_code = status_code
        self._json_data = json_data
        self.text = text or str(json_data)

    def json(self) -> dict[str, Any]:
        return self._json_data


# 模拟 backend 登录成功的 Result 结构
LOGIN_SUCCESS_BODY = {
    "code": 200,
    "message": "操作成功",
    "data": {
        "token": "mock_jwt_token_abc",
        "userId": 1001,
        "username": "testuser",
        "nickname": "测试用户",
        "role": "USER",
        "avatar": None,
        "expiresIn": 3600,
    },
}

LOGIN_FAIL_BODY = {
    "code": 400,
    "message": "账号或密码错误",
    "data": None,
}


@pytest.fixture
def success_response() -> MockResponse:
    return MockResponse(200, LOGIN_SUCCESS_BODY)


@pytest.fixture
def fail_response() -> MockResponse:
    return MockResponse(200, LOGIN_FAIL_BODY)


class TestResponseAccessor:
    def test_get_status_code(self, success_response):
        assert get_response_value(success_response, "status_code") == 200

    def test_get_body_nested_field(self, success_response):
        assert get_response_value(success_response, "body.code") == 200
        assert get_response_value(success_response, "body.data.token") == "mock_jwt_token_abc"
        assert get_response_value(success_response, "body.data.userId") == 1001

    def test_missing_field_raises_key_error(self, success_response):
        with pytest.raises(KeyError, match="不存在"):
            get_response_value(success_response, "body.data.not_exist")


class TestAssertion:
    def test_eq_pass(self, success_response):
        rules = [
            {"eq": ["status_code", 200]},
            {"eq": ["body.code", 200]},
        ]
        run_validations(success_response, rules)  # 不抛异常即通过

    def test_eq_fail(self, fail_response):
        rules = [{"eq": ["body.code", 200]}]
        with pytest.raises(AssertionErrorDetail) as exc:
            run_validations(fail_response, rules)
        assert exc.value.path == "body.code"
        assert exc.value.actual == 400

    def test_contains_pass(self, fail_response):
        rules = [{"contains": ["body.message", "账号或密码错误"]}]
        run_validations(fail_response, rules)

    def test_type_match_pass(self, success_response):
        rules = [
            {"type_match": ["body.data.token", "str"]},
            {"type_match": ["body.data.userId", "int"]},
        ]
        run_validations(success_response, rules)

    def test_login_success_yaml_validate_rules(self, success_response, monkeypatch):
        """用 cases/user/login_success.yaml 里的 validate 规则验 mock 响应。"""
        monkeypatch.setenv("USER_USERNAME", "testuser")
        case = load_case("cases/user/login_success.yaml")
        pool = VariablePool()
        pool.load_env()
        # validate 里含 ${USER_USERNAME}，需先 render（与 Runner 行为一致）
        from utils.render import render

        validate_rules = render(case["steps"][0]["validate"], pool)
        run_validations(success_response, validate_rules)

    def test_login_fail_yaml_validate_rules(self, fail_response):
        cases = load_cases("cases/user/login_fail.yaml")
        # login_fail.yaml 第一条是错误密码
        validate_rules = cases[0]["steps"][0]["validate"]
        run_validations(fail_response, validate_rules)


class TestExtract:
    def test_extract_writes_to_pool(self, success_response):
        pool = VariablePool()
        rules = {
            "token": "body.data.token",
            "user_id": "body.data.userId",
        }
        extracted = extract_vars(success_response, rules, pool)

        assert extracted == {
            "token": "mock_jwt_token_abc",
            "user_id": 1001,
        }
        assert pool.get("token") == "mock_jwt_token_abc"
        assert pool.get("user_id") == 1001

    def test_extract_from_yaml_case(self, success_response):
        case = load_case("cases/user/login_success.yaml")
        pool = VariablePool()
        extract_rules = case["steps"][0]["extract"]
        extract_vars(success_response, extract_rules, pool)
        assert pool.get("token") == "mock_jwt_token_abc"


class TestAssertionExtractChain:
    """模拟完整一步：断言 → 提取 → 下一步渲染 token。"""

    def test_login_then_use_token_in_next_request(self, success_response, monkeypatch):
        case = load_case("cases/user/login_success.yaml")
        step = case["steps"][0]
        pool = VariablePool()
        monkeypatch.setenv("USER_USERNAME", "testuser")
        pool.load_env()

        from utils.render import render

        # 1. 断言（validate 含 ${} 时需先 render，与 Runner 一致）
        run_validations(success_response, render(step["validate"], pool))

        # 2. 提取（第三步）
        extract_vars(success_response, step["extract"], pool)

        # 3. 下一步请求渲染（第二步，串联演示）
        next_headers = render(
            {"Authorization": "Bearer ${token}", "Content-Type": "application/json"},
            pool,
        )
        assert next_headers["Authorization"] == "Bearer mock_jwt_token_abc"
